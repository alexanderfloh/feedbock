require(["jquery"], function($) {

    var createElemFromSelector = function($selector) {
        console.log("createElemFromSelector " + $selector);
        var ret = {
            tag: $selector.prop("tagName"),
            children: []
        };
        $.each($selector.children(), function(idx, val) {
            ret.children.push($(val));
        })
        return ret;
    };
    
    $(function() {
        console.log("do");
        var dump = createElemFromSelector($("html"));
        console.dir(dump);
    });
});
