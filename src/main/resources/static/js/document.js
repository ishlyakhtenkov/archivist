const documentId =$('#documentId').val();
const contentArea = $('#contentArea');
let previousContentOpened = false;
const subscriberColumns = [$('#subscriberColumn1'), $('#subscriberColumn2'), $('#subscriberColumn3')];
const subscriberMap = new Map();
const companySelector = $('#companySelector');

$(window).on('load', () => init());

function init() {
    $('#documentChangesTabButton').on('shown.bs.tab', () => {
        cancelAddChange();
        getChanges();
    });
    $('#documentApplicabilityTabButton').on('shown.bs.tab', () => {
        cancelAddApplicability();
        getApplicabilities();
    });
    $('#documentContentTabButton').on('shown.bs.tab', () => {
        previousContentOpened = false;
        cancelAddContent();
        getLatestContent();
    });
    $('#documentSubscribersTabButton').on('shown.bs.tab', () => {
        getSubscribers();
    });
    $('#deleteApplicabilityModal').on('show.bs.modal', function(e) {
        let decimalNumber = $(e.relatedTarget).data('decimalnumber');
        let id = $(e.relatedTarget).data('id');
        $(e.currentTarget).find('#deleteApplicabilityModalLabel').text(`Delete applicability: ${decimalNumber}?`);
        $(e.currentTarget).find('#deleteApplicabilityModalApplicabilityId').val(id);
        $(e.currentTarget).find('#deleteApplicabilityModalApplicabilityDecimalNumber').val(decimalNumber);
    });
    $('#deleteContentModal').on('show.bs.modal', function(e) {
        let id = $(e.relatedTarget).data('id');
        let changeNumber = $(e.relatedTarget).data('changenumber');
        $(e.currentTarget).find('#deleteContentModalLabel').text(`Delete content (change number: ${changeNumber})?`);
        $(e.currentTarget).find('#deleteContentModalContentId').val(id);
        $(e.currentTarget).find('#deleteContentModalContentChangeNumber').val(changeNumber);
    });
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
    let decimalNumberTd = $('<td></td>').html(`${applicability.applicability.decimalNumber} ${applicability.primal ? '<span class="badge text-bg-primary mt-0" style="font-size: 9px;">Primal</span>' : ''}`);
    let nameTd = $('<td></td>').text(`${applicability.applicability.name != null ? applicability.applicability.name : ''}`);
    let applicabilityRow = $('<tr></tr>');
    applicabilityRow.append(decimalNumberTd);
    applicabilityRow.append(nameTd);
    if (hasAdminOrArchivistRole()) {
        let deleteActionTd = $('<td></td>').addClass('text-end').html(`<a type="button" class="trash-button me-3"
            title="Delete applicability" data-bs-toggle="modal" data-bs-target="#deleteApplicabilityModal"
            data-decimalnumber=${applicability.applicability.decimalNumber} data-id=${applicability.id}> <i class="fa-solid fa-trash"></i></a>`);
        applicabilityRow.append(deleteActionTd);
    } else {
        applicabilityRow.append(('<td></td>'));
    }
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
        } else {
            $('#showPreviousContentButton').attr('hidden', true);
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
    let changeNumberCol = $('<div></div>').addClass('col').html(`<span class="badge rounded-circle text-bg-${bgColor}" title="Change number">${content.changeNumber}</span>`);
    infoRow.append(changeNumberCol);
    if (hasAdminOrArchivistRole()) {
        let deleteButtonCol = $('<div></div>').addClass('col text-end').html(`<a type="button" class="trash-button"
            title="Delete content" data-bs-toggle="modal" data-bs-target="#deleteContentModal"
            data-changenumber=${content.changeNumber} data-id=${content.id}> <i class="fa-solid fa-trash"></i></a>`);
        infoRow.append(deleteButtonCol);
    }
    cardHeader.append(infoRow);
    content.files.forEach(file => {
        let fileRow = $('<div></div>').html(`<a href="/documents/content/download?fileLink=${file.fileLink}" target="_blank">${file.fileName}</a>`);
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
        type: 'DELETE'
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

function showInputContentDiv() {
    $('#addContentButtonDiv').attr('hidden', true);
    $('#inputContentDiv').attr('hidden', false);
}

function cancelAddContent() {
    $('#contentFilesInput').val('');
    $('#contentChangeNumberInput').val('');
    $('#addContentButtonDiv').attr('hidden', false);
    $('#inputContentDiv').attr('hidden', true);
}

function createContent() {
    let changeNumber = $('#contentChangeNumberInput').val();
    let formData = new FormData();
    formData.append('id', documentId);
    formData.append('changeNumber', changeNumber);
    let inputtedFiles = $('#contentFilesInput').prop('files');
    for (let i = 0; i < inputtedFiles.length; i++) {
        formData.append('files', inputtedFiles[i]);
    }
    if (changeNumber.length && inputtedFiles.length) {
        $.ajax({
            url: '/documents/content',
            type: 'POST',
            data: formData,
            processData:false,
            contentType:false
        }).done(function () {
            if (previousContentOpened) {
                getAllContents();
            } else {
                getLatestContent();
            }
            cancelAddContent();
            successToast(`Content (change number = ${changeNumber}) was created`);
        }).fail(function (data) {
            handleError(data, `Failed to create content (change number = ${changeNumber})`);
        });
    }
}

function getSubscribers() {
    clearSubscriberColumns();
    $.ajax({
        url: `/documents/${documentId}/subscribers`
    }).done(subscribers => {
        if (subscribers.length !== 0) {
            let index = 0;
            subscribers.forEach(subscriber => {
                subscriberMap.set(subscriber.id, subscriber);
                subscriberColumns[(index % subscriberColumns.length)].append(generateSubscriberCard(subscriber));
                index++;
            });
        }
    }).fail(function (data) {
        handleError(data, `Failed to get subscribers`);
    });
}

function clearSubscriberColumns() {
    subscriberColumns.forEach(subscriberColumn => subscriberColumn.empty());
}

function generateSubscriberCard(subscriber) {
    let card = $('<div></div>').addClass(`card mt-1 bg-${subscriber.subscribed ? 'light-green' : 'light'}`);
    let cardBody = $('<div></div>').addClass('card-body p-2 text-start');
    let titleRow = $('<div></div>').addClass('row card-title me-2').html(`<div class="col-11 text-truncate">${subscriber.company.name}</div>
                                        <div class="col-1 pe-2"><a type="button" title="Show info" class="ms-1" data-bs-toggle="modal"
                                        data-bs-target="#subscriberInfoModal" data-subscriberid='${subscriber.id}'>
                                        <i class="fa-solid fa-circle-info"></i></a></div>`);
    cardBody.append(titleRow);
    let infoRow = $('<div></div>').addClass('row mt-2').html(`<div class="col-5 small"></div><div class="col-7 small text-end">${subscriber.status.replace('_', ' ').toLowerCase()}</div>`);
    if (hasAdminOrArchivistRole() && (subscriber.subscribed || subscriber.unsubscribeTimestamp != null)) {
        let infoRowHtml = `<a type="button" title='${subscriber.subscribed ? 'Unsubscribe' : 'Resubscribe'}' data-bs-toggle="modal"
                                data-bs-target=${subscriber.subscribed ? '#unsubscribeModal' : '#resubscribeModal'}
                                data-subscriberid='${subscriber.id}' data-subscribername='${subscriber.company.name}'>
                                <span class="badge text-bg-${subscriber.subscribed ? 'danger' : 'success'}">${subscriber.subscribed ? 'Unsub' : 'Resub'}</span></a>`;
        infoRow.find('.col-5').html(infoRowHtml);
    }
    cardBody.append(infoRow);
    card.append(cardBody);
    return card;
}

$('#addSendingModal').on('show.bs.modal', function(e) {
    companySelector.empty();
    fillCompaniesSelector(); // add to company-selector
    $(e.currentTarget).find('#statusSelector').val('');
    $(e.currentTarget).find('#invoiceNumInput').val('');
    $(e.currentTarget).find('#invoiceDateInput').val('');
    $(e.currentTarget).find('#letterNumInput').val('');
    $(e.currentTarget).find('#letterDateInput').val('').css('color', 'transparent');
});

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
        handleError(data, `Failed to get companies`);
    });
}

