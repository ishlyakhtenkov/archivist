$('#deleteModal').on('show.bs.modal', function(e) {
    let title = $(e.relatedTarget).data('title');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete post: ${title}?`);
    $(e.currentTarget).find('#deleteModalPostId').val(id);
    $(e.currentTarget).find('#deleteModalPostTitle').val(title);
});

function deletePost() {
    let deleteModal = $('#deleteModal');
    let id = deleteModal.find('#deleteModalPostId').val();
    let title = deleteModal.find('#deleteModalPostTitle').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: "posts/" + id,
        type: "DELETE"
    }).done(function() {
        deleteTableRow(id, `Post ${title} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete post ${title}`);
    });
}
