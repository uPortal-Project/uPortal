/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import com.objectspace.xml.*;
import com.oroinc.text.regex.*;
import org.jasig.portal.*;
import org.jasig.portal.layout.*;

import java.net.*;

/**
 * This is a user-defined channel for rendering a web page.
 *
 * @author Ken Weiner
 * @version $Revision$
 */
public class CPageRenderer implements org.jasig.portal.IChannel
{
  protected String m_sUrl = null;
  protected String m_sUrlBaseDir = null;
  protected String m_sUrlServer = null;

  private ChannelConfig chConfig = null;

  private static Vector params = null;

  public CPageRenderer()
  {
    params = new Vector();
    params.addElement(new ParameterField("URL", "url", "50", "70", "You have chosen to publish a channel that requires you to provide a URL. Please enter the URL for the channel you wish to publish below.") );
    params.addElement(new ParameterField("Name", "name", "30", "40", "This channel also requires a name parameter. Please enter the name below.") );
  }

  public void init (ChannelConfig chConfig) {this.chConfig = chConfig;}
  public String getName () {return (String) chConfig.get ("name");}
  public boolean isMinimizable () {return true;}
  public boolean isDetachable () {return false;}
  public boolean isRemovable () {return true;}
  public boolean isEditable () {return false;}
  public boolean hasHelp () {return false;}

  public int getDefaultDetachWidth () {return 0;}
  public int getDefaultDetachHeight () {return 0;}

  public Vector getParameters()
  {
    return params;
  }

  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      m_sUrl = (String) chConfig.get ("url");

      if(m_sUrl == null)
      {
        out.print("Cannot render channel: URL is null");
        return;
      }

      // if the URL has only two slashes, terminate it with a third
      if (m_sUrl.substring(m_sUrl.indexOf("/")+1).lastIndexOf("/") ==
	  m_sUrl.substring(m_sUrl.indexOf("/")+1).indexOf("/"))
	  m_sUrl = new String(m_sUrl + "/");
      m_sUrlBaseDir =
	  new String(m_sUrl.substring(0, m_sUrl.lastIndexOf("/")));
      if (m_sUrlBaseDir.substring(7).indexOf("/") != -1)
	  m_sUrlServer =
	      new String(m_sUrlBaseDir.substring(7,    // 7 to skip "http://"
	        m_sUrlBaseDir.substring(7).indexOf("/")+7));
      else
	  m_sUrlServer =
	      new String(m_sUrlBaseDir.substring(7));

      String sLine = null;
      URL url = new URL (m_sUrl);
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      BufferedReader theHTML = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

      StringBuffer sbHTML = new StringBuffer (1024);

      while ((sLine = theHTML.readLine()) != null)
        sbHTML.append (sLine + "\n");

      // Filter out HTML between body tags
      String sHTML = grabHtmlBody (sbHTML.toString ());

      if (sHTML != null)
      {
        sHTML = replaceRelativeImages (sHTML);
        sHTML = replaceRelativeLinks (sHTML);
	if (chConfig.get("eliminateScript") != null &&
	    chConfig.get("eliminateScript").equals("true"))
	  sHTML = eliminateScript (sHTML);
	if (chConfig.get("portalAware") != null &&
	    chConfig.get("portalAware").equals("true"))
	  sHTML = eliminateIrrelevant (sHTML);

        out.println (sHTML);
      }
      else
      {
        out.println ("<b>" + m_sUrl + "</b> cannot be rendered within a channel. <p><i>Note: Pages containing framesets are not allowed.</i>");
      }
    }
    catch (Exception e)
    {
      try
      {
        out.println ("<b>" + m_sUrl + "</b> is currently unreachable.");
      }
      catch (Exception ex)
      {
        Logger.log (Logger.ERROR, e);
      }

      Logger.log (Logger.ERROR, e);
    }
  }

  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }

  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }

  // TODO: should be updated to use ORO regex matching instead of
  // simple String operations
  protected String grabHtmlBody (String sHTML)
  {
    try
    {
      int iBegin, iEnd;

      if (sHTML.indexOf ("<body") >= 0)
      {
        iBegin = sHTML.indexOf ('>', sHTML.indexOf ("<body")) + 1;
        iEnd = sHTML.indexOf ("</body>");
      }
      else if (sHTML.indexOf ("<BODY") >= 0)
      {
        iBegin = sHTML.indexOf ('>', sHTML.indexOf ("<BODY")) + 1;
        iEnd = sHTML.indexOf ("</BODY>");
      }
      else
      {
        iBegin = 0;
        iEnd = sHTML.length();
      }

      return sHTML.substring (iBegin, iEnd);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    return null;
  }

  // replace relative SRC attributes in <IMG> tags with absolute references
  protected String replaceRelativeImages (String sHTML)
  {
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = "<img\\s+(.*?)src=\"/(.*?)\"(.*?)>";
    String t1 = "<img $1src=\"http://" + m_sUrlServer + "/$2\"$3>";

    String r2 = "<img\\s+(.*?)src=\"([^:]*?)\"(.*?)>";
    String t2 = "<img $1src=\"" + m_sUrlBaseDir + "/$2\"$3>";

    Pattern p1 = null;
    Pattern p2 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
      // for truly relative links
      p2 = compiler.compile(r2, Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }

    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    String o2 = Util.substitute(matcher, p2, t2, o1,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o2;
  }

  /** like repaceRelativeImages but with HREF instead of SRC...
   *  coule be integrated later to use the same logic for both */
  protected String replaceRelativeLinks (String sHTML)
  {
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = "<a\\s+(.*?)href=\"/(.*?)\"(.*?)>";
    String t1 = "<a $1href=\"http://" + m_sUrlServer + "/$2\"$3>";

    String r2 = "<a\\s+(.*?)href=\"([^:]*?)\"(.*?)>";
    String t2 = "<a $1href=\"" + m_sUrlBaseDir + "/$2\"$3>";

    Pattern p1 = null;
    Pattern p2 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
      // for truly relative links
      p2 = compiler.compile(r2, Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }
    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    String o2 = Util.substitute(matcher, p2, t2, o1,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o2;
  }

  // get rid of the contents of <script> tags
  protected String eliminateScript(String sHTML) {
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = "<script.*?>.*?</script>";
    String t1 = "<script><!-- Eliminated by UPortal --></script>";

    Pattern p1 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }
    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o1;
  }

  // get rid of everything that's not inside <portal> tags
  protected String eliminateIrrelevant(String sHTML) {
    // uses the ORO regular-expression engine (Perl5-style regexes)
    String r1 = ".*<portal>";
    String t1 = "";

    String r2 = "</portal>.*";
    String t2 = "";

    Pattern p1 = null;
    Pattern p2 = null;

    // Create Perl5Compiler and Perl5Matcher instances.
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher  = new Perl5Matcher();

    // Attempt to compile the pattern.  If the pattern is not valid,
    // report the error and exit.
    try {
      // for relative links with full pathname
      p1 = compiler.compile(r1,	Perl5Compiler.CASE_INSENSITIVE_MASK
	  | Perl5Compiler.SINGLELINE_MASK);
      p2 = compiler.compile(r2,	Perl5Compiler.CASE_INSENSITIVE_MASK
	   | Perl5Compiler.SINGLELINE_MASK);
    } catch(MalformedPatternException e){
      Logger.log(Logger.ERROR, e);
      return sHTML;
    }
    String o1 = Util.substitute(matcher, p1, t1, sHTML,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    String o2 = Util.substitute(matcher, p2, t2, o1,
		    Util.SUBSTITUTE_ALL, Util.INTERPOLATE_ALL);
    return o2;
  }
}
