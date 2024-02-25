$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete album: ${name}?`);
    $(e.currentTarget).find('#deleteModalAlbumId').val(id);
    $(e.currentTarget).find('#deleteModalAlbumName').val(name);
});

function deleteAlbum() {
    let deleteModal = $('#deleteModal');
    let id = deleteModal.find('#deleteModalAlbumId').val();
    let name = deleteModal.find('#deleteModalAlbumName').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: "albums/" + id,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Album ${name} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete album ${name}`);
    });
}
