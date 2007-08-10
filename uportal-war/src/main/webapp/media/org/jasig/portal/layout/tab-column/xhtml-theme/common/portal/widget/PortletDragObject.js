dojo.provide("portal.widget.PortletDragObject");

dojo.require("dojo.dnd.HtmlDragAndDrop");
dojo.require("dojo.dnd.HtmlDragManager");
dojo.require("dojo.dnd.DragAndDrop");

dojo.require("dojo.dom");
dojo.require("dojo.html.*");
dojo.require("dojo.html.style");
dojo.require("dojo.lang.extras");
dojo.require("dojo.lfx.*");
dojo.require("dojo.event");


portal.widget.PortletDragObject = function(id, type){
	this.domNode = dojo.byId(id);
	this.type = type;
	this.constrainToContainer = false;
	this.dragSource = null;
};

dojo.inherits(portal.widget.PortletDragObject, dojo.dnd.HtmlDragObject);
dojo.lang.extend(portal.widget.PortletDragObject, {
	onDragStart: function(e){
		dojo.html.clearSelection();

		this.scrollOffset = dojo.html.getScroll().offset;
		this.dragStartPosition = dojo.html.getAbsolutePosition(this.domNode, true);

		this.dragOffset = {y: this.dragStartPosition.y - e.pageY,
			x: this.dragStartPosition.x - e.pageX};

		this.dragClone = this.createDragNode();

		this.containingBlockPosition = this.domNode.offsetParent ? 
			dojo.html.getAbsolutePosition(this.domNode.offsetParent,true) : {x:0, y:0};

		if (this.constrainToContainer) {
			this.constraints = this.getConstraints();
		}

		// set up for dragging
		with(this.dragClone.style){
			position = "absolute";
			top = this.dragOffset.y + e.pageY + "px";
			left = this.dragOffset.x + e.pageX + "px";
			width = dojo.html.getContentBox(this.domNode).width + "px";
		}

		dojo.body().appendChild(this.dragClone);
		this.domNode.style.display = 'none';

		dojo.event.topic.publish('dragStart', { source: this } );
	},
	
	/**
	 * If the drag operation returned a success we reomve the clone of
	 * ourself from the original position. If the drag operation returned
	 * failure we slide back over to where we came from and end the operation
	 * with a little grace.
	 */
	onDragEnd: function(e){
		switch(e.dragStatus){

			case "dropSuccess":
				dojo.dom.removeNode(this.dragClone);
				this.dragClone = null;
				this.domNode.style.display = 'block';
				var direction = "insertBefore";
				var targetElement = dojo.dom.nextElement(this.domNode);
				if (targetElement == null) {
					direction = "appendAfter";
					targetElement = dojo.dom.prevElement(this.domNode);
					if (targetElement == null) {
						direction = "insertBefore";
						targetElement = this.domNode.parentNode;
					}
				}
				var objectType = this.domNode.id.split("_")[0];
				var action = 'move' + objectType.substr(0,1).toUpperCase() + objectType.substring(1) + 'Here';
                dojo.io.bind({
                    content: { 
	                    action: action, 
	                    sourceID: this.domNode.id.split("_")[1], 
	                    method: direction,
	                    elementID: targetElement.id.split("_")[1]
                    },
                    url: preferencesUrl,
                    load: function(type, data, evt){ },
                    error: function(type, error){ alert("error: " + dojo.errorToString(error)); },
                    mimetype: "text/plain"
                });
				break;

			case "dropFailure": // slide back to the start
				var startCoords = dojo.html.getAbsolutePosition(this.dragClone, true);
				// offset the end so the effect can be seen
				var endCoords = [this.dragStartPosition.x + 1,
					this.dragStartPosition.y + 1];

				// animate
				var line = new dojo.lfx.Line(startCoords, endCoords);
				var anim = new dojo.lfx.Animation(500, line, dojo.lfx.easeOut);
				var dragObject = this;
				//dojo.event.connect(anim, "onAnimate", function(e) {
				//	dragObject.dragClone.style.left = e[0] + "px";
				//	dragObject.dragClone.style.top = e[1] + "px";
				//});
				//dojo.event.connect(anim, "onEnd", function (e) {
					// pause for a second (not literally) and disappear
				//	dojo.lang.setTimeout(function() {
				//			dojo.dom.removeNode(dragObject.dragClone);
							// Allow drag clone to be gc'ed
				//			dragObject.dragClone = null;
				//		},
				//		100);
				//});
				//anim.play();
				dojo.dom.removeNode(dragObject.dragClone);
				dragObject.dragClone = null;
				this.domNode.style.display = 'block';
				break;
		}

		// shortly the browser will fire an onClick() event,
		// but since this was really a drag, just squelch it
		dojo.event.connect(this.domNode, "onclick", this, "squelchOnClick");

		dojo.event.topic.publish('dragEnd', { source: this } );
	}
});

