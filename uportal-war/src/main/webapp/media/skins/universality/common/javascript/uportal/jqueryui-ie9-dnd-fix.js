// HOTFIX: We can't upgrade to jQuery UI 1.8.6 (yet)
// This hotfix makes older versions of jQuery UI drag-and-drop work in IE9
(function($) {
	$.each([$.ui.draggable.prototype, $.ui.draggable.prototype,
	        $.ui.draggable.prototype, $.ui.draggable.prototype,
	        $.ui.draggable.prototype], function (index, value) {
		var inner = value._mouseMove;
		value._mouseMove = function (event) {
	        if ($.browser.msie && document.documentMode >= 9) {
	            event.button = 1;
	        };
	        inner.apply(this, [ event ]);
		};
	});
})(jQuery);
