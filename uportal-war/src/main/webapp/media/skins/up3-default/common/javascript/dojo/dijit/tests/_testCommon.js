/*	
	_testCommon.js - a simple module to be included in dijit test pages to allow
	for easy switching between the many many points of the test-matrix.

	in your test browser, provides a way to switch between available themes,
	and optionally enable RTL (right to left) mode, and/or dijit_a11y (high-
	constrast/image off emulation) ... probably not a genuine test for a11y.

	usage: on any dijit test_* page, press ctrl-f9 to popup links.

	there are currently (2 themes * 4 tests) * (10 variations of supported browsers)
	not including testing individual locale-strings

	you should not be using this in a production enviroment. include
	your css and set your classes manually. for test purposes only ...
*/

(function(){
	var theme = false; var testMode;
	if(window.location.href.indexOf("?") > -1){
		var str = window.location.href.substr(window.location.href.indexOf("?")+1);
		var ary  = str.split(/&/);
		for(var i=0; i<ary.length; i++){
			var split = ary[i].split(/=/),
				key = split[0],
				value = split[1];
			switch(key){
				case "locale":
					// locale string | null
					djConfig.locale = locale = value;
					break;
				case "dir":
					// rtl | null
					document.getElementsByTagName("html")[0].dir = value;
					break;
				case "theme":
					// tundra | soria | noir | squid | null
					theme = value;
					break;
				case "a11y":
					if(value){ testMode = "dijit_a11y"; }
			}
		}
	}		

	// always include the default theme files:
	if(!theme){ theme = djConfig.defaultTestTheme || 'tundra'; }
	var themeCss = dojo.moduleUrl("dijit.themes",theme+"/"+theme+".css");
	var themeCssRtl = dojo.moduleUrl("dijit.themes",theme+"/"+theme+"_rtl.css");
	document.write('<link rel="stylesheet" type="text/css" href="'+themeCss+'"/>');
	document.write('<link rel="stylesheet" type="text/css" href="'+themeCssRtl+'"/>');

	if(djConfig.parseOnLoad){ 
		djConfig.parseOnLoad = false;
		djConfig._deferParsing = true;
	}

	dojo.addOnLoad(function(){

		// set the classes
		
		if(!dojo.hasClass(dojo.body(),theme)){ dojo.addClass(dojo.body(),theme); }
		if(testMode){ dojo.addClass(dojo.body(),testMode); }
			

		// test-link matrix code:
		var node = document.createElement('div');
		node.id = "testNodeDialog";
		dojo.addClass(node,"dijitTestNodeDialog");
		dojo.body().appendChild(node);

		_populateTestDialog(node);
		dojo.connect(document,"onkeypress","_testNodeShow");

		if(djConfig._deferParsing){ dojo.parser.parse(dojo.body()); }

	});

	_testNodeShow = function(/* Event */evt){
		var key = (evt.charCode == dojo.keys.SPACE ? dojo.keys.SPACE : evt.keyCode);
		if(evt.ctrlKey && (key == dojo.keys.F9)){ // F9 is generic enough?
			dojo.style(dojo.byId('testNodeDialog'),"top",(dijit.getViewport().t + 4) +"px");
			dojo.toggleClass(dojo.byId('testNodeDialog'),"dijitTestNodeShowing");
		}
	}

	_populateTestDialog = function(/* DomNode */node){
		// pseudo-function to populate our test-martix-link pop-up
		var base = window.location.pathname;
		var str = "";
		var themes = ["tundra",/*"noir", */ "soria" /* ,"squid" */ ];
		str += "<b>Tests:</b><br><table>";
		dojo.forEach(themes,function(t){
			str += 	'<tr><td><a hr'+'ef="'+base+'?theme='+t+'">'+t+'</'+'a></td>'+
				'<td><a hr'+'ef="'+base+'?theme='+t+'&dir=rtl">rtl</'+'a></td>'+
				'<td><a hr'+'ef="'+base+'?theme='+t+'&a11y=true">a11y</'+'a></td>'+
				'<td><a hr'+'ef="'+base+'?theme='+t+'&a11y=true&dir=rtl">a11y+rtl</'+'a></td>'+
				// too many potential locales to list, use &locale=[lang] to set
				'</tr>';
		});
		str += '<tr><td colspan="4">jump to: <a hr'+'ef="'+(dojo.moduleUrl("dijit.themes","themeTester.html"))+'">themeTester</'+'a></td></tr>';
		str += '<tr><td colspan="4">or: <a hr'+'ef="'+(dojo.moduleUrl("dijit.tests"))+'">tests folder</'+'a></td></tr>';
		node.innerHTML = str + "</table>";
	}
})();
