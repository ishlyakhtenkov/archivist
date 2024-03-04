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
        url: '/employees/by-department',
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

function returnAlbum() {
    let returnAlbumModal = $('#returnAlbumModal');
    let albumId = returnAlbumModal.find('#returnAlbumModalAlbumId').val();
    let name = returnAlbumModal.find('#returnAlbumModalAlbumName').val();
    let returnedDate = returnAlbumModal.find('#returnedDateInput').val();
    if (returnedDate.length) {
        $.ajax({
            url: `/albums/${albumId}/return`,
            type: "PATCH",
            data: "returned=" + returnedDate
        }).done(function() {
            returnAlbumModal.modal('toggle');
            doOnSuccessReturn(albumId);
            successToast(`Album ${name} was returned`);
        }).fail(function(data) {
            handleError(data, `Failed to return album ${name}`);
        });
    }
}

function issueAlbum() {
    let issueAlbumModal = $('#issueAlbumModal');
    let albumId = issueAlbumModal.find('#issueAlbumModalAlbumId').val();
    let employeeId = employeeSelector.val();
    let issued = $('#issueDateInput').val();
    let name = issueAlbumModal.find('#issueAlbumModalAlbumName').val();
    let departmentName = $('#departmentSelector option:selected').text();
    if (employeeId !== null && issued.length) {
        $.ajax({
            url: `/albums/${albumId}/issue`,
            type: 'POST',
            data: JSON.stringify(makeIssuanceToObject(albumId, employeeId, issued)),
            contentType: 'application/json; charset=utf-8'
        }).done((issuance) => {
            issueAlbumModal.modal('toggle');
            doOnSuccessIssue(albumId, departmentName, issuance);
            successToast(`Album ${name} was issued`);
        }).fail(function(data) {
            handleError(data, `Failed to issue album ${name}`);
        });
    }
}

function makeIssuanceToObject(albumId, employeeId, issued) {
    return {
        albumId: albumId,
        employeeId: employeeId,
        issued: issued
    };
}
