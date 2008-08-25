(function(jQuery){

    // if the uPortal scope is not availalable, add it
    jQuery.up = jQuery.up || {};

    // tabs API methods
    jQuery.fn.channelbrowser = function() {
        var method = typeof arguments[0] == 'string' && arguments[0];
        var args = method && Array.prototype.slice.call(arguments, 1) || arguments;

        return this.each(function() {
            if (method) {
                var channelbrowser = jQuery.data(this, 'up-channelbrowser');
                tabs[method].apply(channelbrowser, args);
            } else
                new jQuery.up.channelbrowser(this, args[0] || {});
        });
    };


	jQuery.up.channelbrowser = function(el, options) {
        var self = this;

        this.element = el;
		
		var defaults = {
			handles: new Array()
		};
		
		this.options = jQuery.extend(defaults, options);
		
        jQuery(el).bind('setData.up-channelbrowser', function(event, key, value) {
            self.options[key] = value;
        }).bind('getData.up-channelbrowser', function(event, key) {
            return self.options[key];
        });

        // save instance for later
        jQuery.data(el, 'up-channelbrowser', this);
        
        this.setup();
        
	}

    // instance methods
    jQuery.extend(jQuery.up.channelbrowser.prototype, {
    	
    	setup: function() {

			var self = this;
	        for (var i = 0; i < self.options.handles.length; i++) {
	        	jQuery(self.options.handles[i]).click(function(){self.init();});
	        }
            	
    	},
    	
    	init: function() {
    		var self = this;

	        for (var i = 0; i < self.options.handles.length; i++) {
	        	jQuery(self.options.handles[i]).unbind('click').click(function(){jQuery(self.element).dialog('open');});
	        }
    		
			jQuery("#channelAddingTabs > ul").tabs();
			jQuery(self.element).dialog({height:450, width:500});
			jQuery("#addChannelSearchTerm").keyup(function(){
				self.search(jQuery(this).attr("value"))
			});
			var categorySelect = document.getElementById("categorySelectMenu");
		
			jQuery.get(channelListUrl, {}, function(xml) {
				self.channelXml = xml;
				var matching = new Array();
				jQuery("category:has(channel)", self.channelXml).each(
				    function(){
				        if (!self.isHidden(jQuery(this)))
				            matching.push(jQuery(this));
				    }
				);
		        matching.sort(self.sortCategoryResults);
		        jQuery(matching).each(function(i, val) {
		        	categorySelect.options[i] = new Option(jQuery(this).attr("name"), jQuery(this).attr("ID"));
		        });
				categorySelect.options[0].selected = true;
		       	self.chooseCategory(categorySelect.value);
		       	jQuery(categorySelect).change(function(){self.chooseCategory(this.value)});
		       	
		       	// remove the loading graphics and message
		   		jQuery("#channelLoading").css("display", "none");
		   		jQuery("#categorySelectMenu").css("background-image", "none");
		   		jQuery("#channelSelectMenu").css("background-image", "none");
		   		jQuery(self.element).parent().parent()
		   	      .css("height", jQuery(self.element).parent().height() + 20)
		   	      .css("z-index", 12);
		   		
			});
			
    	},
    	
		chooseCategory: function(categoryId) {
		
			var self = this;
			var channelSelect = document.getElementById("channelSelectMenu");
			jQuery("#channelSelectMenu").html("");
			
		    var matching = new Array();
			jQuery("category[ID=" + categoryId + "]", this.channelXml)
				.find("channel")
				.each(function(){matching.push(jQuery(this))});
		    matching.sort(this.sortChannelResults);

			var j = 0;
		    jQuery(matching).each(function(i, val){
		    	if (i == 0 || jQuery(this).attr("ID") != jQuery(matching[i-1]).attr("ID")) {
		    		channelSelect.options[j] = new Option(jQuery(this).attr("name"), jQuery(this).attr("ID"));
				    j++;
		    	}
		    });
		    channelSelect.options[0].selected = true;
			this.chooseChannel(channelSelect.value);
            jQuery(channelSelect).change(function(){self.chooseChannel(this.value);});
			
		},

		chooseChannel : function(channelId) {
			if (channelId.indexOf("_") > -1)
				channelId = channelId.split("_")[1];
			var channel = jQuery("channel[ID=" + channelId + "]", this.channelXml);
			if (channel.length > 0)
			    channel = jQuery(channel.get(0)); 
		
			jQuery("#channelTitle").text(channel.attr("name"));
			jQuery("#channelDescription").text(channel.attr("description"));
			jQuery("#addChannelId").attr("value", channelId);
			jQuery("#previewChannelLink").unbind("click").click(function(){ 
			    window.location = portalUrl + "?uP_fname=" + channel.attr("fname");
			});
		
		    // if this channel has user-overrideable parameters, present a form allowing the
		    // user to input values
		    var parameters = channel.children("parameter[override=yes]");
		    for (var i = 0; i < parameters.length; i++) {
		        var input = jQuery(document.createElement("input")).attr("type", "hidden").attr("name", jQuery(parameters[i]).attr("name")).attr("value", jQuery(parameters[i]).attr("value"));
		        var p = jQuery(document.createElement("p")).append(input);
		        jQuery("#channelDescription").append(p);
		    }
		
		},
		
    
		search: function(searchTerm) {
			if (searchTerm == null || searchTerm == '') return;
			var self = this;
		    var matching = new Array();
			jQuery("channel[name*=" + searchTerm + "]", this.channelXml).each(function(){matching.push(jQuery(this))});
			jQuery("channel[description*=" + searchTerm + "]", this.channelXml).each(function(){matching.push(jQuery(this))});
			
		    var searchResults = document.getElementById("addChannelSearchResults");
		    searchResults.innerHTML = "";
		
		    matching.sort(this.sortChannelResults);
		    jQuery(matching).each(function(i){
				if ((i == 0 || jQuery(this).attr("ID") != jQuery(matching[i-1]).attr("ID")) && !self.isHidden(jQuery(this))) {
				     jQuery("#addChannelSearchResults").append(
				     	jQuery(document.createElement('li')).append(
				     		jQuery(document.createElement('a'))
				     			.attr("id", jQuery(this).attr("ID")).attr("href", "javascript:;")
				     			.click(function(){self.chooseChannel(this.id);})
				     			.text(jQuery(this).attr("name"))
				     	)
				     );
			     }
		    });
		    
		},
		
		// sort a list of returned channels by name
		sortCategoryResults: function(a, b) {
		    var aname = a.attr("name").toLowerCase();
		    var bname = b.attr("name").toLowerCase();
		    if (aname == 'new') return -1;
		    if (bname == 'new') return 1;
		    if (aname == 'popular') return -1;
		    if (bname == 'popular') return 1;
		    if(aname > bname) return 1;
		    if(aname < bname) return -1;
		    return 0;
		},
		
		// sort a list of returned channels by name
		sortChannelResults: function(a, b) {
		    var aname = a.attr("name").toLowerCase();
		    var bname = b.attr("name").toLowerCase();
		    if(aname > bname) return 1;
		    if(aname < bname) return -1;
		    return 0;
		},
		
		isHidden: function(element) {
		  if (jQuery(element).attr("name") == 'Hidden')
		      return true;
		  else if (jQuery(element).parents("category[@name='Hidden']").size() > 0)
		      return true;
		  else
		      return false;
		}

	});	

})(jQuery);
