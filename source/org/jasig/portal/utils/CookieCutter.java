/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CookieCutter is a utility class which stores, sends and 
 * receives cookies for a CWebProxy channel instance.  
 * It can be used in other classes where appropriate.
 * All cookies which are sent from the proxied application
 * (and therefore need to be sent back) are kept in a
 * Vector.
 */
public class CookieCutter 
{
    
    private static final Log log = LogFactory.getLog(CookieCutter.class);
    
  private Vector cookies;
  private boolean supportSetCookie2;

  /**
   * Instantiates a new CookieCutter object.
   */
  public CookieCutter()
  {
    cookies = new Vector();
    supportSetCookie2 = false;
  }

  /**
   * Returns true if cookies need to be sent to proxied application.
   */
  public boolean cookiesExist()
  {
    if(cookies.size() > 0)
      return true;
    return false;
  }

  /**
   * Sends a cookie header to origin server according to the Netscape
   * specification.
   *
   * @param httpUrlConnect The HttpURLConnection handling this URL connection
   * @param domain The domain value of the cookie
   * @param path The path value of the cookie
   * @param port The port value of the cookie
   */
  public void sendCookieHeader(HttpURLConnection httpUrlConnect, String domain, String path, String port)
  {
     Vector cookiesToSend = new Vector();
     ChannelCookie cookie;
     String cport = "";
     boolean portOk = true;
     for (int index=0; index<cookies.size(); index++)
     {
        cookie = (ChannelCookie) cookies.elementAt(index);
        boolean isExpired;
        Date current = new Date();
        Date cookieExpiryDate = cookie.getExpiryDate();
        if (cookieExpiryDate != null)
           isExpired = cookieExpiryDate.before(current);
        else
           isExpired = false;
        if (cookie.isPortSet())
        {
           cport = cookie.getPort();
           portOk = false;
        }
        if ( !cport.equals("") )
        {
           if (cport.indexOf(port) != -1)
             portOk = true;
        }
        if ( domain.endsWith(cookie.getDomain()) && path.startsWith(cookie.getPath()) && portOk && !isExpired )
          cookiesToSend.addElement(cookie);
     }
     if (cookiesToSend.size()>0)
     {
       //put the cookies in the correct order to send to origin server
       Vector cookiesInOrder= new Vector();
       ChannelCookie c1;
       ChannelCookie c2;
       boolean flag;
       outerloop:
       for (int i=0; i<cookiesToSend.size(); i++)
       {
         c1 = (ChannelCookie) cookiesToSend.elementAt(i);
         flag = false;
         if (cookiesInOrder.size()==0)
           cookiesInOrder.addElement(c1);
         else
         {
           for (int index=0; index<cookiesInOrder.size(); index++)
           {
             c2 = (ChannelCookie) cookiesInOrder.elementAt(index);
             if ( c1.getPath().length() >= c2.getPath().length() )
             {
               cookiesInOrder.insertElementAt(c1, index);
               flag = true;
               continue outerloop;
             }
           }
           if (!flag)
             cookiesInOrder.addElement(c1);
         }
       }
       //send the cookie header
       // **NOTE** This is NOT the syntax of the cookie header according
       // to rfc 2965. Tested under Apache's Tomcat, the servlet engine
       // treats the cookie attributes as separate cookies.
       // This is the syntax according to the Netscape Cookie Specification.
       String headerValue = "";
       ChannelCookie c;
       for (int i=0; i<cookiesInOrder.size(); i++)
       {
         c = (ChannelCookie) cookiesInOrder.elementAt(i);
         if (i == 0)
           headerValue = c.getName() + "=" +c.getValue();
         else
           headerValue = headerValue + "; " + c.getName() + "=" +c.getValue();
       }
       if ( !headerValue.equals("") )
       {
         httpUrlConnect.setRequestProperty("Cookie", headerValue);
       }
     }
  }

  /**
   * Parses the cookie headers and stores the cookies in the 
   * cookies Vector.
   */
  public void storeCookieHeader(HttpURLConnection httpUrlConnect, String domain, String path, String port)
  {
    // store any cookies sent by the channel in the cookie vector
    int index = 1;
    String header;
    while ( (header=httpUrlConnect.getHeaderFieldKey(index)) != null )
    {
       if (supportSetCookie2)
       {
         if (header.equalsIgnoreCase("set-cookie2"))
            processSetCookie2Header(httpUrlConnect.getHeaderField(index), domain, path, port);
       }
       else
       {
         if (header.equalsIgnoreCase("set-cookie2"))
         {
           supportSetCookie2 = true;
           processSetCookie2Header(httpUrlConnect.getHeaderField(index), domain, path, port);
         }
         else if (header.equalsIgnoreCase("set-cookie"))
         {
           try
           {
             processSetCookieHeader(httpUrlConnect.getHeaderField(index), domain, path, port);
           }
           catch(ParseException e)
           {
             log.warn("CookieCutter: Cannot process Set Cookie header: " + e.getMessage());
           }
         }
       }
       index++;
    }
  }

