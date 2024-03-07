let keyword = $('#globalSearch').val().trim();
if (keyword.length) {
    $('.match-area').each(function() {
        let match = new RegExp(keyword, 'gi').exec($(this).text());
        let highlightedMatch = `<span class='bg-warning'>${match}</span>`;
        $(this).html($(this).html().replace(match, highlightedMatch));
    });
}
