(function($) {
	$.fn.jqmAccordion = function(options) {
		var settings = $.extend({
			'categoryShown': 0
		}, options);	

		function init() {
			$('li[data-role=list-divider]')
				.eq(settings.categoryShown)
				.nextUntil('li[data-role=list-divider]')
				.css('display', 'block');
		}
		init();
		return this.delegate('.ui-li-divider', 'click', function() {
			var $this = $(this);
			if($this.next().is(':visible')) {
				return;
			}
			$('.ui-listview > .ui-li:not(.ui-li-divider)').slideUp(150);
			$this.nextUntil('.ui-li-divider').slideDown(300);
		});
	};
})( up.jQuery );

