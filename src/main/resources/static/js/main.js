window.onload = function() {
    setupMenuItems();
    chooseMenuItem();
};

// make menu-items active after clicking
function setupMenuItems() {
    let menuItems = document.querySelectorAll('.menu-item');
    if (menuItems.length) {
        menuItems.forEach((menuItem) => {
            menuItem.addEventListener('click', (e) => {
                menuItems.forEach((menuItem) => {
                    menuItem.classList.remove('active');
                });
                // e.preventDefault();
                menuItem.classList.add('active');
            });
        });
    }
}

// click on menu-item if it was selected before page refresh
function chooseMenuItem() {
    let hash = document.location.hash;
    if (hash.length > 0) {
        let menuItemId = hash.split('#')[1];
        let menuItem = document.getElementById(menuItemId);
        if (menuItem) {
            menuItem.click();
        }
    }
}

function enableUser(checkbox, id) {
    let enabled = checkbox.checked;
    $.ajax({
        url: "users/" + id,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function () {
        successToast('User with id = ' + id + ' was ' + (enabled ? 'enabled' : 'disabled'));
    }).fail(function () {
        $(checkbox).prop('checked', !enabled);
        failToast('Failed to ' + (enabled ? 'enable' : 'disable') + ' user with id = ' + id);
    });
}

function successToast(message) {
    $.toast({
        heading: 'Success',
        text: message,
        showHideTransition: 'slide',
        position: 'bottom-right',
        icon: 'success'
    })
}

function failToast(message) {
    $.toast({
        heading: 'Error',
        text: message,
        showHideTransition: 'slide',
        position: 'bottom-right',
        icon: 'error'
    })
}

$('#userDeleteModal').on('show.bs.modal', function(e) {
    let userName = $(e.relatedTarget).data('bs-name');
    let userId = $(e.relatedTarget).data('bs-id');
    $(e.currentTarget).find('#userDeleteModalLabel').text('Delete user ' + userName + ' ?');
    $(e.currentTarget).find('#userDeleteModalUserId').val(userId);
    $(e.currentTarget).find('#userDeleteModalUserName').val(userName);
});

function deleteUser() {
    let userDeleteModal = $('#userDeleteModal');
    let userId = userDeleteModal.find('#userDeleteModalUserId').val();
    let userName = userDeleteModal.find('#userDeleteModalUserName').val();
    userDeleteModal.modal('toggle');
    $.ajax({
        url: "users/" + userId,
        type: "DELETE"
    }).done(function () {
        successToast('User ' + userName + ' was deleted');
        $('#row-' + userId).remove();
        let x = $('#pagination-info').text();
        let words = x.split(' ');
        let firstDigit = 0;
        let secondDigit = 0;
        let thirdDigit = 0;
        for (let i = 0, j = 0; i < words.length; i++) {
            if (isNumber(words[i])) {
                j++;
                if (j === 1) {
                    firstDigit = words[i];
                }
                if (j > 1) {
                    words[i] = words[i] - 1;
                    if (j === 2) {
                        secondDigit = words[i];
                    } else {
                        thirdDigit = words[i];
                    }
                }
            }
        }
        if (thirdDigit < firstDigit) {
            window.location.href = window.location.pathname;
        } else if (secondDigit < firstDigit) {
            window.location.reload();
        }

        $('#pagination-info').text(words.join(' '));
        console.log();
    }).fail(function () {
        failToast('Failed to delete user ' + userName);
    });
}

function isNumber(n) {
    return /^-?[\d.]+(?:e-?\d+)?$/.test(n); }

$.ajaxSetup({
    beforeSend: function(xhr, settings) {
        if (!csrfSafeMethod(settings.type) && !this.crossDomain) {
            let token = $("meta[name='_csrf']").attr("content");
            let header = $("meta[name='_csrf_header']").attr("content");
            xhr.setRequestHeader(header, token);
        }
    }
});

function csrfSafeMethod(method) {
    // these HTTP methods do not require CSRF protection
    return (/^(GET|HEAD|OPTIONS|TRACE)$/.test(method));
}
