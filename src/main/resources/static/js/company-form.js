window.onload = function() {
    setupDeletePersonRowButtons();
};

function setupDeletePersonRowButtons() {
    $('.btn-del-contact-person').on('click', function() {
        $(this).parent().parent().parent().parent().remove();
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
                           <button type="button" class="btn btn-danger btn-del-contact-person" title="Delete contact">
                               <i class="fa-solid fa-arrow-up"></i> Delete contact <i class="fa-solid fa-arrow-up"></i>
                           </button>
                        </span>
                        <span class="d-none d-lg-block">
                            <button type="button" class="btn btn-danger btn-del-contact-person" title="Delete contact">
                                Delete
                            </button>
                        </span>
                    </div>
                </div>
            </div>`;
}
