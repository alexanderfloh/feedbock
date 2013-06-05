require(["jquery"], function($) {

    // some unrelated test code
    var createElemFromSelector = function($selector) {
        var ret = {
            tag: $selector.prop("tagName"),
            children: []
        };
        $.each($selector.children(), function(idx, val) {
            ret.children.push(createElemFromSelector($(val)));
        })
        return ret;
    };
    
    $(function() {
        console.log("do");
        var dump = createElemFromSelector($("html"));
        console.log(JSON.stringify(dump, null, "\t"));
    });

});
