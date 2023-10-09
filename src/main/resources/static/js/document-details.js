$('#documentGeneralTabButton').on('shown.bs.tab', () => {
    console.log('get general content through AJAX');
    $('#documentGeneralTab').text(`show general content via ajax`);
});

$('#documentChangesTabButton').on('shown.bs.tab', () => {
    console.log('get changes content through AJAX');
    $('#documentChangesTab').text(`show changes content via ajax`);
});
