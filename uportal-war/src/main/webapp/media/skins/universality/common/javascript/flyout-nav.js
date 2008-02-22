// Revision: 2007-09-21 gthompson

function initFlyoutMenus() {

	var yoffset = document.getElementById("portalNavigationList").clientHeight;
	var xoffset = 0;

	$("[@id*=tabLink_]").each(function(){

		var id = $(this).attr("id").split("_")[1];
		if ($("#portalNavigation_" + id).children("iframe").length > 0) {
			var menu = $("#portalSubnavigation_" + id)
				.css("top", yoffset + 'px').css("left", xoffset + 'px')
				.css("position", "absolute").css("z-index", 10);
				
			var frame = $("#navFrame_" + id)
				.css("top", yoffset + 'px').css("left", xoffset + 'px')
				.css("height", document.getElementById("navFrame_" + id).clientHeight + 'px')
				.css("width", document.getElementById("navFrame_" + id).clientWidth + 'px')
				.css("position", "absolute").css("z-index", 9);
			
		}
		xoffset += document.getElementById('tabLink_' + id).clientWidth;
		xoffset += parseInt($('#portalNavigation_' + id).css("margin-right")) + 2;
	});
}

function showSubnav(id) {
	var menu = $("#portalSubnavigation_" + id)
		.css("display", "block");
	var frame = $("#navFrame_" + id)
		.css("display", "block")
		.css("width", document.getElementById("portalSubnavigation_" + id).clientWidth + 'px')
		.css("height", document.getElementById("portalSubnavigation_" + id).clientHeight + 'px');
}

function hideSubnav(id, e) {
    if (!e) var e = window.event;
    var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
    if ($(reltg).parents("[@id=portalNavigation_" + id + "]").length > 0) return;
    $("#portalSubnavigation_" + id).css("display", "none");
    $("#navFrame_" + id).css("display", "none");
}

function findPosY(obj) {
    var curtop = 0;
    if(obj.offsetParent)
        while(1)
        {
          curtop += obj.offsetTop;
          if(!obj.offsetParent)
            break;
          obj = obj.offsetParent;
        }
    else if(obj.y)
        curtop += obj.y;
    return curtop;
}