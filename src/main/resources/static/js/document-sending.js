const companySelector = $('#companySelector');

function setupAddSendingModal(event) {
    companySelector.empty();
    fillCompaniesSelector(); // add to company-selector
    $(event.currentTarget).find('#statusSelector').val('');
    $(event.currentTarget).find('#invoiceNumInput').val('');
    $(event.currentTarget).find('#invoiceDateInput').val('');
    $(event.currentTarget).find('#letterNumInput').val('');
    $(event.currentTarget).find('#letterDateInput').val('').css('color', 'transparent');
}

function fillCompaniesSelector() {
    $.ajax({
        url: '/companies/list'
    }).done(companies => {
        if (companies.length !== 0) {
            companies.forEach(company => {
                companySelector.append($('<option></option>').val(company.id).html(company.name));
            });
            companySelector.val('');
        }
    }).fail(function (data) {
        handleError(data, 'Failed to get companies');
    });
}
