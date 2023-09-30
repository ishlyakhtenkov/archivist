$('#deleteModal').on('show.bs.modal', function(e) {
    let decimalNumber = $(e.relatedTarget).data('decimalnumber');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete document: ${decimalNumber}?`);
    $(e.currentTarget).find('#deleteModalDocumentId').val(id);
    $(e.currentTarget).find('#deleteModalDocumentDecimalNumber').val(decimalNumber);
});

function deleteDocument() {
    let deleteModal = $('#deleteModal');
    let id = deleteModal.find('#deleteModalDocumentId').val();
    let decimalNumber = deleteModal.find('#deleteModalDocumentDecimalNumber').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: "documents/" + id,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Document ${decimalNumber} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete document ${decimalNumber}`);
    });
}
