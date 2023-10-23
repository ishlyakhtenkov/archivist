const documentId =$('#documentId').val();

$(window).on('load', () => init());

function init() {
    checkActionHappened();
    $('#documentApplicabilityTabButton').on('shown.bs.tab', () => {
        cancelAddApplicability();
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
    if (applicabilityDecimalNumber.length) {
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
}

function makeApplicabilityObject() {
    return {
        documentId: documentId,
        decimalNumber: $('#applicabilityDecNumberInput').val(),
        primal: $('#primalCheckbox').is(':checked')
    };
}

const contentArea = $('#contentArea');
let previousContentOpened = false;

$('#deleteContentModal').on('show.bs.modal', function(e) {
    let id = $(e.relatedTarget).data('id');
    let changeNumber = $(e.relatedTarget).data('changenumber');
    $(e.currentTarget).find('#deleteContentModalLabel').text(`Delete content (change number: ${changeNumber})?`);
    $(e.currentTarget).find('#deleteContentModalContentId').val(id);
    $(e.currentTarget).find('#deleteContentModalContentChangeNumber').val(changeNumber);
});

$('#documentContentTabButton').on('shown.bs.tab', () => {
    getLatestContent();
});

function getLatestContent() {
    $.ajax({
        url: `/documents/${documentId}/content/latest`
    }).done(content => {
        contentArea.empty();
        if (content.length !== 0) {
            contentArea.append(generateContentCard(content, 'success'));
            if (content.changeNumber !== 0) {
                if (!previousContentOpened) {
                    $('#showPreviousContentButton').attr('hidden', false);
                }
            } else {
                $('#showPreviousContentButton').attr('hidden', true);
            }
        }
    }).fail(function (data) {
        handleError(data, `Failed to get latest content`);
    });
}

function getAllContents() {
    $.ajax({
        url: `/documents/${documentId}/content/all`
    }).done(contents => {
        $('#showPreviousContentButton').attr('hidden', true);
        contentArea.empty();
        if (contents.length !== 0) {
            for (let i = 0; i < contents.length; i++) {
                contentArea.append(generateContentCard(contents[i], `${i === 0 ? 'success' : 'warning'}`));
            }
        }
        if (!previousContentOpened && contents.length === 1) {
            contentArea.append($('<div></div>').text('No previous content'));
        }
        previousContentOpened = true;
    }).fail(function (data) {
        handleError(data, `Failed to get all contents`);
    });
}

function generateContentCard(content, bgColor) {
    let card = $('<div></div>').addClass('card mb-3');
    let cardHeader = $('<div></div>').addClass('card-header');
    let cardBody = $('<div></div>').addClass('card-body pt-1 pb-2');
    let infoRow = $('<div></div>').addClass('row');
    let changeNumberCol = $('<div></div>').addClass('col').html(`<span class="badge rounded-circle text-bg-${bgColor}">${content.changeNumber}</span>`);
    let deleteButtonCol = $('<div></div>').addClass('col text-end').html(`<a type="button" class="trash-button"
            title="Delete content" data-bs-toggle="modal" data-bs-target="#deleteContentModal"
            data-changenumber=${content.changeNumber} data-id=${content.id}> <i class="fa-solid fa-trash"></i></a>`);
    infoRow.append(changeNumberCol);
    infoRow.append(deleteButtonCol);
    cardHeader.append(infoRow);
    content.files.forEach(file => {
        let fileRow = $('<div></div>').html(`<a href="/documents/content/download?fileLink=${file.fileLink}" target="_blank">${file.name}</a>`);
        cardBody.append(fileRow);
    });
    card.append(cardHeader);
    card.append(cardBody);
    return card;
}

function deleteContent() {
    let deleteModal = $('#deleteContentModal');
    let id = deleteModal.find('#deleteContentModalContentId').val();
    let changeNumber = deleteModal.find('#deleteContentModalContentChangeNumber').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: `content/${id}`,
        type: "DELETE"
    }).done(function() {
        if (previousContentOpened) {
            getAllContents();
        } else {
            getLatestContent();
        }
        successToast(`Content (change number: ${changeNumber}) was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete content (change number: ${changeNumber})`);
    });
}
