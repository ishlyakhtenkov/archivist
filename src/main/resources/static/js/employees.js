$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete employee: ${name}?`);
    $(e.currentTarget).find('#deleteModalEmployeeId').val(id);
    $(e.currentTarget).find('#deleteModalEmployeeName').val(name);
});

function deleteEmployee() {
    let deleteModal = $('#deleteModal');
    let id = deleteModal.find('#deleteModalEmployeeId').val();
    let name = deleteModal.find('#deleteModalEmployeeName').val();
    deleteModal.modal('toggle');
    $.ajax({
        url: "/employees/" + id,
        type: "DELETE"
    }).done(function() {
        $('#row-' + id).remove();
        $(`#bossSelector option[value=${id}]`).remove();
        successToast(`Employee ${name} was deleted`);
    }).fail(function(data) {
        handleError(data, `Failed to delete employee ${name}`);
    });
}

function fireEmployee(checkbox, id) {
    let fired = checkbox.checked;
    let name = checkbox.dataset.name;
    $.ajax({
        url: "/employees/" + id,
        type: "PATCH",
        data: "fired=" + fired
    }).done(function() {
        successToast(`Employee ${name} was ${fired ? 'fired' : 'unfired'}`);
        $(checkbox).prop('title', `${fired ? 'Unfire' : 'Fire'} employee`);
        $('#row-' + id).removeClass(`${fired ? 'text-dark' : 'text-danger'}`).addClass(`${fired ? 'text-danger' : 'text-dark'}`);
    }).fail(function(data) {
        $(checkbox).prop('checked', !fired);
        handleError(data, `Failed to ${fired ? 'fire' : 'unfire'} employee ${name}`);
    });
}
