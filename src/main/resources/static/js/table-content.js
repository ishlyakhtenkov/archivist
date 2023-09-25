const deleteParam = 'delete';

window.onload = function() {
    checkActionHappened();
};

function checkActionHappened() {
    let actionSpan = $("#actionSpan");
    if (actionSpan.length) {
        successToast(`${actionSpan.data('action')}`);
    } else {
        checkDeleteActionHappened();
    }
}

function checkDeleteActionHappened() {
    let queryString = window.location.search;
    let urlParams = new URLSearchParams(queryString);
    if (urlParams.has(deleteParam)) {
        successToast(urlParams.get(deleteParam));
        urlParams.delete(deleteParam);
        let url = (urlParams.toString().length > 0) ? (window.location.pathname + '?' + urlParams) : window.location.pathname;
        window.history.replaceState(null, null, url);
        fixPaginationLinks();
    }
}

function fixPaginationLinks() {
    let paginationLinks = $("a.page-link");
    for (let i = 0; i < paginationLinks.length; i++) {
        let paginationLink = paginationLinks[i];
        let url = paginationLink.href.toString();
        let urlParts = url.split('?');
        let urlParams = new URLSearchParams(urlParts[1]);
        urlParams.delete(deleteParam);
        url = urlParts[0] + '?' + urlParams;
        paginationLink.href = url;
    }
}

function deleteTableRow(id, message) {
    $('#row-' + id).remove();
    let rowTotal = $('.table-row').length;
    if (rowTotal === 0) {
        let urlParams = new URLSearchParams(window.location.search);
        let pageParam = +urlParams.get('page');
        if (pageParam !== 0) {
            urlParams.set('page', `${pageParam - 1}`);
        }
        urlParams.append(deleteParam, message);
        window.location.href = `${window.location.pathname}?${urlParams}`;
    } else {
        updatePaginationInfo();
        successToast(message);
    }
}

function updatePaginationInfo() {
    let paginationSummary = $('#paginationSummary');
    let paginationInfo =paginationSummary.text();
    let words = paginationInfo.split(' ');
    let numberCounter = 0;
    for (let i = words.length - 1; i >= 0; i--) {
        if (isNumber(words[i])) {
            numberCounter++;
            words[i] = words[i] - 1;
        }
        if (numberCounter === 2) {
            break;
        }
    }
    paginationSummary.text(words.join(' '));
}

function isNumber(n) {
    return /^-?[\d.]+(?:e-?\d+)?$/.test(n);
}