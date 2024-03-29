$('#groupAddSendingModal').on('show.bs.modal', (event) => {
    setupAddSendingModal(event);
    $(event.currentTarget).find('#addSendingFileInput').val('').css('color', 'transparent');
});

function createGroupSending() {
    let companyId = companySelector.val();
    let status = $('#statusSelector').val();
    let invoiceNumber = $('#invoiceNumInput').val();
    let invoiceDate = $('#invoiceDateInput').val();
    let inputtedFile = $('#addSendingFileInput').prop('files');
    let letterNumber = $('#letterNumInput').val();
    let letterDate = $('#letterDateInput').val();
    if (companyId !== null && status !== null && invoiceNumber.length && invoiceDate.length && inputtedFile.length) {
        let formData = new FormData();
        formData.append('companyId', companyId);
        formData.append('status', status);
        formData.append('invoiceNumber', invoiceNumber);
        formData.append('invoiceDate', invoiceDate);
        formData.append('file', inputtedFile[0]);
        if (letterNumber.length) {
            formData.append('letterNumber', letterNumber);
        }
        if (letterDate.length) {
            formData.append('letterDate', letterDate);
        }

        $.ajax({
            url: '/tools/group/sending/add',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false
        }).done((result) => {
            $('#groupAddSendingModal').modal('toggle');
            successToast('Sending for documents group was added');
            $('#resultContent').empty();
            $('#resultContent').append(generateResultCard(result.sentDocuments, 'success', 'Sent documents'));
            $('#resultContent').append(generateResultCard(result.alreadySentDocuments, 'primary', 'Earlier sent documents'));
            $('#resultContent').append(generateResultCard(result.notFoundDocuments, 'danger', 'Not found documents'));
            $('#resultModalLabel').text('Group add sending operation result');
            $('#resultModal').modal('toggle');
        }).fail((data) => {
            handleError(data, 'Failed to add sending for documents group');
        });
    }
}

function generateResultCard(documents, color, header) {
    let card = $('<div></div>').addClass(`card border-${color} mt-3`);
    let cardHeader = $('<div></div>').addClass(`card-header text-${color} fw-bold`).text(header);
    let cardBody = $('<div></div>').addClass('card-body');
    let list = $('<ol></ol>');
    documents.forEach(document => list.append($('<li></li>').text(document)));
    cardBody.append(list);
    card.append(cardHeader);
    card.append(cardBody);
    return card;
}

$('#groupDeleteSendingModal').on('show.bs.modal', (event) => {
    $(event.currentTarget).find('#deleteSendingStatusSelector').val('');
    $(event.currentTarget).find('#deleteSendingInvoiceNumInput').val('');
    $(event.currentTarget).find('#deleteSendingInvoiceDateInput').val('');
    $(event.currentTarget).find('#deleteSendingFileInput').val('').css('color', 'transparent');
});

function deleteGroupSending() {
    let status = $('#deleteSendingStatusSelector').val();
    let invoiceNumber = $('#deleteSendingInvoiceNumInput').val();
    let invoiceDate = $('#deleteSendingInvoiceDateInput').val();
    let inputtedFile = $('#deleteSendingFileInput').prop('files');
    if (status !== null && invoiceNumber.length && invoiceDate.length && inputtedFile.length) {
        let formData = new FormData();
        formData.append('status', status);
        formData.append('invoiceNumber', invoiceNumber);
        formData.append('invoiceDate', invoiceDate);
        formData.append('file', inputtedFile[0]);

        $.ajax({
            url: '/tools/group/sending/delete',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false
        }).done((result) => {
            $('#groupDeleteSendingModal').modal('toggle');
            successToast('Sending for documents group was deleted');
            $('#resultContent').empty();
            $('#resultContent').append(generateResultCard(result.processedDocuments, 'success', 'Sending delete for documents'));
            $('#resultContent').append(generateResultCard(result.notProcessedDocuments, 'danger', 'Not found sending for documents or documents itself'));
            $('#resultModalLabel').text('Group delete sending operation result');
            $('#resultModal').modal('toggle');
        }).fail((data) => {
            handleError(data, 'Failed to delete sending for documents group');
        });
    }
}

