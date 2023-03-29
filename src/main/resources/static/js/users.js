window.onload = function() {
    checkUserSuccessDelete();
};

function checkUserSuccessDelete() {
    let queryString = window.location.search;
    let urlParams = new URLSearchParams(queryString);
    if (urlParams.has('deleteSuccess')) {
        successToast('User ' + urlParams.get('deleteSuccess') + ' was deleted');
        urlParams.delete('deleteSuccess');
        let url = (urlParams.toString().length > 0) ? (window.location.pathname + '?' + urlParams) : window.location.pathname;
        window.history.replaceState(null, null, url);
        setupPageLinks();
    }
}

function setupPageLinks() {
    let pageLinks = $("a.page-link");
    for (let i = 0; i < pageLinks.length; i++) {
        let pageLink = pageLinks[i];
        let ref = pageLink.href.toString();
        let urlParts = ref.split('?');
        let urlParams = new URLSearchParams(urlParts[1]);
        urlParams.delete('deleteSuccess');
        ref = urlParts[0] + '?' + urlParams;
        pageLink.href = ref;
    }
}

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
    if (totalEntries < firstEntryNumber) {
        if (totalEntries < 2) {
            window.location.href = window.location.pathname + '?deleteSuccess=' + name;
        } else {
            let queryString = window.location.search;
            let urlParams = new URLSearchParams(queryString);
            urlParams.set('page', (urlParams.get('page') - 1));
            urlParams.append('deleteSuccess', name);
            window.location.href = window.location.pathname + '?' + urlParams;
        }
    } else if (lastEntryNumber < firstEntryNumber) {
        let queryString = window.location.search;
        let urlParams = new URLSearchParams(queryString);
        urlParams.append('deleteSuccess', name);
        window.location.href = window.location.pathname + '?' + urlParams;
    } else {
        paginationSummary.text(words.join(' '));
        successToast('User ' + name + ' was deleted');
    }
}

function isNumber(n) {
    return /^-?[\d.]+(?:e-?\d+)?$/.test(n);
}
