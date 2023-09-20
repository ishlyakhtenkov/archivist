$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete department: ${name}?`);
    $(e.currentTarget).find('#deleteDepartmentForm').attr('action', `/departments/delete/${id}`);
});
