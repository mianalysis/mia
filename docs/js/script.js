
function changeText(text) {
    var display = document.getElementById('text-display');
    display.innerHTML = "";
    display.innerHTML = text;
}

// function toggleDropdown(e) {
//     const _d = $(e.target).closest('.dropdown'),
//         _m = $('.dropdown-menu', _d);
//     setTimeout(function () {
//         const shouldOpen = e.type !== 'click' && _d.is(':hover');
//         _m.toggleClass('show', shouldOpen);
//         _d.toggleClass('show', shouldOpen);
//         $('[data-toggle="dropdown"]', _d).attr('aria-expanded', shouldOpen);
//     }, e.type === 'mouseleave' ? 300 : 0);
    
// }

$('body')
    .on('mouseenter mouseleave', '.dropdown', changeText('woof'))
    .on('click', '.dropdown-menu a', changeText('guff'));
