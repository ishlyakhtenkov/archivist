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
            successToast('Sending to documents group was added');
            $('#resultContent').empty();
            $('#resultContent').append(generateSendingResultCard(result.sentDocuments, 'success', 'Sent documents'));
            $('#resultContent').append(generateSendingResultCard(result.alreadySentDocuments, 'primary', 'Earlier sent documents'));
            $('#resultContent').append(generateSendingResultCard(result.notFoundDocuments, 'danger', 'Not found documents'));
            $('#resultModalLabel').text('Group add sending operation result');
            $('#resultModal').modal('toggle');
        }).fail((data) => {
            handleError(data, 'Failed to add sending to documents group');
        });
    }
}

function generateSendingResultCard(documents, color, header) {
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