window.onload = function() {
    setupDeletePersonRowButtons();
};

function setupDeletePersonRowButtons() {
    $('.btn-del-contact-person').on('click', function() {
        let contactPersonRowIndex = +$(this).attr('id').replace('deleteContactBtn-', '');
        $(this).parent().parent().parent().parent().remove();
        let contactPersonRowMaxIndex = $('.contact-person-row').length;
        for (let i = contactPersonRowIndex + 1; i <= contactPersonRowMaxIndex; i++) {
            $(`#contactPersons-${i}-position`).attr('name', `contactPersons[${i - 1}].position`).attr('id', `contactPersons-${i-1}-position`);
            $(`#contactPersons-${i}-lastName`).attr('name', `contactPersons[${i - 1}].lastName`).attr('id', `contactPersons-${i-1}-lastName`);
            $(`#contactPersons-${i}-firstName`).attr('name', `contactPersons[${i - 1}].firstName`).attr('id', `contactPersons-${i-1}-firstName`);
            $(`#contactPersons-${i}-middleName`).attr('name', `contactPersons[${i - 1}].middleName`).attr('id', `contactPersons-${i-1}-middleName`);
            $(`#contactPersons-${i}-phone`).attr('name', `contactPersons[${i - 1}].phone`).attr('id', `contactPersons-${i-1}-phone`);
            $(`#deleteContactBtn-${i}`).attr('id', `deleteContactBtn-${i - 1}`);
        }
    });
}

function addContactPersonRow() {
    $('#contactPersonBlock').prepend(generateContactPersonRowHtml());
    setupDeletePersonRowButtons();
}

function generateContactPersonRowHtml() {
    let contactPersonRowIndex = $('.contact-person-row').length;
    return `<div class="row d-flex align-items-center contact-person-row">
                <div class="col-lg-3">
                    <div class="mb-4">
                        <div class="form-floating">
                            <input type="text" id="contactPersons-${contactPersonRowIndex}-position" name="contactPersons[${contactPersonRowIndex}].position" class="form-control" required placeholder="Position" />
                            <label for="contactPersons-${contactPersonRowIndex}-position" class="text-muted">Position</label>
                        </div>
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                    <div class="form-floating">
                        <input type="text" id="contactPersons-${contactPersonRowIndex}-lastName" name="contactPersons[${contactPersonRowIndex}].lastName" class="form-control" required placeholder="Last name" />
                        <label for="contactPersons-${contactPersonRowIndex}-lastName" class="text-muted">Last name</label>
                    </div>
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                        <div class="form-floating">
                            <input type="text" id="contactPersons-${contactPersonRowIndex}-firstName" name="contactPersons[${contactPersonRowIndex}].firstName" class="form-control" required placeholder="First name" />
                            <label for="contactPersons-${contactPersonRowIndex}-firstName" class="text-muted">First name</label>
                        </div>
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                    <div class="form-floating">
                        <input type="text" id="contactPersons-${contactPersonRowIndex}-middleName" name="contactPersons[${contactPersonRowIndex}].middleName" class="form-control" required placeholder="Middle name" />
                        <label for="contactPersons-${contactPersonRowIndex}-middleName" class="text-muted">Middle name</label>
                    </div>
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="mb-4">
                    <div class="form-floating">
                        <input type="text" id="contactPersons-${contactPersonRowIndex}-phone" name="contactPersons[${contactPersonRowIndex}].phone" class="form-control" placeholder="Phone" />
                        <label for="contactPersons-${contactPersonRowIndex}-phone" class="text-muted">Phone number</label>
                    </div>
                    </div>
                </div>
                <div class="col-lg-1">
                    <div class="mb-4">
                        <span class="d-lg-none d-grid">
                           <button type="button" id="deleteContactBtn-${contactPersonRowIndex}" class="btn btn-danger btn-del-contact-person" title="Delete contact">
                               <i class="fa-solid fa-arrow-up"></i> Delete contact <i class="fa-solid fa-arrow-up"></i>
                           </button>
                        </span>
                        <span class="d-none d-lg-block">
                            <button type="button" id="deleteContactBtn-${contactPersonRowIndex}" class="btn btn-danger btn-del-contact-person" title="Delete contact">
                                Delete
                            </button>
                        </span>
                    </div>
                </div>
            </div>`;
}
