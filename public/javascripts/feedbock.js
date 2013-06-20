$(function() {

	History.Adapter.bind(window, 'statechange', function() {
		var historyState = History.getState();
		if (historyState.data.state === undefined) {
			_hideDetails();
		} else if (historyState.data.state === 'details') {
			if ($('li.active').size() == 0) {
				_setPreviousToActive();
			}
			_showDetails();
		}
	});
	
	var _showDetails = function() {
		var $element = $('li.active');
		$('body').addClass('details-open');
		$('#details').html($element.find('.details').html());
		
		$('#details').show();
		if ($element.position()) {
			var distanceToTop = $element.position().top - 100;
			$('#details').css('margin-top', distanceToTop);
		}
	};

	var _hideDetails = function() {
		$('body').removeClass('details-open');
		var $element = $('li.active');
		if ($element) {
			_setActiveToPrevious($element);
		}
		$('#details').hide();
	};
	
	var _setActiveToPrevious = function($element) {
		$prev = $('li.previous');
		if ($prev.length > 0) {
			$prev.removeClass('previous');
		}
		
		$element.addClass('previous');
		$element.removeClass('active');
	};
	
	var _setPreviousToActive = function() {
		var $element = $('li.previous');
		if ($element) {
			$element.addClass('active');
			$element.removeClass('previous');
		}
	};
	
	$('#details').on('click', '[data-action=close]', function() {
		_hideDetails();
	});

	$('.tests').on('click', 'li', function() {
		var $this = $(this);
		if ($this.hasClass('active')) {
			History.back();
		} else {
			$(this).addClass('active');
			History.pushState({state : 'details'}, 'feedbock - test details', '?state=details');
		}
	});

	$('.feedbock-logout-link').on('click', function() {
		$('.feedbock-logout-form').submit();
	});

});
