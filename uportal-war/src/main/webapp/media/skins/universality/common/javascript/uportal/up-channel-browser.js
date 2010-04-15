/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
	CONFIGURATION PARAMETERS
	
	channelXmlUrl: source of XML data
	onDataLoad: function to execute when XML data has been successfully
		loaded and processed

*/
(function($){
  $.channelbrowser = function(callerSettings) {
    var settings = $.extend({
      channelXmlUrl: "mvc/channelList?xml=true",
      onDataLoad: null
    }, callerSettings||{});

	var that = this;
	
	/**
	 * Construct a Category from a ChannelRegistry XML element
	 */
	var Category = function(el) {
		return { id: el.attr("ID"), name: el.attr("name"), description: el.attr("description") };
	};
	
	/**
	 * Construct a Channel from a ChannelRegistry XML element
	 */
	var Channel = function(el) {
		var channel = { 
			id: el.attr("chanID"), 
			fname: el.attr("fname"),
			name: el.attr("name"), 
			description: el.attr("description"),
			typeId: el.attr("typeID"),
			userInputs: new Array(),
			state: el.attr("state").toLowerCase()
		};
				
	    var parameters = el.children("parameter[override=yes]");
	    for (var i = 0; i < parameters.length; i++) {
			channel.userInputs.push({ name: $(parameters[i]).attr("name"), value: $(parameters[i]).attr("value") });
	    }
	    return channel;
	}

	that.getCategory = function(categoryId) {
		return Category($("category[ID=" + categoryId + "]:first", settings.channelXml));
	};
	
	that.getChannel = function(channelId) {
		return Channel($("channel[chanID=" + channelId + "]:first", settings.channelXml));
	};

	that.getCategories = function() {
		var matching = new Array();
		$("category:has(channel)", settings.channelXml).each(
			function(){
				if (!isHidden($(this)))
					matching.push(Category($(this)));
			}
		);
		matching.sort(sortCategoryResults);
		return matching;
	};
	
	that.getChannelsForCategory = function(categoryId) {
		
		// find a list of channels in the requested category
		var matching = new Array();
		var channels;
		if (categoryId != null && categoryId != "") {
			channels = $("category[ID=" + categoryId + "]", settings.channelXml).find("channel");
		} else {
			channels = $("channel", settings.channelXml);
		}
		channels.each(function(){matching.push(Channel($(this)))});
		// sort the channel results
		matching.sort(sortChannelResults);

		// eliminate duplicate channels from the category
		var matching2 = new Array();
		var i = 0;
		$(matching).each(function(i, val){
			if (i == 0 || this.id != matching[i-1].id) {
				matching2.push(this);
				i++;
			}
		});
		return matching2;
		
	};
	
	that.searchChannels = function(searchTerm, categoryId) {

		if (searchTerm == null || searchTerm == '') return new Array();
		var matching = new Array();

		var regex = new RegExp(escapeSpecialChars(searchTerm), "i");
        $(that.getChannelsForCategory(categoryId)).filter(function(){
             return regex.test(this.name)
                 || regex.test(this.description);
        }).each(function(){matching.push(this)});
		matching.sort(sortChannelResults);
		
		var matching2 = new Array();
		var i = 0;
		$(matching).each(function(i){
			if ((i == 0 || this.id != matching[i-1].id) && !isHidden($(this))) {
				matching2.push(this);
				i++;
			}
		});
		return matching2;
	};
	
    var escapeSpecialChars = function(str){
        var specials = new RegExp("[.*+?|()\\[\\]{}\\\\]", "g"); // .*+?|()[]{}\
        return str.replace(specials, "\\$&");
    };

	// sort a list of returned channels by name
	var sortCategoryResults = function(a, b) {
		var aname = a.name.toLowerCase();
		var bname = b.name.toLowerCase();
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
		var aname = a.name.toLowerCase();
		var bname = b.name.toLowerCase();
		if(aname > bname) return 1;
		if(aname < bname) return -1;
		if (a.fname.toLowerCase() > b.fname.toLowerCase()) return 1;
		if (a.fname.toLowerCase() < b.fname.toLowerCase()) return -1;
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
		settings.onDataLoad(that.getCategories());
	});
	
	return that;
  };

})(jQuery);
