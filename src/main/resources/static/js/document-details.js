const documentId =$('#documentId').val();

$(window).on('load', () => init());

function init() {
    checkActionHappened();
    $('#documentApplicabilityTabButton').on('shown.bs.tab', () => {
        getApplicabilities();
    });
    $('#deleteApplicabilityModal').on('show.bs.modal', function(e) {
        let decimalNumber = $(e.relatedTarget).data('decimalnumber');
        let id = $(e.relatedTarget).data('id');
        $(e.currentTarget).find('#deleteApplicabilityModalLabel').text(`Delete applicability: ${decimalNumber}?`);
        $(e.currentTarget).find('#deleteApplicabilityModalApplicabilityId').val(id);
        $(e.currentTarget).find('#deleteApplicabilityModalApplicabilityDecimalNumber').val(decimalNumber);
    });
}

function checkActionHappened() {
    let actionSpan = $("#actionSpan");
    if (actionSpan.length) {
        successToast(`${actionSpan.data('action')}`);
    }
}

function getApplicabilities() {
    $.ajax({
        url: `/documents/${documentId}/applicabilities`
    }).done(applicabilities => {
        fillApplicabilitiesTable(applicabilities);
    }).fail(function (data) {
        handleError(data, `Failed to get applicabilities`);
    });
}

function fillApplicabilitiesTable(applicabilities) {
    $('#applicabilitiesTable > tbody').empty();
    if (applicabilities.length !== 0) {
        $('#applicabilitiesTable').attr('hidden', false);
        applicabilities.forEach(applicability => {
            addApplicabilityRow(applicability);
        });
    } else {
        $('#applicabilitiesTable').attr('hidden', true);
    }
}

function addApplicabilityRow(applicability) {
    let decimalNumberTd = $('<td></td>').text(`${applicability.applicability.decimalNumber}`);
    let nameTd = $('<td></td>').html(`${applicability.applicability.name != null ? applicability.applicability.name : ''} ${applicability.primal ? '<span class="badge text-bg-primary mt-0">Primal</span>' : ''}`);
    let deleteActionTd = $('<td></td>').addClass('text-end').html(`<a type="button" class="trash-button me-3"
            title="Delete applicability" data-bs-toggle="modal" data-bs-target="#deleteApplicabilityModal"
            data-decimalnumber=${applicability.applicability.decimalNumber} data-id=${applicability.id}> <i class="fa-solid fa-trash"></i></a>`);
    let applicabilityRow = $('<tr></tr>');
    applicabilityRow.append(decimalNumberTd);
    applicabilityRow.append(nameTd);
    applicabilityRow.append(deleteActionTd);
    $('#applicabilitiesTable > tbody').append(applicabilityRow);
}

function showInputApplicabilityDiv() {
    $('#addApplicabilityButtonDiv').attr('hidden', true);
    $('#inputApplicabilityDiv').attr('hidden', false);
}

function cancelAddApplicability() {
    $('#applicabilityDecNumberInput').val('');
    $('#primalCheckbox').prop('checked', false);
    $('#addApplicabilityButtonDiv').attr('hidden', false);
    $('#inputApplicabilityDiv').attr('hidden', true);
}

function deleteApplicability() {
    let deleteModal = $('#deleteApplicabilityModal');
    let id = deleteModal.find('#deleteApplicabilityModalApplicabilityId').val();
    let decimalNumber = deleteModal.find('#deleteApplicabilityModalApplicabilityDecimalNumber').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: `applicabilities/${id}`,
        type: "DELETE"
    }).done(function() {
        getApplicabilities();
        successToast(`Applicability ${decimalNumber} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete applicability ${decimalNumber}`);
    });
}

function createApplicability() {
    let applicabilityDecimalNumber = $('#applicabilityDecNumberInput').val();
    $.ajax({
        url: '/documents/applicabilities',
        type: 'POST',
        data: JSON.stringify(makeApplicabilityObject()),
        contentType: 'application/json; charset=utf-8'
    }).done(() => {
        getApplicabilities();
        cancelAddApplicability();
        successToast(`Applicability ${applicabilityDecimalNumber} was added`);
    }).fail(function(data) {
        handleError(data, `Failed to create applicability ${applicabilityDecimalNumber}`);
    });
}

function makeApplicabilityObject() {
    return {
        documentId: documentId,
        decimalNumber: $('#applicabilityDecNumberInput').val(),
        primal: $('#primalCheckbox').is(':checked')
    };
}
