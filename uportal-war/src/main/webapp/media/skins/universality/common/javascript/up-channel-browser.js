(function($){

    // if the uPortal scope is not availalable, add it
    $.up = $.up || {};

    // tabs API methods
    $.fn.channelbrowser = function() {
        var method = typeof arguments[0] == 'string' && arguments[0];
        var args = method && Array.prototype.slice.call(arguments, 1) || arguments;

        return this.each(function() {
            if (method) {
                var channelbrowser = $.data(this, 'up-channelbrowser');
                tabs[method].apply(channelbrowser, args);
            } else
                new $.up.channelbrowser(this, args[0] || {});
        });
    };


	$.up.channelbrowser = function(el, options) {
        var self = this;

        this.element = el;
		
		var defaults = {
			handles: new Array()
		};
		
		this.options = $.extend(defaults, options);
		
        $(el).bind('setData.up-channelbrowser', function(event, key, value) {
            self.options[key] = value;
        }).bind('getData.up-channelbrowser', function(event, key) {
            return self.options[key];
        });

        // save instance for later
        $.data(el, 'up-channelbrowser', this);
        
        this.setup();
        
	}

    // instance methods
    $.extend($.up.channelbrowser.prototype, {
    	
    	setup: function() {

			var self = this;
	        for (var i = 0; i < self.options.handles.length; i++) {
	        	$(self.options.handles[i]).click(function(){self.init();});
	        }
            	
    	},
    	
    	init: function() {
    		var self = this;

	        for (var i = 0; i < self.options.handles.length; i++) {
	        	$(self.options.handles[i]).unbind('click').click(function(){$(self.element).dialog('open');});
	        }
    		
			$("#channelAddingTabs > ul").tabs();
			$(self.element).dialog({height:450, width:500});
			$("#addChannelSearchTerm").keyup(function(){
				self.search($(this).attr("value"))
			});
			var categorySelect = document.getElementById("categorySelectMenu");
		
			$.get(channelListUrl, {}, function(xml) {
				self.channelXml = xml;
				var matching = new Array();
				$("category:has(channel)", self.channelXml).each(function(){matching.push($(this))});
		        matching.sort(self.sortCategoryResults);
		        $(matching).each(function(i, val) {
		        	categorySelect.options[i] = new Option($(this).attr("name"), $(this).attr("ID"));
		        });
				categorySelect.options[0].selected = true;
		       	self.chooseCategory(categorySelect.value);
		       	$(categorySelect).change(function(){self.chooseCategory(this.value)});
		       	
		       	// remove the loading graphics and message
		   		$("#channelLoading").css("display", "none");
		   		$("#categorySelectMenu").css("background-image", "none");
		   		$("#channelSelectMenu").css("background-image", "none");
			});
			
    	},
    	
		chooseCategory: function(categoryId) {
		
			var self = this;
			var channelSelect = document.getElementById("channelSelectMenu");
			$("#channelSelectMenu").html("");
			
		    var matching = new Array();
			$("category[ID=" + categoryId + "]", this.channelXml)
				.find("channel")
				.each(function(){matching.push($(this))});
		    matching.sort(this.sortChannelResults);

			var j = 0;
		    $(matching).each(function(i, val){
		    	if (i == 0 || $(this).attr("ID") != $(matching[i-1]).attr("ID")) {
		    		channelSelect.options[j] = new Option($(this).attr("name"), $(this).attr("ID"));
				    $(channelSelect.options[j]).click(function(){self.chooseChannel(this.value);});
				    j++;
		    	}
		    });
		    channelSelect.options[0].selected = true;
			this.chooseChannel(channelSelect.value);
			
		},

		chooseChannel : function(channelId) {
			if (channelId.indexOf("_") > -1)
				channelId = channelId.split("_")[1];
			var channel = $("channel[ID=" + channelId + "]", this.channelXml);
		
			$("#channelTitle").text(channel.attr("name"));
			$("#channelDescription").text(channel.attr("description"));
			$("#addChannelId").attr("value", channelId);
			$("#previewChannelLink").click(function(){ window.location = portalUrl + "?uP_fname=" + channel.attr("fname"); });
		
		    // if this channel has user-overrideable parameters, present a form allowing the
		    // user to input values
		    var parameters = channel.children("parameter[override=yes]");
		    for (var i = 0; i < parameters.length; i++) {
		        var input = $(document.createElement("input")).attr("type", "hidden").attr("name", $(parameters[i]).attr("name")).attr("value", $(parameters[i]).attr("value"));
		        var p = $(document.createElement("p")).append(input);
		        $("#channelDescription").append(p);
		    }
		
		},
		
    
		search: function(searchTerm) {
			if (searchTerm == null || searchTerm == '') return;
			var self = this;
		    var matching = new Array();
			$("channel[name*=" + searchTerm + "]", this.channelXml).each(function(){matching.push($(this))});
			$("channel[description*=" + searchTerm + "]", this.channelXml).each(function(){matching.push($(this))});
			
		    var searchResults = document.getElementById("addChannelSearchResults");
		    searchResults.innerHTML = "";
		
		    matching.sort(this.sortChannelResults);
		    $(matching).each(function(i){
				if (i == 0 || $(this).attr("ID") != $(matching[i-1]).attr("ID")) {
				     $("#addChannelSearchResults").append(
				     	$(document.createElement('li')).append(
				     		$(document.createElement('a'))
				     			.attr("id", $(this).attr("ID")).attr("href", "javascript:;")
				     			.click(function(){self.chooseChannel(this.id);})
				     			.text($(this).attr("name"))
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
		}

	});	

})(jQuery);
