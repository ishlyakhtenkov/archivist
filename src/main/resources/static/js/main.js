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

function enableUser(id, enabled) {
    console.log('enable user ' + id + ' : ' + enabled);
    $.ajax({
        url: "users/" + id,
        type: "POST",
        data: "enabled=" + enabled
    }).done(function () {
        // chkbox.closest("tr").attr("data-userEnabled", enabled);
        // successNoty(enabled ? "common.enabled" : "common.disabled");
    }).fail(function () {
        // $(chkbox).prop("checked", !enabled);
    });
    // let checkbox = $('#' + id + '-checkbox');
    // checkbox.prop('checked', true);
}


