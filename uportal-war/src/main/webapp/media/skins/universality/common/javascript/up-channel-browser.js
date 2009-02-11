/*
	CONFIGURATION PARAMETERS
	
	channelXmlUrl: source of XML data
	onDataLoad: function to execute when XML data has been successfully
		loaded and processed
	onChannelSelect: function to execute on channel selection
		returns a channel object with properties id, name, fname, description,
			and a "userInputs" array containing any user-configurable parameters

*/
(function($){
  $.fn.channelbrowser = function(callerSettings) {
    var settings = $.extend({
      channelXmlUrl: "ajax/channelList",
      categorySelect: null,
	  channelSelect: null,
	  channelSearchInput: null,
	  channelSearchResults: null,
      onDataLoad: null,
	  onChannelSelect: null
    }, callerSettings||{});

	settings.categorySelect = $(settings.categorySelect);
	settings.channelSelect = $(settings.channelSelect);
	settings.channelSearchInput = $(settings.channelSearchInput);
	settings.channelSearchResults = $(settings.channelSearchResults);

	var chooseCategory = function(categoryId) {
	
		settings.channelSelect.html("");
		
		var matching = new Array();
		$("category[ID=" + categoryId + "]", settings.channelXml)
			.find("channel")
			.each(function(){matching.push($(this))});
		matching.sort(sortChannelResults);

		var j = 0;
		$(matching).each(function(i, val){
			if (i == 0 || $(this).attr("ID") != $(matching[i-1]).attr("ID")) {
				settings.channelSelect.get(0).options[j] = new Option($(this).attr("name"), $(this).attr("ID"));
				j++;
			}
		});
		settings.channelSelect
			.change(function(){ chooseChannel(this.value);})
			.children("option:first").attr("selected", true);
		chooseChannel(settings.channelSelect.val());
		
	};

	var chooseChannel = function(channelId) {
		if (channelId.indexOf("_") > -1)
			channelId = channelId.split("_")[1];
		var channelEl = $("channel[ID=" + channelId + "]", settings.channelXml);
		if (channelEl.length > 0)
			channelEl = $(channelEl.get(0)); 
		var channel = { 
			id: channelId, 
			fname: channelEl.attr("fname"),
			name: channelEl.attr("name"), 
			description: channelEl.attr("description"),
			userInputs: new Array()
		};
		
	    var parameters = channelEl.children("parameter[override=yes]");
	    for (var i = 0; i < parameters.length; i++) {
			channel.userInputs.push({ name: $(parameters[i]).attr("name"), value: $(parameters[i]).attr("value") });
	    }
		
		settings.onChannelSelect(channel);
	};

	var search = function(searchTerm) {
		settings.channelSearchResults.html("");
		if (searchTerm == null || searchTerm == '') return;

		var matching = new Array();

		var regex = new RegExp(escapeSpecialChars(searchTerm), "i");
        $("channel", settings.channelXml).filter(function(){
             return regex.test($(this).attr('name'))
                 || regex.test($(this).attr('description'));
        }).each(function(){matching.push($(this))});
	
		matching.sort(sortChannelResults);
		$(matching).each(function(i){
			if ((i == 0 || $(this).attr("ID") != $(matching[i-1]).attr("ID")) && !isHidden($(this))) {
				settings.channelSearchResults.append(
					$(document.createElement('li')).append(
						$(document.createElement('a'))
							.attr("id", $(this).attr("ID")).attr("href", "javascript:;")
							.click(function(){chooseChannel(this.id);})
							.text($(this).attr("name"))
					)
				 );
			 }
		});
		
	};

    var escapeSpecialChars = function(str){
        var specials = new RegExp("[.*+?|()\\[\\]{}\\\\]", "g"); // .*+?|()[]{}\
        return str.replace(specials, "\\$&");
    };

	// sort a list of returned channels by name
	var sortCategoryResults = function(a, b) {
		var aname = a.attr("name").toLowerCase();
		var bname = b.attr("name").toLowerCase();
		if (aname == 'new') return -1;
		if (bname == 'new') return 1;
		if (aname == 'popular') return -1;
		if (bname == 'popular') return 1;
		if(aname > bname) return 1;
		if(aname < bname) return -1;
		return 0;
	};
	
	// sort a list of returned channels by name
	var sortChannelResults = function(a, b) {
		var aname = a.attr("name").toLowerCase();
		var bname = b.attr("name").toLowerCase();
		if(aname > bname) return 1;
		if(aname < bname) return -1;
		return 0;
	};

	var isHidden = function(element) {
	  if ($(element).attr("name") == 'Hidden')
		  return true;
	  else if ($(element).parents("category[name='Hidden']").size() > 0)
		  return true;
	  else
		  return false;
	};

	$.get(settings.channelXmlUrl, {}, function(xml) {
		settings.channelXml = xml;
		// initialize channel browsing
		if (settings.categorySelect.length > 0 && settings.channelSelect.length > 0) {
			var matching = new Array();
			$("category:has(channel)", settings.channelXml).each(
				function(){
					if (!isHidden($(this)))
						matching.push($(this));
				}
			);
			matching.sort(sortCategoryResults);
			$(matching).each(function(i, val) {
				settings.categorySelect.get(0).options[i] = new Option($(this).attr("name"), $(this).attr("ID"));
			});
			settings.categorySelect.change(function(){chooseCategory(this.value)})
				.children("option:first").attr("selected", true);
			chooseCategory(settings.categorySelect.val());
			settings.onDataLoad(xml);
		}
		// initialize channel search
		if (settings.channelSearchInput.length > 0 && settings.channelSearchResults.length > 0) {
			settings.channelSearchInput.keyup(function(){
				search($(this).val());
			});
		}
	});
	
	return this;
  };

})(jQuery);
