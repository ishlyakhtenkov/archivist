$.ajaxSetup({
    beforeSend: function(xhr, settings) {
        if (!csrfSafeMethod(settings.type) && !this.crossDomain) {
            let token = $("meta[name='_csrf']").attr("content");
            let header = $("meta[name='_csrf_header']").attr("content");
            xhr.setRequestHeader(header, token);
        }
    }
});

function csrfSafeMethod(method) {
    // these HTTP methods do not require CSRF protection
    return (/^(GET|HEAD|OPTIONS|TRACE)$/.test(method));
}

function successToast(message) {
    $.toast({
        heading: 'Success',
        text: message,
        showHideTransition: 'slide',
        position: 'bottom-right',
        icon: 'success'
    })
}

function failToast(message) {
    $.toast({
        heading: 'Error',
        text: message,
        showHideTransition: 'slide',
        position: 'bottom-right',
        icon: 'error'
    })
}

function handleError(data, title) {
    if (data.status === 0) {
        window.location.reload();
    }
    let message = `${title}:<br>`;
    if (data.status === 422) {
        let invalidParams = data.responseJSON.invalid_params;
        $.each(invalidParams, function(param, errorMessage) {
            message += `${param}: ${errorMessage}<br>`;
        });
    } else {
        message += data.responseJSON.detail;
    }
    failToast(message);
}