function createSending() {
    let companyId = companySelector.val();
    let status = $('#statusSelector').val();
    let invoiceNum = $('#invoiceNumInput').val();
    let invoiceDate = $('#invoiceDateInput').val();
    if (companyId.length && status.length && invoiceNum.length && invoiceDate.length) {
        $.ajax({
            url: '/documents/sendings',
            type: 'POST',
            data: JSON.stringify(makeSendingToObject()),
            contentType: 'application/json; charset=utf-8'
        }).done(() => {
            getSubscribers();
            $('#addSendingModal').modal('toggle');
            successToast(`Sending was added`);
        }).fail(function(data) {
            handleError(data, 'Failed to add sending');
        });
    }
}

function makeSendingToObject() {
    return {
        documentId: documentId,
        companyId: companySelector.val(),
        status: $('#statusSelector').val(),
        invoiceNumber: $('#invoiceNumInput').val(),
        invoiceDate: $('#invoiceDateInput').val(),
        letterNumber: $('#letterNumInput').val(),
        letterDate: $('#letterDateInput').val()
    };
}

$('#unsubscribeModal').on('show.bs.modal', function(e) {
    let subscriberName = $(e.relatedTarget).data('subscribername');
    let subscriberId = $(e.relatedTarget).data('subscriberid');
    $(e.currentTarget).find('#unsubscribeModalLabel').text(`Unsubscribe: ${subscriberName}?`);
    $(e.currentTarget).find('#unsubscribeModalSubscriberId').val(subscriberId);
    $(e.currentTarget).find('#unsubscribeModalSubscriberName').val(subscriberName);
    $(e.currentTarget).find('#unsubscribeModalUnsubscribeReason').val('');
});

