const albumId =$('#albumId').val();
// const departmentSelector = $('#departmentSelector');
// const employeeSelector = $('#employeeSelector');

window.onload = () => {
    let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
        new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));

    // departmentSelector.on('change', () => {
    //     let departmentId = departmentSelector.val();
    //     fillEmployeesSelector(departmentId);
    // });
}

function deleteIssuance(issuanceId) {
    $.ajax({
        url: `/albums/issuances/${issuanceId}`,
        type: "DELETE"
    }).done(function() {
        getIssuances();
        successToast(`Issuance was deleted`);
    }).fail(function(data) {
        handleError(data, 'Failed to delete issuance');
    });
}

function getIssuances() {
    $.ajax({
        url: `/albums/${albumId}/issuances`
    }).done(issuances => {
        fillIssuancesTable(issuances);
    }).fail(function (data) {
        handleError(data, `Failed to get issuances`);
    });
}

function fillIssuancesTable(issuances) {
    $('#issuancesTable > tbody').empty();
    if (issuances.length !== 0) {
        if (issuances[0].returned == null) {
            $('#holderField').html(`${issuances[0].employee.department.name}, ${issuances[0].employee.lastName} 
                ${issuances[0].employee.firstName} ${issuances[0].employee.middleName} (<i class="fa-solid fa-phone-flip small text-muted"></i> ${issuances[0].employee.phone})`);
            $('#issueButton').attr('hidden', true);
            $('#returnButton').attr('hidden', false);
        } else {
            $('#holderField').html(`Archive (<i class="fa-solid fa-phone-flip small text-muted"></i> 1-96-88)`);
            $('#returnButton').attr('hidden', true);
            $('#issueButton').attr('hidden', false);
        }
        $('#noIssuancesAlert').attr('hidden', true);
        $('#issuancesTable').attr('hidden', false);
        issuances.forEach(issuance => {
            addIssuanceRow(issuance);
        });
        let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
        let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
            new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));
    } else {
        $('#issuancesTable').attr('hidden', true);
        $('#noIssuancesAlert').attr('hidden', false);
        $('#holderField').text('Archive (phone 1-96-88)');
        $('#returnButton').attr('hidden', true);
        $('#issueButton').attr('hidden', false);
    }
}

function addIssuanceRow(issuance) {
    let employeeTd = $('<td></td>').text(`${issuance.employee.department.name}, ${issuance.employee.lastName} 
        ${issuance.employee.firstName.charAt(0)}.${issuance.employee.middleName.charAt(0)}`);
    let issuedTd = $('<td></td>').addClass('text-center').text(issuance.issued);
    let returnedTd = $('<td></td>').addClass('text-center').text(issuance.returned);
    let issuanceRow = $('<tr></tr>');
    issuanceRow.append(employeeTd);
    issuanceRow.append(issuedTd);
    issuanceRow.append(returnedTd);
    if (hasAdminOrArchivistRole()) {
        let deleteIssuanceSubmitHtml = `<button class='btn btn-sm btn-secondary ms-3'>Cancel</button>
                                       <button class='btn btn-sm btn-danger' onclick='deleteIssuance(${issuance.id})'>Delete</button>`;
        let deleteActionTd = $('<td></td>').addClass('text-end').html(`<a tabindex="0" type="button" class="trash-button me-3"
                title="Delete issuance" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-title="Delete this issuance?"
                data-bs-content="${deleteIssuanceSubmitHtml}"> <i class="fa-solid fa-trash"></i></a>`);
        issuanceRow.append(deleteActionTd);
    } else {
        issuanceRow.append($('<td></td>'));
    }
    $('#issuancesTable > tbody').append(issuanceRow);
}

// $('#returnAlbumModal').on('show.bs.modal', function(e) {
//     let name = $(e.relatedTarget).data('name');
//     $(e.currentTarget).find('#returnAlbumModalLabel').text(`Return album: ${name}`);
//     $(e.currentTarget).find('#returnedDateInput').val('');
//     $(e.currentTarget).find('#returnAlbumModalAlbumName').val(name);
// });

function returnAlbum() {
    let returnAlbumModal = $('#returnAlbumModal');
    let name = returnAlbumModal.find('#returnAlbumModalAlbumName').val();
    let returnedDate = returnAlbumModal.find('#returnedDateInput').val();
    if (returnedDate.length) {
        $.ajax({
            url: `/albums/${albumId}/return`,
            type: "PATCH",
            data: "returned=" + returnedDate
        }).done(function() {
            returnAlbumModal.modal('toggle');
            getIssuances();
            successToast(`Album ${name} was returned`);
        }).fail(function(data) {
            handleError(data, `Failed to return album ${name}`);
        });
    }
}

// $('#issueAlbumModal').on('show.bs.modal', function(e) {
//     let name = $(e.relatedTarget).data('name');
//     $(e.currentTarget).find('#issueAlbumModalLabel').text(`Issue album: ${name}`);
//     departmentSelector.empty();
//     employeeSelector.empty();
//     employeeSelector.attr('disabled', true);
//     fillDepartmentsSelector();
//     $(e.currentTarget).find('#issueDateInput').val('');
// });

// function fillDepartmentsSelector() {
//     $.ajax({
//         url: '/departments/list'
//     }).done(departments => {
//         if (departments.length !== 0) {
//             departments.forEach(department => {
//                 departmentSelector.append($('<option></option>').val(department.id).html(department.name));
//             });
//             departmentSelector.val('');
//         }
//     }).fail(function (data) {
//         handleError(data, `Failed to get departments`);
//     });
// }
//
// function fillEmployeesSelector(departmentId) {
//     employeeSelector.attr('disabled', false);
//     employeeSelector.empty();
//     $.ajax({
//         url: '/employees/list/by-department',
//         data: "departmentId=" + departmentId
//     }).done(employees => {
//         if (employees.length !== 0) {
//             employees.forEach(employee => {
//                 employeeSelector.append($('<option></option>').val(employee.id)
//                     .html(`${employee.lastName} ${employee.firstName} ${employee.middleName}`));
//             });
//         }
//     }).fail(function (data) {
//         handleError(data, `Failed to get employees`);
//     });
// }

function issueAlbum() {
    let issueAlbumModal = $('#issueAlbumModal');
    let employeeId = employeeSelector.val();
    let issued = $('#issueDateInput').val();
    let name = issueAlbumModal.find('#issueAlbumModalAlbumName').val();
    if (employeeId !== null && issued.length) {
        $.ajax({
            url: `/albums/${albumId}/issue`,
            type: 'POST',
            data: JSON.stringify(makeIssuanceToObject(albumId, employeeId, issued)),
            contentType: 'application/json; charset=utf-8'
        }).done(() => {
            getIssuances();
            issueAlbumModal.modal('toggle');
            successToast(`Album ${name} was issued`);
        }).fail(function(data) {
            handleError(data, `Failed to issue album ${name}`);
        });
    }
}

// function makeIssuanceToObject(albumId, employeeId, issued) {
//     return {
//         albumId: albumId,
//         employeeId: employeeId,
//         issued: issued
//     };
// }
