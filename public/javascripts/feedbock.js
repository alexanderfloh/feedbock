
$(function() {

	var _hide = function($element) {
		$('body').removeClass('details-open');
		if ($element) {
			$element.removeClass('active');
		}
		$('#details').hide();
	};

	$('#details').on('click', '[data-action=close]', function () { _hide(); } );

	$('.tests li').on('click', function () {
		var $this = $(this);

		// hide
		if ($this.hasClass('active')) {
			_hide($this);

		// show
		} else {
			$('body').addClass('details-open');
			$('.active').removeClass('active');
			$this.addClass('active');
			$('#details').html($this.find('.details').html());
			$('#details').show();
			var distanceToTop = $this.position().top - 100;
			$('#details').css('margin-top', distanceToTop);
		}
	});



});

