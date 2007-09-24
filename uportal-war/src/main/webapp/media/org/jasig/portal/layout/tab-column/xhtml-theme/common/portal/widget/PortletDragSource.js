dojo.provide("portal.widget.PortletDragSource");

dojo.require("dojo.dnd.HtmlDragAndDrop");
dojo.require("dojo.dnd.HtmlDragManager");
dojo.require("dojo.dnd.DragAndDrop");
dojo.require("portal.widget.PortletDragObject");

dojo.require("dojo.dom");
dojo.require("dojo.html.*");
dojo.require("dojo.lang.extras");
dojo.require("dojo.lfx.*");
dojo.require("dojo.event");

portal.widget.PortletDragSource = function(id, type){
	node = dojo.byId(id);
	this.dragObjects = [];
	this.constrainToContainer = false;
	if(node){
		this.domNode = node;
		this.dragObject = node;
		// register us
		//dojo.dnd.DragSource.call(this);
		// set properties that might have been clobbered by the mixin
		this.type = (type)||(this.domNode.nodeName.toLowerCase());
		dojo.dnd.DragSource.prototype.reregister.call(this);
	}
};

dojo.inherits(portal.widget.PortletDragSource, dojo.dnd.HtmlDragSource);
dojo.lang.extend(portal.widget.PortletDragSource, {
	dragClass: "", // CSS classname(s) applied to node when it is being dragged

	onDragStart: function(){
		var dragObj = new portal.widget.PortletDragObject(this.dragObject, this.type);
		if(this.dragClass) { dragObj.dragClass = this.dragClass; }

		if (this.constrainToContainer) {
			dragObj.constrainTo(this.constrainingContainer || this.domNode.parentNode);
		}

		return dragObj;
	}
});

