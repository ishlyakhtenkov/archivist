const userDeleteParam = 'deleteSuccess';

window.onload = function() {
    checkUserActionHappened();
};

function checkUserActionHappened() {
    let actionSpan = $("#action-span");
    if (actionSpan.length) {
        successToast(`User ${actionSpan.data('name')} was ${actionSpan.data('action')}`);
    } else {
        checkUserDeleteHappened();
    }
}

function checkUserDeleteHappened() {
    let queryString = window.location.search;
    let urlParams = new URLSearchParams(queryString);
    if (urlParams.has(userDeleteParam)) {
        successToast(`User ${urlParams.get(userDeleteParam)} was deleted`);
        urlParams.delete(userDeleteParam);
        let url = (urlParams.toString().length > 0) ? (window.location.pathname + '?' + urlParams) : window.location.pathname;
        window.history.replaceState(null, null, url);
        fixPaginationLinks();
    }
}

function fixPaginationLinks() {
    let paginationLinks = $("a.page-link");
    for (let i = 0; i < paginationLinks.length; i++) {
        let paginationLink = paginationLinks[i];
        let url = paginationLink.href.toString();
        let urlParts = url.split('?');
        let urlParams = new URLSearchParams(urlParts[1]);
        urlParams.delete(userDeleteParam);
        url = urlParts[0] + '?' + urlParams;
        paginationLink.href = url;
    }
}

function enableUser(checkbox, id) {
    let enabled = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: "users/" + id,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function() {
        successToast(`User ${name} was ${enabled ? 'enabled' : 'disabled'}`);
    }).fail(function(data) {
        $(checkbox).prop('checked', !enabled);
        handleError(data, `Failed to ${enabled ? 'enable' : 'disable'} user ${name}`);
    });
}

$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete user: ${name}`);
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
    }).done(function() {
        updatePageContent(id, name);
    }).fail(function(data) {
        handleError(data, `Failed to delete user ${name}`);
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
        if (totalEntries === 0) {
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

$('#changePasswordModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#changePasswordModalLabel').text(`Change password ${name ? `for: ${name}` : ''}`);
    if (name) {
        $(e.currentTarget).find('#changePasswordModalUserName').val(name);
    }
    if (id) {
        $(e.currentTarget).find('#changePasswordModalUserId').val(id);
    }
    $(e.currentTarget).find('#new-password').val('');
    $(e.currentTarget).find('#repeat-password').val('');
    $(e.currentTarget).find('#checkPasswordMatch').text('');
    $(e.currentTarget).find('#change-password-button').prop('disabled', false);
});

function changePassword() {
    let changePasswordModal = $('#changePasswordModal');
    let id = changePasswordModal.find('#changePasswordModalUserId').val();
    let name = changePasswordModal.find('#changePasswordModalUserName').val();
    let password = changePasswordModal.find('#new-password').val();
    let repeatPassword = changePasswordModal.find('#repeat-password').val();
    if (password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: "users/password/" + id,
            type: "PATCH",
            data: "password=" + password
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast(`Password for ${name} was changed`);
        }).fail(function (data) {
            handleError(data, `Failed to change password for ${name}`);
        });
    }
}

function changePasswordProfile() {
    let changePasswordModal = $('#changePasswordModal');
    let password = changePasswordModal.find('#new-password').val();
    let repeatPassword = changePasswordModal.find('#repeat-password').val();
    if (password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: "profile/password",
            type: "PATCH",
            data: "password=" + password
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast('Password was changed');
        }).fail(function(data) {
            handleError(data, 'Failed to change password');
        });
    }
}

function handleError(data, message) {
    if (data.status === 0) {
        window.location.reload();
    }
    let detail = data.responseJSON.detail;
    if (detail.includes('changePassword')) {
        detail = detail.replace('changePassword.', '');
    }
    failToast(`${message}:<br>${detail}`);
}

$("#repeat-password").on('keyup', function(){
    checkPasswordsMatch();
});

$("#new-password").on('keyup', function(){
    checkPasswordsMatch();
});

function checkPasswordsMatch() {
    let password = $("#new-password").val();
    let repeatPassword = $("#repeat-password").val();
    if (repeatPassword.length && password !== repeatPassword) {
        $("#repeat-password").addClass('form-control is-invalid');
        $("#checkPasswordMatch").html('<li>password does not match</li>');
        $('#submit-with-pass-button').prop('disabled', true);
    }
    else {
        $("#repeat-password").removeClass('form-control is-invalid');
        $("#repeat-password").addClass('form-control');
        $("#checkPasswordMatch").html('');
        $('#submit-with-pass-button').prop('disabled', false);
    }
}
