
$(function() {
	
	History.Adapter.bind(window,'statechange',function(){ // Note: We are using statechange instead of popstate
        var State = History.getState(); // Note: We are using History.getState() instead of event.state
        console.log("State = " + JSON.stringify(State, null, "\t"));
        if (State.data.state === undefined) {
        	console.log("call _hide()")
        	_hide($("li.active"));
        }
    });


	var _hide = function($element) {
		$('body').removeClass('details-open');
		if ($element) {
			$element.removeClass('active');
		}
		$('#details').hide();
	};

	$('#details').on('click', '[data-action=close]', function () { _hide(); } );

	$('.tests').on('click', 'li', function () {
		var $this = $(this);

		// hide
		if ($this.hasClass('active')) {
			_hide($this);
			console.log("hide");
		// show
		} else {
			$('body').addClass('details-open');
			$('.active').removeClass('active');
			$this.addClass('active');
			$('#details').html($this.find('.details').html());
			$('#details').show();
			var distanceToTop = $this.position().top - 100;
			$('#details').css('margin-top', distanceToTop);
			History.pushState({state: "details"}, "State details", "?state=details");
		}
	});

	$('.feedbock-logout-link').on('click', function() {
		$('.feedbock-logout-form').submit();
	});

});

