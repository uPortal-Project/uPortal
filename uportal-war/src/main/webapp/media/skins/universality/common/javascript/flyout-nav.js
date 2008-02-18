// Revision: 2007-09-21 gthompson

function showSubnav(id) {
    var menu = document.getElementById("navMenu_" + id);
    menu.style.display = "block";
    var frame = document.getElementById("navFrame_" + id);
    frame.style.display = "block";
    frame.style.width = menu.clientWidth + 'px';
    frame.style.height = menu.clientHeight + 'px';
}

function hideSubnav(id, e) {
    if (!e) var e = window.event;
    var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
    while (reltg.id != ('tab_' + id) && reltg.nodeName != 'BODY')
	reltg= reltg.parentNode
	if (reltg.id == ('tab_' + id)) return;
    document.getElementById("navMenu_" + id).style.display = "none";
    document.getElementById("navFrame_" + id).style.display = "none";
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