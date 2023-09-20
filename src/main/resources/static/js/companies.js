$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete company: ${name}?`);
    $(e.currentTarget).find('#deleteModalCompanyId').val(id);
    $(e.currentTarget).find('#deleteModalCompanyName').val(name);
});

function deleteCompany() {
    let deleteModal = $('#deleteModal');
    let id = deleteModal.find('#deleteModalCompanyId').val();
    let name = deleteModal.find('#deleteModalCompanyName').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: "companies/" + id,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Company ${name} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete company ${name}`);
    });
}
