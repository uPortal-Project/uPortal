// Revision: 2007-8-24 gthompson

// Single Cookie Handler Object to encapsulate cookie functions
var UniconCookieHandlerObject = function()
{
    var thisRef = this;
    this.setCookie = function (name,value,days)
    {
        var expires = "";
        if (days)
        {
            var date = new Date();
            date.setTime(date.getTime()+(days*24*60*60*1000));
            expires = "; expires="+date.toGMTString();
        }
        document.cookie = name+"="+value+expires+"; path=/";
        //alert(name+"="+value+expires+"; path=/");
    };

    this.getCookie = function (name)
    {
        var nameTest = name + "=";
        var cookies = document.cookie.split(';');
        for(var i=0; i < cookies.length; i++)
        {
            var cookie = cookies[i];
            //remove leading whitespace
            while (cookie.charAt(0)==' ') 
            {
                cookie = cookie.substring(1,cookie.length);
            }
            if (cookie.indexOf(nameTest) == 0) 
            {
                return cookie.substring(nameTest.length,cookie.length);
            }
        }
        return null;
    };

    this.deleteCookie = function (name)
    {
        thisRef.setCookie(name,"",-1);
    };

};
var UniconCookieHandler = new UniconCookieHandlerObject();