$('#resubscribeModal').on('show.bs.modal', function(e) {
    let subscriberName = $(e.relatedTarget).data('subscribername');
    let subscriberId = $(e.relatedTarget).data('subscriberid');
    $(e.currentTarget).find('#resubscribeModalLabel').text(`Resubscribe: ${subscriberName}?`);
    $(e.currentTarget).find('#resubscribeModalSubscriberId').val(subscriberId);
    $(e.currentTarget).find('#resubscribeModalSubscriberName').val(subscriberName);
});

$('#subscriberInfoModal').on('show.bs.modal', function(e) {
    let subscriber = subscriberMap.get($(e.relatedTarget).data('subscriberid'));
    fillSubscriberInfoModalLabel(subscriber);
    fillUnsubscribeInfoArea(subscriber);
    getSendings(subscriber.company.id);
});

function fillSubscriberInfoModalLabel(subscriber) {
    let subscribedBadgeText;
    if (subscriber.subscribed) {
        subscribedBadgeText = 'Subscribed';
    } else if (subscriber.unsubscribeTimestamp != null) {
        subscribedBadgeText = 'Unsubscribed';
    } else {
        subscribedBadgeText = 'Not subscribed';
    }
    let subscribedBadgeHtml = `<span class="badge text-bg-${subscriber.subscribed ? 'success' : 'secondary'}">${subscribedBadgeText}</span>`;
    $('#subscriberInfoModalLabel').html(`${subscriber.company.name} ${subscribedBadgeHtml}`);
}

