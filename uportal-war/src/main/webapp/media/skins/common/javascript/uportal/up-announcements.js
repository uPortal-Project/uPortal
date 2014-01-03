var up = up || {};

(function($) { // jQuery scope
    
    var selector = '.up-portlet-announcements-wrapper[rel]';
    var topicList = [];
    var wrapperList = null;

    var init = function() {
        wrapperList = $(selector);
        wrapperList.each(function() {
            //initalise the data arrays
            $(this).data('announcements', []);

            // grab topics
            var wrapperTopics = $(this).attr("rel").split(",");
            topicList = topicList.concat(wrapperTopics);
        });
        topicList = unique(topicList);
        
        getMessages();
       
        setInterval(function() {
            render();
        }, 5000);

    };

    var unique = function(array) {
        return $.grep(array, function(el, index) {
        	if(el != "") {
                return index == $.inArray(el, array);
            }
        });
    };
    
    var getMessages = function() {       
        var requestQueryString = "topics=" + topicList.join("&topics=");
        $.ajax({
            type: "POST",
            url: '/uPortal/p/announcements.ctf3/max/resource.uP',
            data: requestQueryString,
            complete: function(data, status,jqXHR) {
                //$.each(data.responseJSON, function(topic, announcements) {
                $.each($.parseJSON(data.responseText), function(topic, announcements) {
                	var filteredList = wrapperList.filter(function(index) {
                		var wrapperTopics = $(this).attr("rel").split(",");
                		if($.inArray(topic, wrapperTopics) > -1) {
                			return true;
                		}
                		return false;
                	});

                	filteredList.each(function(){
                		var mydata = $(this).data('announcementdata') || [];
                        var merged = mydata.concat(announcements);
                        $(this).data('announcementdata', merged);
                    });
                });
            },
            dataType: "json"
        });       
    };

    var render = function() {
        wrapperList.each(function(i, wrapper) {
            var renderIndex = $(wrapper).data('renderIndex') || 0;
    
            var myAnnouncements = $(wrapper).data('announcementdata');
            if ('undefined' !== typeof myAnnouncements) {
	            var announcement = myAnnouncements[renderIndex];
	            var msgIndex = myAnnouncements.indexOf(announcement) + 1;
	
	            var countStr = "(" + msgIndex + " of " + myAnnouncements.length + ") ";
	
	            $(wrapper).fadeTo("slow", 0.1, function() {
	                $(this).html('<div class="item">'+countStr+'<h2>'+announcement.title+'</h2><p>'+announcement.message+'</p></div>');
	                               $(this).fadeTo("slow", 1);
	            });
	           
	            if(renderIndex == myAnnouncements.length - 1) {
	                $(wrapper).data('renderIndex',0);
	            }
	            else {
	                $(wrapper).data('renderIndex',renderIndex + 1);
	            }       
            }
        });
    };

    up.announcements = init;

 })(jQuery); 