  /**
   * Processes the Cookie2 header.
   *
   * @param headerVal The value of the header
   * @param domain The domain value of the cookie
   * @param path The path value of the cookie
   * @param port The port value of the cookie
   */
  private void processSetCookie2Header (String headerVal, String domain, String path, String port)
  {
     StringTokenizer headerValue = new StringTokenizer(headerVal, ",");
     StringTokenizer cookieValue;
     ChannelCookie cookie;
     String token;
     while (headerValue.hasMoreTokens())
     {
       cookieValue = new StringTokenizer(headerValue.nextToken(), ";");
       token = cookieValue.nextToken();
       if ( token.indexOf("=") != -1)
       {
          cookie = new ChannelCookie ( token.substring(0, token.indexOf("=")),
                                token.substring(token.indexOf("=")+1).trim() );
       }
       else
       {
          log.debug("CWebProxy: Invalid Header: \"Set-Cookie2:"+headerVal+"\"");
          cookie = null;
       }
       // set max-age, path and domain of cookie
       if (cookie != null)
       {
          boolean ageSet = false;
          boolean domainSet = false;
          boolean pathSet = false;
          boolean portSet = false;
          while( cookieValue.hasMoreTokens() )
          {
            token = cookieValue.nextToken();
            if ( (!ageSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("max-age") )
            {
               cookie.setMaxAge(Integer.parseInt(token.substring(token.indexOf("=")+1).trim()) );
               ageSet = true;
            }
            else if ( (!domainSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("domain") )
            {
               cookie.setDomain(token.substring(token.indexOf("=")+1).trim());
               domainSet = true;
               cookie.domainIsSet();
            }
            else if ( (!pathSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("path") )
            {
               cookie.setPath(token.substring(token.indexOf("=")+1).trim());
               pathSet = true;
               cookie.pathIsSet();
            }
            else if ( !portSet && token.toLowerCase().indexOf("port")!=-1)
            {
               if (token.indexOf("=")==-1)
                 cookie.setPort(port);
               else
                 cookie.setPort(token.substring(token.indexOf("=")+1).trim());
               portSet = true;
               cookie.portIsSet();
            }
          }
          if (!domainSet)
          {
             cookie.setDomain(domain);
          }
          if (!pathSet)
          {
             cookie.setPath(path);
          }
          // set the version attribute
          cookie.setVersion(1);
          // checks to see if this cookie should replace one already stored
          for (int index = 0; index < cookies.size(); index++)
          {
             ChannelCookie old = (ChannelCookie) cookies.elementAt(index);
             if ( cookie.getName().equals(old.getName()) )
             {
                String newPath = cookie.getPath();
                String newDomain = cookie.getDomain();
                String oldPath = old.getPath();
                String oldDomain = old.getDomain();
                if (newDomain.equalsIgnoreCase(oldDomain) && newPath.equals(oldPath))
                     cookies.removeElement(old);
             }
          }
          // handles the max-age cookie attribute (according to rfc 2965)
          int expires = cookie.getMaxAge();
          if (expires < 0)
          {
            // cookie persists until browser shutdown so add cookie to
            // cookie vector
            cookies.addElement(cookie);
          }
          else if (expires == 0)
          {
            // cookie is to be discarded immediately, do not store
          }
          else
          {
            // add the cookie to the cookie vector and then
            // set the expiry date for the cookie
            Date d = new Date();
            cookie.setExpiryDate(new Date((long) d.getTime()+(expires*1000)) );
            cookies.addElement(cookie);
          }
      }
    }
  }

  /**
   * Processes the Cookie header.
   *
   * @param headerVal The value of the header
   * @param domain The domain value of the cookie
   * @param path The path value of the cookie
   * @param port The port value of the cookie
   */
  private void processSetCookieHeader (String headerVal, String domain, String path, String port)
  throws ParseException
  {
     StringTokenizer cookieValue;
     String token;
     ChannelCookie cookie;
     if ( ( (headerVal.indexOf("Expires=") != -1)
              || (headerVal.indexOf("expires=") != -1) )
              || (headerVal.indexOf("EXPIRES=") != -1) )
     {
       // there is only one cookie (old netscape spec)
       cookieValue = new StringTokenizer(headerVal, ";");
       token = cookieValue.nextToken();
       if ( token.indexOf("=") != -1)
       {
          cookie = new ChannelCookie ( token.substring(0, token.indexOf("=")), token.substring(token.indexOf("=")+1).trim() );
       }
       else
       {
          log.debug("CWebProxy: Invalid Header: \"Set-Cookie:"+headerVal+"\"");
          cookie = null;
       }
       // set max-age, path and domain of cookie
       if (cookie != null)
       {
         boolean ageSet = false;
         boolean domainSet = false;
         boolean pathSet = false;
         while( cookieValue.hasMoreTokens() )
         {
           token = cookieValue.nextToken();
           if ( (!ageSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("expires") )
           {
              SimpleDateFormat f = new SimpleDateFormat("EEE, d-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);
	      f.setTimeZone(TimeZone.getTimeZone("GMT"));
              f.setLenient(true);
              Date date = f.parse( token.substring(token.indexOf("=")+1).trim());
              Date current = new Date();
              if (date!=null)
              {
                //set max-age for cookie
                long l;
                if (date.before(current))
                   //accounts for the case where max age is 0 and cookie
                   //should be discarded immediately
                   l = 0;
                else
                   l = date.getTime() - current.getTime();
                int exp = (int) l / 1000;
                cookie.setMaxAge(exp);
                ageSet = true;
              }
           }
           if ( (!domainSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("domain") )
           {
              cookie.setDomain(token.substring(token.indexOf("=")+1).trim());
              domainSet = true;
           }
           if ( (!pathSet && (token.indexOf("=")!=-1)) && token.substring(0, token.indexOf("=")).trim().equalsIgnoreCase("path") )
           {
              cookie.setPath(token.substring(token.indexOf("=")+1).trim());
              pathSet = true;
           }
         }
         if (!domainSet)
         {
            cookie.setDomain(domain);
         }
         if (!pathSet)
         {
            cookie.setPath(path);
         }
         // sets the version attribute of the cookie
         cookie.setVersion(0);
         // checks to see if this cookie should replace one already stored
         for (int index = 0; index < cookies.size(); index++)
         {
            ChannelCookie old = (ChannelCookie) cookies.elementAt(index);
            if ( cookie.getName().equals(old.getName()) )
            {
               String newPath = cookie.getPath();
               String newDomain = cookie.getDomain();
               String oldPath = old.getPath();
               String oldDomain = old.getDomain();
               if ( newDomain.equalsIgnoreCase(oldDomain) && newPath.equals(oldPath) )
                  cookies.removeElement(old);
            }
         }
         // handles the max-age cookie attribute (according to rfc 2965)
         int expires = cookie.getMaxAge();
         if (expires < 0)
         {
           // cookie persists until browser shutdown so add cookie to
           // cookie vector
           cookies.addElement(cookie);
         }
         else if (expires == 0)
         {
           // cookie is to be discarded immediately, do not store
         }
         else
         {
           // add the cookie to the cookie vector and then
           // set the expiry date for the cookie
           Date d = new Date();
           cookie.setExpiryDate( new Date((long)d.getTime()+(expires*1000) ) );
           cookies.addElement(cookie);
         }
       }
     }
     else
     {
       // can treat according to RCF 2965
       processSetCookie2Header(headerVal, domain, path, port);
     }
  }

  /**
   * This class is used by any channel receiving cookies from a
   * backend application to store cookie information.
   * ChannelCookie extends javax.servlet.http.Cookie
   * and contains methods to query the cookie's attribute status.
   *
   */
  private class ChannelCookie extends Cookie
  {

     protected String port = null;
     protected boolean pathSet = false;
     protected boolean domainSet = false;
     protected boolean portSet = false;
     protected Date expiryDate = null;

     public ChannelCookie(String name, String value)
     {
       super(name, value);
     }

     public void setExpiryDate(Date expiryDate)
     {
       this.expiryDate = expiryDate;
     }

     public Date getExpiryDate()
     {
       return expiryDate;
     }

     public String getPath()
     {
       String path = super.getPath();
       if (path.startsWith("\"") && path.endsWith("\""))
         path = path.substring(1, path.length()-1);
       return path;
     }

     public String getValue()
     {
       String value = super.getValue();
       if (value.startsWith("\"") && value.endsWith("\""))
         value = value.substring(1, value.length()-1);
       return value;
     }

     public void pathIsSet()
     {
       pathSet = true;
     }

     public void domainIsSet()
     {
       domainSet = true;
     }

     public void portIsSet()
     {
       portSet = true;
     }

     public void setPort(String port)
     {
       this.port = port;
     }

     public String getPort()
     {
       return port;
     }

     public boolean isPathSet()
     {
       return pathSet;
     }

     public boolean isDomainSet()
     {
       return domainSet;
     }

     public boolean isPortSet()
     {
       return portSet;
     }
  }

}
