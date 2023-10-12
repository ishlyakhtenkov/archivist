$('#deleteModal').on('show.bs.modal', function(e) {
    let decimalNumber = $(e.relatedTarget).data('decimalnumber');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete document: ${decimalNumber}?`);
    $(e.currentTarget).find('#deleteDocumentForm').attr('action', `/documents/delete/${id}`);
});
