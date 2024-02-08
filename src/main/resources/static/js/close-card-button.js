let cardPrevUrl = document.referrer ? document.referrer :
    window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
if (sessionStorage.getItem('cardPrevUrl') == null) {
    let cardPrevUrlPathName = cardPrevUrl.replace(window.location.origin, '');
    if (cardPrevUrlPathName.substring(0, cardPrevUrlPathName.lastIndexOf('/')).length === 0) {
        sessionStorage.setItem('cardPrevUrl', cardPrevUrl);
    }
}

function closeCard() {
    let cardPrevUrl = sessionStorage.getItem('cardPrevUrl');
    if (cardPrevUrl) {
        sessionStorage.removeItem('cardPrevUrl');
    } else {
        cardPrevUrl = window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    }
    window.location.href = cardPrevUrl;
}
