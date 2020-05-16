// File#: _1_table
// Usage: codyhouse.co/license
(function() {
    function initTable(table) {
        checkTableLayour(table); // switch from a collapsed to an expanded layout
        Util.addClass(table, 'table--loaded'); // show table

        // custom event emitted when window is resized
        table.addEventListener('update-table', function(event){
            checkTableLayour(table);
        });
    };

    function checkTableLayour(table) {
        var layout = getComputedStyle(table, ':before').getPropertyValue('content').replace(/\'|"/g, '');
        Util.toggleClass(table, tableExpandedLayoutClass, layout != 'collapsed');
    };

    var tables = document.getElementsByClassName('js-table'),
        tableExpandedLayoutClass = 'table--expanded';
    if( tables.length > 0 ) {
        var j = 0;
        for( var i = 0; i < tables.length; i++) {
            var beforeContent = getComputedStyle(tables[i], ':before').getPropertyValue('content');
            if(beforeContent && beforeContent !='' && beforeContent !='none') {
                (function(i){initTable(tables[i]);})(i);
                j = j + 1;
            } else {
                Util.addClass(tables[i], 'table--loaded');
            }
        }

        if(j > 0) {
            var resizingId = false,
                customEvent = new CustomEvent('update-table');
            window.addEventListener('resize', function(event){
                clearTimeout(resizingId);
                resizingId = setTimeout(doneResizing, 300);
            });

            function doneResizing() {
                for( var i = 0; i < tables.length; i++) {
                    (function(i){tables[i].dispatchEvent(customEvent)})(i);
                };
            };

            (window.requestAnimationFrame) // init table layout
                ? window.requestAnimationFrame(doneResizing)
                : doneResizing();
        }
    }
}());