/**
 * University of British Columbia ("UBC") will freely share software
 * registered in the JA-SIG Clearing House with institutions of
 * higher-education for their non-profit use.  The borrowing institution
 * will not share or distribute the software without the consent of
 * UBC.  By its use, the borrowing institution agrees to indemnify
 * and hold harmless UBC against all loss, cost, damage, liability,
 * injury or expense, including reasonable attorneys' fees, arising out
 * of their use of the software.
 *
 * Those desiring to incorporate this software into commercial products
 * or use for commercial purposes should contact:
 *
 *   Associate Director of Info Sys, ITServices, UBC
 *   6356 Agricultural Road
 *   Vancouver, B.C.,  CANADA
 *   V6T 1Z2
 *
 *   Tel: 604-822-6611
 *
 *
 * SOFTWARE IS PROVIDED "AS IS."  TO THE MAXIMUM EXTENT PERMITTED BY LAW,
 * UBC DISCLAIMS ALL WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. UBC DOES
 * NOT WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN SOFTWARE
 * WILL MEET ANY REQUIREMENTS OR NEEDS OF THE BORROWING INSTITUTION, OR
 * THAT SOFTWARE WILL OPERATE ERROR FREE, OR IN AN UNINTERRUPTED FASHION,
 * OR THAT ANY DEFECTS OR ERRORS IN SOFTWARE WILL BE CORRECTED, OR THAT
 * SOFTWARE IS COMPATIBLE WITH ANY PARTICULAR PLATFORM.

 * IN NO EVENT WILL UBC BE LIABLE TO ANY BORROWING INSTITUTION OR
 * ANY THIRD PARTY FOR ANY INCIDENTAL OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, WITHOUT LIMITATION, INDIRECT, SPECIAL, PUNITIVE, OR
 * EXEMPLARY DAMAGES) ARISING OUT OF THE USE OF OR INABILITY TO USE
 * SOFTWARE, OR FOR ANY CLAIM BY ANY OTHER PARTY, EVEN IF UBC HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */

/**
 * Web IMAP email channel
 *
 * @author George Lindholm, ITServices, UBC
 * @author Ken Weiner, IBS
 * @version $Revision$
 */

 /**
 * The uPortal WebMail channel is an IMAP based email client,
 * allowing access to messages, attachments, and folders.
 *
 * Installation:
 *  to install the WebMail channel you'll need:
 *    - a IMAP server
 *    - a SMTP server
 *    - the Java Activation Framework package (1.0.1 or later)
 *    - the JavaMail package (1.1.3 or later)
 *    - the Oreilly Servlet package (com.oreilly.servlet) (31oct2000 or later),
 *      used for sending attachments. Download it from
 *        http://www.servlets.com/resources/com.oreilly.servlet/index.html
 *
 *
 * Files:
 *    CIMAPMail.java - IMAP Mail client
 *    MailCheck.java - Mail check Applet
 *    dispatch2.jsp  - Interface back into the portal for downloading attachments
 *    hlp_email.htm  - Experimental prototype help file
 *
 * Configuration:
 *   the following channel parameters need to appear in the channel definition:
 *
 *    name:             Channel Client name. Used by the portal itself to label
 *                      the channel.
 *                      (Something like "your_institution Web Mail")
 *    host:             Imap host
 *    port:             Imap port (defaults to 143)
 *    smtphost:         Smtp host for sending email
 *    smtpport:         Smtp port (defaults to 25)
 *    domain:           Your portals email domain
 *    caption:          Client Name (see name). Displayed in the client
 *    organization:     Organizational name for outgoing mail
 *    sentName:         Name of folder that will hold sent messages
 *    trashName:        Name of folder that will hold deleted messages
 *    maxMsgSize:       Maximum size of a message (including attachments) that one
 *                      can send
 *    folderDir:        Directory in users home directory that will hold the users
 *                      folders (optional)
 *    sessionUsername:  session attribute that holds the username to use when
 *                      authenticating the imap connection
 *    sessionPassword:  session attribute for the password of sessionUsername
 *    loginText:        Greeting text on channel explaining what values should be
 *                      used to login to the channel
 *
 * Authentication:
 *   the WebMail client is capable of prompting the user for a username/password
 *   for the imap session. This dialogue will show up if the
 *   sessionUsername/sessionPassword fail, or are not both supplied.
 *   If the session attributes portalhost and secureportalhost exists, then they
 *   will be used to provide secure authentication. Ie. the authentication form
 *   will post to the securehost, and once authenticated, the code will direct the
 *   browser to flip back to portalhost (probably non-secure). At UBC, we have:
 *      portalhost = http://my.ubc.ca
 *      secureportalhost = https://my.ubc.ca
 *    These values would probably be set in the web.xml file.
 *    (I'm sure that there is a cleaner way of doing this.)
 *
 *  Setup:
 *    dispatch2.jsp - in the / directory. This
 *      is a heavily modified version of dispatch.jsp need for attachments. You
 *      need this version because dispatch.jsp will wrap the attachment data with
 *      the portal html corrupting the download. The second problem is that the jsp
 *      compiler will leave newlines in the source file alone so they will also be
 *      downloaded with the attachment.
 *
 *      WARNING. If you edit the file, you have to make sure that you don't cause
 *      any newlines to added at the end of the file or outside the java code. These
 *      will cause attachment downloading to fail.
 *
 *    hlp_email.htm - in the /help directory
 *
 *    MailCheck - If you want to use the realtime mail notification applet, change
 *      USE_APPLET to be true and place MailCheck.class in /applets.
 *
 *      Note, that this will "disable" any session timeout support your
 *      jsp/http server may provide since each "new mail" check will appear as a
 *      http request by the user to the server, sigh.
 *
 *  Issues:
 *   Things to think about:
 *
 *    The authentication process is very cumbersome. Each browser/platform has
 *    it's own (irritating) behaviour when going from http to https to http.
 *    Would a login applet be the answer, or can something be done with JavaScript?
 *
 *    There is currently no support for folders of folders (creating nor navigating).
 *    They are displayed but there is no way to interact with one.
 *
 *    A user address book/LDAP support is needed
 *
 *    There needs to be a way to bypass the session timeout code in the server
 *    so that the mailcheck applet can be used. It would probably entail adding
 *    session timeout code to the uPortal *.jsp files and providing for some
 *    way for the client can ask to have a http request not count towards the
 *    inactivity counter. This will also be needed for any other type of
 *    real-time notifications in the uPortal.
 *
 *    Channel specific persistent configuration is needed. Each user should
 *    be able to configure their own colour choices, define a .signature file, etc.
 *
 *
 *    George Lindholm       November 7, 2000
 *    George.Lindholm@ubc.ca
 */

package org.jasig.portal.channels;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.event.*;
import javax.mail.search.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.activation.FileDataSource;
import javax.activation.DataHandler;

import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.ParamPart;

import org.jasig.portal.*;
import org.jasig.portal.layout.*;
import java.util.zip.*;

public final class CIMAPMail implements org.jasig.portal.IChannel, HttpSessionBindingListener
{


  ChannelConfig chConfig = null;
  private static Vector params = null;
  private final static String rcsVersion = "$Revision$"; // from rcs/cvs
  private final static String clientVersion = rcsVersion.substring (rcsVersion.indexOf (":") + 1, rcsVersion.length ()-1).trim ();

  // Configurable parameters
  private static String sName = null;
  private static String sIMAPHost = null;         // IMAP host
  private static int iIMAPPort = 0;               // IMAP port
  private static String sSMTPHost = null;         // IMAP host
  private static int iSMTPPort = 0;               // IMAP port
  private static String sCaption = null;      // Application name
  private static String sOrganization = null; // Service Provider name
  private static String sTrashName = null;    // "Wastebasket"
  private static String sSentName = null;     // "Sent Items"
  private static int iMaxMessageSize = 0;     // Largest message we will send
  private static String sFolderDir = null;    // users folder directory
  private static String sDomainName = null;   // Our email domain
  private static String sSessionUsername = null;  // session atribute for authenticated username
  private static String sSessionPassword = null; // session atribute for password of authenticated username
  private static String sLoginText = null;

  // Candidates for configurable parameters
  private static final int iMsgsPerPage = 20;
  private static final int iFldrsPerPage = 20;
  private static final String sNavBarColor = "#99ccff";
  private static final String sHeaderColor = "#dddddd";
  private static final String sControlColor = "#ffffff";
  private static final String sColumnHeadersColor = "#cccccc"; // From, Subject, etc line
  private static final String sTableCellColor = "#cafcac";
  private static final String sUnseenMsgColor = "#ffffcc"; // Unseen messages
  private static final String sSeenMsgColor = "#99ccff"; // Seen messages
  private static final String sDraftsName = "Drafts"; //"Drafts"

  // User configuration
  private boolean showFullHeaders = false;

  // button messages
  private static final String deleteMessagesButtonTxt = "Delete";
  private static final String deleteMessageButtonTxt = "Delete";
  private static final String forwardMessageButtonTxt = "Forward";
  private static final String nextMessageButtonTxt = "Next message";
  private static final String moveMessagesButtonTxt = "Move";
  private static final String replyMessageButtonTxt = "Reply";
  private static final String replyAllMessageButtonTxt = "Reply All";
  private static final String returnToMessageButtonTxt = "Return to ";
  private static final String searchMessagesButtonTxt = "Search";
  private static final String clearSearchMessagesButtonTxt = "Clear Search";
  private static final String searchFolderButtonTxt = "Search Folder";
  private static final String checkNewMailButtonTxt = "Check for new mail";
  private static final String newFolderButtonTxt = "New";
  private static final String createFolderButtonTxt = "Create";
  private static final String deleteFolderButtonTxt = "Delete";
  private static final String renameFolderButtonTxt = "Rename";
  private static final String renameFolderNameButtonTxt = "Rename Folder";
  private static final String emptyFolderButtonTxt = "Empty "; // folder name appended

  // Used to keep state
  // Initialized in respondToAuthentication
  private int iMsgsStartAt;
  private int iMsgsEndAt;
  private int iTotalFldrs;
  private int iFldrsStartAt;
  private int iFldrsEndAt;
  private boolean authenticated;
  private ActiveFolder activeFolder;
  private ActiveFolder inbox;
  private Comparator msgSortOrder;
  private ImapFolder imapFolders;
  private Part[] includeAttachments = null; // Attachments from a Draft message needed when sending draft
  private boolean onetimeSetupDone = false;

  // These should be deduced from portal authentication (hard-coded for now)
  private String sUser = null; //ask
  private String sPassword = null; // ask
  private String sUserEmail = null; // get from session
  private String sFirstName = ""; // get from session
  private String sLastName = ""; // get from session

  private static String layoutUrl = null; // get from HTTP request
  private static String portalHost = null; // URL to non-secure host
  private static String securePortalHost = null; // URL to secure host
  private boolean redirectBack = false;     // return to http after https authentication
  private static final String sProtocol = "imap";
  private static final String sMailbox = "INBOX";
  private static String[] sSpecialFolderNames = null; // Folders managed by the client
  private URLName urlName = null; // imap url
  private Properties props = System.getProperties ();
  Session session = null;
  Store store = null;

  // Compilation constants
  private static final boolean DEBUG = false;
  private static final int MAX_ATTACHMENTS = 5;
  private static final boolean USE_APPLET = false; // Generate HTML code to use applet to access mail channel
                                                   // Not used since it causes session timeouts to not work

  private FetchProfile fetchProfile = null;
  private char folderSeparator;  // IMAP folder separator
  private static final String crlf = "\r\n";  // zip file new line sequence
  private static final DateFormat httpDateFormat = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

  // Metrics about our session
  private int messagesRead = 0;
  private int messagesDeleted = 0;
  private int messagesMoved = 0;
  private int messagesDownloaded = 0;
  private int attachmentsViewed = 0;
  private int messagesSent = 0;
  private boolean showMetrics = false;

  /**
   * Wrapper class to make FilePart data available as a DataSource
   */
  public class MultipartDataSource implements javax.activation.DataSource {
    ByteArrayOutputStream buff = null;
    String contentType = null;
    String filename = null;

    public MultipartDataSource(FilePart filePart) throws IOException {
      contentType = filePart.getContentType();
      filename = filePart.getFileName();
      buff = new ByteArrayOutputStream();
      filePart.writeTo(buff);
    }
    public void finalize() {
      buff = null;
    }
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(buff.toByteArray());
    }
    public OutputStream getOutputStream() throws IOException {
      throw new IOException("getOutputStream() not implemented");
    }
    public String getContentType() {
      return contentType;
    }
    public String getName() {
      return filename;
    }
    public File getFile() throws Exception {
      throw new Exception("getFile() not implemented");
    }
    public void setFileTypeMap(javax.activation.FileTypeMap p0) throws Exception {
      throw new Exception("setFileTypeMap() not implemented");
    }
  }

  private class ImapFolderException extends Exception {
  public ImapFolderException (){}
    public ImapFolderException (String eMsg) {
      super (eMsg);
    }
  }

  private class CIMAPLostConnectionException extends Exception {
  public CIMAPLostConnectionException (){super ();}
    public CIMAPLostConnectionException (String eMsg) {
      super (eMsg);
    }
  }

  private StoreListener storeListener = new StoreListener () {
    public void notification (StoreEvent e) {
      String eventMsg = e.getMessage ();
      int eventType = e.getMessageType ();
      if (DEBUG) Logger.log (Logger.DEBUG, "store event: " + eventMsg + ", " + eventType);
    }
  };

  public CIMAPMail ()
  {
    params = new Vector ();
    params.addElement (new ParameterField ("Name", "name", "30", "30", "What text should be displayed in the channel header area."));
    params.addElement (new ParameterField ("IMAP Host", "host", "30", "64", "Enter the IMAP host."));
    params.addElement (new ParameterField ("IMAP Port", "port", "4", "8", "Enter the IMAP port."));
    params.addElement (new ParameterField ("SMTP Host", "smtphost", "30", "64", "Enter the SMTP host."));
    params.addElement (new ParameterField ("SMTP Port", "smtpport", "4", "8", "Enter the SMTP port."));
    params.addElement (new ParameterField ("Domain", "domain", "4", "32", "Enter the email domain."));
    params.addElement (new ParameterField ("Caption", "caption", "15", "15", "Enter a caption for the main mail page."));
    params.addElement (new ParameterField ("Organization", "organization", "15", "32", "Enter the organization which will be included in outgoing mail headers."));
    params.addElement (new ParameterField ("Sent Mail Folder", "sentName", "15", "15", "Enter the exact name of the folder which will hold sent messages."));
    params.addElement (new ParameterField ("Trash Folder", "trashName", "15", "15", "Enter the exact name of the folder which will hold discarded messages untill they are permanently deleted."));
    params.addElement (new ParameterField ("Maximum Message Size", "maxMsgSize", "4", "4", "Enter the maximum size of a message (in megabytes)."));
    params.addElement (new ParameterField ("User Mail Folder Directory", "folderDir", "15", "255", "Enter the name of the directory that will contain the users mail folders (may be empty)."));
    params.addElement (new ParameterField ("Session Username attribute", "sessionUsername", "8", "64", "Enter the name of the session attribute for the authenticated username."));
    params.addElement (new ParameterField ("Session User Password attribute", "sessionPassword", "8", "64", "Enter the name of the session attribute for the password of the authenticated username."));
    params.addElement (new ParameterField ("Login Text", "loginText", "32", "64", "Enter the login text."));
  }

  public void init (ChannelConfig chConfig)
  {
    String sParameter;

    this.chConfig = chConfig;
    sName = (String) chConfig.getParameter ("name");
    sIMAPHost = (String) chConfig.getParameter ("host");
    props.put ("mail.smtp.host", sIMAPHost);
    sParameter = (String)chConfig.getParameter ("port");
    if (sParameter != null) {
      try {
        iIMAPPort = Integer.parseInt (sParameter);
      } catch (NumberFormatException nfe) {
      }
    }
    if (iIMAPPort == 0) {
      iIMAPPort = 143;        // IMAP default
    }

    sSMTPHost = (String) chConfig.getParameter ("smtphost");
    if (sSMTPHost != null) {
      props.put ("mail.smtp.host", sSMTPHost);
    }
    sParameter = (String)chConfig.getParameter ("smtpport");
    if (sParameter != null) {
      try {
        iSMTPPort = Integer.parseInt (sParameter);
      } catch (NumberFormatException nfe) {
      }
    }
    if (iSMTPPort == 0) {
      iSMTPPort = 25;        // SMTP default
    }
    props.put ("mail.smtp.port", iSMTPPort + "");

    sCaption = (String) chConfig.getParameter ("caption");
    sOrganization = (String) chConfig.getParameter ("organization");
    sDomainName = (String) chConfig.getParameter ("domain");
    sSentName = (String) chConfig.getParameter ("sentName");
    sTrashName = (String) chConfig.getParameter ("trashName");
    sFolderDir = (String) chConfig.getParameter ("folderDir");
    sSessionUsername = (String) chConfig.getParameter ("sessionUsername");
    sSessionPassword = (String) chConfig.getParameter ("sessionPassword");
    sLoginText = (String) chConfig.getParameter ("loginText");
    if (sFolderDir != null && sFolderDir.trim ().length () == 0) {
      sFolderDir = null;
    }

    sSpecialFolderNames = new String[] {"Inbox", sDraftsName, sSentName, sTrashName};

    sParameter = (String)chConfig.getParameter ("maxMsgSize");
    if (sParameter != null) {
      try {
        iMaxMessageSize = Integer.parseInt (sParameter);
      } catch (NumberFormatException nfe) {
      }
    }
    if (iMaxMessageSize <= 0) {
      iMaxMessageSize = 3;
    }
    iMaxMessageSize = iMaxMessageSize * 1024 * 1024;

    fetchProfile = new FetchProfile ();
    fetchProfile.add (FetchProfile.Item.ENVELOPE);

    httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

  }

