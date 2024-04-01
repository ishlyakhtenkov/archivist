let cancelPrevUrl = document.referrer ? document.referrer :
    window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
if (sessionStorage.getItem('cancelPrevUrl') == null) {
    sessionStorage.setItem('cancelPrevUrl', cancelPrevUrl);
}

function cancelCard() {
    let cancelPrevUrl = sessionStorage.getItem('cancelPrevUrl');
    if (cancelPrevUrl) {
        sessionStorage.removeItem('cancelPrevUrl');
    } else {
        cancelPrevUrl = window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    }
    window.location.href = cancelPrevUrl;
}