$('#groupUnsubscribeModal').on('show.bs.modal', (event) => {
    let companySelector = $('#unsubscribeCompanySelector');
    companySelector.empty();
    fillCompaniesSelector(companySelector); // add to company-selector
    $(event.currentTarget).find('#unsubscribeReason').val('');
    $(event.currentTarget).find('#unsubscribeFileInput').val('').css('color', 'transparent');
});

function unsubscribeGroup() {
    let companyId = $('#unsubscribeCompanySelector').val();
    let unsubscribeReason = $('#unsubscribeReason').val();
    let inputtedFile = $('#unsubscribeFileInput').prop('files');
    if (companyId !== null && unsubscribeReason.length && inputtedFile.length) {
        let formData = new FormData();
        formData.append('companyId', companyId);
        formData.append('unsubscribeReason', unsubscribeReason);
        formData.append('file', inputtedFile[0]);

        $.ajax({
            url: '/tools/group/unsubscribe',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false
        }).done((result) => {
            $('#groupUnsubscribeModal').modal('toggle');
            successToast('Subscribers for documents group were unsubscribed');
            $('#resultContent').empty();
            $('#resultContent').append(generateResultCard(result.processedDocuments, 'success', 'Documents which subscribers were unsubscribed'));
            $('#resultContent').append(generateResultCard(result.notProcessedDocuments, 'danger', 'Not found subscribers or documents itself'));
            $('#resultModalLabel').text('Group unsubscribe operation result');
            $('#resultModal').modal('toggle');
        }).fail((data) => {
            handleError(data, 'Failed to unsubscribe subscribers for documents group');
        });
    }
}

$('#groupResubscribeModal').on('show.bs.modal', (event) => {
    let companySelector = $('#resubscribeCompanySelector');
    companySelector.empty();
    fillCompaniesSelector(companySelector); // add to company-selector
    $(event.currentTarget).find('#resubscribeFileInput').val('').css('color', 'transparent');
});

function resubscribeGroup() {
    let companyId = $('#resubscribeCompanySelector').val();
    let inputtedFile = $('#resubscribeFileInput').prop('files');
    if (companyId !== null && inputtedFile.length) {
        let formData = new FormData();
        formData.append('companyId', companyId);
        formData.append('file', inputtedFile[0]);

        $.ajax({
            url: '/tools/group/resubscribe',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false
        }).done((result) => {
            $('#groupResubscribeModal').modal('toggle');
            successToast('Subscribers for documents group were resubscribed');
            $('#resultContent').empty();
            $('#resultContent').append(generateResultCard(result.processedDocuments, 'success', 'Documents which subscribers were resubscribed'));
            $('#resultContent').append(generateResultCard(result.notProcessedDocuments, 'danger', 'Not found subscribers or documents itself'));
            $('#resultModalLabel').text('Group resubscribe operation result');
            $('#resultModal').modal('toggle');
        }).fail((data) => {
            handleError(data, 'Failed to resubscribe subscribers for documents group');
        });
    }
}

$('#groupDownloadModal').on('show.bs.modal', (event) => {
    $(event.currentTarget).find('#downloadFileInput').val('').css('color', 'transparent');
});

function showGroupContentDownloadInfo() {
    let inputtedFile = $('#downloadFileInput').prop('files');
    if (inputtedFile.length) {
        $('#groupDownloadModal').modal('toggle');
        successToast('Content for documents group has started downloading');
        $('#resultContent').empty();
        $('#resultContent').append('<p>Content for documents group has started downloading.</p> ' +
            '<p>Check the content.zip file in the Downloads directory when the download is complete. ' +
            'The content.zip file contains documents content and operation result file.</p>');
        $('#resultModalLabel').text('Group content download operation info');
        $('#resultModal').modal('toggle');
    }
}