public String getID () {return chConfig.getChannelID ();}
public String getName () {return sName;}

public boolean isMinimizable () {return true;}
public boolean isDetachable () {return true;}
public boolean isRemovable () {return true;}
public boolean isEditable () {return false;}
public boolean hasHelp () {return false;}

public int getDefaultDetachWidth () {return 175;}
public int getDefaultDetachHeight () {return 250;}

  private final FolderListener folderListener = new FolderListener () {
    public void folderCreated (FolderEvent e) {
      try {
        //Logger.log(Logger.DEBUG, "folder created event for " + e.getFolder().getName());
        imapFolders.add (e.getFolder ());
      } catch (MessagingException me) {
        Logger.log (Logger.ERROR, "folderCreated" + me);
      }
    }
    public void folderDeleted (FolderEvent e) {
      //Logger.log(Logger.DEBUG, "folder deleted event for " + e.getFolder().getName());
      try {
        imapFolders.deleted (e.getFolder ().getName ());
    } catch (ImapFolderException sfe) {}
    }
    public void folderRenamed (FolderEvent e) {
      //Logger.log(Logger.DEBUG, "folder renamed event from " + e.getFolder().getName() + " to " + e.getNewFolder());
      try {
        imapFolders.rename (e.getFolder ().getName (), e.getNewFolder ().getName ());
      } catch (ImapFolderException sfe) {
      }
    }
  };

  public Vector getParameters ()
  {
    return params;
  }

  /**
   * Private version of buildURL that uses dispatch2.jsp. This is for attachments
   * @param the method to be called
   * @param the channel id
   * @returns URL to call method
   */
  private static String myBuildURL (String sMethodName, String sChannelID) {
    StringBuffer sbURL = new StringBuffer ("dispatch2.jsp");
    sbURL.append ("?method=");
    sbURL.append (URLEncoder.encode (sMethodName));
    sbURL.append ("&channelID=");
    sbURL.append (URLEncoder.encode (sChannelID));
    return sbURL.toString ();
  }

  /**
   * Mail summary within portal layout
   * Indicates if there are unread messages and links to
   * check mail and compose a message
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void render (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try {
      HttpSession httpSession = req.getSession (false);

      if (!onetimeSetupDone) {  // Should be done in init() but no access to session there
        if (portalHost == null) {
          portalHost = (String)httpSession.getAttribute("portalhost");
        }
        if (securePortalHost == null) {
          securePortalHost = (String)httpSession.getAttribute("secureportalhost");
        }
        layoutUrl = req.getRequestURI(); // URL path of the dispatch jsp

        /*
         * Register with the servlet environment so that we will be
         * notified when the session is invalidated (when the user logs out, or
         * the session times out).
         */
        httpSession.setAttribute ("CIMAPMail", this);

        onetimeSetupDone = true;
      }

      if (!authenticated) {
        sUser = (String) httpSession.getAttribute (sSessionUsername);
        sPassword = (String) httpSession.getAttribute (sSessionPassword);

        if (sUser != null && sPassword != null) {
          initialize (httpSession);

        } else {
          authenticateUser (req, res, out);
          return;
        }
      }
      if (redirectBack) {  // This should switch us back into http mode from https
        redirectBack = false;
        out.println("<script type=\"text/javascript\">");
        out.println("window.location.href=\"" + portalHost + layoutUrl + "\"");
        out.println("</script>");
        // layout.jsp will call back into render allowing us to complete the call.
        return;
      }

      String requestURI = req.getRequestURI ();
      String portalURI = requestURI.substring (0, requestURI.lastIndexOf ("/") + 1);
      LayoutBean layout = (LayoutBean) httpSession.getAttribute ("layoutBean");
      if (USE_APPLET) {
        out.println("<p><applet code=\"org.jasig.portal.channels.imapmail.MailCheck.class\" codebase=\"applets/\" width=\"200\" height=\"80\"></p>");
        out.println("<param name=\"backgroundColor\" value=\"" + layout.getBackgroundColor(req, res, out) + "\">");
        out.println("<param name=\"foregroundColor\" value=\"" + layout.getForegroundColor(req, res, out) + "\">");
        out.println("<param name=\"checkMailUrl\" value=\"" + portalURI + myBuildURL("checkNewMail", chConfig.getChannelID()) + "\">");
        out.println("<param name=\"composeMailUrl\" value=\"" + portalURI + DispatchBean.buildURL("composeMessage", chConfig) + "\">");
        out.println("<param name=\"readMailUrl\" value=\"" + portalURI + DispatchBean.buildURL("lastMsgPage", chConfig) + "\">");
        out.println("<param name=\"checkTime\" value=\"1\">");
      }
      out.print ("<table align=\"center\">");
      out.println ("<tr><td align=\"center\">You have " + inbox.getUnreadMessageCount () + " unread message(s)</td></tr>");
      out.println ("<tr><td><table align=\"center\" ><tr><td align=\"center\"><a href=\"" + DispatchBean.buildURL ("lastMsgPage", chConfig) + "\">Read</a>");
      out.println ("<td align=\"center\"><a href=\"" + DispatchBean.buildURL ("composeMessage", chConfig) + "\">Compose</a>");
      out.println ("<td align=\"center\"><a href=\"index.jsp\">Check</a></td></tr></table></td></tr>");
      out.println ("</table>");
      if (USE_APPLET) {
        out.println("</applet>");
      }
    } catch (AuthenticationFailedException afe) {
      try {
        out.println ("<Strong>Unable to authenticate with mail server</strong>");
      } catch (IOException ioe) {
      }
      authenticateUser (req, res, out);
      Logger.log (Logger.WARN, "Invalid Username/Password: " + sUser);
    } catch (Exception e) {
      displayErrorMsg (e, false, out);
      Logger.log (Logger.ERROR, "render" + e);
    }
  }

  /** Print start of form html
   * @param the method for Dispatch to call
   * @param the JspWriter object
   */
  private void printFormStart (String method, JspWriter out) throws IOException {
    printFormStart(method, false, null, out, false);
  }
  private void printFormStart (String method, JspWriter out, boolean autoClose) throws IOException {
    printFormStart(method, false, null, out, autoClose);
  }
  private void printFormStart (String method, boolean multipart, String secureHost, JspWriter out) throws IOException {
    printFormStart(method, multipart, secureHost, out, false);
  }
  private void printFormStart (String method, boolean multipart, String secureHost, JspWriter out, boolean autoClose) throws IOException {
    String enctype="";
    String action = "dispatch.jsp";

    if (multipart) {
      enctype = " enctype=\"multipart/form-data\"";
      action = DispatchBean.buildURL (method, chConfig);  // To get by dispatchBean
    }

    if (secureHost != null) {
      action = secureHost + "/" + action;
    }
    String autoCloseForm = "";
    if (autoClose) {
      autoCloseForm = " onSubmit=\"setTimeout('window.close', 5000)\"";
    }
    out.println ("<form action=\"" + action + "\" method=\"post\"" + autoCloseForm + enctype + ">");
    out.println ("<input type=\"hidden\" name=\"method\" value=\"" + method + "\">");
    out.println ("<input type=\"hidden\" name=\"channelID\" value=\"" + chConfig.getChannelID () + "\">");
  }

  /**
   * Present authentication screen
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void authenticateUser (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
    try {
      HttpSession session = req.getSession (false);
      printFormStart ("respondToAuthentication", false, securePortalHost, out, false);
      if (securePortalHost != null && portalHost != null &&
        !portalHost.equalsIgnoreCase(securePortalHost)) {
        redirectBack = true;  // Need to flip back to non-secure later on
      }
      String username = (String) session.getAttribute (sSessionUsername);

      // User input fields
      out.println ("<table border=\"0\" cellspacing=\"2\" cellpadding=\"3\" width=\"100%>\"");
      if (sLoginText != null) {
        out.println (" <tr><td colspan=\"3\"><left>");
        out.println (sLoginText + "<br>");
        out.println ( "</left></td></tr>");
      }
      out.println (" <tr align=\"left\">");
      out.println ("  <td><strong>Username</strong></td>");
      out.println ("  <td><input name=\"username\" value=\"" + (username == null ? "" : username) + "\" size=\"8\"</td>");
      out.println ("  <td width=\"99%\"></td>");
      out.println (" </tr>");
      out.println (" <tr align=\"left\">");
      out.println ("  <td><strong>Password</strong></td>");
      out.println ("  <td><input type=\"password\" name=\"password\" size=\"8\"</td>");
      out.println ("  <td width=\"99%\"></td>");
      out.println (" </tr>");
      out.println (" <tr align=\"left\">");
      out.println ("  <td><input type=\"submit\" name=\"login\" value=\"Login\"></td>");
      out.println ("  <td><input type=\"reset\"></td>");
      out.println ("  <td width=\"99%\"></td>");
      out.println (" </tr>");
      out.println ("</table>");
      out.println ("</form>");

    } catch (Exception e) {
      Logger.log (Logger.ERROR, "authenticateUser" + e);
    }

  }
  /**
   * Authenticate the user with the imap store
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void respondToAuthentication (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
    HttpSession session = req.getSession(false);
    sUser = req.getParameter ("username");
    sPassword = req.getParameter ("password");
    if (sUser != null && sUser.trim ().length () > 0 &&
    sPassword != null || sPassword.trim ().length () > 0) {
      try {
        initialize (session);
        Logger.log(Logger.INFO, "Mailstore authenticated " + sUser);

      } catch (AuthenticationFailedException afe) {
        displayErrorMsg ("Invalid Username/Password", false, out);
        return;
      } catch (Exception e) {
        displayErrorMsg (e, false, out);
        Logger.log (Logger.ERROR, "respondToAuthentication (" + sUser + "): " + e);
        return;
      }
    }
    DispatchBean.finish (req, res); // We're either authenticated or we try this again.
  }

  /**
   * List the folders in the active store
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void listFolders (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try {

      checkIMAPConnection (req, res);

      Thread.yield (); // allow listener to run

      // Navigation bar (top)
      printNavBar (out);

      // Header and title
      printHeader ("Folders", out);

      //print out the folders
      listFoldersInternals (req,res,out);

      // Navigation bar (bottom)
      printNavBar (out);
    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
    } catch (Exception e) {
      Logger.log (Logger.ERROR, "listFolders:" + e);
    }
  }

  private void listFoldersInternals (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    int iTotalMsgs = 0;
    int iTotalNewMsgs = 0;
    try
    {
      printFormStart ("respondToFldrControls", false, null, out);

      // Controls for this page (top)
      printFolderListControls (req, res, out);

      // List folders in active store
      out.println ("<table border=\"0\" cellspacing=\"2\" cellpadding=\"3\" width=\"100%\">");

      // Headers
      out.println ("<tr bgcolor=\"" + sColumnHeadersColor + "\">");
      out.println ("  <td>Select</td>");
      out.println ("  <td>Folder</td>");
      out.println ("  <td>Messages</td>");
      out.println ("  <td>Unread</td>");
      out.println("   <td>&nbsp;</td>");
      out.println ("</tr>");
      // Loop through folders
      for (int iFldr = iFldrsStartAt - 1; iFldr < iFldrsEndAt; iFldr++)
      {
        ImapFolder.FolderData folderData = null;
        try {
          folderData = imapFolders.getFolderData (iFldr);
          if (!folderData.exists) continue;

          out.println ("<tr bgcolor=\"" + sTableCellColor + "\">");

          // Checkbox
          out.println ("<td class=\"PortalMail\" align=\"center\" width=\"45\">");

          String sFolderName = folderData.folderName;

          if (!folderData.specialFolder) {
            out.println ("  <input type=\"checkbox\" name=\"folder\" value=\"" + sFolderName + "\">");
          } else {
            out.println ("&nbsp;");
          }

          out.println ("</td>");

          // Folders

          out.print("   <td class=\"PortalMail\">");
          if (folderData.folderContainer) {
            out.println (URLEncoder.encode (folderData.fullName) + " (holds folders)</td>");
          } else {
            out.println ("<a href=\"" + DispatchBean.buildURL ("setActiveFolder", chConfig) + "&folder=" + URLEncoder.encode (sFolderName) + "\">" + sFolderName + "</a></td>");

            // Messages
            int iMsgCount = folderData.messageCount;
            out.println ("  <td class=\"PortalMail\">" + iMsgCount + "</td>");
            iTotalMsgs += iMsgCount;

            // New Messages
            int iNewMsgCount = folderData.unreadMessageCount;
            out.println ("  <td class=\"PortalMail\">" + iNewMsgCount + "</td>");
            iTotalNewMsgs += iNewMsgCount;

            out.println ("<td class=\"PortalMail\"><a href=\"" + myBuildURL ("downloadFolder", chConfig.getChannelID()) + "&folder=" + URLEncoder.encode (sFolderName) + "\">download</a></td>");

            out.println ("</tr>");
          }
        } catch (MessagingException me) {
          out.println ("<tr>" + me + " -> " + (folderData == null ? "null" : folderData.folderName) + "</tr>");
          Logger.log (Logger.ERROR, "listFolders:" + me);
        }
      }

      // Totals
      out.println ("<tr>");
      out.println ("<td colspan=2 align=right>Total</td>");
      out.println ("<td align=left>" + iTotalMsgs + "</td>");
      out.println ("<td align=left>" + iTotalNewMsgs + "</td>");
      out.println ("</table>");

      // Controls for this page (bottom)
      printFolderListControls (req, res, out);
      out.println ("</form>");
    } catch (IOException e)
    {
      Logger.log (Logger.ERROR, "listFolders:" + e);
    }

  }

  /**
   * Outputs controls for listFolders screen
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void printFolderListControls (HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    out.println ("<table border=\"0\" cellpadding=\"2\" cellspacing=\"3\" width=\"100%\">");
    out.println ("<tr bgcolor=\"" + sControlColor + "\">");
    out.println ("<td>");
    out.println ("<input type=submit name=submit value=\"" + newFolderButtonTxt + "\">");
    out.println ("<input type=submit name=submit value=\"" + deleteFolderButtonTxt + "\">");
    out.println ("<input type=submit name=submit value=\"" + renameFolderButtonTxt + "\">");
    out.println ("<input type=submit name=submit value=\"" + emptyFolderButtonTxt + sTrashName + "\">");
    out.println ("</td>");

    // Pagination controls
    out.println ("<td align=right>");

    if (iFldrsStartAt > iFldrsPerPage + 1)
    out.println ("<a href=\"" + DispatchBean.buildURL ("firstFldrPage", chConfig) + "\">First</a> | ");

    if (iFldrsStartAt > 1)
    out.println ("<a href=\"" + DispatchBean.buildURL ("prevFldrPage", chConfig) + "\">Previous</a> | ");

    out.println ("showing " + iFldrsStartAt + "-" + iFldrsEndAt + " of " + iTotalFldrs);

    if (iFldrsEndAt < iTotalFldrs)
    out.println (" | <a href=\"" + DispatchBean.buildURL ("nextFldrPage", chConfig) + "\">Next</a>");

    if (iFldrsEndAt < iTotalFldrs && iFldrsEndAt + iFldrsPerPage < iTotalFldrs)
    out.println (" | <a href=\"" + DispatchBean.buildURL ("lastFldrPage", chConfig) + "\">Last</a>");

    out.println ("</td>");

    out.println ("</tr>");
    out.println ("</table>");
  }

  /**
   * Goto first page of folders
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void firstFldrPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    iTotalFldrs = getTotalFolders ();
    iFldrsStartAt = 1;

    if (iTotalFldrs > iFldrsPerPage)
    iFldrsEndAt = iFldrsPerPage;
    else
    iFldrsEndAt = iTotalFldrs;

    listFolders (req, res, out);
  }

  /**
   * Goto previous page of folders
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void prevFldrPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    iTotalFldrs = getTotalFolders ();

    if (iFldrsStartAt - iFldrsPerPage > 0)
    {
      iFldrsStartAt -= iFldrsPerPage;
      iFldrsEndAt -= iFldrsPerPage;
    }
    else
    {
      iFldrsStartAt = 1;
      iFldrsEndAt = iFldrsPerPage;
    }

    listFolders (req, res, out);
  }

  /**
   * Goto next page of folders
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void nextFldrPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    iTotalFldrs = getTotalFolders ();

    if (iFldrsEndAt + iFldrsPerPage > iTotalFldrs)
    {
      iFldrsStartAt = iTotalFldrs - iFldrsPerPage + 1;
      iFldrsEndAt = iTotalFldrs;
    }
    else
    {
      iFldrsStartAt += iFldrsPerPage;
      iFldrsEndAt += iFldrsPerPage;
    }

    listFolders (req, res, out);
  }

  /**
   * Goto last page of folders
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void lastFldrPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    iTotalFldrs = getTotalFolders ();
    iFldrsEndAt = iTotalFldrs;

    if (iFldrsEndAt - iFldrsPerPage < 1)
    iFldrsStartAt = 1;
    else
    iFldrsStartAt = iFldrsEndAt - iFldrsPerPage + 1;

    listFolders (req, res, out);
  }

  /**
   * Respond to folder controls
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void respondToFldrControls (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    String sSubmitValue = req.getParameter ("submit");
    try {
      checkIMAPConnection (req, res);

      if (sSubmitValue != null)
      {
        // If the user clicked "New", prompt the user for a new folder name
        if (sSubmitValue.equals (newFolderButtonTxt))
        {
          enterFolderName (req, res, out);
          return;
        } else if (sSubmitValue.equals (renameFolderButtonTxt)) {
          String sFolders[];
          if ( (sFolders = req.getParameterValues ("folder")) == null ||
          sFolders.length == 0) {
            displayErrorMsg ("Please select a folder to be renamed", true, out);
          } else if (sFolders.length > 1) {
            displayErrorMsg ("You can only rename one folder at a time", true, out);
          } else {
            enterFolderName (req, res, out, sFolders[0]);
          }
          return;
        }

        if (activeFolder != null) {
          activeFolder.finalize ();  // Can't manipulate folder if it is open
          activeFolder = null;
        }

        // If the user entered a new folder name, create the folder
        if (sSubmitValue.equals (createFolderButtonTxt))
        {
          String sNewFolderName = req.getParameter ("newFolderName");
          if ( sNewFolderName.length ()==0) {
            displayErrorMsg ("Folder must have a name.", true, out);
            return;
          } else if ( sNewFolderName.indexOf (folderSeparator) > 0) {
            displayErrorMsg (sNewFolderName + " contains an illegal character.", true, out);
            return;
          }
          Folder newFolder = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sNewFolderName);
          newFolder.create (Folder.HOLDS_MESSAGES);
          imapFolders.add (newFolder);
          Thread.yield (); // allow listener to run
        }

        // If the user entered a new folder name, rename the folder
        if (sSubmitValue.equals (renameFolderNameButtonTxt))
        {
          String sOldFolderName = req.getParameter ("oldFolderName");
          String sNewFolderName = req.getParameter ("newFolderName");
          if (sNewFolderName == null || sNewFolderName.trim ().length () == 0) {
            displayErrorMsg ("Please enter a new name for the folder", true, out);
            return;
          }

          Folder oldFolder = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sOldFolderName);
          Folder newFolder = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sNewFolderName);
          try {
            if (oldFolder.renameTo (newFolder)) {
              imapFolders.rename (sOldFolderName, sNewFolderName);
            } else {
              throw new ImapFolderException ("Unable to rename folder '" + sOldFolderName +
              "' to '" + sNewFolderName + "'");
            }
            Thread.yield (); // allow listener to run
          } catch (ImapFolderException sfe) {
            displayErrorMsg(sfe, false, out);
            return;
          } catch (MessagingException me) {
            displayErrorMsg(me, false, out);
            return;
          }
        }

        // If the user clicked "Delete", delete the folder if it is empty
        else if (sSubmitValue.equals (deleteFolderButtonTxt)) {
          String sFolders[];
          if ( (sFolders = req.getParameterValues ("folder")) == null || sFolders.length == 0) {
            displayErrorMsg ("Please select a folder to be deleted.", true, out);
            listFoldersInternals (req,res,out);
            printNavBar (out);

            return;
          }
          for (int i = 0; i < sFolders.length; i++) {
            Folder folder = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sFolders[i]);

            if (folder.getType () == Folder.HOLDS_FOLDERS) {
              int subFolders = folder.list ().length;
              if (subFolders == 0) {
                folder.delete (true);
                imapFolders.deleted (folder.getName ());
              } else {
                displayErrorMsg ("Can't delete folder \"" + sFolders[i] + "\" because it still has " +
                subFolders + " folders in it.<br>", true, out);

                listFoldersInternals (req,res,out);
                printNavBar (out);
                return;
              }
            } else {
              if (folder.getMessageCount () == 0) {
                folder.delete (true);
                imapFolders.deleted (folder.getName ());
                Thread.yield (); // allow listener to run
              } else {
                displayErrorMsg ("Can't delete folder \"" + sFolders[i] + "\" because it still has " +
                folder.getMessageCount () + " messages in it.<br>", true, out);
                listFoldersInternals (req,res,out);
                printNavBar (out);
                return;
              }
            }
          }
        }

        // If the user clicked "Empty Trash", expunge messages in the Trash folder
        else if (sSubmitValue.equals (emptyFolderButtonTxt + sTrashName)) {
          Folder trash = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sTrashName);
          trash.open (Folder.READ_WRITE);
          Message[] msgs = trash.getMessages ();
          Flags flags = new Flags (Flags.Flag.DELETED);
          trash.setFlags (msgs, flags, true);
          trash.close (true);
        }
      }
    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
      return;
    } catch (Exception e) {
      Logger.log (Logger.ERROR, "respondToFldrControls:"+e);
    }

    // I need some logic here to determine what messages to show after a delete
    // Set start and end messages and then call:  listMessages (req, res, out);
    firstFldrPage (req, res, out);
  }

  /**
   * Prompt user for a folder name
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void enterFolderName (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
    enterFolderName (req, res, out, null);
  }
  private void enterFolderName (HttpServletRequest req, HttpServletResponse res, JspWriter out, String oldName)
  {
    try
    {
      // Navigation bar (top)
      printNavBar (out);

      // Header and title
      printHeader ("Folders", out);

      printFormStart ("respondToFldrControls", false, null, out);

      // Prompt for new folder name
      String command;
      if (oldName == null) {
        out.println ("<p>&nbsp;&nbsp;&nbsp;New folder name: ");
        command = createFolderButtonTxt;
      } else {
        out.println ("<p>&nbsp;&nbsp;&nbsp;Rename folder " + oldName + " to: ");
        out.println ("<input type=hidden name=oldFolderName value=\"" + oldName + "\">");
        command = renameFolderNameButtonTxt;
      }
      out.println ("<input type=text name=newFolderName size=30 maxlength=35>");
      out.println ("<input type=submit name=submit value=\""+ command + "\">");
      out.println ("<input type=submit name=submit value=\"Cancel\">");
      out.println ("</form>");

      // Navigation bar (bottom)
      printNavBar (out);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, "enterFolderName:"+ e);
    }
  }

  /**
   * Connects to the store and retrieves the default folder object
   * @return the Folder object representing the default folder
   */
  private Folder getDefaultFolder () throws MessagingException {
    Folder defaultFolder = null;

    if (!store.isConnected ()) {
      throw new MessagingException ("Lost connection to the mail store");
    }

    defaultFolder = store.getDefaultFolder ();

    if (defaultFolder == null) {
      throw new MessagingException ("No default folder");
    }

    return defaultFolder;
  }

  /**
   * Return an ActiveFolder for the desired folder
   * @param folder to access
   * @return opened folder
   */
  private ActiveFolder createActiveFolder (String sFolder) throws MessagingException {
    if (sFolder.equalsIgnoreCase (sMailbox)) {
      return inbox;
    } else {
      return new ActiveFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sFolder);
    }
  }

  /**
   * Connects to the store and retrieves a folder object
   * @param the name of the folder to get
   * @return the Folder object
   */
  private Folder getFolder (String sFolder) throws MessagingException {
    Folder folder = null;

    Folder defaultFolder = getDefaultFolder ();

    if (defaultFolder == null) {
      throw new MessagingException ("No default folder");
    }

    folder = defaultFolder.getFolder (sFolder);

    if (folder == null) {
      throw new MessagingException ("Invalid folder");
    }
    return folder;
  }

  /**
   * Get a string representing the system flags that have been set.
   * This method is included for debugging.
   * @param a message
   * @return a string representing the system flags that have been set
   */
  private String getSystemFlags (Message msg) throws MessagingException
  {
    StringBuffer sbFlags = new StringBuffer  ();

    // Examine ALL system flags for this message
    Flags.Flag[] sf = msg.getFlags ().getSystemFlags ();
    for (int i = 0; i < sf.length; i++)
    {
      if (sf[i] == Flags.Flag.ANSWERED)
      sbFlags.append ("ANSWERED ");
      else if (sf[i] == Flags.Flag.DELETED)
      sbFlags.append ("DELETED ");
      else if (sf[i] == Flags.Flag.DRAFT)
      sbFlags.append ("DRAFT ");
      else if (sf[i] == Flags.Flag.FLAGGED)
      sbFlags.append ("FLAGGED ");
      else if (sf[i] == Flags.Flag.RECENT)
      sbFlags.append ("RECENT ");
      else if (sf[i] == Flags.Flag.SEEN)
      sbFlags.append ("SEEN ");
      else if (sf[i] == Flags.Flag.USER)
      sbFlags.append ("USER ");
    }
    return sbFlags.toString ();
  }

  /**
   * List the messages in the active folder
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void listMessages (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try {
      checkIMAPConnection (req, res);

      Thread.yield (); // allow listener to run
      if (activeFolder == null) {
        displayErrorMsg ("No folder active", false, out);
        return;
      }

      showMetrics = true;
      if (activeFolder.isFiltered() && (iMsgsStartAt < 0 || iMsgsEndAt > activeFolder.getMessageCount())) {
        /**
          * We are probably here due to the back button being pressed
          */
        activeFolder.clearSearch();
        lastMsgPage(req, res, out);
        return;
      }

      String sActiveFolderName = activeFolder.getFolderName ();

      // Navigation bar (top)
      printNavBar (out);

      // Unread message count and title
      printHeader (sActiveFolderName + (activeFolder.isFiltered() ? " (filtered)" : "") + " - <font size=\"3\">" + activeFolder.getUnreadMessageCount () + " unread message(s)</font>", out);

      printFormStart ("respondToMsgControls", false, null, out);
      if (activeFolder.getMessageCount () > 0)
      {
        // Controls for this page (top)
        printMessageListControls (true, req, res, out);

        // List messages in active folder
        out.println ("<table border=\"0\" cellspacing=\"2\" cellpadding=\"3\" width=\"100%\">");

        // Headers
        out.println ("<tr bgcolor=\"" + sColumnHeadersColor + "\">");
        out.println ("  <td>Select</td>");
        out.println("<td>&nbsp;</td>"); // Replied to column
        printMsgColumnTitle ("from", "From", msgSortOrder == ORDER_BY_FROM_ASCENDING || msgSortOrder == ORDER_BY_FROM_DESCENDING, out);
        printMsgColumnTitle ("subject", "Subject", msgSortOrder == ORDER_BY_SUBJECT_ASCENDING || msgSortOrder == ORDER_BY_SUBJECT_DESCENDING, out);
        printMsgColumnTitle ("date", "Time/Date", msgSortOrder == ORDER_BY_DATE_ASCENDING || msgSortOrder == ORDER_BY_DATE_DESCENDING, out);
        printMsgColumnTitle ("size", "Size", msgSortOrder == ORDER_BY_SIZE_ASCENDING || msgSortOrder == ORDER_BY_SIZE_DESCENDING, out);
        out.println ("</tr>");

        activeFolder.fetchHeaders (iMsgsStartAt - 1, iMsgsEndAt); // Preload headers
        // Loop through messages
        for (int iMsg = iMsgsStartAt - 1; iMsg < iMsgsEndAt; iMsg++)
        {
          Message msg = activeFolder.getMessage (iMsg);
          if (msg.isSet (Flags.Flag.DELETED)) {
            continue; // Don't show deleted messages
          }
          out.println ("<tr bgcolor=\"" + (msg.isSet (Flags.Flag.SEEN) ? sSeenMsgColor : sUnseenMsgColor) + "\">");

          // Checkbox
          out.println ("  <td align=\"center\" width=\"45\"><input type=\"checkbox\" name=\"msg\" value=\"" + iMsg + "\">");

          if (DEBUG) out.println ("#" + msg.getMessageNumber () + " " + getSystemFlags (msg));
          out.println ("  </td>");

          //  Replied
          out.print("  <td>");
          if (msg.isSet (Flags.Flag.ANSWERED)) {
            out.print("R");
          } else {
            out.print("&nbsp;");
          }
          out.println("  </td>");

          // From
          Address[] addresses;
          out.println ("<td class=\"PortalMail\">");

          if ( (addresses = msg.getFrom ()) != null)
          {
            for (int iAddr = 0; iAddr < addresses.length;  iAddr++)
            {
              InternetAddress ia = (InternetAddress) addresses[iAddr];
              String sPersonal = ia.getPersonal ();
              String sAddress = ia.getAddress ();

              if (sPersonal != null)
              out.println (sPersonal + " &lt;" + sAddress + "&gt;<br>");
              else
              out.println (sAddress + "<br>");
            }
          }

          out.println ("</td>");

          // Subject
          String sSubject = msg.getSubject ();
          if (sSubject == null || sSubject.trim ().length () == 0) {
            sSubject = "[none]";
          } else {
            sSubject = HTMLescape (sSubject);
          }
          out.print ("  <td class=\"PortalMail\">");
          if (sActiveFolderName.equals (sDraftsName)) {
            out.print ("  <a href=\"" + DispatchBean.buildURL ("composeMessage", chConfig) + "&action=draft&msg=" + iMsg + "\">" + sSubject + "</a>");
          } else {
            out.print ("  <a href=\"" + DispatchBean.buildURL ("readMessage", chConfig) + "&msg=" + iMsg + "\">" + sSubject + "</a>");
          }
          out.println ("  </td>");

          // Time/Date
          Date date = msg.getSentDate ();
          out.println ("  <td nowrap class=\"PortalMail\">" + (date != null ? date.toString () : "Unknown") + "</td>");

          // Size
          int iSize = msg.getSize ();
          out.println ("  <td class=\"PortalMail\">" + formatSize (iSize) + "</td>");

          out.println ("</tr>");
        }

        out.println ("</table>");

        // Controls for this page (bottom)
        printMessageListControls (false, req, res, out);
      }
      else
      {
        printMessageListControls (true, req, res, out);
        out.println ("<p align=center><strong><em>This folder is empty.</em></strong><p>");
      }
      out.println ("</form>");

      // Navigation bar (bottom)
      printNavBar (out);
    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
    } catch (FolderClosedException fce) {
      displayErrorMsg (fce, false, out);
    } catch (Exception e) {
      displayErrorMsg (e, false, out);
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * User configurations
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void setUps (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try {
      printFormStart("respondToSetupControls", out, true);
      out.println("<input type=\"checkbox\" name=\"fullheaders\" " +
        (showFullHeaders ? "checked" : "") + "> Show full headers<br>");
      out.println("<input type=\"submit\" name=\"submit\" value=\"Configure\">");
      out.println("</form>");
    } catch (IOException ioe) {
      Logger.log(Logger.WARN, ioe);
    }
  }

  /**
   * Handle user configuration requests
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void respondToSetupControls (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    String submitValue = req.getParameter ("submit");
    if (submitValue != null) {
      String fullHeaders = req.getParameter ("fullheaders");
      if (fullHeaders != null) {
        showFullHeaders = fullHeaders.equals("on");
      } else {
        showFullHeaders = false;
      }
    }
    try {
      out.println("Please close this window and refresh/reload the current page to see the effect of your changes.");
    } catch (IOException ioe) {
      Logger.log(Logger.ERROR, ioe);
    }
  }

  /**
   * Outputs controls for listMessages screen
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void printMessageListControls (boolean top, HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    try
    {
      out.println ("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">");
      out.println (" <tr bgcolor=\"" + sControlColor + "\">");
      out.println ("  <td>");
      out.println ("   <table>");
      out.println ("    <tr>");

      out.println ("     <td align=\"left\"><input type=\"submit\" name=\"submit\" value=\""+ checkNewMailButtonTxt + "\"></td>");
      if (activeFolder.isFiltered()) {
        out.println ("     <td align=\"left\"><input type=\"submit\" name=\"submit\" value=\"" + clearSearchMessagesButtonTxt + "\"></td>");
      } else {
        out.println ("     <td align=\"left\"><input type=\"submit\" name=\"submit\" value=\"" + searchMessagesButtonTxt + "\"></td>");
      }
      if (activeFolder.getMessageCount () > 0) {
        out.println ("     <td nowrap align=\"left\">");
        out.println ("      <table border=\"1\" cellpadding=\"0\" cellspacing=\"0\">");
        out.println ("       <tr nowrap>");
        out.println ("        <td align=\"left\">");
        out.println ("           <table border=\"0\" cellpadding=\"1\" cellspacing=\"1\"><tr>");
        out.println ("            <td align=\"left\"><input type=\"submit\" name=\"submit\" value=\"" + deleteMessagesButtonTxt + "\"></td>");
        out.println ("            <td>");
        out.println ("             <table border=\"1\" cellpadding=\"0\" cellspacing=\"0\">");
        out.println ("              <tr><td>");
        out.println ("                <table border=\"0\" cellpadding=\"0\" cellspacing=\"1\"><tr>");
        out.println ("               <td align=\"left\">");
        out.println ("                <input type=\"submit\" name=\"submit\" value=\"" + moveMessagesButtonTxt + "\">");
        out.println ("               </td>");
        out.println ("               <td align=\"left\">");
        // Drop down box of folder names
        if (top) {  //the control for the top of the page
          out.println ("                <select name=\"destinationFolder1\">");
        } else {   //control for the bottom of the page
          out.println ("                <select name=\"destinationFolder2\">");
        }
        out.println   ("                 <option value=\"" + "" + "\">- Choose Folder -");
        ImapFolder.FolderData[] folders = imapFolders.getFolderNames ();
        for (int iFldr = 0; iFldr < folders.length; iFldr++) {
          if (!folders[iFldr].exists) continue;
          if (!activeFolder.getFolderName ().equalsIgnoreCase (folders[iFldr].folderName)) {
            out.println ("                 <option value=\"" + folders[iFldr].folderName + "\">" + folders[iFldr].folderName);
          }
        }
        out.println ("                </select>");
        out.println ("               </td>");
        out.println ("               </tr></table>");
        out.println ("              </td></tr>");
        out.println ("             </table>");
        out.println ("            </td>");
        out.println ("            <td><input type=\"checkbox\" name=\"AllMessages\"> All messages</td>");
        out.println ("           </tr></table>");
        out.println ("          </td></tr>");
        out.println ("       </tr>");
        out.println ("      </table>");
        out.println ("     </td>");
      }
      out.println ("    </tr>");
      out.println ("   </table>");
      out.println ("  </td>");
      out.println (" </tr>");

      if (activeFolder.getMessageCount () > 0) {
        // Pagination controls
        out.println (" <tr>");
        out.println ("  <td align=left>");

        if (iMsgsStartAt > iMsgsPerPage + 1) {
          out.println ("   <a href=\"" + DispatchBean.buildURL ("firstMsgPage", chConfig) + "\">First</a> | ");
        }

        if (iMsgsStartAt > 1) {
          out.println ("   <a href=\"" + DispatchBean.buildURL ("prevMsgPage", chConfig) + "\">Previous</a> | ");
        }

        out.println ("showing " + iMsgsStartAt + "-" + iMsgsEndAt + " of " + activeFolder.getMessageCount ());

        if (iMsgsEndAt < activeFolder.getMessageCount ()) {
          out.println (" | <a href=\"" + DispatchBean.buildURL ("nextMsgPage", chConfig) + "\">Next</a>");
        }

        if (iMsgsEndAt < activeFolder.getMessageCount () && iMsgsEndAt + iMsgsPerPage < activeFolder.getMessageCount ()) {
          out.println (" | <a href=\"" + DispatchBean.buildURL ("lastMsgPage", chConfig) + "\">Last</a>");
        }

        out.println ("  </td>");

        out.println (" </tr>");
      }
      out.println ("</table>");
    } catch (Exception e) {
      Logger.log (Logger.ERROR, "printMessageListControls:"+e);
    }
  }

  /**
   * Goto first page of messages
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void firstMsgPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    int totalMsgs;

    totalMsgs = activeFolder.getMessageCount ();
    iMsgsStartAt = 1;

    if (totalMsgs > iMsgsPerPage)
    iMsgsEndAt = iMsgsPerPage;
    else
    iMsgsEndAt = totalMsgs;

    listMessages (req, res, out);
  }

  /**
   * Goto previous page of messages
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void prevMsgPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    int totalMsgs;

    totalMsgs = activeFolder.getMessageCount ();

    if (iMsgsStartAt - iMsgsPerPage > 0)
    {
      iMsgsStartAt -= iMsgsPerPage;
      iMsgsEndAt -= iMsgsPerPage;
    }
    else
    {
      iMsgsStartAt = 1;
      iMsgsEndAt = iMsgsPerPage;
    }

    listMessages (req, res, out);
  }

  /**
   * Goto next page of messages
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void nextMsgPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    int totalMsgs;

    totalMsgs = activeFolder.getMessageCount ();

    if (iMsgsEndAt + iMsgsPerPage > totalMsgs)
    {
      iMsgsStartAt = totalMsgs - iMsgsPerPage + 1;
      iMsgsEndAt = totalMsgs;
    }
    else
    {
      iMsgsStartAt += iMsgsPerPage;
      iMsgsEndAt += iMsgsPerPage;
    }

    listMessages (req, res, out);
  }

  /**
   * Set the active folder
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void setActiveFolder (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    String sMailbox;
    if (!authenticated) {
      render (req, res, out);
      return;
    }

    if ( (sMailbox = req.getParameter ("folder")) != null) {
      try {
        checkIMAPConnection (req, res);

        if (activeFolder != null) {
          try {
            activeFolder.finalize (); // Free resources used for current folder
          } catch (FolderClosedException fce) {
            displayErrorMsg(fce, false, out);
            return;
          }
        }

        activeFolder = createActiveFolder (sMailbox);
        if (activeFolder == null) {
          displayErrorMsg ("Lost connection to mail server", false, out);
          return;
        }
      } catch (CIMAPLostConnectionException lce) {
        // Call to lastMsgPage below is what we want
      } catch (FolderClosedException fce) {
        displayErrorMsg(fce, false, out);
        return;
      } catch (Exception e) {
        displayErrorMsg(e, false, out);
        Logger.log (Logger.ERROR, "setActiveFolder:" + e);
        return;
      }
    }

    lastMsgPage (req, res, out);
  }

  /**
   * Goto last page of messages
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void lastMsgPage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    if (!authenticated) {
      try {
        res.sendRedirect (DispatchBean.buildURL ("render", chConfig));
      } catch (IOException ie) {
        displayErrorMsg(ie, true, out);
        Logger.log (Logger.ERROR, "setActiveFolder:"+ie);
        return;
      }
    }

    try {
      checkIMAPConnection (req, res);
      if (activeFolder==null) activeFolder=inbox;
      iMsgsEndAt = activeFolder.getMessageCount ();

      if (iMsgsEndAt - iMsgsPerPage < 1) {
        iMsgsStartAt = 1;
      } else {
        iMsgsStartAt = iMsgsEndAt - iMsgsPerPage + 1;
      }

      listMessages (req, res, out);
    } catch (CIMAPLostConnectionException lce) {
      try {
        res.sendRedirect (DispatchBean.buildURL ("render", chConfig));
      } catch (IOException ie) {
        Logger.log (Logger.ERROR, ie);
      }
    } catch (AuthenticationFailedException afe) {
      displayErrorMsg (afe, true, out);
    } catch (Exception e) {
      Logger.log (Logger.ERROR, e);
      displayErrorMsg (e, true, out);
    }
  }

  /**
   * Search, delete or move messages
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void respondToMsgControls (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    String sSubmitValue = req.getParameter ("submit");
    String allMessages = req.getParameter("AllMessages");
    // If the user clicked "Delete", move selected messages to wastebasket
    if (sSubmitValue != null) {
      try {
        checkIMAPConnection (req, res);

        if (sSubmitValue.equals (checkNewMailButtonTxt)) {
          // Does nothing. The real work will be done in printHeader()

        } else if (sSubmitValue.equals (searchMessagesButtonTxt)) {
          // Search the current folder for specific messages
          printSearchMsgForm(out);
          return;

        } else if (sSubmitValue.equals (clearSearchMessagesButtonTxt)) {
          activeFolder.clearSearch();
          lastMsgPage(req, res, out);
          return;

        } else if (sSubmitValue.equals (searchFolderButtonTxt)) {
          String criteriaText = req.getParameter ("criteriatext");
          String criteria = req.getParameter ("criteria");
          if (criteriaText.trim().length() == 0) {
            displayErrorMsg("Please specify the search text", false, out);
            printSearchMsgForm(out);
            return;

          } else if (criteria == null) {
            displayErrorMsg("Please specify the search criteria", false, out);
            printSearchMsgForm(out);
            return;
          }

          SearchTerm term;
          if (criteria.equals("Sender")) {
            term = new FromStringTerm(criteriaText);
          } else if (criteria.equals("Subject")) {
            term = new SubjectTerm(criteriaText);
          } else {
            displayErrorMsg(criteria + " is an unrecognized search item", true, out);
            return;
          }
          activeFolder.search(term);

        } else if (sSubmitValue.equals (deleteMessagesButtonTxt) ||
          sSubmitValue.equals (moveMessagesButtonTxt)) {

          String destinationFolder = sTrashName;
          if (sSubmitValue.equals (moveMessagesButtonTxt)) {
            if ( (destinationFolder = req.getParameter ("destinationFolder1")).trim ().equals ("") &&
             (destinationFolder = req.getParameter ("destinationFolder2")).trim ().equals ("")) {
              displayErrorMsg ("Missing destination folder", true, out);
              return;
            }
          }

          if (allMessages == null || !allMessages.equalsIgnoreCase("on")) {
            String sMsgs[];
            if ( (sMsgs = req.getParameterValues ("msg")) == null ||
              sMsgs.length == 0) {
              displayErrorMsg ("Please select the message(s) to be " + sSubmitValue + "d", true, out);
              return;
            }
            Collection cMsgs = new ArrayList (sMsgs.length);
            for (int i = 0; i < sMsgs.length; i++) {
              try {
                int msg = Integer.parseInt (sMsgs[i]);
                Message deletedMessage = activeFolder.getMessage (msg);
                cMsgs.add (deletedMessage);
              } catch (Exception e) {
                Logger.log (Logger.ERROR, "respondToMsgControls: Unable to get message " +
                  sMsgs[i] + "in " + activeFolder.getFolderName() +  ": " + e);
              }
            }

            if (cMsgs.size() > 0) {
              Message[] msgs = (Message[]) cMsgs.toArray (new Message[0]);
              activeFolder.removeMsg (msgs, destinationFolder);
              if (sSubmitValue.equals (moveMessagesButtonTxt)) {
                messagesMoved += msgs.length;
              } else {
                messagesDeleted += msgs.length;
              }
            }

            if (cMsgs.size() != sMsgs.length) {
              displayErrorMsg("Unable to delete some or all of your messages", false, out);
            }
          } else { // asked for all messages
            activeFolder.removeMsg (destinationFolder);
          }
        }

        /**
         * We've done something to the folder so fix what we will show next
         */
        adjustFolderPosition();

      } catch (CIMAPLostConnectionException lce) {
        lastMsgPage (req, res, out);
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        displayErrorMsg ("Message no longer exists.", true, out);
      } catch (NullPointerException npe) {
        displayErrorMsg ("Message no longer exists.", true, out);
      } catch (Exception e) {
        displayErrorMsg (e, true, out);
        Logger.log (Logger.ERROR, "respondToMsgControls:" + e);
        Logger.log (Logger.DEBUG, e);
      }
    } else {
      Logger.log(Logger.ERROR, "respondToMsgControls called without submit value!!");
    }

    listMessages (req, res, out);
  }

  /**
   * Outputs a navigation bar containing links to
   * Inbox, Folders, Compose, Address, Set-ups, and Help
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void printNavBar (JspWriter out)
  {
    try {
      out.println ("<table border=\"0\" cellpadding=\"2\" cellspacing=\"3\" width=\"100%\">");
      out.println ("<tr bgcolor=\"" + sNavBarColor + "\">");
      out.println ("<td><a href=\"" + DispatchBean.buildURL ("setActiveFolder", chConfig) + "&folder=" + sMailbox + "\">Inbox</a></td>");
      out.println ("<td><a href=\"" + DispatchBean.buildURL ("firstFldrPage", chConfig) + "\">Folders</a></td>");
      out.println ("<td><a href=\"" + DispatchBean.buildURL ("composeMessage", chConfig) + "\">Compose</a></td>");
      out.println ("<td>Address</td>");
      out.println ("<td><a href=\"JavaScript:openWin('" + DispatchBean.buildURL ("setUps", chConfig) + "', 'detachWindow', 500, 500)\">Set-ups</a></td>");
      out.println ("<td><a href=\"JavaScript:openWin('/help/hlp_email.htm', 'detachWindow', 500, 500)\">Help</a></td>");
      out.println ("</tr>");
      out.println ("</table>");
    }
    catch (IOException e)
    {
      Logger.log (Logger.ERROR,"JSPWRITER ERROR:" + e.toString ());
    }
  }

  /**
   * Gets total folders in store
   * @param the name of the folder
   * @return the number of messages in sFolder
   */
  private int getTotalFolders ()
  {
    return (imapFolders == null ? 0 : imapFolders.size ());
  }

  /** is this a special folder
   * @param Foldername to check
   */
  private static final boolean isSpecialFolder (String folderName) {
    for (int i = 0; i < sSpecialFolderNames.length; i++) {
      if (folderName.equalsIgnoreCase (sSpecialFolderNames[i])) {
        return true;
      }
    }
    return false;
  }

  /** Returns a HTML safe version of the text
   * @param String to present
   * @return a HTML safe version
   */
  static String[][] htmlCharacters= new String[][] {
  {"<", "&lt;"},
  {">", "&gt;"},
  {"&", "&amp;"},
  {"'", "&#039;"},
  {"\"", "&quot;"},
  };

  private static final String replaceHTMLcharacter (String character) {
    for (int i = 0; i < htmlCharacters.length; i++) {
      if (character.equals (htmlCharacters[i][0])) {
        return htmlCharacters[i][1];
      }
    }
    return character;
  }

  private static final String HTMLescape (String text) {
    StringBuffer newText = new StringBuffer (text.length ());
    for (int i = 0; i < text.length (); i++) {
      String charAt = text.substring (i, i+1);
      newText.append (replaceHTMLcharacter (charAt));
    }
    return newText.toString ();
  }

  /**
   * Returns a formatted String representing the size of the message
   * @param the size of the message in bytes
   * @return a formatted String representing the size of the message
   */
  private static String formatSize (int iSize)
  {
    String sSize = null;

    if (iSize < 1024) {
      sSize = iSize + "";
    } else if (iSize < 1024 * 1024) {
      sSize = iSize/1024 + "K";
    } else {
      sSize = iSize/ (1024*1024) + "M";
    }

    return sSize;
  }

  /**
   * Read a message
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void readMessage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try {

      checkIMAPConnection (req, res);

      // Get message
      int iMsg = Integer.parseInt (req.getParameter ("msg"));
      Message msg = activeFolder.openMessage (iMsg);
      MessageParts msgParts = new MessageParts (msg);

      // Navigation bar (top)
      printNavBar (out);

      // Header and title
      printHeader ("Read", out);

      printFormStart ("respondToReadMsgControls", false, null, out);
      out.println ("<input type=\"hidden\" name=\"msg\" value=\"" + iMsg + "\">");

      // Message Controls (top)
      printReadMsgControls (req, res, out);

      // 2 cell table containing message headers in left cell and attachments in right
      out.println ("<table><tr valign=\"top\"><td>");

      // Message headers
      if (showFullHeaders) {
        Enumeration headers = msg.getAllHeaders();
        while (headers.hasMoreElements ()) {
          Header header = (Header)headers.nextElement ();
          out.println(header.getName() + ": " + header.getValue() + "<br>");
        }
      } else {
        String sFrom = "&nbsp;" + msgParts.getFrom ();
        String sTo = "&nbsp;" + msgParts.getTo ();
        String sCc = "&nbsp;" + msgParts.getCc ();

        String sSubject = "&nbsp;" + HTMLescape(msgParts.getSubject ());
        String sDate = "&nbsp;" + msgParts.getDate ();

        out.println ("<table>");
        out.println ("<tr><td align=\"right\">From:</td><td>" + sFrom + "</td></tr>");
        out.println ("<tr><td align=\"right\">To:</td><td>" + sTo + "</td></tr>");
        out.println ("<tr><td align=\"right\">Cc:</td><td>" + sCc + "</td></tr>");
        out.println ("<tr><td align=\"right\">Subject:</td><td>" + sSubject + "</td></tr>");
        out.println ("<tr><td align=\"right\">Date:</td><td>" + sDate + "</td></tr>");
        out.println ("</table>");
      }

      out.println ("</td>");

      out.println ("<td width=\"10%\"></td>");

      // Attachments
      out.println ("<td valign=\"top\" width=\"40%\">");

      // Message body
      Part textPart = msgParts.getBodyText ();
      Part[] attachments = msgParts.getAttachments ();
      if (attachments.length > 0) {
        out.println ("<Strong>Attachments:</strong><br>");
        for (int i = 0; i < attachments.length; i++) {
          String attachmentName = attachmentName(attachments[i], i);
          out.println ("<A HREF=\"" + myBuildURL("respondToReadAttachment", chConfig.getChannelID()) +
          "&msg=" + iMsg + "&attachment=" + i + "\" target=\"_blank\">" +
           attachmentName + "</A>");
        }
      }

      out.println ("</td></tr></table><HR/>");

      if (textPart == null) {
        out.println ("<STRONG>Message has no displayable text</STRONG>");
      } else if (textPart.isMimeType ("text/html")) {
        out.println (textPart.getContent ());
      } else {
        BufferedReader in = new BufferedReader (new InputStreamReader (textPart.getInputStream ()));

        try {
          String line;
          while ( (line = in.readLine ()) != null) {
            out.println (HTMLescape(line) + "<br>");
          }
        } finally {
          in.close ();
        }
      }

      // Message Controls (bottom)
      printReadMsgControls (req, res, out);
      out.println ("</form>");

      // Navigation bar (bottom)
      printNavBar (out);
      messagesRead++;

    } catch (IndexOutOfBoundsException oiibe) {
      /**
       * Probably tried to use the back button on a searched folder
       */
      activeFolder.clearSearch();
      lastMsgPage (req, res, out);
    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
    } catch (FolderClosedException fce) {
      displayErrorMsg (fce, true, out);
    } catch (Exception e) {
      displayErrorMsg (e, true, out);
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * Outputs controls for read message screen
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void printReadMsgControls (HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    out.println ("<table border=\"0\" cellpadding=\"2\" cellspacing=\"3\" width=\"100%\">");
    out.println ("<tr bgcolor=\"" + sControlColor + "\">");
    out.println ("<td>");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"" + replyMessageButtonTxt + "\">");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"" + replyAllMessageButtonTxt + "\">");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"" + forwardMessageButtonTxt  + "\">");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"" + deleteMessageButtonTxt + "\">");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"" + returnToMessageButtonTxt + activeFolder.getFolderName () +"\">");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"" + nextMessageButtonTxt + "\">");
    out.println ("</td>");
    out.println("</tr>");
    out.println ("<tr bgcolor=\"" + sControlColor + "\">");

    // Pagination controls
    /*
    out.println ("<td align=right>");

    if (iFldrsStartAt > 1)
    out.println ("<a href=\"" + DispatchBean.buildURL ("prevFldrPage", chConfig) + "\">Previous</a> | ");

    out.println ("showing " + iFldrsStartAt + "-" + iFldrsEndAt + " of " + iTotalFldrs);

    if (iFldrsEndAt < iTotalFldrs)
    out.println (" | <a href=\"" + DispatchBean.buildURL ("nextFldrPage", chConfig) + "\">Next</a>");

    out.println ("</td>");
     */

    out.println ("</tr>");
    out.println ("</table>");
  }

  /**
   * Print search form
   * @param the JspWriter object
   */
  private void printSearchMsgForm (JspWriter out) throws IOException
  {

    // Navigation bar (top)
    printNavBar (out);

    printFormStart("respondToMsgControls", out);
    out.println("<table border=\"0\">");
    out.println("<tr><td>Criteria <input size=\"30\" name=\"criteriatext\"></td></tr>");
    out.println("<tr><td><input type=\"radio\" name=\"criteria\" value=\"Sender\">Sender</td></tr>");
    out.println("<tr><td><input type=\"radio\" name=\"criteria\" value=\"Subject\">Subject</td></tr>");
    out.println("<tr><td><input type=\"submit\" name=\"submit\" value=\"Search Folder\"></td></tr>");
    out.println("</table>");
    out.println("</form>");

    // Navigation bar (bottom)
    printNavBar (out);
  }

  /**
   * Respond to read message controls
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void respondToReadMsgControls (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try {
      checkIMAPConnection (req, res);

      String sSubmitValue = req.getParameter ("submit");

      if (sSubmitValue != null)
      {
        int iMsg = Integer.parseInt (req.getParameter ("msg"));
        // If the user clicked "Reply", goto compose screen using this message as a draft
        if (sSubmitValue.equals (replyMessageButtonTxt)) {
          res.sendRedirect (DispatchBean.buildURL ("composeMessage", chConfig) + "&action=reply&msg=" + iMsg);

          // If the user clicked "Reply All", goto compose screen using this message as a draft
        } else if (sSubmitValue.equals (replyAllMessageButtonTxt)) {
          res.sendRedirect (DispatchBean.buildURL ("composeMessage", chConfig) + "&action=replyAll&msg=" + iMsg);

          // If the user clicked "Forward", goto compose screen using this message as a draft
        } else if (sSubmitValue.equals (forwardMessageButtonTxt)) {
          res.sendRedirect (DispatchBean.buildURL ("composeMessage", chConfig) + "&action=forward&msg=" + iMsg);

          // If the user clicked "Delete", delete the message
        } else if (sSubmitValue.equals (deleteMessageButtonTxt)) {
          Message[] deletedMsgs = new Message[1];
          deletedMsgs[0] = activeFolder.getMessage (iMsg);
          if (activeFolder.getFolderName ().equals (this.sTrashName)) {
            //if we are in the trash, then delete that one message
            activeFolder.deleteMessage(deletedMsgs);
          } else {
            // Copy message to trash folder
            activeFolder.removeMsg (deletedMsgs, sTrashName);
          }

          adjustFolderPosition();

          // Go to the next message in the folder
          if (iMsg >= activeFolder.getMessageCount ()) {
            listMessages (req, res, out);
          } else {
            res.sendRedirect (DispatchBean.buildURL ("readMessage", chConfig) + "&msg=" + iMsg);
          }
        } else if (sSubmitValue.startsWith (returnToMessageButtonTxt)) {
          listMessages (req, res, out);

          // Step to the next message in folder
        } else if (sSubmitValue.equals (nextMessageButtonTxt)) {
          iMsg++;
          if (iMsg >= activeFolder.getMessageCount ()) {
            listMessages (req, res, out);
          } else {
            res.sendRedirect (DispatchBean.buildURL ("readMessage", chConfig) + "&msg=" + iMsg);
          }
        }
        return;
      }
    } catch (IndexOutOfBoundsException oiibe) {
      /**
       * Probably tried to use the back button on a searched folder
       */
      activeFolder.clearSearch();
      lastMsgPage (req, res, out);
    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
    } catch (Exception e) {
      Logger.log (Logger.ERROR, e);
      try {
        out.println ("Unable to find/display attachment");
      } catch (IOException ie) {
        Logger.log (Logger.ERROR, ie);
      }
    }
  }
  /**
   * Respond to read message attachment url
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void respondToReadAttachment (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try {
      checkIMAPConnection (req, res);

      int iMsg = Integer.parseInt (req.getParameter ("msg"));
      String index = req.getParameter ("attachment");
      if (index == null) {
        displayErrorMsg ("No attachment selected", true, out);
      } else {
        int attachmentIndex = Integer.parseInt (index);
        Message msg = activeFolder.openMessage (iMsg);
        MessageParts msgParts = new MessageParts (msg);
        Part attachment = msgParts.getAttachments ()[attachmentIndex];

        String contentType = attachment.getContentType ();
        String attachmentName = attachmentName(attachment, attachmentIndex);

        res.addHeader("Accept-Ranges", "bytes");
        res.addHeader("Content-Disposition", "inline; filename=\"" + attachmentName + "\"");
        res.addHeader("Cache-Control", "private");   // Have to override (possible) no-cache directives
        int colonPos = contentType.indexOf (";");
        if (colonPos > 0) {
          res.setContentType (contentType.substring (0, colonPos).toLowerCase ());
        } else {
          res.setContentType (contentType.toLowerCase ());
        }
        Date sentDate = msg.getSentDate();
        if (sentDate != null) {
          res.addHeader("Last-Modified", httpDateFormat.format(sentDate));
        }

        OutputStream browser = res.getOutputStream();
        InputStream in = attachment.getInputStream ();
        try {
          int bufferSize = 8192;
          byte[] buff = new byte[bufferSize];
          int bytesRead;

          while ((bytesRead = in.read (buff, 0, bufferSize)) != -1) {
            browser.write(buff, 0, bytesRead);
          }
        } catch (IOException ioe) {
          /* Browser probably closed the connection */
        } finally {
          in.close();
          browser.close();  // We've sent everything that we want to
        }
        attachmentsViewed++;
      }
    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
    } catch (Exception e) {
      Logger.log (Logger.ERROR, e);
      try {
        out.println ("Unable to find/display attachment");
      } catch (IOException ie) {
        Logger.log (Logger.ERROR, "respondToReadAttachment:"+ie);
      }
    }
  }

  /**
   * Deduce you say, the mime type
   * @param Body part to examine
   * @result Mime type string
   */
  private String mimeType(Part attachment) throws MessagingException {
    String mimeType = "";
    String contentType;

    if (attachment.isMimeType("text/plain")) {
      mimeType = "txt";
    } else if (attachment.isMimeType("text/html")) {
      mimeType = "htm";
    } else if ((contentType = attachment.getContentType()) != null) {
      int pos = contentType.indexOf("/");
      int endPos = contentType.indexOf(";");
      if (endPos > pos) {
        mimeType = contentType.substring(pos+1, endPos).toLowerCase();
      } else {
        mimeType = contentType.substring(pos+1).toLowerCase();
      }
    }

    return "." + mimeType;
  }
  /**
   * Deduce what name we should give to an attachment
   * @param The attachment to inspect
   * @returns an attachment name
   */
  private String attachmentName (Part attachment, int index) {
    String attachmentName = null;
    try {
      attachmentName = attachment.getFileName ();
      String contentType = null;

      if (attachmentName == null) {
        contentType = attachment.getContentType ();
        if (contentType != null) {
          int pos = contentType.indexOf("name=");
          if (pos > 0) {
            int endPos = contentType.indexOf(";", pos + 5);
            if (endPos > pos) {
                attachmentName = contentType.substring(pos+5, endPos);
            } else {
              attachmentName = contentType.substring(pos+5);
            }
          }
        }
      }

      if (attachmentName == null) {
        String disposition = attachment.getDisposition ();
        if (disposition != null) {
          int pos = disposition.indexOf("filename=");
          if (pos > 0) {
            int endPos = disposition.indexOf(";", pos + 9);
            if (endPos > pos) {
              attachmentName = disposition.substring(pos+9, endPos);
            } else {
              attachmentName = disposition.substring(pos+9);
            }
          }
        }
      }

      if (attachmentName == null) {
        attachmentName = "attachment" + index + mimeType(attachment);
      }

      if (attachmentName != null && attachmentName.indexOf(".") < 0) {
        attachmentName = attachmentName + mimeType(attachment);
      }
    } catch (MessagingException me) {
      attachmentName = "attachment" + index ;
    }

    return attachmentName;
  }

  /**
   * Download the contents of a folder as a zip file
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void downloadFolder (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    if (!authenticated) {
      render (req, res, out);
      return;
    }

    String folderName;
    if ( (folderName = req.getParameter ("folder")) != null) {
      try {
        checkIMAPConnection (req, res);

        Folder folder;
        if (folderName.equalsIgnoreCase (sMailbox)) {
          folder = inbox.folder;
        } else {
          folder = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + folderName);
          folder.open(Folder.READ_ONLY);
          if (folder == null) {
            displayErrorMsg ("Lost connection to mail server", false, out);
            return;
          }
        }
        Message messages[] = folder.getMessages();

        res.addHeader("Accept-Ranges", "bytes");
        res.addHeader("Content-Disposition", "inline; filename=\"" + folderName + ".zip\"");
        res.addHeader("Cache-Control", "private");   // Have to override (possible) no-cache directives
        res.setContentType("application/zip");
        res.addHeader("Last-Modified", httpDateFormat.format(new Date()));

        ZipOutputStream zout = new ZipOutputStream(res.getOutputStream());
        zout.setLevel(Deflater.BEST_COMPRESSION);
        zout.setMethod(zout.DEFLATED);
        StringBuffer pad = new StringBuffer();
        int padLength = (folder.getMessageCount() + "").length();
        for (int i = padLength; i > 0; i--) {
          pad.append("0");
        }
        for (int i = 1; i <= messages.length; i++) {
          MessageParts msgParts = new MessageParts (messages[i-1], true);
          int indexLength = (i + "").length();
          String messageName = (pad.toString() + i).substring(indexLength) + " " +
            msgParts.getSubject().replace('/', '_');

          Part[] attachments = msgParts.getAttachments ();
          Part bodyText = msgParts.getBodyText();
          ZipEntry zpart = null;
          String fileExtension = ".txt";
          String htmlBreak = "";

          if (bodyText.isMimeType("text/html")) {
            fileExtension = ".htm";
            htmlBreak = "<br/>";
          }
          zpart = new ZipEntry(folderName + "/" + messageName + fileExtension);
          Date fileDate = messages[i-1].getSentDate();
          if (fileDate == null) {
            fileDate = new Date();
          }
          zpart.setTime(fileDate.getTime());
          zout.putNextEntry(zpart);
          try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            Message msg = msgParts.getMsg();
            Enumeration headers = msg.getAllHeaders();
            while (headers.hasMoreElements ()) {
              Header header = (Header)headers.nextElement ();
              printWriter.print(header.getName() + ": " + header.getValue() + htmlBreak + crlf);
            }
            printWriter.print(htmlBreak + crlf);
            printWriter.flush();
            printWriter.close();
            zout.write(stringWriter.getBuffer().toString().getBytes());
            if (bodyText != null) {
              InputStream in = bodyText.getInputStream ();
              try {
                int bytes;
                byte buffer[] = new byte[8192];
                while ( (bytes = in.read (buffer, 0, 8192)) > -1) {
                  zout.write (buffer, 0, bytes);
                }
              } finally {
                in.close();
              }
            }
          } catch (Exception e) {
            Logger.log(Logger.ERROR, e);
          }
          zout.closeEntry();

          for (int j = 0; j < attachments.length; j++) {
            String partName = attachmentName(attachments[j], j);
            zpart = new ZipEntry(folderName + "/" + messageName + "/" + partName);
            zout.putNextEntry(zpart);
            try {
              InputStream in = attachments[j].getInputStream ();
              try {
                int bytes;
                byte buffer[] = new byte[8192];
                while ( (bytes = in.read (buffer, 0, 8192)) > -1) {
                  zout.write (buffer, 0, bytes);
                }
              } finally {
                in.close();
              }
            } catch (Exception e) {
              Logger.log(Logger.ERROR, e);
            }
            zout.closeEntry();
          }
        }
        zout.flush();
        zout.finish();
        zout.close();

        messagesDownloaded += messages.length;
        if (!folderName.equalsIgnoreCase (sMailbox)) {
          folder.close(false);
        }

      } catch (CIMAPLostConnectionException lce) {
        // Call to lastMsgPage below is what we want
      } catch (FolderClosedException fce) {
        displayErrorMsg(fce, false, out);
        return;
      } catch (Exception e) {
        displayErrorMsg(e, false, out);
        Logger.log (Logger.ERROR, e);
        return;
      }
    }
  }

  /**
   * Compose a message
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void composeMessage (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      if (!authenticated) {
        try {
          res.sendRedirect (DispatchBean.buildURL ("render", chConfig));
        } catch (IOException ie) {
          displayErrorMsg(ie, true, out);
          Logger.log (Logger.ERROR, "composeMessage:"+ie);
          return;
        }
      }
      checkIMAPConnection (req, res);

      showMetrics = true;
      // Might be coming from forward or reply...
      Message prevMsg = null;
      MessageParts msgParts = null;
      String sAction = req.getParameter ("action");
      String sTo = "";
      String sCc = "";
      String sBcc = "";
      String sSubject = "";
      String sIncludedMsgText = "";
      boolean repliedTo = false;

      if (sAction != null) {
        prevMsg = activeFolder.getMessage (Integer.parseInt (req.getParameter ("msg")));
        msgParts = new MessageParts (prevMsg);
        includeAttachments  = null;

        if (sAction.equals ("forward"))
        {
          sSubject = "Fwd: " + HTMLescape (msgParts.getSubject ());
          sIncludedMsgText = getIncludedMessageText (msgParts);
          includeAttachments = msgParts.getAttachments ();
        }
        else if (sAction.equals ("reply"))
        {
          repliedTo = true;
          sTo = msgParts.getFrom ();
          sSubject = "Re: " + HTMLescape (msgParts.getSubject ());
          sIncludedMsgText = getIncludedMessageText (msgParts);
        }
        else if (sAction.equals ("replyAll"))
        {
          repliedTo = true;
          sTo = msgParts.getFrom () + ", " + msgParts.getTo ();
          sCc = msgParts.getCc ();
          sSubject = "Re: " + HTMLescape (msgParts.getSubject ());
          sIncludedMsgText = getIncludedMessageText (msgParts);
        }
        else if (sAction.equals ("draft"))
        {
          sTo = msgParts.getTo ();
          sCc = msgParts.getCc ();
          sBcc = msgParts.getBcc ();
          sSubject = HTMLescape (msgParts.getSubject ());
          sIncludedMsgText = getIncludedMessageText (msgParts, true);
          includeAttachments = msgParts.getAttachments ();
        }
      }

      // Navigation bar (top)
      printNavBar (out);

      // Header and title
      printHeader ("Compose", out);

      printFormStart ("respondToComposeMsgControls", true, null, out);
      if (repliedTo) {
        out.println("<input type=\"hidden\" name=\"replymsg\" value=\"" + req.getParameter ("msg") + "\"");
      }
      // Message Controls (top)
      printComposeMsgControls (sAction != null, req, res, out);

      // 2 cell table containing message headers in left cell and address book links in right
      out.println ("<table><tr><td>");

      // Message headers
      out.println ("<table>");
      out.println ("<tr><td align=right>To: </td><td><input type=text name=\"to\" value=\"" + sTo + "\" size=\"40\"></td></tr>");
      out.println ("<tr><td align=right>Cc: </td><td><input type=text name=\"cc\" value=\"" + sCc + "\" size=\"40\"></td></tr>");
      out.println ("<tr><td align=right>Bcc: </td><td><input type=text name=\"bcc\" value=\"" + sBcc + "\" size=\"40\"></td></tr>");
      out.println ("<tr><td align=right>Subj: </td><td><input type=text name=\"subject\" value=\"" + sSubject + "\" size=\"40\"></td></tr>");
      out.println ("</table>");

      out.println ("</td><td align=\"center\">");

      // Directory
      out.println ("Directory Service<br>");
      out.println ("My Address Book<br>");
      out.println ("A-F G-L M-R S-Z<br>");
      out.println ("All Group<br>");
      out.println ("</td>");

      out.println ("</tr></table>");

      // Message body
      out.println ("<table>");
      out.println ("<tr>");
      out.println ("<td><textarea name=\"body\" rows=\"15\" cols=\"72\" wrap=\"virtual\">");
      out.println (sIncludedMsgText); // If coming from forward or reply
      out.println ("</textarea></td>");

      // Attachments
      out.println ("<td valign=\"top\">");
      out.println ("<Strong>Attachments:</Strong><br>");
      for (int i = 0; i < MAX_ATTACHMENTS; i++) {
        if (includeAttachments != null && i < includeAttachments.length) {
          out.println (includeAttachments[i].getFileName () + "<br>");
        } else {
          out.println ("<input type=\"file\" name=\"bodypart" + i + "\"><br>");
        }
      }
      out.println ("</td>");

      out.println ("</table>");

      // Message Controls (bottom)
      printComposeMsgControls (sAction != null, req, res, out);
      out.println ("</form>");

      // Navigation bar (bottom)
      printNavBar (out);

    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
    } catch (Exception e) {
      Logger.log (Logger.ERROR, "composeMessage:"+e);
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * Print a Message listing column title
   * @param the html text
   * @param the column header
   * @param If the column header should stand out
   * @param the JspWriter object
   */
  private void printMsgColumnTitle (String sortField, String caption, boolean bold, JspWriter out) throws IOException {
    out.print ("  <td><a href=\"" + DispatchBean.buildURL ("sortList", chConfig) + "&list=msgs&sortBy=" + sortField + "\">");
    if (bold) {
      out.print ("<strong>");
    }
    out.print (caption);
    if (bold) {
      out.print ("</strong>");
    }
    out.println ("</a></td>");
  }

  /**
   * Outputs controls for compose message screen
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void printComposeMsgControls (boolean needFolderControl, HttpServletRequest req, HttpServletResponse res, JspWriter out) throws IOException
  {
    out.println ("<table border=\"0\" cellpadding=\"2\" cellspacing=\"3\" width=\"100%\">");
    out.println ("<tr bgcolor=\"" + sControlColor + "\">");
    out.println ("<td>");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"Send\">");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"Save as draft\">");
    out.println ("<input type=\"submit\" name=\"submit\" value=\"Cancel\">");
    if (needFolderControl) {
      out.println ("<input type=\"submit\" name=\"submit\" value=\"Return to folder\">");
    }
    out.println ("</td>");
    out.println ("</tr>");
    out.println ("</table>");
  }

  /**
   * Respond to compose message controls
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void respondToComposeMsgControls (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    ArrayList attachments = new ArrayList(MAX_ATTACHMENTS);
    HashMap parameters = new HashMap();

    try {
      checkIMAPConnection (req, res);

      /**
       * You only get one chance to process the request stream so
       * cache everything now and process the request later on
       */
      MultipartParser multi = new MultipartParser (req, iMaxMessageSize, true, true);
      com.oreilly.servlet.multipart.Part attachmentPart;
      while ((attachmentPart = multi.readNextPart()) != null) {
        String partName = attachmentPart.getName();
        if (attachmentPart.isParam()) {
          ParamPart parameterPart = (ParamPart) attachmentPart;
          String paramValue = parameterPart.getStringValue();
          parameters.put(partName, paramValue);

        } else if (attachmentPart.isFile()) {
          FilePart filePart = (FilePart) attachmentPart;
          String filename = filePart.getFileName();
          if (filename != null) {
            MimeBodyPart bp = new MimeBodyPart ();
            bp.setDisposition (Part.ATTACHMENT);
            bp.setFileName (filename);
            bp.setDataHandler(new DataHandler(new MultipartDataSource(filePart)));
            attachments.add(bp);
          }
        }
      }

      String sSubmitValue = (String) parameters.get("submit");
      if (sSubmitValue != null) {
        // If the user clicked "Return to folder"
        if (sSubmitValue.equals ("Return to folder")) {
          listMessages (req, res, out);
        } else {
          printNavBar (out);

          // If the user clicked "Send", send the message
          if (sSubmitValue.equals ("Send")) {
            // Send the message

            Message msg = constructMessage (parameters, attachments);
            Transport.send (msg);
            messagesSent++;

            // Save copy in sent folder
            msg.setSentDate (new Date ());
            Message[] msgs = new Message[1];
            msgs[0] = msg;
            Folder sent = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sSentName);

            // Create Sent Items folder if it doesn't already exist
            if (!sent.exists ()) {
              sent.create (Folder.HOLDS_MESSAGES);
              imapFolders.add (sent);
            }

            sent.open (Folder.READ_WRITE);
            sent.appendMessages (msgs);
            sent.close (false);

            /**
             * If we are replying to a message, set the replied to flag for
             * that message
             */
            String replyMsg = (String) parameters.get("replymsg");
            if (replyMsg != null) {
              int iReplyMsg = Integer.parseInt(replyMsg);
              Message[] oldMsg = new Message[1];
              oldMsg[0] = activeFolder.getMessage(iReplyMsg);
              Flags flags = new Flags (Flags.Flag.ANSWERED);
              flags.add (Flags.Flag.SEEN);
              activeFolder.folder.setFlags (oldMsg, flags, true);
            }

            out.println ("<p>&nbsp;&nbsp;Your mail <strong>" + HTMLescape (msg.getSubject ()) + "</strong> has been sent.  A copy was saved in your " + sSentName + " folder.</p>");
          }

          // If the user clicked "Save as draft", construct message and save it in drafts folder
          else if (sSubmitValue.equals ("Save as draft")) {
            Message msg = constructMessage (parameters, attachments);

            msg.setSentDate (new Date ());
            Message[] msgs = new Message[1];
            msgs[0] = msg;
            if (activeFolder != null &&
              activeFolder.getFolderName ().equals (sDraftsName)) {
              activeFolder.folder.appendMessages (msgs);
              activeFolder.folder.expunge ();       // Remove old copies of draft
            } else {
              Folder drafts = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sDraftsName);

              // Create Drafts folder if it doesn't already exist
              if (!drafts.exists ()) {
                drafts.create (Folder.HOLDS_MESSAGES);
                imapFolders.add (drafts);
              }

              drafts.open (Folder.READ_WRITE);
              Flags flags = new Flags (Flags.Flag.DRAFT);
              msg.setFlags (flags, true);
              drafts.appendMessages (msgs);
              drafts.close (true);
            }

            out.println ("<p>&nbsp;&nbsp;Your mail <strong>" + HTMLescape ((String)parameters.get("subject")) + "</strong> has been saved in the " + sDraftsName + " folder.</p>");
          }

          // If the user clicked "Cancel",
          else if (sSubmitValue.equals ("Cancel")) {
            out.println ("<p>&nbsp;&nbsp;Message cancelled.  Your message has <strong>not</strong> been sent.</p>");
          }

          printNavBar (out);
        }
      }
    } catch (CIMAPLostConnectionException lce) {
      lastMsgPage (req, res, out);
    } catch (MessagingException me) {
      Logger.log (Logger.ERROR, me);
      displayErrorMsg (me, true, out);
    } catch (Exception e) {
      displayErrorMsg (e, true, out);
      Logger.log (Logger.ERROR, e);
    } finally {
      attachments.clear();
      parameters.clear();
    }

    return;
  }

  /**
   * Clean up a recipient list. Basicaaly this involves replacing ";" (Outlook people)
   * with "," (JavaMail).
   * @param receipients string
   * @result clean Internet Address object
   */
  private InternetAddress[] cleanAddressList(String recipients) throws AddressException {
    StringBuffer list = new StringBuffer(recipients);
    while (true) {
      try {
        return InternetAddress.parse(list.toString());
      } catch (AddressException ae) {
        int charPos = ae.getPos();
        if (charPos > 0 && list.substring(charPos, charPos + 1).equals(";")) {
          list.replace(charPos, charPos + 1, ",");
        } else {
          throw ae;
        }
      }
    }
  }

  /**
   * Construct a message object from compose inputs
   * @param the HashMap of parameters
   * @param ArrayList of attachments as MimeBodyParts
   * @returns assembled Message object
   */
  private Message constructMessage (HashMap parameters, ArrayList attachments) throws IOException, MessagingException
  {
    String sTo = (String) parameters.get("to");
    String sCc = (String) parameters.get("cc");
    String sBcc = (String) parameters.get("bcc");
    String sSubject = (String) parameters.get("subject");
    String sBody = (String) parameters.get("body");

    Message msg = new MimeMessage (session);
    msg.setFrom (new InternetAddress (sUserEmail, sFirstName + " " + sLastName));

    if (sTo != null && sTo.length () > 0)
    {
      msg.setRecipients (Message.RecipientType.TO, cleanAddressList(sTo));
    }

    if (sCc != null && sCc.length () > 0)
    {
      msg.setRecipients (Message.RecipientType.CC, cleanAddressList(sCc));
    }

    if (sBcc != null && sBcc.length () > 0)
    {
      msg.setRecipients (Message.RecipientType.BCC, cleanAddressList(sBcc));
    }

    msg.setSubject (sSubject != null ? sSubject : "[none]");

    msg.addHeader ("X-Mailer", "uPortal WEB email client " + clientVersion);
    msg.addHeader ("Organization", sOrganization);

    if (attachments.size () > 0 ||
     (includeAttachments != null && includeAttachments.length > 0)) {
      MimeMultipart mp = new MimeMultipart ();

      // First add text of message
      if (sBody != null && sBody.trim ().length () > 0) {
        MimeBodyPart body = new MimeBodyPart ();
        body.setDisposition (Part.INLINE);
        body.setContent (sBody, "text/plain");
        mp.addBodyPart (body);
      }

      if (includeAttachments != null) { // Attachments from draft message
        for (int i = 0; i < includeAttachments.length; i++) {
          MimeBodyPart bp = new MimeBodyPart ();
          bp.setDisposition (Part.ATTACHMENT);
          bp.setDataHandler (includeAttachments[i].getDataHandler ());
          bp.setFileName (includeAttachments[i].getFileName ());
          mp.addBodyPart (bp);
        }
        includeAttachments = null;
      }

      for (int i = 0; i < attachments.size (); i++) {
        MimeBodyPart bp  = (MimeBodyPart) attachments.get (i);
        mp.addBodyPart (bp);
      }
      msg.setContent (mp);
    } else {
      msg.setContent (sBody, "text/plain");
    }
    msg.saveChanges ();

    return msg;
  }

  /**
   * Format message text by appending headers and '>' to each line
   * @param the message being forwarded or replied to
   * @return message text
   */
  private String getIncludedMessageText (MessageParts msgParts) throws IOException, MessagingException{
    return getIncludedMessageText(msgParts, false);
  }
  /**
   * Format message text by appending headers and '>' to each line
   * @param the message being forwarded or replied to
   * @param Whether message is a draft message
   * @return message text
   */
  private String getIncludedMessageText (MessageParts msgParts, boolean bIsDraft) throws IOException, MessagingException
  {
    Part bodyText = msgParts.getBodyText ();
    StringBuffer sbMsgText = new StringBuffer ();

    // Return empty string if message is not of type TEXT/PLAIN
    if (bodyText == null || !bodyText.isMimeType ("text/plain")) {
      return "";
    } else if (bIsDraft) {
      BufferedReader in = new BufferedReader (new InputStreamReader (bodyText.getInputStream ()));
      try {
        String line;
        while ( (line = in.readLine ()) != null) {
          sbMsgText.append (line);
        }
      } finally {
        in.close ();
      }
    } else {
      // Return message text with ">"
      sbMsgText.append ("-----Original Message-----\n");
      if (showFullHeaders) {
        Message msg = msgParts.getMsg();
        Enumeration headers = msg.getAllHeaders();
        while (headers.hasMoreElements ()) {
          Header header = (Header)headers.nextElement ();
          sbMsgText.append ("> " + header.getName() + ": " + header.getValue() + "\n");
        }
      }

      sbMsgText.append ("\n");
      if (!showFullHeaders) {
        sbMsgText.append ("> Date: " + msgParts.getDate () + "\n");
        sbMsgText.append ("> From: " + msgParts.getFrom () + "\n");
        sbMsgText.append ("> Subject: " + msgParts.getSubject () + "\n");
        sbMsgText.append ("> To: " + msgParts.getTo () + "\n");
      }
      sbMsgText.append (">\n");

      BufferedReader in = new BufferedReader (new InputStreamReader (bodyText.getInputStream ()));
      try {
        String line;
        while ( (line = in.readLine ()) != null) {
          sbMsgText.append ("> " + line);
        }
      } finally {
        in.close ();
      }
    }

    return sbMsgText.toString ();
  }

  /**
   * correct the boundry markers of which messages we are to display
   */
  private void adjustFolderPosition() {
    int totalMsgs = activeFolder.getMessageCount ();
    if (iMsgsStartAt + iMsgsPerPage >= totalMsgs) { // need to adjust last page position
      iMsgsStartAt = totalMsgs - iMsgsPerPage + 1;
      if (iMsgsStartAt < 1) {
        iMsgsStartAt = 1;
      }
      iMsgsEndAt = totalMsgs;
    }
  }

  /**
   * Present error message to the user
   * @param Exception object
   * @param Whether we should print navigation bar
   * @param JspWriter object
   */
  private void displayErrorMsg (Exception e, boolean navigationBar, JspWriter out) {
    displayErrorMsg (new String[] {e.getMessage ()}, navigationBar, out);
  }

  /**
   * Present error message to the user
   * @param Error message to display
   * @param Whether we should print navigation bar
   * @param JspWriter object
   */
  private void displayErrorMsg (String errorMsg, boolean navigationBar, JspWriter out) {
    displayErrorMsg (new String[] {errorMsg}, navigationBar, out);
  }
  /**
   * Present error message to the user
   * @param Array of error messages to display
   * @param Whether we should print navigation bar
   * @param JspWriter object
   */
  private void displayErrorMsg (String[] errorMsg, boolean navigationBar, JspWriter out) {
    try {
      if (navigationBar) {
        // Navigation bar (top)
        printNavBar (out);
      }

      out.println ("<H2>Error:</H2>");
      for (int i = 0; i < errorMsg.length; i++) {
        out.println ("<STRONG>" + errorMsg[i] + "</STRONG><br>");
      }

    } catch (Exception e) {
      Logger.log (Logger.ERROR, "displayErrorMsg:"+e);
    }
  }

  /**
   * Print the client header
   * @param the client name
   * @param the JspWriter object
   */
  private void printHeader (String sTitle, JspWriter out) throws IOException, MessagingException
  {
    // Header and title
    out.println ("<table border=\"0\" cellspacing=\"2\" cellpadding=\"3\" width=\"100%\">");
    out.println ("<tr bgcolor=\"" + sHeaderColor + "\">");
    out.println ("<td><font size=\"4\">" + sTitle + "</font></td>");
    if (inbox != null && inbox.mailCheck ()) {
      out.println ("<td><font size=\"4\">You have new mail</font></td>");
    }
    out.println ("<td align=\"right\"><font size=\"4\">" + sCaption  + "</font>" + " " + clientVersion + "</td>");
    out.println ("</tr>");
    out.println ("</table>");
  }

  /** Initialize our world
   * @param the session object
   */
  private void initialize (HttpSession httpSession) throws Exception, AuthenticationFailedException {
    if (sUser == null || sPassword == null) {
      throw new AuthenticationFailedException ("Invalid Username/Password");
    }

    try {
      urlName = new URLName (sProtocol, sIMAPHost, iIMAPPort, sMailbox, sUser, sPassword);
      session = Session.getDefaultInstance (props, null);
      store = session.getStore (urlName);
      store.addStoreListener (storeListener);
      store.connect ();
      sUserEmail = sUser + "@" + sDomainName;

      if (inbox == null) {
        inbox = new ActiveFolder (sMailbox);
        inbox.setDontClose (true); // Keep open for duration of channel
      }

      folderSeparator = inbox.folder.getSeparator ();

      activeFolder = inbox;

      if (imapFolders == null) {
        imapFolders = new ImapFolder (1); // At least inbox
        imapFolders.add (inbox);

        Folder folderDir;
        if (sFolderDir == null) {
          folderDir = inbox.folder;
        } else {
          folderDir = getFolder (sFolderDir);
          if (!folderDir.exists ()) {
            folderDir.create (Folder.HOLDS_FOLDERS);
          }
        }

        Folder[] storeFolders = folderDir.list ();
        if (storeFolders != null) {
          imapFolders.ensureCapacity (storeFolders.length + 1);
          for (int i = 0; i < storeFolders.length; i++) {
            if (storeFolders[i].exists ()) {
              imapFolders.add (storeFolders[i]);
            }
          }
        }

        store.addFolderListener (folderListener);
        authenticated = true;
      }
    } catch (Exception e) {
      cleanup ();
      throw e;
    }

    iMsgsStartAt = 1;
    iMsgsEndAt = iMsgsPerPage;
    iTotalFldrs = 0;
    iFldrsStartAt = 1;
    iFldrsEndAt = iFldrsPerPage;
    msgSortOrder = ORDER_BY_DATE_ASCENDING;
    adjustFolderPosition();
  }

  /** Clean up in preparation to shut down
   */
  private void cleanup () {
    try {
      store.removeStoreListener (storeListener);
      store.removeFolderListener (folderListener);
    } catch (Exception e) { /* ignore */ }

    includeAttachments = null;

    if (imapFolders != null) {
      try {
        imapFolders.finalize ();
      } catch (Exception e) { /* ignore */ }
      imapFolders = null;
    }

    if (activeFolder != null) {
      try {
        activeFolder.finalize ();
      } catch (Exception me) { /* ignore */ }
      activeFolder = null;
    }

    if (inbox != null) {
      inbox.setDontClose (false);
      try {
        inbox.finalize ();
      } catch (Exception me) { /* ignore */}
      inbox = null;
    }

    authenticated = false;
    try {
      store.close ();
    } catch (Exception me) { /* ignore */ }
  }

  /**
   * Attempt to restore our state with the imap server
   * @param the servlet request object
   */
  private void reconnect (HttpServletRequest req) throws Exception, AuthenticationFailedException {
    cleanup ();
    initialize (req.getSession (false));
  }

  /**
   * See if we still have our IMAP connection
   * @param the servlet request object
   * @param the servelet response object
   */
  private void checkIMAPConnection (HttpServletRequest req, HttpServletResponse res) throws CIMAPLostConnectionException, AuthenticationFailedException {
    Thread.yield ();
    try {
      if (inbox != null) {
        int a = inbox.folder.getUnreadMessageCount ();
      } else {
        store.close();
      }
    } catch (MessagingException me) {
      try {
        store.close ();
      } catch (MessagingException me2) {
        //
      }
    }
    if (!store.isConnected ()) {
      Logger.log(Logger.DEBUG, "Lost connection to store, re-initializing.");

      try {
        reconnect (req);
        throw new CIMAPLostConnectionException ();
      } catch (Exception e) {
        if (e instanceof CIMAPLostConnectionException) {
          throw (CIMAPLostConnectionException)e;
        }
        Logger.log (Logger.ERROR, "checkIMAPConnection:"+e);
        DispatchBean.finish (req, res);   // Can't re-initialize up, go back to portal
        return;
      }
    }
  }

  /**
   * logout this imap session
   * @param the servlet request object
   * @param the servlet response object
   */
  public void logout (HttpServletRequest req, HttpServletResponse res) {
    HttpSession httpSession = req.getSession (false);
    httpSession.removeAttribute ("CIMAPMail"); // Causes cleanup to be called
  }

  public void finish (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    DispatchBean.finish (req, res);
  }

  public void edit (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel is not editable
  }

  public void help (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    // This channel has no help
  }

  /**
   * see if there is new mail in inbox. Invoked by the mailcheck applet
   * @param the servlet request object
   * @param the servlet response object
   * @param the jspwriter object
   */
  public void checkNewMail (HttpServletRequest req, HttpServletResponse res, JspWriter out) {
    try {
      res.setContentType ("text/html");
      PrintWriter myOut = res.getWriter ();

      if (inbox == null) {
        myOut.println ("<mailstate>Connection to mail server unavailable</mailstate>");
      } else {
        try {
          boolean newMsgs = inbox.mailCheck ();

          int count = inbox.getUnreadMessageCount ();

          myOut.println ("<mailstate>You have " + count + " unread message" + (count == 1 ? "" : "s") +  ":" +
           (newMsgs ? "1" : "0") + "</mailstate>");
        } catch (FolderClosedException fce) {
          myOut.println ("<mailstate>Connection to mail server unavailable</mailstate>");
        } catch (MessagingException me) {
          myOut.println ("<mailstate>Connection to mail server unavailable</mailstate>");
        }
      }
      myOut.close ();
    } catch (Exception e) {
      Logger.log (Logger.ERROR, "checkIMAPConnection:"+e);
    }
  }

  /**
   * sort a folder
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void sortList (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    String sList = req.getParameter ("list");
    String sSortBy = req.getParameter ("sortBy");

    if (sList != null && sSortBy != null)
    {
      if (sSortBy.equals ("from")) {
        if (msgSortOrder == ORDER_BY_FROM_ASCENDING) {
          msgSortOrder = ORDER_BY_FROM_DESCENDING;
        } else {
          msgSortOrder = ORDER_BY_FROM_ASCENDING;
        }
      } else if (sSortBy.equals ("subject")) {
        if (msgSortOrder == ORDER_BY_SUBJECT_ASCENDING) {
          msgSortOrder = ORDER_BY_SUBJECT_DESCENDING;
        } else {
          msgSortOrder = ORDER_BY_SUBJECT_ASCENDING;
        }
      } else if (sSortBy.equals ("date")) {
        if (msgSortOrder == ORDER_BY_DATE_ASCENDING) {
          msgSortOrder = ORDER_BY_DATE_DESCENDING;
        } else {
          msgSortOrder = ORDER_BY_DATE_ASCENDING;
        }
      } else if (sSortBy.equals ("size")) {
        if (msgSortOrder == ORDER_BY_SIZE_ASCENDING) {
          msgSortOrder = ORDER_BY_SIZE_DESCENDING;
        } else {
          msgSortOrder = ORDER_BY_SIZE_ASCENDING;
        }
      }

      if (sList.equals ("msgs")) {
        activeFolder.sort (msgSortOrder);
        listMessages (req, res, out);
      } else if (sList.equals ("folders")) {
        listFolders (req, res, out);
      }
    }
  }

  static final private int ORDER_BY_FROM (Object o1, Object o2, int order)
  {
    try
    {
      Message m1 = (Message) o1;
      Message m2 = (Message) o2;

      Address[] addrs1 = m1.getFrom ();
      Address[] addrs2 = m2.getFrom ();

      String sPersonal1 = ( (InternetAddress) addrs1[0]).getPersonal ();
      String sPersonal2 = ( (InternetAddress) addrs2[0]).getPersonal ();
      String sAddress1 = ( (InternetAddress) addrs1[0]).getAddress ();
      String sAddress2 = ( (InternetAddress) addrs2[0]).getAddress ();
      String sFrom1 = (sPersonal1 != null ? sPersonal1 : "") + sAddress1;
      String sFrom2 = (sPersonal2 != null ? sPersonal2 : "") + sAddress2;

      return sFrom1.compareTo (sFrom2) * order;
    }
    catch (Exception e)
    {
      Logger.log (Logger.WARN, e);
    }
    return 0;
  };

  static private final Comparator ORDER_BY_FROM_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_FROM (o1, o2, 1);
    }
  };

  static private final Comparator ORDER_BY_FROM_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_FROM (o1, o2, -1);
    }
  };

  static final private int ORDER_BY_SUBJECT (Object o1, Object o2, int order)
  {
    try
    {
      String subject1 = ( (Message) o1).getSubject ();
      String subject2 = ( (Message) o2).getSubject ();
      if (subject1 == null || subject2 == null) {
        return -1;
      }
      return subject1.compareToIgnoreCase (subject2) * order;
    }
    catch (Exception e)
    {
      Logger.log (Logger.WARN, e);
    }
    return 0;
  };

  static private final Comparator ORDER_BY_SUBJECT_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SUBJECT (o1, o2, 1);
    }
  };

  static private final Comparator ORDER_BY_SUBJECT_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SUBJECT (o1, o2, -1);
    }
  };

  static private final int ORDER_BY_DATE (Object o1, Object o2, int order)
  {
    try
    {
      Date d1 = ( (Message) o1).getSentDate ();
      Date d2 = ( (Message) o2).getSentDate ();
      if (d1 == null || d2 == null) {
        return -1;
      } else {
        return d1.compareTo (d2) * order;
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.WARN, e);
    }
    return 0;
  };

  static private final Comparator ORDER_BY_DATE_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_DATE (o1, o2, 1);
    }
  };

  static private final Comparator ORDER_BY_DATE_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_DATE (o1, o2, -1);
    }
  };

  static private final int ORDER_BY_SIZE (Object o1, Object o2, int order)
  {
    try
    {
      Message m1 = (Message) o1;
      Message m2 = (Message) o2;
      Integer m1size = new Integer (m1.getSize ());
      Integer m2size = new Integer (m2.getSize ());
      return m1size.compareTo (m2size) * order;
    }
    catch (Exception e)
    {
      Logger.log (Logger.WARN, e);
    }
    return 0;
  };

  static private final Comparator ORDER_BY_SIZE_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SIZE (o1, o2, 1);
    }
  };

  static private final Comparator ORDER_BY_SIZE_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SIZE (o1, o2, -1);
    }
  };

  static private final int ORDER_BY_NAME (Object o1, Object o2, int order)
  {
    try
    {
      Folder f1 = (Folder) o1;
      Folder f2 = (Folder) o2;
      return f1.getFullName ().compareToIgnoreCase (f2.getFullName ()) * order;
    }
    catch (Exception e)
    {
      Logger.log (Logger.WARN, e);
    }
    return 0;
  };

  static private final Comparator ORDER_BY_NAME_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_NAME (o1, o2, 1);
    }
  };

  static private final Comparator ORDER_BY_NAME_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_NAME (o1, o2, -1);
    }
  };


  static private final Comparator ORDER_BY_FOLDERNAME = new Comparator () {
    public int compare (Object o1, Object o2) {
      try {
        String f1 = (String) o1;
        String f2 = (String) o2;
        return f1.compareToIgnoreCase (f2);
      } catch (Exception e) {
        Logger.log (Logger.WARN, e);
      }
      return 0;
    }
  };

  /**
   * the ImapFolder class is used to manage the list of folders that we have
   */
  private class ImapFolder extends Vector {
    public class FolderData {
      boolean exists;
      boolean specialFolder;
      boolean folderContainer;
      String fullName;
      String folderName;
      int messageCount;
      int unreadMessageCount;
    }
    private List specialFolders = new ArrayList ();
    private List normalFolders = new ArrayList ();

    public ImapFolder () {
      super ();
    }

    public ImapFolder (int initialSize) {
      super (initialSize);
    }

    public void finalize () {
      specialFolders.clear ();
      specialFolders = null;
      normalFolders.clear ();
      normalFolders = null;
      clear ();
    }

    public void add (String folderName) throws ImapFolderException {
      addFolder (folderName);
    }

    public void add (ActiveFolder folder) throws MessagingException {
      addFolder (folder.folder.getName ());
    }

    public void add (Folder folder) throws MessagingException {
      addFolder (folder.getName ());
      folder.close (false); // Don't need it any more
    }

    private void addFolder (String folderName) {
      if (duplicate (folderName)) { // Already been seen
        return;
      }

      if (isSpecialFolder (folderName)) {
        specialFolders.add (folderName);
      } else {
        normalFolders.add (folderName);
      }

      rebuildFolderList ();
    }

    /**
     * Create the "public" of folders that we show
     */
    private void rebuildFolderList () {
      clear ();
      addAll (specialFolders);
      if (normalFolders.size () > 0) {
        Collections.sort (normalFolders, ORDER_BY_FOLDERNAME);
        addAll (normalFolders);
      }
    }

    /**
     * Check if this folder is already in the list
     * @param folder name to check
     * @result duplicate folder
     */
    private boolean duplicate (String folderName) {
      for (int i = 0; i < specialFolders.size (); i++) {
        String existingFolder = (String)specialFolders.get (i);
        if (existingFolder.equals (folderName)) {
          return true;
        }
      }

      for (int i = 0; i < normalFolders.size (); i++) {
        String existingFolder = (String)normalFolders.get (i);
        if (existingFolder.equals (folderName)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Return the list of folders
     * @result folder list
     */
    public FolderData[] getFolderNames () throws MessagingException {
      ArrayList folders = new ArrayList (this.size ());
      for (int i = 0; i < this.size (); i++ ) {
        FolderData folderData = getFolderData (i);
        folders.add (folderData);
      }
      return (FolderData[])folders.toArray (new FolderData[0]);
    }

    /**
     * Return the data for a specific folder
     * @param array index of folder
     * @result FolderData object
     */
    public FolderData getFolderData (int index) throws MessagingException {
      Thread.yield (); // Allow listener to run

      FolderData folderData = new FolderData ();
      String folderName = (String) super.get (index);

      Folder folder;
      if (folderName.equalsIgnoreCase (inbox.getFolderName ())) {
        folder = inbox.folder;
      } else {
        folder = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator)+folderName);
      }
      folderData.exists = folder.exists ();
      folderData.folderName = folder.getName ();
      folderData.specialFolder = isSpecialFolder (folderData.folderName);
      folderData.fullName = folder.getFullName ();
      folderData.folderContainer = folder.getType () == Folder.HOLDS_FOLDERS;
      if (!folderData.folderContainer) {
        folderData.unreadMessageCount = folder.getUnreadMessageCount ();
        folderData.messageCount = folder.getMessageCount ();
      }

      return folderData;
    }

    /**
     * Remove a folder because it has been deleted
     * @param folder name
     */
    public void deleted (String folderName) throws ImapFolderException {
      if (isSpecialFolder (folderName)) {
        throw new ImapFolderException (folderName + " can't be deleted");
      }
      normalFolders.remove (folderName);
      rebuildFolderList ();
    }

    /**
     * A folder has been rename
     * @param old folder name
     * @param new folder name
     */
    public void rename (String oldFolderName, String newFolderName) throws ImapFolderException {
      if (isSpecialFolder (newFolderName)) {
        throw new ImapFolderException ("Can't rename " + oldFolderName + " to " +
        newFolderName + " since the new name is not valid");
      }
      if (normalFolders.indexOf (oldFolderName) < 0) {
        throw new ImapFolderException (oldFolderName + " is not in the subscription list");
      }
      normalFolders.remove (oldFolderName);
      normalFolders.add (newFolderName);
      rebuildFolderList ();
    }
  }

  /**
   * the MsgFolder class manages our cached version of the users messages
   * and tries to ensure that it remains in synch with the real list on the
   * imap server.
   *
   * It tries to appear as a Folder class object
   */
  private class MsgFolder {
    protected Folder folder = null;
    private boolean specialFolder = false;
    private int specialFolderIndex = 0;
    protected boolean dontClose = false;
    private ArrayList folderMsgs = null;
    protected ArrayList searchMsgs = null;
    protected boolean searchActive = false;
    protected int unreadMessages = 0;
    protected volatile boolean newMessages = false;

    private MessageCountAdapter messageListener = new MessageCountAdapter () {
      public void messagesAdded (MessageCountEvent ev) {
        Message[] newMsgs = ev.getMessages ();
        synchronized (folderMsgs) {
          try {
            unreadMessages += newMsgs.length;
            newMessages = true;
            addMessages (newMsgs);
          } catch (MessagingException e) {
            Logger.log (Logger.ERROR, "messageListener:"+e);
          }
        }
      }

      public void messagesRemoved (MessageCountEvent ev) { // Only happens when folder is expunged
        Message[] removedMsgs = ev.getMessages ();
        synchronized (folderMsgs) {
          if (folderMsgs != null) {
            folderMsgs.removeAll (Arrays.asList (removedMsgs));
          }
        }
      }
    };

    private ConnectionListener connectionListener = new ConnectionListener () {
      public void opened (ConnectionEvent e) {
        //Logger.log(Logger.DEBUG, "received connection opened event for " + e);
      }
      public void closed (ConnectionEvent e) {
        //Logger.log(Logger.DEBUG, "received connection closed event " + e);
      }
      public void disconnected (ConnectionEvent e) {
        //Logger.log(Logger.DEBUG, "received connection disconnected event " + e);
      }
    };


    public MsgFolder (Folder folder) throws MessagingException {
      this (folder, false);
    }

    public MsgFolder (String sFolder) throws MessagingException {
      initializeFolder (getFolder (sFolder), false);
    }

    public MsgFolder (Folder folder, boolean writeAccess) throws MessagingException {
      initializeFolder (folder, writeAccess);
    }

    public MsgFolder (String sFolder, boolean writeAccess) throws MessagingException {
      initializeFolder (getFolder (sFolder), writeAccess);
    }

    private void initializeFolder (Folder newFolder, boolean writeAccess) throws MessagingException {
      if (newFolder == null) {
        throw new MessagingException ("Null folder passed");
      }
      folder = newFolder;

      String sFolderName = getFolderName ();

      for (int i = 0; i < sSpecialFolderNames.length; i++) {
        if (sFolderName.equalsIgnoreCase (sSpecialFolderNames[i])) {
          specialFolder = true;
          specialFolderIndex = i;
          break;
        }
      }

      folder.open (writeAccess ? Folder.READ_WRITE : Folder.READ_ONLY);

      folderMsgs = new ArrayList (folder.getMessageCount ());
      unreadMessages = folder.getUnreadMessageCount ();
      folder.addMessageCountListener (messageListener);
      folder.addConnectionListener (connectionListener);
      folderMsgs.addAll (Arrays.asList (folder.getMessages ()));
    }

    public void finalize () throws MessagingException {
      if (folder != null && !dontClose) { //
        try {
          folder.removeMessageCountListener (messageListener);
          Thread.yield();
        } catch (Exception e) {
        }
        synchronized (folderMsgs) {
          try {
            if (folder.isOpen ()) {
              folder.close (true);
            }
          } catch (Exception e) {
          }
          folderMsgs.clear ();
          folderMsgs = null;
          clearSearch();
          folder = null;
        }
      }
    }

    /**
     * Add new messages to our internal list
     * @param Array of messages to add
     */
    private void addMessages (Message[] newMsgs) throws MessagingException {
      fetchHeaders (newMsgs);
      folderMsgs.addAll (Arrays.asList (newMsgs));
    }

    /**
     * Fetch headers for a range of messages
     * @param Index of first message to fetch
     * @param Index of last message to fetch
     */
    private void fetchHeaders (int startIndex, int endIndex, ArrayList msgs) throws MessagingException {
      synchronized (msgs) {
        Message[] newMsgs = new Message[endIndex - startIndex];
        for (int i = startIndex; i < endIndex; i++) {
          newMsgs[i - startIndex] = (Message)msgs.get (i);
        }
        fetchHeaders (newMsgs);
      }
    }

    public void fetchHeaders (int startIndex, int endIndex) throws MessagingException {
      fetchHeaders (startIndex, endIndex, folderMsgs);
    }

    private void fetchHeaders (Message[] msgs) throws MessagingException {
      if (folder != null) {
        folder.fetch (msgs, fetchProfile);
      }
    }

    /**
     * Return the name of a folder
     * @result folder name
     */
    public String getFolderName () {
      String folderName = folder.getName ();
      if (folderName.equalsIgnoreCase ("INBOX")) {
        folderName = "Inbox";
      }
      return folderName;
    }

    public String getFullName () {
      return folder.getFullName ();
    }

    /**
     * Retrieve a message
     * @param the message index to get
     * @return the Message object
     */
    public Message getMessage (int msg) {
      if (searchActive) {
        return (Message) searchMsgs.get (msg);
      } else {
        return (Message) folderMsgs.get (msg);
      }
    }

    /**
     * Retrieve a message for display
     * @param the message index to get
     * @return the Message object
     */
    public Message openMessage (int msg) throws MessagingException {
      Message message = getMessage (msg);
      if (!message.isSet (Flags.Flag.SEEN)) {
        unreadMessages--;
      }
      return message;
    }

    public int getMessageCount () {
      if (searchActive) {
        return searchMsgs.size();
      } else {
        return folderMsgs.size ();
      }
    }

    public int getUnreadMessageCount () throws MessagingException {
      return unreadMessages;
    }

    public boolean isFiltered() {
      return searchActive;
    }

    public boolean exists () throws MessagingException {
      return folder.exists ();
    }

    public void setDontClose (boolean dontClose) {
      this.dontClose = dontClose;
    }

    public void sort (Comparator c) {
      synchronized (folderMsgs) {
        Collections.sort (folderMsgs, c);
      }
    }

    public void search(SearchTerm criteria) throws MessagingException {
      Message[] msgs = folder.search(criteria);
      fetchHeaders (msgs);
      clearSearch();
      searchMsgs = new ArrayList(msgs.length);
      searchMsgs.addAll (Arrays.asList (msgs));
      searchActive = true;
    }

    public void clearSearch() {
      searchActive = false;
      if (searchMsgs != null) {
        searchMsgs.clear();
        searchMsgs = null;
      }
    }

    /**
     * Remove these messages from our internal list
     *
     */
    public void deleteMessage (Message[] removedMsgs) throws MessagingException
    {
      for (int i = 0; i < removedMsgs.length; i++) {
        if (!removedMsgs[i].isSet (Flags.Flag.SEEN)) {
          unreadMessages--;
        }
      }

      /*
        Remove the message from our list before we expunge them from
        the folder so that delete events generated by expunge doesn't beat us to
        the punch.
      */
      synchronized (folderMsgs) {
        if (!folderMsgs.removeAll (Arrays.asList (removedMsgs))) {
          throw new MessagingException ("unable to remove messages");
        }
        if (searchMsgs != null) {
          searchMsgs.removeAll (Arrays.asList (removedMsgs));
        }
      }
    }
  }

  /**
   * ActiveFolder represents a folder open for read/write
   */
  private class ActiveFolder extends MsgFolder {
    public ActiveFolder (Folder folder) throws MessagingException {
      super (folder, true);
    }

    public ActiveFolder (String folder) throws MessagingException {
      super (folder, true);
    }

    public void finalize () throws MessagingException {
      folder.expunge (); // When should we do this????
      clearSearch();
      if (!dontClose) {
        super.finalize ();
      }
    }

    public void removeMsg (String sCopyFolder) throws MessagingException, Exception {
      if (searchActive) {
        removeMsg((Message[]) searchMsgs.toArray(new Message[0]), sCopyFolder);
      } else {
        removeMsg(folder.getMessages(), sCopyFolder);
      }
    }

    /**
     * Purge message from the current folder and copy them to a specified folder
     * @param messages to delete
     * @param folder to copy them to
     */
    public void removeMsg (Message[] removedMsgs, String sCopyFolder) throws MessagingException, Exception {
      if (sCopyFolder == null || sCopyFolder.equals ("")) {
        throw new MessagingException ("'" + sCopyFolder + "' is an invalid folder name");
      }
      try {
        if (!getFolderName().equals(sTrashName) || // Not in trash folder
          !sCopyFolder.equals(sTrashName)) {       // or not copying to trash
          Folder copyFolder;

          if (inbox.getFolderName().equalsIgnoreCase(sCopyFolder)) {
            copyFolder = inbox.folder;
          } else {
            copyFolder = getFolder ( (sFolderDir == null ? "" : sFolderDir + folderSeparator) + sCopyFolder);

            // Create destination folder if it doesn't already exist
            if (!copyFolder.exists ()) {
              copyFolder.create (Folder.HOLDS_MESSAGES);
              imapFolders.add (copyFolder);
            }
          }

          folder.copyMessages (removedMsgs, copyFolder);
        }

        Flags flags = new Flags (Flags.Flag.DELETED);
        flags.add (Flags.Flag.SEEN);
        folder.setFlags (removedMsgs, flags, true);
        deleteMessage (removedMsgs);

        // Should be safe to expunge now
        folder.expunge ();

      } catch (Exception e) {
        throw e;
      }
    }

    /**
     * See if any new mail has arrived since the last time we checked
     * @result new mail
     */
    public boolean mailCheck () throws MessagingException {
      boolean newMail;

      folder.getMessageCount (); // query server
      Thread.yield ();
      newMail = newMessages;
      newMessages = false;

      return newMail;
    }
  }

  /**
   * Provides formatted version of message parts
   * @author Ken Weiner
   */
  private class MessageParts
  {
    private Message msg = null;
    private String sFrom = "";
    private String sTo = "";
    private String sCc = "";
    private String sBcc = "";
    private String sSubject = "";
    private String sDate = "";
    private String sContentType = "";
    private Object content = null;
    private Address[] addresses;
    private ArrayList attachments = new ArrayList ();
    private Part bodyText = null;
    private String lt = "&lt;"; // "<"
    private String gt = "&gt;"; // ">"

    public MessageParts (Message msg) throws IOException, MessagingException
    {
      this(msg, false);
    }

    public MessageParts (Message msg, boolean noHtmlTags) throws IOException, MessagingException
    {
      if (noHtmlTags) {
        lt = "<";
        gt = ">";
      }

      this.msg = msg;
      formatFrom ();
      formatTo ();
      formatCc ();
      formatBcc ();
      formatSubject ();
      formatDate ();
      this.sContentType = msg.getContentType ();
      try {
        this.content = msg.getContent ();
      } catch (UnsupportedEncodingException uee) {
      }
      findAttachments ( (Part)msg);
    }

    public void finalize() {
      msg = null;
      if (attachments != null) {
        attachments.clear();
        attachments = null;
      }
      bodyText = null;
      content = null;
    }

    public String getFrom () {return sFrom;}
    public String getTo () {return sTo;}
    public String getCc () {return sCc;}
    public String getBcc () {return sBcc;}
    public String getSubject () {return sSubject;}
    public String getDate () {return sDate;}
    public String getContentType () {return sContentType;}
    public Object getContent () {return content;}
    public Part[] getAttachments () {return (Part[]) attachments.toArray (new Part[0]);}
    public Part getBodyText () {return bodyText;}
    public Message getMsg() {return msg;};

    private void formatFrom () throws MessagingException
    {
      if ( (addresses = msg.getFrom ()) != null) {
        InternetAddress ia = (InternetAddress) addresses[0];
        String sPersonal = ia.getPersonal ();
        String sAddress = ia.getAddress ();

        if (sPersonal != null) {
          sFrom += sPersonal + " " + lt + sAddress + gt;
        } else {
          sFrom += sAddress;
        }
      }
    }

    private void formatTo () throws MessagingException
    {
      if ( (addresses = msg.getRecipients (Message.RecipientType.TO)) != null)
      {
        for (int iAddr = 0; iAddr < addresses.length; iAddr++)
        {
          if (iAddr > 0)
          sTo += ", ";

          InternetAddress ia = (InternetAddress) addresses[iAddr];
          sTo += ia.getAddress ();
        }
      }
    }

    private void formatCc () throws MessagingException
    {
      if ( (addresses = msg.getRecipients (Message.RecipientType.CC)) != null)
      {
        for (int iAddr = 0; iAddr < addresses.length; iAddr++)
        {
          if (iAddr > 0)
          sCc += ", ";

          InternetAddress ia = (InternetAddress) addresses[iAddr];
          sCc += ia.getAddress ();
        }
      }
    }

    private void formatBcc () throws MessagingException
    {
      if ( (addresses = msg.getRecipients (Message.RecipientType.BCC)) != null)
      {
        for (int iAddr = 0; iAddr < addresses.length; iAddr++)
        {
          if (iAddr > 0)
          sBcc += ", ";

          InternetAddress ia = (InternetAddress) addresses[iAddr];
          sBcc += ia.getAddress ();
        }
      }
    }

    private void formatSubject () throws MessagingException
    {
      sSubject += (msg.getSubject () == null ? "" : msg.getSubject ());
    }

    private void formatDate () throws MessagingException
    {
      Date date = msg.getSentDate ();
      sDate += date != null ? date.toString () : "Unknown";
    }

    /**
     * Find the attachments. Try to determine the primary body text
     */
    private void findAttachments (Part part) throws MessagingException, IOException {
      if (bodyText == null &&
        part.isMimeType ("multipart/alternative")) { // Grab the first displayable body part

        Multipart mPart = (Multipart) part.getContent ();
        for (int i = 0; i < mPart.getCount (); i++) {
          part = mPart.getBodyPart (i);
          if (bodyText == null &&
           (part.isMimeType ("text/plain") || part.isMimeType ("text/html"))) {
            bodyText = part;
          } else if (bodyText != null &&
          bodyText.isMimeType ("text/plain") && part.isMimeType ("text/html")) {
            bodyText = part;
          } else if (! (bodyText != null &&
          bodyText.isMimeType ("text/html") && part.isMimeType ("text/plain"))) {
            attachments.add (part);
          }
        }

      } else if (part.isMimeType ("multipart/*")) {
        Multipart mp = (Multipart) part.getContent ();

        for (int i = 0; i < mp.getCount (); i++) {
          findAttachments (mp.getBodyPart (i));
        }
      } else {
        if (bodyText == null &&
         (part.isMimeType ("text/plain") || part.isMimeType ("text/html"))) {
          bodyText = part;        // Grab the first displayable body part
        } else {
          attachments.add (part);
        }
      }
    }
  }

  /**
   * Session management, part of HttpSessionBindingListener interface
   */

  public void valueBound (HttpSessionBindingEvent event) {
  }

  public void valueUnbound (HttpSessionBindingEvent event) {
    cleanup ();
    if (showMetrics) {
      Logger.log(Logger.INFO, "WebMail metric: Read " + messagesRead + ", Deleted " + messagesDeleted +
        ", Moved " + messagesMoved + ", Downloaded " + messagesDownloaded + ", Attachments " + attachmentsViewed +
        ", Sent " + messagesSent);
    }
  }
}
