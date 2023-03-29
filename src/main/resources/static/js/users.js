function enableUser(checkbox, id) {
    let enabled = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: "users/" + id,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function () {
        successToast('User ' + name + ' was ' + (enabled ? 'enabled' : 'disabled'));
    }).fail(function () {
        $(checkbox).prop('checked', !enabled);
        failToast('Failed to ' + (enabled ? 'enable' : 'disable') + ' user ' + name);
    });
}

$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('bs-name');
    let id = $(e.relatedTarget).data('bs-id');
    $(e.currentTarget).find('#deleteModalLabel').text('Delete user ' + name + ' ?');
    $(e.currentTarget).find('#deleteModalUserId').val(id);
    $(e.currentTarget).find('#deleteModalUserName').val(name);
});

function deleteUser() {
    let deleteModal = $('#deleteModal');
    let id = deleteModal.find('#deleteModalUserId').val();
    let name = deleteModal.find('#deleteModalUserName').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: "users/" + id,
        type: "DELETE"
    }).done(function () {
        updatePageContent(id, name);
    }).fail(function () {
        failToast('Failed to delete user ' + name);
    });
}

function updatePageContent(id, name) {
    $('#user-row-' + id).remove();

    let paginationSummary = $('#pagination-summary');
    let paginationSummaryText = paginationSummary.text();
    let words = paginationSummaryText.split(' ');
    let firstEntryNumber = 0;
    let lastEntryNumber = 0;
    let totalEntries = 0;
    let wordsNumberCounter = 0;
    for (let i = 0; i < words.length; i++) {
        if (isNumber(words[i])) {
            wordsNumberCounter++;
            if (wordsNumberCounter === 1) {
                firstEntryNumber = words[i];
            } else {
                words[i] = words[i] - 1;
                if (wordsNumberCounter === 2) {
                    lastEntryNumber = words[i];
                } else {
                    totalEntries = words[i];
                }
            }
        }
    }
    let afterHideToastFunc = null;
    let duration = null;
    if (totalEntries < firstEntryNumber) {
        afterHideToastFunc = function () {
            window.location.href = window.location.pathname;
        }
        duration = 700;
    } else if (lastEntryNumber < firstEntryNumber) {
        afterHideToastFunc = function () {
            window.location.reload();
        };
        duration = 700;
    } else {
        paginationSummary.text(words.join(' '));
    }
    successToast('User ' + name + ' was deleted', duration, afterHideToastFunc);
}

function isNumber(n) {
    return /^-?[\d.]+(?:e-?\d+)?$/.test(n);
}