function fillUnsubscribeInfoArea(subscriber) {
    $('#unsubscribeInfo').empty();
    if (subscriber.unsubscribeTimestamp != null) {
        let unsubscribeInfoArea = $('#unsubscribeInfo');
        let unsubscribeInfo = $('<div></div>').addClass('mb-2').text(`Unsubscribed ${new Date(subscriber.unsubscribeTimestamp).toLocaleString()} due to: ${subscriber.unsubscribeReason}`);
        unsubscribeInfoArea.append(unsubscribeInfo);
    }
}

function getSendings(companyId) {
    $('#sendingsTable').attr('hidden', true);
    $('#sendingsTable > tbody').empty();
    $.ajax({
        url: `/documents/${documentId}/sendings/by-company`,
        data: `companyId=${companyId}`
    }).done(sendings => {
        if (sendings.length !== 0) {
            $('#sendingsTable').attr('hidden', false);
            sendings.forEach(sending => {
                addSendingRow(sending, companyId);
            });
            let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
            let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
                new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));
        } else {
            $('#subscriberInfoModal').modal('toggle');
        }
    }).fail(function (data) {
        handleError(data, `Failed to get sendings`);
    });
}

function addSendingRow(sending, companyId) {
    let docStatusTd = $('<td></td>').text(`${sending.invoice.status}`);
    let invoiceTd = $('<td></td>').html(`${sending.invoice.number} (${new Date(sending.invoice.date).toLocaleDateString()})`);
    let letterTdHtml = '';
    if (sending.invoice.letter.number != null) {
        letterTdHtml = `${sending.invoice.letter.number} (${new Date(sending.invoice.letter.date).toLocaleDateString()})`;
    }
    let letterTd = $('<td></td>').html(letterTdHtml);
    let deleteSendingSubmitHtml = `<button class='btn btn-sm btn-secondary ms-3'>Cancel</button>
                                       <button class='btn btn-sm btn-danger' onclick='deleteSending(${sending.id}, ${companyId})'>Delete</button>`;

    let sendingRow = $('<tr></tr>');
    sendingRow.append(docStatusTd);
    sendingRow.append(invoiceTd);
    sendingRow.append(letterTd);
    if (hasAdminOrArchivistRole()) {
        let deleteActionTd = $('<td></td>').addClass('text-end').html(`<a tabindex="0" type="button" class="trash-button me-3"
                title="Delete sending" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-title="Delete this sending?"
                data-bs-content="${deleteSendingSubmitHtml}"> <i class="fa-solid fa-trash"></i></a>`);
        sendingRow.append(deleteActionTd);
    } else {
        sendingRow.append($('<td></td>'));
    }
    $('#sendingsTable > tbody').append(sendingRow);
}

function deleteSending(sendingId, companyId) {
    $.ajax({
        url: `sendings/${sendingId}`,
        type: "DELETE"
    }).done(function() {
        getSendings(companyId);
        getSubscribers();
        successToast('Sending was deleted');
    }).fail(function(data) {
        handleError(data, 'Failed to delete sending');
    });
}

function resubscribe() {
    let resubscribeModal = $('#resubscribeModal');
    let id = resubscribeModal.find('#resubscribeModalSubscriberId').val();
    let name = resubscribeModal.find('#resubscribeModalSubscriberName').val();
    resubscribeModal.modal('toggle');
    $.ajax({
        url: `subscribers/${id}/resubscribe`,
        type: "PATCH"
    }).done(function() {
        getSubscribers();
        successToast(`${name} was resubscribed`);
    }).fail(function(data) {
        handleError(data, `Failed to resubscribe ${name}`);
    });
}

