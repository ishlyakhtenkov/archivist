window.onload = () => {
    if ($('.change-row').length === 0) {
        addEmptyChangeRow();
    }
    let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
        new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));
}

function addChangeRow(id, decimalNumber, changeNumber) {
    let changeRowIndex = $('.change-row').length;
    let documentTd = $('<td></td>').html(`<input type="hidden" id="${'changeId-' + changeRowIndex}" name="changes[${changeRowIndex}].id" value="${id}">
            <input type="text" id="${'changeDecimalNumber-' + changeRowIndex}" name="changes[${changeRowIndex}].decimalNumber" class="form-control" title="Document decimal number" placeholder="Document decimal number" value="${decimalNumber}" required />`);
    let changeNumberTd = $('<td></td>').html(`<input type="number" id="${'changeChangeNumber-' + changeRowIndex}" name="changes[${changeRowIndex}].changeNumber" class="form-control" title="Change number" placeholder="Change number" value="${changeNumber}" required />`);
    let deleteChangeSubmitHtml = `<button class='btn btn-sm btn-secondary ms-3'>Cancel</button>
                                       <button class='btn btn-sm btn-danger' onclick='deleteChangeRow(${changeRowIndex})'>Delete</button>`;
    let deleteActionTd = $('<td></td>').addClass('text-end').html(`<a tabindex="0" type="button" class="trash-button me-3 mt-1" id="deleteChangeBtn-${changeRowIndex}"
                title="Delete change" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-title="Delete this change?"
                data-bs-content="${deleteChangeSubmitHtml}"> <i class="fa-solid fa-trash"></i></a>`);
    let changeRow = $('<tr></tr>').addClass('change-row').attr('id', `changeRow-${changeRowIndex}`);
    changeRow.append(documentTd);
    changeRow.append(changeNumberTd);
    changeRow.append(deleteActionTd);
    $('#changesTable > tbody').append(changeRow);
    let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
        new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));
}

function addEmptyChangeRow() {
    addChangeRow('', '', '');
}

function deleteChangeRow(changeRowIndex) {
    let changeRowMaxIndex = $('.change-row').length - 1;
    $('#changeRow-' + changeRowIndex).remove();
    if (changeRowIndex !== changeRowMaxIndex) {
        for (let i = changeRowIndex + 1; i <= changeRowMaxIndex; i++) {
            $('#changeRow-' + i).attr('id', `changeRow-${i - 1}`);
            $('#changeId-' + i).attr('name', `changes[${i - 1}].id`).attr('id', `changeId-${i - 1}`);
            $('#changeDecimalNumber-' + i).attr('name', `changes[${i - 1}].decimalNumber`).attr('id', `changeDecimalNumber-${i - 1}`);
            $('#changeChangeNumber-' + i).attr('name', `changes[${i - 1}].changeNumber`).attr('id', `changeChangeNumber-${i - 1}`);
            let deleteChangeSubmitHtml = `<button class='btn btn-sm btn-secondary ms-3'>Cancel</button>
                                       <button class='btn btn-sm btn-danger' onclick='deleteChangeRow(${i - 1})'>Delete</button>`;
            $('#deleteChangeBtn-' + i).attr('data-bs-content', deleteChangeSubmitHtml).attr('id', `deleteChangeBtn-${i - 1}`);
        }
    }
    let popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    let popoverList = [...popoverTriggerList].map(popoverTriggerEl =>
        new bootstrap.Popover(popoverTriggerEl, {html : true, sanitize: false}));
    if ($('.change-row').length === 0) {
        addEmptyChangeRow();
    }
}

function deleteFile() {
    $('#editFileInput').attr('hidden', true);
    $('#fileInputDiv').html('<input type="file" accept="application/pdf" id="fileInput" name="file" class="form-control" required />');
}

$('#nameInput').on('change', () => {
    if ($('#idInput').val() === '') {
        removeLoadedChanges();
        checkHasChanges($('#nameInput').val());
    }
});

function removeLoadedChanges() {
    $('.change-row').each(function () {
        let changeRowIndex = +$(this).attr('id').replace('changeRow-', '');
        if ($(`#changeId-${changeRowIndex}`).val() !== '') {
            deleteChangeRow(changeRowIndex);
        }
    });
}


function checkHasChanges(changeNoticeName) {
    $.ajax({
        url: '/change-notices/changes/by-change-notice',
        data: `changeNoticeName=${changeNoticeName}`
    }).done(changes => {
        if (changes.length !== 0) {
            changes.forEach(change => {
                addChangeRow(change.id, change.document.decimalNumber, change.changeNumber);
                if ($('#changeDecimalNumber-0').val() === '' && $('#changeChangeNumber-0').val() === '') {
                    deleteChangeRow(0);
                }
            });
        }
    }).fail(function (data) {
        handleError(data, `Failed to check changes`);
    });
}
