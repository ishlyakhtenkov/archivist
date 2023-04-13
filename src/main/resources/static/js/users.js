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
        deleteTableRow(id, `User ${name} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete user ${name}`);
    });
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

$('#forgotPasswordModal').on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#email').val('');
});

function forgotPassword() {
    let forgotPasswordModal = $('#forgotPasswordModal');
    let email = forgotPasswordModal.find('#email').val();
    if (email.length) {
        $.ajax({
            url: "profile/forgotPassword",
            type: "POST",
            data: "email=" + email
        }).done(function () {
            forgotPasswordModal.modal('toggle');
            $('#emailSentModal').modal('toggle');
        }).fail(function(data) {
            handleError(data, 'Failed to reset password');
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
