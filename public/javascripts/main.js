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

    var crateDomDump = function() {
        return createElemFromSelector($("html"));
    };

    var interceptEvents = function() {
        $("html").on("click", "body", function(e) {
            console.log("click" + e);
        });
    };

    interceptEvents();

    //var webSocket = new WebSocket("ws://localhost:9000/instrumentation")
    
    $(function() {
        console.log(JSON.stringify(crateDomDump(), null, "\t"));
    });

});
