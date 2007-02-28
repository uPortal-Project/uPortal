dojo.provide("portal.widget.PortletDropTarget");

dojo.require("dojo.dnd.HtmlDragAndDrop");
dojo.require("dojo.dnd.HtmlDragManager");
dojo.require("dojo.dnd.DragAndDrop");

dojo.require("dojo.dom");
dojo.require("dojo.html.*");
dojo.require("dojo.lang.extras");
dojo.require("dojo.lfx.*");
dojo.require("dojo.event");


portal.widget.PortletDropTarget = function(id, types){
	if (arguments.length == 0) { return; }
	this.domNode = dojo.byId(id);
	dojo.dnd.DropTarget.call(this);
	if(types && dojo.lang.isString(types)) {
		types = [types];
	}
	this.acceptedTypes = types || [];
	dojo.dnd.dragManager.registerDropTarget(this);
}
dojo.inherits(portal.widget.PortletDropTarget, dojo.dnd.HtmlDropTarget);

dojo.lang.extend(portal.widget.PortletDropTarget, {
	elements: [],
	onDragOver: function(e){
		return this.accepts(e.dragObjects);
	},
	
	_getNodeUnderMouse: function(e){
		this.elements = [];
		for (var i = 0, child; i < this.domNode.childNodes.length; i++) {
			child = this.domNode.childNodes[i];
			if (child.nodeType != dojo.dom.ELEMENT_NODE) { continue; }
			this.elements.push(child);
		}
		for (var i = 0, child; i < this.elements.length; i++) {
			child = this.elements[i];
			var pos = dojo.html.getAbsolutePosition(child, true);
	    	var inner = dojo.html.getBorderBox(child);	    	
			if (e.pageY >= pos.y && e.pageY <= (pos.y+inner.height)) { return i; }
		}
		return -1;
	},

	createDropIndicator: function() {
	   this.dropIndicator = document.createElement("div");
	   this.dropIndicator.className = "dropIndicator";
	    with (this.dropIndicator.style) {
	    	var inner = dojo.html.getBorderBox(this.domNode);	    	
	        width = (inner.width-10) + "px";
	        height = "100px";
	    }
	    document.body.appendChild(this.dropIndicator);	    
	},

	onDragMove: function(e, dragObjects){
		var position;
		var child;
		var i = this._getNodeUnderMouse(e);
		if(i >= 0) { return };
		
		if (!this.dropIndicator) {
			this.createDropIndicator();
		}
		var clone = dragObjects[0].dragClone;
		with (this.dropIndicator.style) {
	    	var inner = dojo.html.getBorderBox(this.domNode);	    	
	        width = (inner.width-10) + "px";
	    	inner = dojo.html.getBorderBox(clone);	    	
			height = inner.height+"px";
			clone.style.width = width;
		}
		
		for (var i = 0, child; i < this.elements.length; i++) {
			child = this.elements[i];
			var pos = dojo.html.getAbsolutePosition(child, true);
			if (e.pageY < pos.y) {
				this._insertNode(this.dropIndicator,child,"before");
				return;
			}
		}
		this._insertNode(this.dropIndicator,"after");
	},
	
	/**
	 * Inserts the DragObject as a child of this node relative to the
	 * position of the mouse.
	 *
	 * @return true if the DragObject was inserted, false otherwise
	 */
	onDrop: function(e){
		this.onDragOut(e);
		var i = this._getNodeUnderMouse(e);
		if (i < 0) {
			// It is a space between portlets so figure out which one it is above
			if (this.domNode.childNodes.length) {
				var child = null;
				for (var i = 0; i < this.elements.length; i++) {
					child = this.elements[i];
					var pos = dojo.html.getAbsolutePosition(child, true);
					if (e.pageY < pos.y) {
						return this._insertNode(e.dragObject.domNode, child, "before");
					}
				}
				if (child != null)
					return this._insertNode(e.dragObject.domNode, child, "after");
			}
			return this._insertNode(e.dragObject.domNode, this.domNode, "append");
		}
		var child = this.elements[i];
		if (dojo.html.gravity(child, e) & dojo.html.gravity.NORTH) {
			return this._insertNode(e.dragObject.domNode, child, "before");
		} else {
			return this._insertNode(e.dragObject.domNode, child, "after");
		}
	},

	_insertNode: function(node, refNode, position) {
		if(position == "before") {
			return dojo.html.insertBefore(node, refNode);
		} else if(position == "after") {
			return dojo.html.insertAfter(node, refNode);
		} else if(position == "append") {
			refNode.appendChild(node);
			return true;
		}

		return false;
	}
});