function unsubscribe() {
    let unsubscribeModal = $('#unsubscribeModal');
    let id = unsubscribeModal.find('#unsubscribeModalSubscriberId').val();
    let name = unsubscribeModal.find('#unsubscribeModalSubscriberName').val();
    let unsubscribeReason = unsubscribeModal.find('#unsubscribeModalUnsubscribeReason').val();
    if (unsubscribeReason.length) {
        $.ajax({
            url: `subscribers/${id}/unsubscribe`,
            type: "PATCH",
            data: "unsubscribeReason=" + unsubscribeReason
        }).done(function() {
            unsubscribeModal.modal('toggle');
            getSubscribers();
            successToast(`${name} was unsubscribed`);
        }).fail(function(data) {
            handleError(data, `Failed to unsubscribe ${name}`);
        });
    }
}

function getChanges() {
    $.ajax({
        url: `/documents/${documentId}/changes`
    }).done(changes => {
        fillChangesTable(changes);
    }).fail(function (data) {
        handleError(data, `Failed to get changes`);
    });
}

function fillChangesTable(changes) {
    $('#changesTable > tbody').empty();
    if (changes.length !== 0) {
        $('#changesTable').attr('hidden', false);
        changes.forEach(change => {
            addChangeRow(change);
        });
        let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
        let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
            new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));
    } else {
        $('#changesTable').attr('hidden', true);
    }
}

function addChangeRow(change) {
    let changeNumberTd = $('<td></td>').text(change.changeNumber);
    let changeNoticeTd = $('<td></td>').addClass('text-start').text(change.changeNotice.name);
    let changeDateTd = $('<td></td>').text(change.changeNotice.releaseDate);
    let changeRow = $('<tr></tr>');
    changeRow.append(changeNumberTd);
    changeRow.append(changeNoticeTd);
    changeRow.append(changeDateTd);
    if (hasAdminOrArchivistRole()) {
        let deleteChangeSubmitHtml = `<button class='btn btn-sm btn-secondary ms-3'>Cancel</button>
                                       <button class='btn btn-sm btn-danger' onclick='deleteChange(${change.id}, ${change.changeNumber})'>Delete</button>`;
        let deleteActionTd = $('<td></td>').addClass('text-end').html(`<a tabindex="0" type="button" class="trash-button me-3"
                title="Delete change" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-title="Delete this change?"
                data-bs-content="${deleteChangeSubmitHtml}"> <i class="fa-solid fa-trash"></i></a>`);
        changeRow.append(deleteActionTd);
    } else {
        changeRow.append($('<td></td>'));
    }
    $('#changesTable > tbody').append(changeRow);
}

function showInputChangeDiv() {
    $('#addChangeButtonDiv').attr('hidden', true);
    $('#inputChangeDiv').attr('hidden', false);
}

function cancelAddChange() {
    $('#changeChangeNumberInput').val('');
    $('#changeChangeNoticeNameInput').val('');
    $('#changeChangeNoticeDateInput').attr('type', 'text').val('');
    $('#addChangeButtonDiv').attr('hidden', false);
    $('#inputChangeDiv').attr('hidden', true);
}

function createChange() {
    let changeNumber = $('#changeChangeNumberInput').val();
    if (changeNumber.length && $('#changeChangeNoticeNameInput').val().length &&
        $('#changeChangeNoticeDateInput').val().length) {
        $.ajax({
            url: '/documents/changes',
            type: 'POST',
            data: JSON.stringify(makeChangeObject()),
            contentType: 'application/json; charset=utf-8'
        }).done(() => {
            getChanges();
            cancelAddChange();
            successToast(`Change ${changeNumber} was added`);
        }).fail(function(data) {
            handleError(data, `Failed to create change ${changeNumber}`);
        });

    }
}

function makeChangeObject() {
    return {
        documentId: documentId,
        changeNoticeName: $('#changeChangeNoticeNameInput').val(),
        changeNoticeDate: $('#changeChangeNoticeDateInput').val(),
        changeNumber: $('#changeChangeNumberInput').val()
    };
}

function deleteChange(changeId, changeNumber) {
    $.ajax({
        url: `changes/${changeId}`,
        type: "DELETE"
    }).done(function() {
        getChanges();
        successToast(`Change ${changeNumber} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete change ${changeNumber}`);
    });
}
