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

let cardsAccordionOpened = null;
let sessionCardsAccordionOpened = sessionStorage.getItem('cardsAccordionOpened');
if (sessionCardsAccordionOpened != null) {
    cardsAccordionOpened = sessionCardsAccordionOpened === 'true';
} else {
    cardsAccordionOpened = isActive('documentsNavLink') || isActive('changeNoticesNavLink');
    sessionStorage.setItem('cardsAccordionOpened', cardsAccordionOpened);
}
if (cardsAccordionOpened === true) {
    $('#cardsAccordion').addClass('show');
    $('#cardsNavLinkIcon').html('<i class="fa-solid fa-chevron-up"></i>');
} else {
    if (isActive('documentsNavLink') || isActive('changeNoticesNavLink')) {
        $('#cardsNavLink').addClass('active');
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

function useCardsAccordion() {
    cardsAccordionOpened = !cardsAccordionOpened;
    sessionStorage.setItem('cardsAccordionOpened', cardsAccordionOpened);
    if (cardsAccordionOpened === true) {
        $('#cardsNavLink').removeClass('active');
        $("#cardsNavLinkIcon").html('<i class="fa-solid fa-chevron-up"></i>');
    } else {
        $("#cardsNavLinkIcon").html('<i class="fa-solid fa-chevron-down"></i>');
        if (isActive('documentsNavLink') || isActive('changeNoticesNavLink')) {
            $('#cardsNavLink').addClass('active');
        }
    }
}

function isActive(elementId) {
    return $('#' + elementId).hasClass('active');
}
