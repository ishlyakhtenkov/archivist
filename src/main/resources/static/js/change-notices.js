$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete change notice: ${name}?`);
    $(e.currentTarget).find('#deleteModalChangeNoticeId').val(id);
    $(e.currentTarget).find('#deleteModalChangeNoticeName').val(name);
});

function deleteChangeNotice() {
    let deleteModal = $('#deleteModal');
    let id = deleteModal.find('#deleteModalChangeNoticeId').val();
    let name = deleteModal.find('#deleteModalChangeNoticeName').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: "change-notices/" + id,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Change notice ${name} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete change notice ${name}`);
    });
}
