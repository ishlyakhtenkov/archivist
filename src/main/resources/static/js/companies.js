window.onload = function() {
    setupDeletePersonRowButtons();
};

$('#deleteModal').on('show.bs.modal', function(e) {
    let name = $(e.relatedTarget).data('name');
    let id = $(e.relatedTarget).data('id');
    $(e.currentTarget).find('#deleteModalLabel').text(`Delete company: ${name}`);
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

function addContactPersonRow() {
    $('#contact-person-block').prepend(generateContactPersonRowHtml());
    setupDeletePersonRowButtons();
}

function generateContactPersonRowHtml() {
    let contactPersonRowIndex = $('.contact-person-row').length;
    return `<div class="row d-flex align-items-center contact-person-row">
                <div class="col-lg-3">
                    <div class="mb-4">
                        <input type="text" name="contactPersons[${contactPersonRowIndex}].position" class="form-control" required placeholder="Position" />
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                        <input type="text" name="contactPersons[${contactPersonRowIndex}].lastName" class="form-control" required placeholder="Last name" />
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                        <input type="text" name="contactPersons[${contactPersonRowIndex}].firstName" class="form-control" required placeholder="First name" />
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                        <input type="text" name="contactPersons[${contactPersonRowIndex}].middleName" class="form-control" required placeholder="Middle name" />
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                        <input type="text" name="contactPersons[${contactPersonRowIndex}].phone" class="form-control" placeholder="Phone" />
                    </div>
                </div>
                <div class="col-lg-1">
                    <div class="mb-4">
                        <span class="d-lg-none d-grid">
                           <button type="button" class="btn btn-danger btn-del-contact-person" title="Delete">
                               <i class="fa-solid fa-arrow-up"></i> Delete contact <i class="fa-solid fa-arrow-up"></i>
                           </button>
                        </span>
                        <span class="d-none d-lg-block">
                            <button type="button" class="btn btn-danger btn-del-contact-person" title="Delete">
                                Delete
                            </button>
                        </span>
                    </div>
                </div>
            </div>`;
}

function setupDeletePersonRowButtons() {
    $('.btn-del-contact-person').on('click', function() {
        $(this).parent().parent().parent().parent().remove();
    });
}
