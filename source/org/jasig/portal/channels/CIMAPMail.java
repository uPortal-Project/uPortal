/**
 *  Copyright © 2001 University of British Columbia
 *
 *  University of British Columbia ("UBC") will freely share software
 *  registered in the JA-SIG Clearing House with institutions of
 *  higher-education for their non-profit use.  The borrowing institution
 *  will not share or distribute the software without the consent of
 *  UBC.  By its use, the borrowing institution agrees to indemnify
 *  and hold harmless UBC against all loss, cost, damage, liability,
 *  injury or expense, including reasonable attorneys' fees, arising out
 *  of their use of the software.
 *
 *  Those desiring to incorporate this software into commercial products
 *  or use for commercial purposes should contact:
 *
 *  Associate Director of Info Sys, ITServices, UBC
 *  6356 Agricultural Road
 *  Vancouver, B.C.,  CANADA
 *  V6T 1Z2
 *
 *  Tel: 604-822-6611
 *
 *
 *  SOFTWARE IS PROVIDED "AS IS."  TO THE MAXIMUM EXTENT PERMITTED BY LAW,
 *  UBC DISCLAIMS ALL WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 *  IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. UBC DOES
 *  NOT WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN SOFTWARE
 *  WILL MEET ANY REQUIREMENTS OR NEEDS OF THE BORROWING INSTITUTION, OR
 *  THAT SOFTWARE WILL OPERATE ERROR FREE, OR IN AN UNINTERRUPTED FASHION,
 *  OR THAT ANY DEFECTS OR ERRORS IN SOFTWARE WILL BE CORRECTED, OR THAT
 *  SOFTWARE IS COMPATIBLE WITH ANY PARTICULAR PLATFORM.
 *  IN NO EVENT WILL UBC BE LIABLE TO ANY BORROWING INSTITUTION OR
 *  ANY THIRD PARTY FOR ANY INCIDENTAL OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, WITHOUT LIMITATION, INDIRECT, SPECIAL, PUNITIVE, OR
 *  EXEMPLARY DAMAGES) ARISING OUT OF THE USE OF OR INABILITY TO USE
 *  SOFTWARE, OR FOR ANY CLAIM BY ANY OTHER PARTY, EVEN IF UBC HAS
 *  BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
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
 *    - the JavaMail package (1.2 or later)
 *    - the Oreilly Servlet package (com.oreilly.servlet) (19Jun2001 or later),
 *      used for sending attachments. Download it from
 *        http://www.servlets.com/resources/com.oreilly.servlet/index.html
 *    - the jTidy package (org.w3c.tidy.Tidy) to convert errant html email to xml acceptable
 *        http://lempinen.net/sami/jtidy/
 *
 *
 * Files:
 *    CIMAPMail.java - IMAP Mail client
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
 *    folderDir:        Directory in users home directory that will hold the users
 *                      folders (optional)
 *    loginText:        Greeting text on channel explaining what values should be
 *                      used to login to the channel
 *
 * Authentication:
 *   the WebMail client is capable of prompting the user for a username/password
 *   for the imap session. This dialogue will show up if the
 *   sessionUsername/sessionPassword fail, or are not both supplied.
 *
 * ---> NOT IMPLEMENTED YET!!!
 *   If the session attributes portalhost and secureportalhost exists, then they
 *   will be used to provide secure authentication. Ie. the authentication form
 *   will post to the securehost, and once authenticated, the code will direct the
 *   browser to flip back to portalhost (probably non-secure). At UBC, we have:
 *      portalhost = http://my.ubc.ca
 *      secureportalhost = https://my.ubc.ca
 *    These values would probably be set in the web.xml file.
 *    (I'm sure that there is a cleaner way of doing this.)
 *
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

import javax.activation.DataHandler;

import com.oreilly.servlet.multipart.FilePart;

import org.jasig.portal.*;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;
import org.xml.sax.ContentHandler;
import java.util.zip.*;

import org.w3c.tidy.Tidy;

public final class CIMAPMail implements IChannel, IMimeResponse
{
  private static String rcsVersion = "$Revision$"; // from rcs/cvs
  private static String clientVersion = "2.00d";
  private static boolean DEBUG = false;

  // Configurable parameters
  private static class WebmailConfig {
    static String sName = null;
    static String sIMAPHost = null;     // IMAP host
    static int iIMAPPort = 0;        // IMAP port
    static String sSMTPHost = null;     // IMAP host
    private static int iSMTPPort = 0;        // IMAP port
    private static String sCaption = null;   // Application name
    private static String sOrganization = null; // Service Provider name
    private static String sTrashName = null;  // "Wastebasket"
    private static String sSentName = null;   // "Sent Items"
    private static int iMaxMessageSize = 0;   // Largest message we will send
    private static String sFolderDir = null;  // users folder directory
    private static String sDomainName = null;  // Our email domain
    private static String sSessionUsername = null; // session atribute for authenticated username
    private static String sSessionPassword = null; // session atribute for password of authenticated username
    private static String sLoginText = null;

    private static String sProtocol = "imap";
    private static String sMailbox = "INBOX";

  }
  private static WebmailConfig config = new WebmailConfig();

    /** Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  /** Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent (PortalEvent ev)
  {
    if (ev.getEventNumber() == ev.SESSION_DONE) {
      cleanup ();
      if (metrics.showMetrics) {
        LogService.instance().log(LogService.INFO, "WebMail metric: " + metrics);
        metrics.showMetrics = false;
      }
    }
  }

  private class UserPrefs {
  // Candidates for configurable parameters
    int iMsgsPerPage = 15;
    int iFldrsPerPage = 7;
    String sNavBarColor = "#99ccff";
    String sHeaderColor = "#dddddd";
    String sControlColor = "#ffffff";
    String sColumnHeadersColor = "#cccccc"; // From, Subject, etc line
    String sTableCellColor = "#cafcac";
    String sUnseenMsgColor = "#ffffcc"; // Unseen messages
    String sSeenMsgColor = "#99ccff"; // Seen messages
    boolean showFullHeaders = false;
  }
  private UserPrefs userPrefs = new UserPrefs();

  private static String sDraftsName = "Drafts"; //"Drafts"

  // User configuration

  // button messages
  private static String nextMessageButtonTxt = "Next message";
  private static String returnToMessageButtonTxt = "Return to ";
  private static String checkNewMailButtonTxt = "Check for new mail";

  // Used to keep state
  // Initialized in respondToAuthentication
  //private int iMsgsStartAt;
  private boolean authenticated;
  private ActiveFolder activeFolder;
  private ActiveFolder inbox;
  private Comparator msgSortOrder;
  private ImapFolder imapFolders;
  private boolean onetimeSetupDone = false;

  // These should be deduced from portal authentication (hard-coded for now)
  private String sUser = null; //ask
  private String sPassword = null; // ask
  private String sUserEmail = null; // get from session
  private String sFullName = ""; // get from IPerson

  private static String layoutUrl = null; // get from HTTP request
  private static String portalHost = null; // URL to non-secure host
  private static String securePortalHost = null; // URL to secure host
  private boolean redirectBack = false;   // return to http after https authentication
  private static String[] sSpecialFolderNames = null; // Folders managed by the client
  private URLName urlName = null; // imap url
  private Properties props = System.getProperties ();
  private Session session = null;
  private Store store = null;

  // Compilation constants
  private static boolean USE_APPLET = false; // Generate HTML code to use applet to access mail channel
  // Not used since it causes session timeouts to not work

  private FetchProfile fetchProfile = null;
  private char folderSeparator; // IMAP folder separator
  private static DateFormat httpDateFormat = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

  // Metrics about our session
  private class Metrics {
    int messagesRead = 0;
    int messagesDeleted = 0;
    int messagesMoved = 0;
    int messagesDownloaded = 0;
    int attachmentsViewed = 0;
    int messagesSent = 0;
    boolean showMetrics = true;

    public String toString() {
      return "Read " + messagesRead + ", Deleted " + messagesDeleted +
            ", Moved " + messagesMoved + ", Downloaded " + messagesDownloaded +
            ", Attachments " + attachmentsViewed +
            ", Sent " + messagesSent;
    }
  }

  private Metrics metrics = new Metrics();


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
        if (DEBUG) LogService.instance().log(LogService.DEBUG, "store event: " + eventMsg + ", " + eventType);
      }
    };

  private ChannelRuntimeData runtimeData = null;
  private static String fs = File.separator;
  private static final String sslLocation = "/org/jasig/portal/channels/CIMAPMail/CIMAPMail.ssl";
  public CIMAPMail ()
  {
  }

  private FolderListener folderListener = new FolderListener () {
    public void folderCreated (FolderEvent e) {
        try {
          //LogService.instance().log(LogService.DEBUG, "folder created event for " + e.getFolder().getName());
          imapFolders.add (e.getFolder ());
        } catch (MessagingException me) {
          LogService.instance().log(LogService.ERROR, "folderCreated" + me);
        }
    }
    public void folderDeleted (FolderEvent e) {
        //LogService.instance().log(LogService.DEBUG, "folder deleted event for " + e.getFolder().getName());
        try {
          imapFolders.deleted (e.getFolder ().getName ());
        } catch (ImapFolderException sfe) {}
    }
    public void folderRenamed (FolderEvent e) {
        //LogService.instance().log(LogService.DEBUG, "folder renamed event from " + e.getFolder().getName() + " to " + e.getNewFolder());
        try {
          imapFolders.rename (e.getFolder ().getName (), e.getNewFolder ().getName ());
        } catch (ImapFolderException sfe) {
        }
    }
  };


  /**
   * Passes ChannelStaticData to the channel.
   * This is done during channel instantiation time.
   * see org.jasig.portal.StaticData
   * @param sd channel static data
   * @see ChannelStaticData
   */
  private ChannelStaticData staticData;
  public void setStaticData (ChannelStaticData sd) {
    staticData = sd;
    String sParameter;

    IPerson person = sd.getPerson();
    if (person != null) {
      sFullName = person.getFullName();
    }
    if (sFullName == null) {
      sFullName = "";
    }
    config.sName = sd.getParameter("name");
    config.sIMAPHost = sd.getParameter("host");
    props.put ("mail.smtp.host", config.sIMAPHost);
    sParameter = sd.getParameter("port");
    if (sParameter != null) {
      try {
        config.iIMAPPort = Integer.parseInt (sParameter);
      } catch (NumberFormatException nfe) {
      }
    }
    if (config.iIMAPPort == 0) {
      config.iIMAPPort = 143;    // IMAP default
    }

    config.sSMTPHost = sd.getParameter("smtphost");
    if (config.sSMTPHost != null) {
      props.put ("mail.smtp.host", config.sSMTPHost);
    }
    sParameter = sd.getParameter("smtpport");
    if (sParameter != null) {
      try {
        config.iSMTPPort = Integer.parseInt (sParameter);
      } catch (NumberFormatException nfe) {
      }
    }
    if (config.iSMTPPort == 0) {
      config.iSMTPPort = 25;    // SMTP default
    }
    props.put ("mail.smtp.port", config.iSMTPPort + "");

    config.sCaption = sd.getParameter("caption");
    config.sOrganization = sd.getParameter("organization");
    config.sDomainName = sd.getParameter("domain");
    config.sSentName = sd.getParameter("sentName");
    config.sTrashName = sd.getParameter("trashName");
    config.sFolderDir = sd.getParameter("folderDir");
    config.sSessionUsername = sd.getParameter("sessionUsername");
    config.sSessionPassword = sd.getParameter("sessionPassword");
    config.sLoginText = sd.getParameter("loginText");
    if (config.sFolderDir != null && config.sFolderDir.trim ().length () == 0) {
      config.sFolderDir = null;
    }

    sSpecialFolderNames = new String[] {"Inbox", sDraftsName, config.sSentName, config.sTrashName};

    fetchProfile = new FetchProfile ();
    fetchProfile.add (FetchProfile.Item.ENVELOPE);

    httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

  }

  public interface IXmlMethod {
    public abstract boolean isThisMethod(String methodName);
    public abstract void doit(ChannelRuntimeData rd) throws Exception;
    public abstract String renderString() throws Exception;
    public abstract StringWriter respond(String submitValue) throws Exception;
    public abstract StringWriter display() throws Exception;
  }

  private abstract class XmlMethod implements IXmlMethod {
    protected String weAre = "unknown";
    public Exception exception = null;

    public String getWeAre() { return weAre; }

    public void setException(Exception e) {
      exception = e;
      LogService.instance().log(LogService.ERROR, e);
    }

    public boolean isThisMethod(String methodName) {
      return methodName.equals(weAre);
    }
    public void doit(ChannelRuntimeData rd) throws Exception {
      if (DEBUG) System.err.println("default doit for " + weAre);
    }

    public StringWriter respond (String submitValue) throws Exception {
      return null;
    }

    public void download(OutputStream out) throws IOException {
    }

    public String renderString() throws Exception {
      if (DEBUG) System.err.println("generic renderString for " + weAre);

      String submitValue = runtimeData.getParameter("submit");
      if (DEBUG) System.err.println("submit=" + submitValue);
      if (submitValue != null) {
        StringWriter result = respond(submitValue);
        if (result != null) {
          return result.toString();
        } else {
          return null;
        }
      } else {
        String xml = display().toString();
        return xml;
      }
    }
  }

  private XmlMethod authenticateMethod = new Authenticate();
  private XmlMethod statusMethod = new MailStatus();
  private XmlMethod composeMethod = new ComposeMessage();
  private XmlMethod displayMethod = new DisplayMessage();
  private XmlMethod listMessages = new ListMessages();
  private XmlMethod listFolders = new ListFolders();

  private XmlMethod[] jumpTable = new XmlMethod[] {
    authenticateMethod,
    statusMethod,
    listMessages,
    displayMethod,
    composeMethod,
    listFolders,
    new SetupMethod(),
  };

  private XmlMethod findJumptableEntry(String method) {
    for (int i = 0; i < jumpTable.length; i++) {
      if (jumpTable[i].isThisMethod(method)) {
        return jumpTable[i];
      }
    }
    return null;
  }

  private XmlMethod activeMethod = null;

  private class Authenticate extends XmlMethod {
    String username;
    String password;
    public Authenticate() {
      weAre = "authenticate";
    }
    public void doit(ChannelRuntimeData rd) {
      if (DEBUG) System.err.println("doit for " + weAre);
      runtimeData = rd;
      /*
      if (false && sUser == null) { // Get password from authentication stack
        HttpSession httpSession = rd.getHttpRequest().getSession (false);
        sUser = (String) httpSession.getAttribute (config.sSessionUsername);
        sPassword = (String) httpSession.getAttribute (config.sSessionPassword);
      }
      */
      username = runtimeData.getParameter("username");
      password = runtimeData.getParameter("password");
    }

    public StringWriter display() {
      return null;
    }

    public String renderString() {
      String loginError = "";
      try {
        initialize(username, password);
        if (DEBUG) System.err.println("authorized");
        if (false) {
          //runtimeData.redirect("action=mailstatus");

          return null;
       } else {
          activeMethod = statusMethod;
         return statusMethod.renderString();
       }
      } catch (Exception e) {
        if (DEBUG) System.err.println(e);
        loginError = "<error>" + e.getMessage() + "</error>\n";
      }
      if (DEBUG) System.err.println("renderString for " + weAre);
      return "<" + weAre + ">\n" + loginError + (config.sLoginText == null ? "" : "<loginText>" + config.sLoginText + "</loginText>") +
        "<username>" + (sUser == null ? "" : sUser) + "</username>\n</" + weAre + ">\n";
    }
  }

  private class ListMessages extends XmlMethod {
    private String xslTag = "listMessages";
    int iMsgsStartAt;
    int iMsgsEndAt;
    private String searchFolderButtonTxt = "Search Folder";
    private String deleteMessagesButtonTxt = "Delete";
    private String searchMessagesButtonTxt = "Search";
    private String clearSearchMessagesButtonTxt = "Clear Search";
    private String moveMessagesButtonTxt = "Move";

    public ListMessages() {
      weAre = xslTag;
      reset();
    }

    public void reset() {
      iMsgsStartAt = 0;
      iMsgsEndAt = userPrefs.iMsgsPerPage;
      msgSortOrder = ORDER_BY_DATE_ASCENDING;
    }

    public StringWriter respond(String submitValue) throws Exception {
      StringWriter xml = new StringWriter();
      xml.write("<" + weAre + ">\n");
      String allMessages = runtimeData.getParameter("AllMessages");
      // If the user clicked "Delete", move selected messages to wastebasket
        if (submitValue.equals (checkNewMailButtonTxt)) {
          // Does nothing. The real work will be done in printHeader()

        } else if (submitValue.equals (searchMessagesButtonTxt)) {
          // Search the current folder for specific messages
          printSearchMsgForm(xml);
          xml.write("</" + weAre + ">\n");
          return xml;

        } else if (submitValue.equals (clearSearchMessagesButtonTxt)) {
          activeFolder.clearSearch();
          runtimeData.setParameter("page", "last");
          return display();

        } else if (submitValue.equals (searchFolderButtonTxt)) {
          String criteriaText = runtimeData.getParameter ("criteriatext");
          String criteria = runtimeData.getParameter ("criteria");
          if (criteriaText.trim().length() == 0) {
            return displayErrorMsg("Please specify the search text", false);
          } else if (criteria == null) {
            return displayErrorMsg("Please specify the search criteria", false);
          }

          SearchTerm term;
          if (criteria.equals("Sender")) {
            term = new FromStringTerm(criteriaText);
          } else if (criteria.equals("Subject")) {
            term = new SubjectTerm(criteriaText);
          } else {
            return displayErrorMsg(criteria + " is an unrecognized search item", true);
          }
          activeFolder.search(term);
          runtimeData.setParameter("page", "last");

        } else if (submitValue.equals (deleteMessagesButtonTxt) ||
              submitValue.equals (moveMessagesButtonTxt)) {

          String destinationFolder = config.sTrashName;
          if (submitValue.equals (moveMessagesButtonTxt)) {
            if ( (destinationFolder = runtimeData.getParameter ("destinationFolder1")).trim ().equals ("") &&
               (destinationFolder = runtimeData.getParameter ("destinationFolder2")).trim ().equals ("")) {
              return displayErrorMsg ("Missing destination folder", true);
            }
          }

          if (allMessages == null || !allMessages.equalsIgnoreCase("on")) {
            String sMsgs[];
            if ( (sMsgs = runtimeData.getParameterValues ("msg")) == null ||
               sMsgs.length == 0) {
              return displayErrorMsg ("Please select the message(s) to be " + submitValue + "d", true);
            }
            Collection cMsgs = new ArrayList (sMsgs.length);
            for (int i = 0; i < sMsgs.length; i++) {
              try {
                int msg = Integer.parseInt (sMsgs[i]);
                Message deletedMessage = activeFolder.getMessage (msg);
                cMsgs.add (deletedMessage);
              } catch (Exception e) {
                LogService.instance().log(LogService.ERROR, "respondToMsgControls: Unable to get message " +
                      sMsgs[i] + "in " + activeFolder.getFolderName() + ": " + e);
              }
            }

            if (cMsgs.size() > 0) {
              Message[] msgs = (Message[]) cMsgs.toArray (new Message[0]);
              activeFolder.removeMsg (msgs, destinationFolder);
              if (submitValue.equals (moveMessagesButtonTxt)) {
                metrics.messagesMoved += msgs.length;
              } else {
                metrics.messagesDeleted += msgs.length;
              }
            }

            if (cMsgs.size() != sMsgs.length) {
              return displayErrorMsg("Unable to delete some or all of your messages", false);
            }
          } else { // asked for all messages
            activeFolder.removeMsg (destinationFolder);
          }
        }

      return display();
    }

    String newFolder = null;
    String pageIndex = null;
    public void setNewFolder(String newFolder) {
      this.newFolder = newFolder;
    }
    public void setPageIndex(String pageIndex) {
      this.pageIndex = pageIndex;
    }
    public void doit(ChannelRuntimeData rd) {
      if (DEBUG) System.err.println("doit for " + weAre);
      runtimeData = rd;
      String value;
      if ((value = runtimeData.getParameter("folder")) != null){
        setNewFolder(value);
      }
      if ((value = runtimeData.getParameter("page")) != null){
        setPageIndex(value);
      }
    }
    public StringWriter display()  throws Exception{
      if (DEBUG) System.err.println("renderString for " + weAre);
      StringWriter xml = new StringWriter();

        if (activeFolder == null) {
          activeFolder = inbox;
        }

        if (DEBUG) System.err.println("list messages in " + newFolder);
        if (newFolder != null &&
          !activeFolder.getFolderName().equalsIgnoreCase(newFolder)) {
          try {
            activeFolder.finalize (); // Free resources used for current folder
          } catch (FolderClosedException fce) {
          }
          activeFolder = createActiveFolder (newFolder);
          runtimeData.setParameter("page", "last");
        }

        String sActiveFolderName = activeFolder.getFolderName ();


        xml.write("<" + weAre + (activeFolder.isFiltered() ? " filtered=\"yes\"" : "") + ">\n");

        // Navigation bar
        printNavBar (xml);

        // Unread message count and title
        printHeader (sActiveFolderName, activeFolder.getUnreadMessageCount(), xml);

        int totalMsgs = activeFolder.getMessageCount ();
        if (totalMsgs > 0) {
          if (pageIndex != null) {
            if (pageIndex.equals("last")) {
              iMsgsEndAt = totalMsgs;
              if (iMsgsEndAt - userPrefs.iMsgsPerPage < 0) {
                iMsgsStartAt = 0;
              } else {
                iMsgsStartAt = iMsgsEndAt - userPrefs.iMsgsPerPage;
              }
            } else {
              try {
                iMsgsStartAt = Integer.parseInt(pageIndex);
              } catch (Exception e) {
                iMsgsStartAt = totalMsgs - userPrefs.iMsgsPerPage - 1;
              }
            }
          }

          if (iMsgsStartAt > totalMsgs) {
            iMsgsStartAt = totalMsgs - userPrefs.iMsgsPerPage - 1;
          }
          if (iMsgsStartAt < 0) {
            iMsgsStartAt = 0;
          }
          iMsgsEndAt = iMsgsStartAt + userPrefs.iMsgsPerPage;
          if (iMsgsEndAt > totalMsgs) {
            iMsgsEndAt = totalMsgs;
          }
          if (iMsgsStartAt > iMsgsEndAt - userPrefs.iMsgsPerPage) {
            iMsgsStartAt = iMsgsEndAt - userPrefs.iMsgsPerPage;
          }
          if (iMsgsStartAt < 0) {
            iMsgsStartAt = 0;
          }

          xml.write("<controls>\n");
          xml.write("<buttons bgcolor=\"" + userPrefs.sControlColor + "\">");
          xml.write("<folders>\n");
          if (DEBUG) System.err.println(">folder list");
          String[] folders = (String [])imapFolders.toArray(new String[0]);
          for (int iFldr = 0; iFldr < folders.length; iFldr++) {
            if (!activeFolder.getFolderName ().equalsIgnoreCase (folders[iFldr])) {
              xml.write("<folder value=\"" + folders[iFldr] + "\">" +
                   folders[iFldr] + "</folder>");
            }
          }
          if (DEBUG) System.err.println("<folder list");

          xml.write("</folders>\n");
          xml.write("</buttons>\n");
          xml.write("</controls>\n");

          xml.write("<pagination action=\"" + weAre + "\" bgcolor=\"" + userPrefs.sControlColor + "\"");
          if (iMsgsStartAt > userPrefs.iMsgsPerPage + 1) {
            xml.write(" first=\"0\"");
          }

          if (iMsgsStartAt > 1) {
            xml.write(" prev=\"" + (iMsgsStartAt - userPrefs.iMsgsPerPage) + "\"");
          }

          xml.write(" start=\"" + (iMsgsStartAt + 1) + "\" end=\"" + iMsgsEndAt + "\" total=\"" + totalMsgs + "\"");

          if (iMsgsEndAt < totalMsgs) {
            xml.write(" next=\"" + (iMsgsStartAt + userPrefs.iMsgsPerPage) + "\"");
          }

          if (iMsgsEndAt < totalMsgs && iMsgsEndAt + userPrefs.iMsgsPerPage < totalMsgs) {
            xml.write(" last=\"last\"");
          }
          xml.write("/>");

          xml.write("<messages bgcolor=\"" + userPrefs.sColumnHeadersColor + "\">\n");

          xml.write("<headers bgcolor=\"" + userPrefs.sColumnHeadersColor + "\">\n");
          xml.write("<header align=\"center\">Select</header>\n");
          xml.write("<header>&#160;</header>\n");
          xml.write("<header value=\"from\" align=\"left\"" +
               ((msgSortOrder == ORDER_BY_FROM_ASCENDING || msgSortOrder == ORDER_BY_FROM_DESCENDING) ? " active=\"yes\"" : "") +
               ">From</header>\n");
          xml.write("<header value=\"subject\" align=\"left\"" +
               ((msgSortOrder == ORDER_BY_SUBJECT_ASCENDING || msgSortOrder == ORDER_BY_SUBJECT_DESCENDING) ? " active=\"yes\"" : "") +
               ">Subject</header>\n");
          xml.write("<header value=\"date\" align=\"left\"" +
               ((msgSortOrder == ORDER_BY_DATE_ASCENDING || msgSortOrder == ORDER_BY_DATE_DESCENDING) ? " active=\"yes\"" : "") +
               ">Time/Date</header>\n");
          xml.write("<header value=\"size\" align=\"right\"" +
               ((msgSortOrder == ORDER_BY_SIZE_ASCENDING || msgSortOrder == ORDER_BY_SIZE_DESCENDING) ? " active=\"yes\"" : "") +
               ">Size</header>\n");
          xml.write("</headers>\n");

          if (DEBUG) System.err.println(">Fetching messages");
          activeFolder.fetchHeaders (iMsgsStartAt, iMsgsEndAt); // Preload headers
          if (DEBUG) System.err.println("<Fetched messages");
          // Loop through messages
          for (int iMsg = iMsgsStartAt; iMsg < iMsgsEndAt; iMsg++) {
            Message msg = activeFolder.getMessage (iMsg);
            //if (DEBUG) out.println ("#" + msg.getMessageNumber () + " " + getSystemFlags (msg));
            if (msg.isSet (Flags.Flag.DELETED)) {
              continue; // Don't show deleted messages
            }
            String bg = (msg.isSet (Flags.Flag.SEEN) ? userPrefs.sSeenMsgColor : userPrefs.sUnseenMsgColor);
            xml.write("<message bgcolor=\"" + bg + "\" msg=\"" + iMsg + "\"");
            xml.write(" size=\"" + formatSize (msg.getSize ()) + "\"");
            if (msg.isSet (Flags.Flag.ANSWERED)) {
              xml.write(" status=\"R\"");
            }
            xml.write(sActiveFolderName.equals (sDraftsName) ? " draft=\"yes\"" : "");
            xml.write(">\n");

            // From
            Address[] addresses;

            xml.write("<from>\n");
            if ( (addresses = msg.getFrom ()) != null) {
              for (int iAddr = 0; iAddr < addresses.length; iAddr++) {
                InternetAddress ia = (InternetAddress) addresses[iAddr];
                String sPersonal = ia.getPersonal ();

                xml.write("<address>");
                if (sPersonal != null) {
                  xml.write("<personal>" + HTMLescape(sPersonal) + "</personal>");
                }
                xml.write("<email>" + HTMLescape(ia.getAddress()) + "</email>");
                xml.write("</address>\n");
              }
            }
            xml.write("</from>\n");

            // Subject
            String sSubject = msg.getSubject ();
            xml.write("<subject>");
            if (sSubject == null || sSubject.trim ().length () == 0) {
              xml.write("[none]");
            } else {
              xml.write(HTMLescape(sSubject));
            }
            xml.write("</subject>\n");

            // Time/Date
            Date date = msg.getSentDate ();
            xml.write("<date>" + (date != null ? date.toString () : "Unknown") + "</date>\n");

            xml.write("</message>\n");
          }
          xml.write("</messages>");
        } else {
          //printMessageListControls (true, req, res, out);
          //out.println ("<p align=center><strong><em>This folder is empty.</em></strong><p>");
        }

      xml.write("</" + weAre + ">\n");
      return xml;
    }

    /**
     * sort a folder
     * @param the servlet request object
     * @param the servlet response object
     * @param the JspWriter object
     */
    public StringWriter sortList () throws Exception
    {
      String sSortBy = runtimeData.getParameter ("sortBy");

      if (sSortBy != null) {
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

          activeFolder.sort (msgSortOrder);
        }

      return listMessages.display();
    }

    /**
     * Print search form
     * @param the JspWriter object
     */
    private void printSearchMsgForm (StringWriter xml) throws IOException
    {

      // Navigation bar
      printNavBar (xml);
      xml.write("<searchFolder/>");
    }
  }

  void redirect(ChannelRuntimeData rd, String action, String args) throws Exception {
    // rd.redirect(runtimeData.getBaseActionURL(), "action=" + action + (!args.equals("") ? "&" + args : ""));
  }

  private class DisplayMessage extends XmlMethod {
    private String xslTag = "displayMessage";
    private String replyMessageButtonTxt = "Reply";
    private String replyAllMessageButtonTxt = "Reply All";
    private String forwardMessageButtonTxt = "Forward";
    private String deleteMessageButtonTxt = "Delete";
    private Tidy tidy = new Tidy(); // We have to make sure the html is XML compliant

    public DisplayMessage() {
      weAre = xslTag;
      tidy.setXHTML (true);
      tidy.setDocType ("omit");
      tidy.setQuiet(true);
      tidy.setShowWarnings(false);
      tidy.setNumEntities(true);
      tidy.setWord2000(true);
      try {
        if ( System.getProperty("os.name").indexOf("Windows") != -1 ) {
          tidy.setErrout( new PrintWriter ( new FileOutputStream (new File ("nul") ) ) );
        } else {
          tidy.setErrout( new PrintWriter ( new FileOutputStream (new File ("/dev/null") ) ) );
        }
      } catch (FileNotFoundException fnfe) { /* Ignore */}

    }

    private int msgIndex;
    public void doit(ChannelRuntimeData rd) throws Exception {
      if (DEBUG) System.err.println("doit for " + weAre);
      runtimeData = rd;
      String value ;
      if ((value = runtimeData.getParameter("msg")) != null) {
        msgIndex = Integer.parseInt(value);
      }

      if ((value = runtimeData.getParameter("attachment")) != null) {
        int attachmentIndex = Integer.parseInt(value);
        Message msg = activeFolder.openMessage (msgIndex);
        MessageParts msgParts = new MessageParts (msg);
        Part attachment = msgParts.getAttachments ()[attachmentIndex];

        String contentType = attachment.getContentType ();
        downloadName = attachmentName(attachment, attachmentIndex);
        downloadHeaders = new HashMap();
        downloadHeaders.put("Accept-Ranges", "bytes");
        downloadHeaders.put("Content-Disposition", "inline; filename=\"" + downloadName + "\"");
        downloadHeaders.put("Cache-Control", "private");  // Have to override (possible) no-cache directives
        int colonPos = contentType.indexOf (";");
        if (colonPos > 0) {
          downloadContentType = contentType.substring (0, colonPos).toLowerCase ();
        } else {
          downloadContentType = contentType.toLowerCase ();
        }
        Date sentDate = msg.getSentDate();
        if (sentDate != null) {
          downloadHeaders.put("Last-Modified", httpDateFormat.format(sentDate));
        }

        downloadInputStream = attachment.getInputStream ();
        metrics.attachmentsViewed++;

      }
      if (DEBUG) System.err.println("doit for " + weAre);
    }
    public void setMsg(int msg) {
      msgIndex = msg;
    }
    public StringWriter respond(String submitValue)  throws Exception {
      StringWriter xml = new StringWriter();
      xml.write("<" + xslTag + ">\n");

        // If the user clicked "Reply", goto compose screen using this message as a draft
        if (submitValue.equals (replyMessageButtonTxt)) {
          if (true) {
            activeMethod = composeMethod;
            ((ComposeMessage)composeMethod).setAction("reply");
            ((ComposeMessage)composeMethod).setPrevMsg(msgIndex);
            return activeMethod.display();
          } else {
            redirect(runtimeData, composeMethod.getWeAre(), "mode=reply&prevMsg=" + msgIndex);
          }
          return null;

          // If the user clicked "Reply All", goto compose screen using this message as a draft
        } else if (submitValue.equals (replyAllMessageButtonTxt)) {
          if (true) {
            activeMethod = composeMethod;
            ((ComposeMessage)composeMethod).setAction("replyAll");
            ((ComposeMessage)composeMethod).setPrevMsg(msgIndex);
            return activeMethod.display();
          } else {
            redirect(runtimeData, composeMethod.getWeAre(), "mode=replyAll&prevMsg=" + msgIndex);
          }
          return null;

          // If the user clicked "Forward", goto compose screen using this message as a draft
        } else if (submitValue.equals (forwardMessageButtonTxt)) {
          if (true) {
            activeMethod = composeMethod;
            ((ComposeMessage)composeMethod).setAction("forward");
            ((ComposeMessage)composeMethod).setPrevMsg(msgIndex);
            return activeMethod.display();
          } else {
            redirect(runtimeData, composeMethod.getWeAre(), "mode=forward&prevMsg=" + msgIndex);
          }
          return null;

          // If the user clicked "Delete", delete the message
        } else if (submitValue.equals (deleteMessageButtonTxt)) {
          Message[] deletedMsgs = new Message[1];
          deletedMsgs[0] = activeFolder.getMessage (msgIndex);
          if (activeFolder.getFolderName ().equals (config.sTrashName)) {
            //if we are in the trash, then delete that one message
            activeFolder.deleteMessage(deletedMsgs);
          } else {
            // Copy message to trash folder
            activeFolder.removeMsg (deletedMsgs, config.sTrashName);
          }

          // Go to the next message in the folder
          if (msgIndex >= activeFolder.getMessageCount ()) {
           if (true) {
             activeMethod = listMessages;
             ((ListMessages)listMessages).setNewFolder(null);
             ((ListMessages)listMessages).setPageIndex("last");
              return activeMethod.display();
           } else {
              redirect(runtimeData, listMessages.getWeAre(), "page=last");
            }
          } else {
            if (true) {
              activeMethod = displayMethod;
              ((DisplayMessage) displayMethod).setMsg(msgIndex);
              return activeMethod.display();
            } else {
              redirect(runtimeData, getWeAre(), "msg=" + msgIndex);
            }
          }
          return null;

        } else if (submitValue.startsWith (returnToMessageButtonTxt)) {
          if (true) {
            activeMethod = listMessages;
            return activeMethod.display();
          } else {
            redirect(runtimeData, listMessages.getWeAre(), "");
          }
          return null;

          // Step to the next message in folder
        } else if (submitValue.equals (nextMessageButtonTxt)) {
          msgIndex++;
          if (msgIndex >= activeFolder.getMessageCount ()) {
            if (true) {
            activeMethod = listMessages;
             ((ListMessages)listMessages).setNewFolder(null);
             ((ListMessages)listMessages).setPageIndex("last");
              return activeMethod.display();
            } else {
              redirect(runtimeData, listMessages.getWeAre(), "page=last");
            }
          } else {
            if (true) {
              activeMethod = displayMethod;
              ((DisplayMessage)displayMethod).setMsg(msgIndex);
              return activeMethod.display();
            } else {
              redirect(runtimeData, getWeAre(), "msg=" + msgIndex);
            }
          }
          return null;
        }

      xml.write("</" + xslTag + ">\n");
      return xml;
    }

    public StringWriter display() throws Exception {
      if (DEBUG) System.err.println("Display msg=" + msgIndex);
      if (DEBUG) System.err.println("renderString for " + xslTag);
      StringWriter xml = new StringWriter();
      xml.write("<" + xslTag + ">\n");

        Message msg = activeFolder.openMessage (msgIndex);
        MessageParts msgParts = new MessageParts (msg);

        // Navigation bar
        printNavBar (xml);

        // Header and title
        printHeader ("Read", xml);

        //printFormStart ("respondToReadMsgControls", false, null, out);
        xml.write ("<hidden name=\"msg\" value=\"" + msgIndex + "\"/>");

        // Message Controls
        xml.write("<controls bgcolor=\"" + userPrefs.sControlColor + "\">\n");
        xml.write("<button>" + replyMessageButtonTxt + "</button>\n");
        xml.write("<button>" + replyAllMessageButtonTxt + "</button>\n");
        xml.write("<button>" + forwardMessageButtonTxt + "</button>\n");
        xml.write("<button>" + deleteMessageButtonTxt + "</button>\n");
        xml.write("<button>" + HTMLescape(returnToMessageButtonTxt + activeFolder.getFolderName()) +"</button>\n");
        xml.write("<button>" + nextMessageButtonTxt + "</button>\n");
        xml.write("</controls>");

        // Message headers
        xml.write("<headers>\n");
        if (userPrefs.showFullHeaders) {
          Enumeration headers = msg.getAllHeaders();
          while (headers.hasMoreElements ()) {
            Header header = (Header)headers.nextElement ();
            xml.write("<header name=\"" + header.getName() + "\">" + HTMLescape(header.getValue()) + "</header>\n");
          }
        } else {
          xml.write("<header name=\"From\">" + msgParts.getFrom () + "</header>\n");
          xml.write("<header name=\"To\">" + msgParts.getTo () + "</header>\n");
          xml.write("<header name=\"Cc\">" + msgParts.getCc () + "</header>\n");
          xml.write("<header name=\"Subject\">" + HTMLescape(msgParts.getSubject ()) + "</header>\n");
          xml.write("<header name=\"Date\">" + msgParts.getDate () + "</header>\n");
        }
        xml.write("</headers>\n");

        // Message body
        Part textPart = msgParts.getBodyText ();
        Part[] attachments = msgParts.getAttachments ();
        if (attachments.length > 0) {
          xml.write("<attachments>");
          for (int i = 0; i < attachments.length; i++) {
            String attachmentName = attachmentName(attachments[i], i);
            xml.write("<attachment msg=\"" + msgIndex + "\" attachment=\"" + i + "\">" + attachmentName + "</attachment>");
          }
          xml.write("</attachments>");
        }

        xml.write("<msgbody>");
        if (textPart == null) {
          xml.write("<msgtext><strong>Message has no displayable text</strong></msgtext>\n");
        } else if (textPart.isMimeType ("text/html")) {
          ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
          tidy.parse(textPart.getInputStream (), out);
          xml.write("<msgtext>\n" + out.toString() + "\n</msgtext>");
        } else {
          BufferedReader in = new BufferedReader (new InputStreamReader (textPart.getInputStream ()));

          try {
            String line;
            xml.write("<msgtext>");
            while ( (line = in.readLine ()) != null) {
              xml.write(HTMLescape(line)+ "<br/>");
            }
            xml.write("</msgtext>");
          } finally {
            in.close ();
          }
        }
        xml.write("</msgbody>");

        metrics.messagesRead++;

      xml.write("</" + xslTag + ">\n");
      return xml;
    }

  }

  private class ListFolders extends XmlMethod {
    private String xslTag = "listFolders";
    private int iFldrsStartAt;
    private int iFldrsEndAt;
    private String pageIndex = null;

    private String newFolderButtonTxt = "New";
    private String createFolderButtonTxt = "Create";
    private String deleteFolderButtonTxt = "Delete";
    private String renameFolderButtonTxt = "Rename";
    private String renameFolderNameButtonTxt = "Rename Folder";
    private String emptyFolderButtonTxt = "Empty "; // folder name appended


    public ListFolders() {
      weAre = xslTag;
      reset();
    }

    void reset() {
      iFldrsStartAt = 1;
      iFldrsEndAt = userPrefs.iFldrsPerPage;
    }

    public StringWriter respond(String submitValue) throws Exception {
      StringWriter xml = new StringWriter();
      xml.write("<" + xslTag + ">\n");

        // If the user clicked "New", prompt the user for a new folder name
        if (submitValue.equals (newFolderButtonTxt)) {
          enterFolderName (xml);
          xml.write("</" + xslTag + ">\n");
          return xml;
        } else if (submitValue.equals (renameFolderButtonTxt)) {
          String sFolders[];
          if ( (sFolders = runtimeData.getParameterValues ("folder")) == null ||
             sFolders.length == 0) {
            return displayErrorMsg ("Please select a folder to be renamed", true);
          } else if (sFolders.length > 1) {
            return displayErrorMsg ("You can only rename one folder at a time", true);
          } else {
            enterFolderName (xml, sFolders[0]);
          }

          xml.write("</" + xslTag + ">\n");
          return xml;
        }

        if (activeFolder != null) {
          activeFolder.finalize (); // Can't manipulate folder if it is open
          activeFolder = null;
        }

        // If the user entered a new folder name, create the folder
        if (submitValue.equals (createFolderButtonTxt)) {
          String sNewFolderName = runtimeData.getParameter("newFolderName");
          if ( sNewFolderName.length ()==0) {
            return displayErrorMsg ("Folder must have a name.", true);
          } else if ( sNewFolderName.indexOf (folderSeparator) > 0) {
            return displayErrorMsg (sNewFolderName + " contains an illegal character.", true);
          }
          Folder newFolder = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + sNewFolderName);
          newFolder.create (Folder.HOLDS_MESSAGES);
          imapFolders.add (newFolder);
          Thread.yield (); // allow listener to run
        }

        // If the user entered a new folder name, rename the folder
        if (submitValue.equals (renameFolderNameButtonTxt))
          {
            String sOldFolderName = runtimeData.getParameter("oldFolderName");
            String sNewFolderName = runtimeData.getParameter("newFolderName");
            if (sNewFolderName == null || sNewFolderName.trim ().length () == 0) {
              return displayErrorMsg ("Please enter a new name for the folder", true);
            }

            Folder oldFolder = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + sOldFolderName);
            Folder newFolder = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + sNewFolderName);
            try {
              if (oldFolder.renameTo (newFolder)) {
                imapFolders.rename (sOldFolderName, sNewFolderName);
              } else {
                return displayErrorMsg ("Unable to rename folder '" + sOldFolderName +
                                "' to '" + sNewFolderName + "'", false);
              }
              Thread.yield (); // allow listener to run
            } catch (ImapFolderException sfe) {
              return displayErrorMsg(sfe, false);
            } catch (MessagingException me) {
              return displayErrorMsg(me, false);
            }
          }

        // If the user clicked "Delete", delete the folder if it is empty
        else if (submitValue.equals (deleteFolderButtonTxt)) {
          String sFolders[];
          if ( (sFolders = runtimeData.getParameterValues("folder")) == null || sFolders.length == 0) {
            return displayErrorMsg ("Please select a folder to be deleted.", true);
          }
          for (int i = 0; i < sFolders.length; i++) {
            Folder folder = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + sFolders[i]);

            if (folder.getType () == Folder.HOLDS_FOLDERS) {
              int subFolders = folder.list ().length;
              if (subFolders == 0) {
                folder.delete (true);
                imapFolders.deleted (folder.getName ());
              } else {
                return displayErrorMsg ("Can't delete folder \"" + sFolders[i] + "\" because it still has " +
                         subFolders + " folders in it.<br>", true);

              }
            } else {
              if (folder.getMessageCount () == 0) {
                folder.delete (true);
                imapFolders.deleted (folder.getName ());
                Thread.yield (); // allow listener to run
              } else {
                return displayErrorMsg ("Can't delete folder \"" + sFolders[i] + "\" because it still has " +
                         folder.getMessageCount () + " messages in it.<br>", true);
              }
            }
          }
        }

        // If the user clicked "Empty Trash", expunge messages in the Trash folder
        else if (submitValue.equals (emptyFolderButtonTxt + config.sTrashName)) {
          Folder trash = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + config.sTrashName);
          trash.open (Folder.READ_WRITE);
          Message[] msgs = trash.getMessages ();
          Flags flags = new Flags (Flags.Flag.DELETED);
          trash.setFlags (msgs, flags, true);
          trash.close (true);
        }

      // I need some logic here to determine what messages to show after a delete
      // Set start and end messages and then call: listMessages (req, res, out);
      // firstFldrPage (req, res, out);

      return display();
    }

    public void setPageIndex(String pageIndex) {
      this.pageIndex = pageIndex;
    }

    public void doit(ChannelRuntimeData rd) throws PortalException {
      if (DEBUG) System.err.println("doit for " + weAre);
      runtimeData = rd;
      String value;
      if ((value = runtimeData.getParameter("page")) != null){
        setPageIndex(value);
      }

      downloadName = runtimeData.getParameter("downloadFolder");
      if (downloadName != null) { // Set up for download
        downloadHeaders = new HashMap();
        downloadHeaders.put("Accept-Ranges", "bytes");
        downloadHeaders.put("Content-Disposition", "inline; filename=\"" + downloadName + ".zip\"");
        downloadHeaders.put("Cache-Control", "private");  // Have to override (possible) no-cache directives
        downloadContentType = "application/zip";
        downloadHeaders.put("Last-Modified", httpDateFormat.format(new Date()));
        downloadInputStream = null;
      }
    }


    public void download(OutputStream out) throws IOException {
        final String crlf = "\r\n"; // new line sequence for a zip file

        if (downloadName == null) {
          throw new IOException("Folder to download not proided in URL");
        }
        String folderName = downloadName;
        try {
          Folder folder;
          if (folderName.equalsIgnoreCase (config.sMailbox)) {
            folder = inbox.folder;
          } else {
            folder = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + folderName);
            folder.open(Folder.READ_ONLY);
          }
          Message messages[] = folder.getMessages();

          ZipOutputStream zout = new ZipOutputStream(out);
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

            if (bodyText != null && bodyText.isMimeType("text/html")) {
              fileExtension = ".htm";
              htmlBreak = "<br/>";
            }
            zpart = new ZipEntry(folderName + "/" + messageName.trim() + fileExtension);
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
              LogService.instance().log(LogService.ERROR, e);
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
                LogService.instance().log(LogService.ERROR, e);
              }
              zout.closeEntry();
            }
            zout.flush();
          }
          zout.flush();
          zout.finish();
          zout.close();

          metrics.messagesDownloaded += messages.length;
          if (!folderName.equalsIgnoreCase (config.sMailbox)) {
            folder.close(false);
          }

        } catch (MessagingException me) {
          LogService.instance().log(LogService.ERROR, me);
          throw new IOException(me.getMessage());
        } catch (IOException e) {
          LogService.instance().log(LogService.ERROR, e);
          throw e;
        }
    }

    public StringWriter display() {
      StringWriter xml = new StringWriter();
      int totalFldrs = imapFolders.size();

      if (DEBUG) System.err.println("renderString for " + xslTag);
      xml.write("<" + xslTag + ">\n");
      try {
        // Navigation bar (top)
        printNavBar (xml);

        // Header and title
        printHeader ("Folders", xml);

        // Pagination controls
        if (pageIndex != null) {
            if (pageIndex.equals("last")) {
              iFldrsEndAt = totalFldrs;
              if (iFldrsEndAt - userPrefs.iFldrsPerPage < 1) {
                iFldrsStartAt = 1;
              } else {
                iFldrsStartAt = iFldrsEndAt - userPrefs.iFldrsPerPage;
              }
            } else {
              try {
                iFldrsStartAt = Integer.parseInt(pageIndex);
              } catch (Exception e) {
                iFldrsStartAt = totalFldrs - userPrefs.iFldrsPerPage;
              }
            }
          }

          if (iFldrsStartAt > totalFldrs) {
            iFldrsStartAt = totalFldrs - userPrefs.iFldrsPerPage;
          }
          if (iFldrsStartAt < 1) {
            iFldrsStartAt = 1;
          }
          iFldrsEndAt = iFldrsStartAt + userPrefs.iFldrsPerPage;
          if (iFldrsEndAt > totalFldrs) {
            iFldrsEndAt = totalFldrs;
          }
          if (iFldrsStartAt > iFldrsEndAt - userPrefs.iFldrsPerPage) {
            iFldrsStartAt = iFldrsEndAt - userPrefs.iFldrsPerPage;
          }
          if (iFldrsStartAt < 1) {
            iFldrsStartAt = 1;
          }

        xml.write("<pagination action=\"" + weAre + "\" bgcolor=\"" + userPrefs.sControlColor + "\"");
        if (iFldrsStartAt > userPrefs.iFldrsPerPage) {
          xml.write(" first=\"1\"");
        }

        if (iFldrsStartAt > 1){
          xml.write(" prev=\"" + (iFldrsStartAt - userPrefs.iFldrsPerPage) + "\"");
        }

        xml.write(" start=\"" + iFldrsStartAt +
             "\" end=\"" + iFldrsEndAt +
             "\" total=\"" + totalFldrs + "\"");

        if (iFldrsEndAt < totalFldrs){
          xml.write(" next=\"" + (iFldrsStartAt + userPrefs.iFldrsPerPage) + "\"");
        }

        if (iFldrsEndAt < totalFldrs && iFldrsEndAt + userPrefs.iFldrsPerPage < totalFldrs){
          xml.write(" last=\"" + (totalFldrs - userPrefs.iFldrsPerPage) + "\"");
        }
        xml.write("/>\n");

        // Controls for this page (top)
        xml.write("<controls bgcolor=\"" + userPrefs.sControlColor + "\">");
        xml.write("<button>" + newFolderButtonTxt + "</button>\n");
        xml.write("<button>" + deleteFolderButtonTxt + "</button>\n");
        xml.write("<button>" + renameFolderButtonTxt + "</button>\n");
        xml.write("<button>" + emptyFolderButtonTxt + config.sTrashName + "</button>\n");
        xml.write("</controls>\n");

        // List folders in active store

        // Headers
        xml.write("<headers bgcolor=\"" + userPrefs.sColumnHeadersColor + "\">\n");
        xml.write("<header>Select</header>\n");
        xml.write("<header>Folder</header>\n");
        xml.write("<header>Messages</header>\n");
        xml.write("<header>Unread</header>\n");
        xml.write("<header>&#160;</header>\n");
        xml.write("</headers>\n");
        // Loop through folders

        int iTotalMsgs = 0;
        int iTotalNewMsgs = 0;
        xml.write("<folders>\n");
        for (int iFldr = iFldrsStartAt - 1; iFldr < iFldrsEndAt; iFldr++) {
          ImapFolder.FolderData folderData = null;
          try {
            folderData = imapFolders.getFolderData (iFldr);
            if (!folderData.exists) continue;

            xml.write("<folder bgcolor=\"" + userPrefs.sTableCellColor + "\"");

            String sFolderName = folderData.folderName;

            if (folderData.specialFolder) {
              xml.write(" special=\"yes\"");
            }

            // Folders

            if (folderData.folderContainer) {
              xml.write(" folders=\"yes\"");
            } else {
              // Messages
              int iMsgCount = folderData.messageCount;

              xml.write(" messages=\"" + iMsgCount + "\"");
              iTotalMsgs += iMsgCount;

              // New Messages
              int iNewMsgCount = folderData.unreadMessageCount;
              xml.write(" unread=\"" + iNewMsgCount + "\"");
              iTotalNewMsgs += iNewMsgCount;

            }
            xml.write(">" + HTMLescape(sFolderName) + "</folder>\n");
          } catch (Exception e) {
            //out.println ("<tr>" + me + " -> " + (folderData == null ? "null" : folderData.folderName) + "</tr>");
            LogService.instance().log(LogService.ERROR, e);
          }
        }
        xml.write("</folders>\n");

        // Totals
        xml.write("<totals messages=\"" + iTotalMsgs + "\" unread=\"" + iTotalNewMsgs + "\"/>\n");

      } catch (Exception e) {
      }

      xml.write("</" + xslTag + ">\n");
      return xml;
    }

    /**
     * Prompt user for a folder name
     * @param the servlet request object
     * @param the servlet response object
     * @param the JspWriter object
     */
    private void enterFolderName (StringWriter xml) {
      enterFolderName (xml, null);
    }
    private void enterFolderName (StringWriter xml, String oldName)
    {
      try {
        // Navigation bar
        printNavBar (xml);

        // Header and title
        printHeader ("Folders", xml);

        // Prompt for new folder name
        String command;
        xml.write("<enterFolder");
        if (oldName == null) {
          command = createFolderButtonTxt;
        } else {
          xml.write (" oldname=\"" + oldName + "\"");
          command = renameFolderNameButtonTxt;
        }
        xml.write(" mode=\"" + command + "\"/>\n");
      } catch (MessagingException me) {
      } catch (IOException ioe) {
      }
    }
  }

  private class Template extends XmlMethod {
    public Template() {
      weAre = "mailstatus";
    }

    public void doit(ChannelRuntimeData rd) {
      if (DEBUG) System.err.println("doit for " + weAre);
    }

    public StringWriter respond(String submitValue) {
      StringWriter xml = new StringWriter();
      return xml;
    }

    public StringWriter display() {
      StringWriter xml = new StringWriter();
      return xml;
    }
  }

  private class MailStatus extends XmlMethod {
    public MailStatus() {
      weAre = "mailstatus";
    }

    public StringWriter display() {
      StringWriter xml = new StringWriter();
      if (DEBUG) System.err.println("renderString for " + weAre);
      String messageCount;
      try {
        messageCount = inbox.getUnreadMessageCount() + "";
      } catch (MessagingException me) {
        messageCount = "unavailable";
      }

      xml.write("<" + weAre + ">\n");
      xml.write("<unread>" + messageCount + "</unread>");
      xml.write("</" + weAre + ">\n");
      return xml;
    }
  }

  private class SetupMethod extends XmlMethod {
    public SetupMethod() {
      weAre = "setup";
    }

    public StringWriter respond(String submitValue) throws Exception {
      if (submitValue.equals("Configure")) {
        String fullHeaders = runtimeData.getParameter ("fullheaders");
        if (fullHeaders != null) {
          userPrefs.showFullHeaders = fullHeaders.equals("on");
        } else {
          userPrefs.showFullHeaders = false;
        }

        if (DEBUG) System.err.println("returning to " + runtimeData.getParameter("returnTo"));
      }
          if (true) {
            activeMethod = listMessages;
            return activeMethod.display();
          } else {
           redirect(runtimeData, runtimeData.getParameter("returnTo"), "");
          }
      return null;
    }

    public StringWriter display() {
      StringWriter xml = new StringWriter();
      xml.write("<" + weAre + ">\n");
      xml.write("<fullheaders " + (userPrefs.showFullHeaders ? " fullheaders=\"on\"" : "") + "/>");
      xml.write("</" + weAre + ">\n");

      return xml;
    }
  }

  private class ComposeMessage extends XmlMethod {
    private int MAX_ATTACHMENTS = 5;
    private Part[] includeAttachments = null; // Attachments from a Draft message needed when sending draft
    private Message msg = null;

    public ComposeMessage() {
      weAre = "composeMessage";
    }

    public void finalize() {
      includeAttachments = null; // Attachments from a Draft message needed when sending draft
      msg = null;
    }


    public StringWriter respond(String submitValue) throws Exception {
      StringWriter xml = new StringWriter();
      xml.write("<" + weAre + ">\n");

        msg = constructMessage ();

        // If the user clicked "Return to folder"
        if (submitValue.equals ("Return to folder")) {
          if (true) {
            activeMethod = listMessages;
            return activeMethod.display();
          } else {
            redirect(runtimeData, listMessages.getWeAre(), "");
          }
          return null;
        } else if (submitValue.startsWith("Remove ")) {
        } else {
          printNavBar (xml);

          // If the user clicked "Send", send the message
          if (submitValue.equals ("Send")) {
            // Send the message

            try {
              Transport.send (msg);
              metrics.messagesSent++;

              // Save copy in sent folder
              msg.setSentDate (new Date ());
              Message[] msgs = new Message[1];
              msgs[0] = msg;
              Folder sent = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + config.sSentName);

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
              String replyMsg = runtimeData.getParameter("replymsg");
              if (replyMsg != null) {
                int iReplyMsg = Integer.parseInt(replyMsg);
                Message[] oldMsg = new Message[1];
                oldMsg[0] = activeFolder.getMessage(iReplyMsg);
                Flags flags = new Flags (Flags.Flag.ANSWERED);
                flags.add (Flags.Flag.SEEN);
                activeFolder.folder.setFlags (oldMsg, flags, true);
              }

              xml.write("<sentmsg><subject>" + HTMLescape (msg.getSubject ()) + "</subject><savedFolder>" + config.sSentName + "</savedFolder></sentmsg>");
              includeAttachments = null;
              msg = null;
            } catch (AddressException ae) {
              return displayErrorMsg (ae, false);
            } catch (SendFailedException sfe) {
              return displayErrorMsg (sfe, false);
            }
          }

          // If the user clicked "Save as draft", construct message and save it in drafts folder
          else if (submitValue.equals ("Save as draft")) {
            try {
              msg.setSentDate (new Date ());
              Message[] msgs = new Message[1];
              msgs[0] = msg;
              if (activeFolder != null &&
                activeFolder.getFolderName ().equals (sDraftsName)) {
                activeFolder.folder.appendMessages (msgs);
                activeFolder.folder.expunge ();    // Remove old copies of draft
              } else {
                Folder drafts = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + sDraftsName);

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

              xml.write("<savedmsg><subject>" + HTMLescape (runtimeData.getParameter("subject")) + "</subject><folder>" + sDraftsName + "</folder></savedmsg>");
              includeAttachments = null;
              msg = null;
            } catch (AddressException ae) {
              return displayErrorMsg (ae, false);
            }
          }

          // If the user clicked "Cancel",
          else if (submitValue.equals ("Cancel")) {
            xml.write("<cancelled/>");
            includeAttachments = null;
            msg = null;
          } else if (submitValue.equals("To:")) {
            return display();
          }
        }

      xml.write("</" + weAre + ">\n");
      return xml;
    }

    private String sAction;
    private int prevMsgIndex;
    public void doit(ChannelRuntimeData rd) {
      if (DEBUG) System.err.println("doit for " + weAre);
      runtimeData = rd;
      String value;
      if ((value = runtimeData.getParameter("mode")) != null) {
        setAction(value);
      }
      if ((value = runtimeData.getParameter("prevMsg")) != null) {
        setPrevMsg(Integer.parseInt(value));
      }
    }
    public void setAction(String action) {
      sAction = action;
    }
    public void setPrevMsg(int prevMsgIndex) {
      this.prevMsgIndex = prevMsgIndex;
    }
    public StringWriter display() throws Exception {
      StringWriter xml = new StringWriter();
      xml.write("<" + weAre + ">\n");

        metrics.showMetrics = true;
        // Might be coming from forward or reply...
        Message prevMsg = null;
        MessageParts msgParts = null;
        String sTo = "";
        String sCc = "";
        String sBcc = "";
        String sSubject = "";
        String sIncludedMsgText = "";
        boolean repliedTo = false;

        if (DEBUG) System.err.println("mode=" + sAction);
        if (DEBUG) System.err.println("compose msg = " + msg);
        if (sAction != null) {
          prevMsg = activeFolder.getMessage (prevMsgIndex);
          if (DEBUG) System.err.println("prevMsg=" + prevMsgIndex);
          msgParts = new MessageParts (prevMsg);
          includeAttachments = null;
          if (sAction.equals ("forward")) {
              sSubject = "Fwd: " + HTMLescape (msgParts.getSubject ());
              sIncludedMsgText = getIncludedMessageText (msgParts);
              includeAttachments = msgParts.getAttachments ();
          } else if (sAction.equals ("reply")) {
              repliedTo = true;
              sTo = msgParts.getFrom ();
              sSubject = "Re: " + HTMLescape (msgParts.getSubject ());
              sIncludedMsgText = getIncludedMessageText (msgParts);
          } else if (sAction.equals ("replyAll")) {
              repliedTo = true;
              sTo = msgParts.getFrom () + ", " + msgParts.getTo ();
              sCc = msgParts.getCc ();
              sSubject = "Re: " + HTMLescape (msgParts.getSubject ());
              sIncludedMsgText = getIncludedMessageText (msgParts);
          } else if (sAction.equals ("draft")) {
              sTo = msgParts.getTo ();
              sCc = msgParts.getCc ();
              sBcc = msgParts.getBcc ();
              sSubject = HTMLescape (msgParts.getSubject ());
              sIncludedMsgText = getIncludedMessageText (msgParts, true, userPrefs.showFullHeaders);
              includeAttachments = msgParts.getAttachments ();
          }
        } else if (msg != null) {
          if (DEBUG) System.err.println("Picking up compose message");
          msgParts = new MessageParts(msg);
          sTo = msgParts.getTo ();
          sCc = msgParts.getCc ();
          sBcc = msgParts.getBcc ();
          sSubject = HTMLescape (msgParts.getSubject ());
          sIncludedMsgText = getIncludedMessageText (msgParts, true, false);
          includeAttachments = msgParts.getAttachments ();
        }

        // Navigation bar
        printNavBar (xml);

        // Header and title
        printHeader ("Compose", xml);

        if (repliedTo) {
          xml.write("<hidden name=\"replymsg\" value=\"" + runtimeData.getParameter ("msg") + "\"/>\n");
        }

        // Message Controls
        xml.write("<controls bgcolor=\"" + userPrefs.sControlColor + "\">");
        xml.write("<button>Send</button>\n");
        xml.write("<button>Save as draft</button>\n");
        xml.write("<button>Cancel</button>\n");
        if (sAction != null) {
          xml.write("<button>Return to folder</button>\n");
        }
        xml.write("</controls>\n");

        // Message headers
        xml.write("<recipient tag=\"To\">" + sTo + "</recipient>\n");
        xml.write("<recipient tag=\"Cc\">" + sCc + "</recipient>\n");
        xml.write("<recipient tag=\"Bcc\">" + sBcc + "</recipient>\n");
        xml.write("<subject>" + sSubject + "</subject>\n");


        xml.write("<body>" + sIncludedMsgText +"</body>\n"); // If coming from forward or reply

        // Attachments
        xml.write("<attachments>");
        int attachments = MAX_ATTACHMENTS;
        if (includeAttachments != null) {
          for (int i = 0; i < MAX_ATTACHMENTS && i < includeAttachments.length; i++) {
            xml.write("<attachment>" + includeAttachments[i].getFileName() + "</attachment>\n");
            attachments--;
          }
        }
        for (int i = 0; i < attachments; i++) {
          xml.write("<getattachment/>");
        }
        xml.write("</attachments>\n");

      xml.write("</" + weAre + ">\n");
      return xml;
    }

    /**
     * Construct a message object from compose inputs
     * @param the HashMap of parameters
     * @param ArrayList of attachments as MimeBodyParts
     * @returns assembled Message object
     */
    private Message constructMessage () throws IOException, MessagingException
    {
      String sTo = runtimeData.getParameter("To");
      String sCc = runtimeData.getParameter("Cc");
      String sBcc = runtimeData.getParameter("Bcc");
      String sSubject = runtimeData.getParameter("subject");
      String sBody = runtimeData.getParameter("body");
      if (DEBUG) System.err.println("Body text: " + sBody);
      if (DEBUG) System.err.println("To text: " + sTo);
      Message msg = new MimeMessage (session);
      msg.setFrom (new InternetAddress (sUserEmail, sFullName));

      if (sTo != null && sTo.length () > 0) {
          msg.setRecipients (Message.RecipientType.TO, cleanAddressList(sTo));
      }

      if (sCc != null && sCc.length () > 0) {
          msg.setRecipients (Message.RecipientType.CC, cleanAddressList(sCc));
      }

      if (sBcc != null && sBcc.length () > 0) {
          msg.setRecipients (Message.RecipientType.BCC, cleanAddressList(sBcc));
      }

      msg.setSubject (sSubject != null ? sSubject : "[none]");

      msg.addHeader ("X-Mailer", "uPortal WEB email client " + clientVersion);
      msg.addHeader ("Organization", config.sOrganization);

      ArrayList attachments = new ArrayList(MAX_ATTACHMENTS);
      org.jasig.portal.MultipartDataSource[] fileParts = (org.jasig.portal.MultipartDataSource[]) runtimeData.getObjectParameterValues("attachment");
      if (fileParts != null) {
        for (int i = 0; i < fileParts.length; i++) {
          if (DEBUG) System.err.println("attachment=" + fileParts[i]);
          if (fileParts[i] != null) {
            if (DEBUG) System.err.println("found filepart " + fileParts[i].getName());
            attachments.add(fileParts[i]);
          }
        }
      }

      if (attachments.size () > 0 ||
        (includeAttachments != null && includeAttachments.length > 0)) {
        MimeMultipart mp = new MimeMultipart ();

        // First add text of message
        if (sBody != null && sBody.trim ().length () > 0) {
          MimeBodyPart body = new MimeBodyPart ();
          body.setDisposition (Part.INLINE);
          body.setContent (sBody, "text/plain");
          mp.addBodyPart (body);
          if (DEBUG) System.err.println("Added body text");
        }

        if (includeAttachments != null) { // Attachments from draft message
          for (int i = 0; i < includeAttachments.length; i++) {
            MimeBodyPart bp = new MimeBodyPart ();
            bp.setDisposition (Part.ATTACHMENT);
            bp.setDataHandler (includeAttachments[i].getDataHandler ());

            bp.setFileName (attachmentName(includeAttachments[i], i));
            mp.addBodyPart (bp);
          }
          includeAttachments = null;
        }

        for (int i = 0; i < attachments.size(); i++) {
          org.jasig.portal.MultipartDataSource filePart = (org.jasig.portal.MultipartDataSource) attachments.get(i);
          MimeBodyPart bp = new MimeBodyPart ();
          bp.setDisposition (Part.ATTACHMENT);
          bp.setFileName (filePart.getName());
          if (DEBUG) System.err.println("Attaching " + filePart.getName());
          bp.setDataHandler(new DataHandler(filePart));
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
     * Clean up a recipient list. Basically this involves replacing ";" (Outlook people)
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
   * Format message text by appending headers and '>' to each line
   * @param the message being forwarded or replied to
   * @return message text
   */
  private String getIncludedMessageText (MessageParts msgParts) throws IOException, MessagingException{
    return getIncludedMessageText(msgParts, false, userPrefs.showFullHeaders);
  }
  /**
   * Format message text by appending headers and '>' to each line
   * @param the message being forwarded or replied to
   * @param Whether message is a draft message
   * @return message text
   */
  private String getIncludedMessageText (MessageParts msgParts, boolean bIsDraft, boolean showFullHeaders) throws IOException, MessagingException
  {
    Part bodyText = msgParts.getBodyText ();
    StringBuffer sbMsgText = new StringBuffer ();
    if (DEBUG) System.err.println("body text recovered: " +  bodyText);
    // Return empty string if message is not of type TEXT/PLAIN
    if (bodyText == null || !bodyText.isMimeType ("text/*")) {
      return "";
    } else if (bIsDraft) {
      BufferedReader in = new BufferedReader (new InputStreamReader (bodyText.getInputStream ()));
      try {
        String line;
        while ( (line = in.readLine ()) != null) {
          sbMsgText.append (HTMLescape(line));
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
          sbMsgText.append ("> " + HTMLescape(line) + "\n");
        }
      } finally {
        in.close ();
      }
    }

    return sbMsgText.toString ();
  }

  }

  // rendering Layer

  /**
   * Passes ChannelRuntimeData to the channel.
   * This function is called prior to the renderXML() call.
   * @param rd channel runtime data
   * @see ChannelRuntimeData
   */
  public void setRuntimeData(ChannelRuntimeData rd) {
    runtimeData = rd;

    if (!authenticated) {
      activeMethod = authenticateMethod;
    } else {
      String methodAskedFor = runtimeData.getParameter("action");
      if (methodAskedFor != null) {
        activeMethod = findJumptableEntry(methodAskedFor);
        if (activeMethod == null) {
          if (DEBUG) System.err.println("missing method " + methodAskedFor);
          activeMethod = statusMethod;
        }
      } else {
        activeMethod = statusMethod;
      }
    }

    try {
      activeMethod.doit(rd);
    } catch (Exception e) {
      activeMethod.setException(e);
    }
  }

  /**
   * Ask channel to render its content.
   * @param out the SAX ContentHandler to output content to
   */
  public void renderXML (ContentHandler out) {
    String xmlHeader = "<?xml version=\"1.0\"?>\n";
      try {
        String weAre;
        String xmlString = null;
        try {
          try {
            if (!authenticated) {
              if (DEBUG) System.err.println("renderXML unauthorized");
              authenticateMethod.renderString();
              weAre = activeMethod.getWeAre(); // Now status method
                xmlString =  activeMethod.renderString();
            } else {
              if (DEBUG) System.err.println("renderXML authorized");
              checkIMAPConnection();
              xmlString =  activeMethod.renderString();
            }
          } catch (CIMAPLostConnectionException clce) {
            if (DEBUG) System.err.println("renderXML lostconnection");
            activeMethod = listMessages;
            xmlString =  listMessages.renderString();
          }
        } catch (Exception e) {
          activeMethod = listMessages;
          xmlString = displayErrorMsg(e, true).toString();
          LogService.instance().log(LogService.ERROR, e);
        }
        if (xmlString != null) {
          weAre = activeMethod.getWeAre();
          if (DEBUG) System.err.println(weAre + ":" + xmlString);

          XSLT xslt = new XSLT(this);
          xslt.setXML(xmlString);
          xslt.setXSL(sslLocation, weAre, runtimeData.getBrowserInfo());
          xslt.setTarget(out);
          xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
          xslt.setStylesheetParameter("workerActionURL", runtimeData.getWorkerActionURL(PortalSessionManager.FILE_DOWNLOAD));
          xslt.transform();
        }
      } catch (Exception e) {
        LogService.instance().log(LogService.ERROR, e);
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
    if (sFolder.equalsIgnoreCase (config.sMailbox)) {
      if (DEBUG) System.err.println("reusing INBOX");
      return inbox;
    } else {
      if (DEBUG) System.err.println("opening " + sFolder);
      return new ActiveFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + sFolder);
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
    StringBuffer sbFlags = new StringBuffer ();

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
   * Outputs a navigation bar containing links to
   * Inbox, Folders, Compose, Address, Set-ups, and Help
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  private void printNavBar (StringWriter xml) {
    xml.write("<navigationBar bgcolor=\"" + userPrefs.sNavBarColor +
         "\" inbox=\"" + config.sMailbox + "\" returnMethod=\"" + activeMethod.getWeAre() + "\"/>\n");
  }

  /** is this a special folder
   * @param Foldername to check
   */
  private static boolean isSpecialFolder (String folderName) {
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
  private static final String[][] htmlCharacters= new String[][] {
    {"<", "&lt;"},
    {">", "&gt;"},
    {"&", "&amp;"},
    {"'", "&#039;"},
    {"\"", "&quot;"},
  };

  private static String replaceHTMLcharacter (String character) {
    for (int i = 0; i < htmlCharacters.length; i++) {
      if (character.equals (htmlCharacters[i][0])) {
        return htmlCharacters[i][1];
      }
    }
    return character;
  }

  private static String HTMLescape (String text) {
    if (text != null) {
      StringBuffer newText = new StringBuffer (text.length ());
      for (int i = 0; i < text.length (); i++) {
        String charAt = text.substring (i, i+1);
        newText.append (replaceHTMLcharacter (charAt));
      }
      return newText.toString ();
    } else {
      return "null string";
    }
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
   * Deduce you say, the mime type
   * @param Body part to examine
   * @result Mime type string
   */
  private static String mimeType(Part attachment) throws MessagingException {
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
  private static String attachmentName (Part attachment, int index) {
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
   * Present error message to the user
   * @param Exception object
   * @param Whether we should print navigation bar
   * @param JspWriter object
   */
  private StringWriter displayErrorMsg (Exception e, boolean navigationBar) {
    return displayErrorMsg (new String[] {HTMLescape(e.getMessage ())}, navigationBar);
  }
  /**
   * Present error message to the user
   * @param Error message to display
   * @param Whether we should print navigation bar
   * @param JspWriter object
   */
  private StringWriter displayErrorMsg (String errorMsg, boolean navigationBar) {
    return displayErrorMsg (new String[] {errorMsg}, navigationBar);
  }
  /**
   * Present error message to the user
   * @param Array of error messages to display
   * @param Whether we should print navigation bar
   * @param JspWriter object
   */
  private StringWriter displayErrorMsg (String[] errorMsg, boolean navigationBar) {
    StringWriter xml = new StringWriter();

    xml.write("<errors>");
    if (navigationBar) {
      printNavBar (xml);
    }

    for (int i = 0; i < errorMsg.length; i++) {
      xml.write("<error>" + errorMsg[i] + "</error>");
    }
    xml.write("</errors>");

    return xml;
  }

  /**
   * Print the client header
   * @param the client name
   * @param the JspWriter object
   */
  private void printHeader (String sTitle, StringWriter xml) throws IOException, MessagingException
  {
    printHeader(sTitle, -1, xml);
  }
  private void printHeader(String title, int unread, StringWriter xml) {
    xml.write("<headerBar bgcolor=\"" + userPrefs.sHeaderColor + "\"" +
         " version=\"" + clientVersion + "\"" +
         " caption=\"" + config.sCaption + "\"");
    try {
      if (inbox != null && inbox.mailCheck ()) {
        xml.write(" newmail=\"yes\"");
      }
    } catch (MessagingException me) {
    }
    if (unread >= 0) {
      xml.write(" unread=\"" + unread + "\"");
    }
    xml.write(">" + HTMLescape(title) + "</headerBar>\n");
  }


  /** Initialize our world
   * @param the session object
   */
  private void initialize() throws Exception, AuthenticationFailedException {
    initialize(sUser, sPassword);
  }
  private void initialize (String username, String password) throws Exception, AuthenticationFailedException {
    if (username == null && password == null) {
      throw new AuthenticationFailedException ("");
    }

    if (username == null || password == null) {
      throw new AuthenticationFailedException ("Invalid Username/Password");
    }

    try {
      urlName = new URLName (config.sProtocol, config.sIMAPHost, config.iIMAPPort, config.sMailbox, username, password);
      if (DEBUG) System.err.println(urlName.toString());
      sUser = username;
      sPassword = password;

      session = Session.getDefaultInstance (props, null);
      session.setDebug(false);
      store = session.getStore (urlName);
      store.addStoreListener (storeListener);
      store.connect ();
      sUserEmail = sUser + "@" + config.sDomainName;

      if (inbox == null) {
        inbox = new ActiveFolder (config.sMailbox);
        inbox.setDontClose (true); // Keep open for duration of channel
      }

      folderSeparator = inbox.folder.getSeparator ();

      activeFolder = inbox;

      if (imapFolders == null) {
        imapFolders = new ImapFolder (1); // At least inbox
        imapFolders.add (inbox);

        Folder folderDir;
        if (config.sFolderDir == null) {
          folderDir = inbox.folder;
        } else {
          folderDir = getFolder (config.sFolderDir);
          if (!folderDir.exists ()) {
            folderDir.create (Folder.HOLDS_FOLDERS);
          }
        }

        Folder[] storeFolders = folderDir.list ();
        if (DEBUG) System.err.println("storeFolders="+storeFolders);
        if (storeFolders != null) {
          if (DEBUG) System.err.println(storeFolders.length + " folders found");
          imapFolders.ensureCapacity (storeFolders.length + 1);
          for (int i = 0; i < storeFolders.length; i++) {
            try {
              if (storeFolders[i].exists ()) {
                imapFolders.add (storeFolders[i].getName());
              }
            } catch (Exception e) {
              System.err.println(e);
            }
          }
        }

        store.addFolderListener (folderListener);
        authenticated = true;
        if (DEBUG) System.err.println("authenticated");
      }
    } catch (Exception e) {
      if (DEBUG) System.err.println(e);
      cleanup ();
      throw e;
    }

    ((ListMessages)listMessages).reset();
    ((ListFolders)listFolders).reset();
  }

  /** Clean up in preparation to shut down
   */
  private void cleanup () {
    try {
      store.removeStoreListener (storeListener);
      store.removeFolderListener (folderListener);
    } catch (Exception e) { /* ignore */ }


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
  private void reconnect () throws Exception, AuthenticationFailedException {
    System.err.println("reconnecting");
    cleanup ();
    initialize ();
  }

  private void checkIMAPConnection () throws Exception, CIMAPLostConnectionException, AuthenticationFailedException {
    Thread.yield ();
    try {
      if (inbox != null) {
        int a = inbox.folder.getUnreadMessageCount ();
      } else {
        if (store != null) {
          store.close();
        }
      }
    } catch (MessagingException me) {
      try {
        if (store != null) {
          store.close ();
        }
      } catch (MessagingException me2) {
        //
      }
    }

    if (store == null || !store.isConnected ()) {
      LogService.instance().log(LogService.DEBUG, "Lost connection to store, re-initializing.");

      try {
        reconnect ();
        throw new CIMAPLostConnectionException ();
      } catch (Exception e) {
        if (e instanceof CIMAPLostConnectionException) {
          throw (CIMAPLostConnectionException)e;
        }
        LogService.instance().log(LogService.ERROR, "checkIMAPConnection:" + e);
        throw e;
      }
    }
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

          myOut.println ("<mailstate>You have " + count + " unread message" + (count == 1 ? "" : "s") + ":" +
                  (newMsgs ? "1" : "0") + "</mailstate>");
        } catch (FolderClosedException fce) {
          myOut.println ("<mailstate>Connection to mail server unavailable</mailstate>");
        } catch (MessagingException me) {
          myOut.println ("<mailstate>Connection to mail server unavailable</mailstate>");
        }
      }
      myOut.close ();
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, "checkIMAPConnection:"+e);
    }
  }


  static private int ORDER_BY_FROM (Object o1, Object o2, int order)
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
        LogService.instance().log(LogService.WARN, e);
      }
    return 0;
  };

  static private Comparator ORDER_BY_FROM_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_FROM (o1, o2, 1);
    }
    };

  static private Comparator ORDER_BY_FROM_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_FROM (o1, o2, -1);
    }
    };

  static private int ORDER_BY_SUBJECT (Object o1, Object o2, int order)
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
        LogService.instance().log(LogService.WARN, e);
      }
    return 0;
  };

  static private Comparator ORDER_BY_SUBJECT_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SUBJECT (o1, o2, 1);
    }
    };

  static private Comparator ORDER_BY_SUBJECT_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SUBJECT (o1, o2, -1);
    }
    };

  static private int ORDER_BY_DATE (Object o1, Object o2, int order)
  {
    try {
        Date d1 = ( (Message) o1).getSentDate ();
        Date d2 = ( (Message) o2).getSentDate ();
        if (d1 == null || d2 == null) {
          return -1;
        } else {
          return d1.compareTo (d2) * order;
        }
    } catch (Exception e) {
        LogService.instance().log(LogService.WARN, e);
    }
    return 0;
  };

  static private Comparator ORDER_BY_DATE_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_DATE (o1, o2, 1);
    }
  };

  static private Comparator ORDER_BY_DATE_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_DATE (o1, o2, -1);
    }
  };

  static private int ORDER_BY_SIZE (Object o1, Object o2, int order)
  {
    try {
        Message m1 = (Message) o1;
        Message m2 = (Message) o2;
        Integer m1size = new Integer (m1.getSize ());
        Integer m2size = new Integer (m2.getSize ());
        return m1size.compareTo (m2size) * order;
    } catch (Exception e) {
        LogService.instance().log(LogService.WARN, e);
    }
    return 0;
  };

  static private Comparator ORDER_BY_SIZE_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SIZE (o1, o2, 1);
    }
  };

  static private Comparator ORDER_BY_SIZE_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_SIZE (o1, o2, -1);
    }
  };

  static private int ORDER_BY_NAME (Object o1, Object o2, int order)
  {
    try {
        Folder f1 = (Folder) o1;
        Folder f2 = (Folder) o2;
        return f1.getFullName ().compareToIgnoreCase (f2.getFullName ()) * order;
    } catch (Exception e) {
        LogService.instance().log(LogService.WARN, e);
    }
    return 0;
  };

  static private Comparator ORDER_BY_NAME_ASCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_NAME (o1, o2, 1);
    }
  };

  static private Comparator ORDER_BY_NAME_DESCENDING = new Comparator ()
  {
    public int compare (Object o1, Object o2)
    {
      return ORDER_BY_NAME (o1, o2, -1);
    }
  };

  static private Comparator ORDER_BY_FOLDERNAME = new Comparator () {
    public int compare (Object o1, Object o2) {
      try {
        String f1 = (String) o1;
        String f2 = (String) o2;
        return f1.compareToIgnoreCase (f2);
      } catch (Exception e) {
        LogService.instance().log(LogService.WARN, e);
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
      if (specialFolders != null) {
        specialFolders.clear ();
        specialFolders = null;
      }

      if (normalFolders != null) {
        normalFolders.clear ();
        normalFolders = null;
      }
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
      if (folder.isOpen()) {
        folder.close (false); // Don't need it any more
      }
    }

    private void addFolder (String folderName) {
      if (duplicate (folderName)) { // Already been seen
        return;
      }

      if (DEBUG) System.err.println("Adding folder " + folderName);
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
    public FolderData[] getFolders () throws MessagingException {
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
      if (index >= super.size()) {
        folderData.exists =false;   // fix foldersEndAt GNL !!!
        return null;
      }
      String folderName = (String) super.get (index);

      Folder folder;
      if (folderName.equalsIgnoreCase (inbox.getFolderName ())) {
        folder = inbox.folder;
      } else {
        folder = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator)+folderName);
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
              LogService.instance().log(LogService.ERROR, "messageListener:"+e);
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
          //LogService.instance().log(LogService.DEBUG, "received connection opened event for " + e);
        }
        public void closed (ConnectionEvent e) {
          //LogService.instance().log(LogService.DEBUG, "received connection closed event " + e);
        }
        public void disconnected (ConnectionEvent e) {
          //LogService.instance().log(LogService.DEBUG, "received connection disconnected event " + e);
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
      if (DEBUG) System.err.println("Search found " + searchMsgs.size()  + " messages");
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
        if (!getFolderName().equals(config.sTrashName) || // Not in trash folder
          !sCopyFolder.equals(config.sTrashName)) {    // or not copying to trash
          Folder copyFolder;

          if (inbox.getFolderName().equalsIgnoreCase(sCopyFolder)) {
            copyFolder = inbox.folder;
          } else {
            copyFolder = getFolder ( (config.sFolderDir == null ? "" : config.sFolderDir + folderSeparator) + sCopyFolder);

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
      sTo = formatRecipient(Message.RecipientType.TO);
      sCc = formatRecipient(Message.RecipientType.CC);
      sBcc = formatRecipient(Message.RecipientType.BCC);
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
        sFrom += "<addresses>" + formatAddress((InternetAddress) addresses[0]) + "</addresses>";
      }
    }

    private String formatRecipient(Message.RecipientType type) throws MessagingException {
      String recipients = "";
      if ( (addresses = msg.getRecipients (type)) != null) {
        recipients += "<addresses>";
        for (int iAddr = 0; iAddr < addresses.length; iAddr++) {
          recipients += formatAddress((InternetAddress) addresses[iAddr]);
        }
        recipients += "</addresses>";
      }
      if (DEBUG) System.err.println("addresses: " + recipients);
      return recipients;
    }
    private String formatAddress(InternetAddress ia) {
      String address = "<address>";
      String sPersonal = ia.getPersonal ();
      String sAddress = ia.getAddress ();
      if (sPersonal != null) {
        address += "<personal>" + HTMLescape(sPersonal) + "</personal><email>" + HTMLescape(sAddress) + "</email>";
      } else {
        address += "<email>" + sAddress + "</email>";
      }
      address += "</address>";
      if (DEBUG) System.err.println("addess: " + address);
      return address;
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
          if (DEBUG) System.err.println("disposition: " + part.getDisposition());
          if (bodyText == null && (part.getDisposition() == null ||  part.getDisposition().equalsIgnoreCase("inline")) &&
            (part.isMimeType ("text/plain") || part.isMimeType ("text/html"))) {
            bodyText = part;
            if (DEBUG) System.err.println("Found text bodytext at " + i);
          } else if (bodyText != null && (part.getDisposition() == null ||  part.getDisposition().equalsIgnoreCase("inline")) &&
                bodyText.isMimeType ("text/plain") && part.isMimeType ("text/html")) {
            bodyText = part; // Choose html over plain text
            if (DEBUG) System.err.println("Found html bodytext at " + i);
          } else if ((part.getDisposition() != null && part.getDisposition().equalsIgnoreCase("attachment")) ||
                  !(bodyText != null && bodyText.isMimeType ("text/html") && part.isMimeType ("text/plain"))) {
            if (DEBUG) System.err.println("Found attachment " + i + "=" + part.getFileName());
            attachments.add (part);
          }
        }

      } else if (part.isMimeType ("multipart/*")) {
        Multipart mp = (Multipart) part.getContent ();

        for (int i = 0; i < mp.getCount (); i++) {
          findAttachments (mp.getBodyPart (i));
        }
      } else {
          if (DEBUG) System.err.println("disposition (single): " + part.getDisposition());
          if (bodyText == null && (part.getDisposition() == null || part.getDisposition().equalsIgnoreCase("inline")) &&
            (part.isMimeType ("text/plain") || part.isMimeType ("text/html"))) {
            bodyText = part;    // Grab the first displayable body part
          } else {
            attachments.add (part);
          }
      }
    }
  }


  /**
   * IMimeResponse processing
   */
  private String downloadContentType;
  private InputStream downloadInputStream;
  private String downloadName;
  private Map downloadHeaders;
  public String getContentType() {
    return downloadContentType;
  }
  public InputStream getInputStream() {
    return downloadInputStream;
  }

  public void downloadData(OutputStream out) throws IOException {
    activeMethod.download(out);
  }

  public String getName() {
    return downloadName;
  }
  public Map getHeaders() {
    return downloadHeaders;
  }

  public void freeResponseResources() {
    if (downloadInputStream != null) {
      try {
        downloadInputStream.close();
      } catch (IOException ioe) {
        LogService.instance().log(LogService.ERROR, "CIMAPMail::freeResponseResources: " + ioe);
      }
    }
    downloadContentType = null;
    downloadName = null;
    if (downloadHeaders != null) {
      downloadHeaders.clear();
      downloadHeaders = null;
    }
  }
}
