let referencesAccordionOpened = null;
let sessionReferencesAccordionOpened = sessionStorage.getItem('referencesAccordionOpened');
if (sessionReferencesAccordionOpened != null) {
    referencesAccordionOpened = sessionReferencesAccordionOpened === 'true';
} else {
    referencesAccordionOpened = isActive('companiesNavLink') || isActive('departmentsNavLink');
    sessionStorage.setItem('referencesAccordionOpened', referencesAccordionOpened);
}
if (referencesAccordionOpened === true) {
    $('#referencesAccordion').addClass('show');
    $('#referencesNavLinkIcon').html('<i class="fa-solid fa-chevron-up"></i>');
} else {
    if (isActive('companiesNavLink') || isActive('departmentsNavLink')) {
        $('#referencesNavLink').addClass('active');
    }
}

function useReferencesAccordion() {
    referencesAccordionOpened = !referencesAccordionOpened;
    sessionStorage.setItem('referencesAccordionOpened', referencesAccordionOpened);
    if (referencesAccordionOpened === true) {
        $('#referencesNavLink').removeClass('active');
        $("#referencesNavLinkIcon").html('<i class="fa-solid fa-chevron-up"></i>');
    } else {
        $("#referencesNavLinkIcon").html('<i class="fa-solid fa-chevron-down"></i>');
        if (isActive('companiesNavLink') || isActive('departmentsNavLink')) {
            $('#referencesNavLink').addClass('active');
        }
    }
}

function isActive(elementId) {
    return $('#' + elementId).hasClass('active');
}