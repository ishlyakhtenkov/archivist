const departmentSelector = $('#departmentSelector');
const employeeSelector = $('#employeeSelector');

departmentSelector.on('change', () => {
    let departmentId = departmentSelector.val();
    fillEmployeesSelector(departmentId);
});

$('#returnAlbumModal').on('show.bs.modal', function(e) {
    let id = $(e.relatedTarget).data('id');
    let name = $(e.relatedTarget).data('name');
    $(e.currentTarget).find('#returnAlbumModalLabel').text(`Return album: ${name}`);
    $(e.currentTarget).find('#returnedDateInput').val('');
    $(e.currentTarget).find('#returnAlbumModalAlbumId').val(id);
    $(e.currentTarget).find('#returnAlbumModalAlbumName').val(name);
});

$('#issueAlbumModal').on('show.bs.modal', function(e) {
    let id = $(e.relatedTarget).data('id');
    let name = $(e.relatedTarget).data('name');
    $(e.currentTarget).find('#issueAlbumModalLabel').text(`Issue album: ${name}`);
    departmentSelector.empty();
    employeeSelector.empty();
    employeeSelector.attr('disabled', true);
    fillDepartmentsSelector();
    $(e.currentTarget).find('#issueDateInput').val('');
    $(e.currentTarget).find('#issueAlbumModalAlbumId').val(id);
    $(e.currentTarget).find('#issueAlbumModalAlbumName').val(name);
});

function fillDepartmentsSelector() {
    $.ajax({
        url: '/departments/list'
    }).done(departments => {
        if (departments.length !== 0) {
            departments.forEach(department => {
                departmentSelector.append($('<option></option>').val(department.id).html(department.name));
            });
            departmentSelector.val('');
        }
    }).fail(function (data) {
        handleError(data, `Failed to get departments`);
    });
}

function fillEmployeesSelector(departmentId) {
    employeeSelector.attr('disabled', false);
    employeeSelector.empty();
    $.ajax({
        url: '/employees/list/by-department',
        data: "departmentId=" + departmentId
    }).done(employees => {
        if (employees.length !== 0) {
            employees.forEach(employee => {
                employeeSelector.append($('<option></option>').val(employee.id)
                    .html(`${employee.lastName} ${employee.firstName} ${employee.middleName}`));
            });
        }
    }).fail(function (data) {
        handleError(data, `Failed to get employees`);
    });
}

function makeIssuanceToObject(employeeId, issued) {
    return {
        albumId: albumId,
        employeeId: employeeId,
        issued: issued
    };
}
