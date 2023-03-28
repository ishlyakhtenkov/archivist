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
