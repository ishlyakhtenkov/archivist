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

function doOnSuccessReturn(albumId) {
    $(`#albumHolder-${albumId}`).html(`Archive (<i class="fa-solid fa-phone-flip small text-muted"></i> 1-96-88)`);
    $(`#returnButton-${albumId}`).attr('hidden', true);
    $(`#issueButton-${albumId}`).attr('hidden', false);
}

function doOnSuccessIssue(albumId, departmentName, issuance) {
    $(`#albumHolder-${albumId}`).html(`${departmentName}, ${issuance.employee.lastName}
                ${issuance.employee.firstName.charAt(0)}.${issuance.employee.middleName.charAt(0)}.
                (<i class="fa-solid fa-phone-flip small text-muted"></i> ${issuance.employee.phone})`);
    $(`#issueButton-${albumId}`).attr('hidden', true);
    $(`#returnButton-${albumId}`).attr('hidden', false);
}
