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

package org.jasig.portal.channels.imapmail;

//Title:
//$Header$
//Author:      George Lindholm
//Company:     ITServices
//Description: Real time mail check applet for browsers. Uses 1.0 methods due to
//              broken Mac browsers
//package applets;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

public final class MailCheck extends Applet {
  Color backgroundColour;
  Color foregroundColour;
  Label msgText;
  String portURL;
  String readUrl;
  String composeUrl;
  String checkMailUrl;
  int checkTime;
  Thread msgThread;
  boolean threadStop = false;
  static final boolean DEBUG = false;

  static String mailstateTag = "<mailstate>";

  //Get a parameter value
  public String getParameter(String key, String def) {
    return (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public MailCheck() {
  }


  // Not in the 1.0 Color class
  public static Color decodeColor(String nm) throws NumberFormatException {
    Integer intval = Integer.decode(nm);
    int i = intval.intValue();
    return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
  }

  //Initialize the applet
  public void init() {

    readUrl = this.getParameter("readMailUrl");
    if (readUrl == null) {
      System.err.println("Unable to retrieve applet parameters");
      return;
    }
    composeUrl = this.getParameter("composeMailUrl");
    checkMailUrl = this.getParameter("checkMailUrl");
    try {
      checkTime = Integer.parseInt(getParameter("checkTime"));
    } catch(Exception e) {
      e.printStackTrace();
      checkTime = 1;
    }
    try {
      backgroundColour = decodeColor(this.getParameter("backgroundColor"));
    } catch(Exception e) {
      e.printStackTrace();
      backgroundColour = new Color(0xffffff);
    }
    try {
      foregroundColour = decodeColor(this.getParameter("foregroundColor"));
    } catch(Exception e) {
      e.printStackTrace();
      foregroundColour = new Color(0x000000);
    }

    String portalHost = getCodeBase().getHost();
    int portalPort = getCodeBase().getPort();
    String portalProtocol = getCodeBase().getProtocol();
    String portalFile = getCodeBase().getFile();

    portURL = portalProtocol + "://" + portalHost + ":" + portalPort;
    try {
      if (true) {
        jbInit();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void callMethod(String methodUrl) {
    try {
      URL url = new URL(portURL + methodUrl);
      if (DEBUG) System.err.println("->URL: " + url);
      getAppletContext().showDocument(url);
      if (DEBUG) System.err.println("<-URL" + url);
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  Button readMail = new Button("Read Mail");
  Button composeMail = new Button("Compose");
  //Component initialization
  private void jbInit() throws Exception {
    setLayout(new FlowLayout(FlowLayout.LEFT));
    Panel p1 = new Panel();
    Panel p2 = new Panel();
    p1.setLayout(new FlowLayout(FlowLayout.LEFT));
    p2.setLayout(new FlowLayout(FlowLayout.LEFT));
    add("north", p1);
    add("south", p2);
    setBackground(backgroundColour);
    setForeground(foregroundColour);
    msgText = new Label("Checking for unread messages on server");
    p1.add(msgText);
    try {
      Dimension d = getSize(); // 1.1 support
      if (DEBUG) System.err.println("1.1 support");
      /*
      readMail.addActionListener(new ActionListener () {
        public void actionPerformed (ActionEvent a) {
          callMethod(readUrl);
        }
      });
      composeMail.addActionListener(new ActionListener () {
        public void actionPerformed (ActionEvent a) {
          callMethod(composeUrl);
        }
      });
      */
    } catch (NoSuchMethodError nsme) { // 1.0 (triggered by getSize() call)
      if (DEBUG) System.err.println("1.0 support");
    }

    p2.add(readMail);
    p2.add(composeMail);

    msgThread = new Thread() {
      public void run () {
        URL url;

        try {
          url = new URL(portURL + checkMailUrl);
        } catch (MalformedURLException e) {
          msgText.setText("Unable to start thread");
          return;
        }

        while(!threadStop) {
          try {
  	    msgText.setText("Checking for new mail");
            if (DEBUG) System.err.println("->uc: " + url);
            URLConnection uc = url.openConnection();
            if (DEBUG) System.err.println("<-uc: " + url);
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            DataOutputStream dos = new DataOutputStream(uc.getOutputStream());
            dos.close();
            if (DEBUG) System.err.println("wrote request");

            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));

            String mailstate = null;
            if (DEBUG) System.err.println("->read()");
              while ((mailstate = in.readLine()) != null) {
  	        if (mailstate.length() <= mailstateTag.length() ||
  	            !mailstate.startsWith(mailstateTag)) {
  	          continue;
              }

              if (DEBUG) System.err.println("=" + mailstate);
	        int colonPos = mailstate.indexOf(":");
                if (colonPos > 1) {
		  msgText.setText(mailstate.substring(mailstateTag.length(),
                                                      colonPos));
                  if (mailstate.substring(colonPos, colonPos + 2).equals(":1")) {
                    getAppletContext().showStatus("You have new mail");
                  }
		  break;
	      } else {
	        msgText.setText("Bad response from server");
	        if (DEBUG) System.err.println("Read:'" + mailstate + "'");
	      }
            }
            if (DEBUG) System.err.println("<-read()");
            in.close();
          } catch (IOException e) {
            if (DEBUG) System.err.println(e);
            msgText.setText("Server problem");
          }

          try {
            Thread.sleep(checkTime * 60 * 1000);
          } catch (InterruptedException e) {}
        }
      }
    };
  }

  public boolean action(Event evt, Object arg) {
    if (evt.target.equals(readMail)) {
      callMethod(readUrl);
    } else if (evt.target.equals(composeMail)) {
      callMethod(composeUrl);
    } else {
      return super.action(evt,arg);
    }

    return true;
  }

  //Start the applet
  public void start() {
    //repaint();
    msgThread.start();
  }

  //Stop the applet
  public void stop() {
    threadStop = true;
  }

  //Destroy the applet
  public void destroy() {
    threadStop = true;
    msgThread.interrupt();
  }

  //Get Applet information
  public String getAppletInfo() {
    return "Applet Information";
  }

  //Get parameter info
  public String[][] getParameterInfo() {
    String[][] pinfo =
      {
      {"backgroundColor", "Int", "Background colour"},
      {"foregroundColor", "Int", "Foreground colour"},
      {"readMailUrl", "String", "Read mail URL"},
      {"composeMailUrl", "String", "Compose URL"},
      {"checkMailUrl", "String", "Check for new mail URL"},
      {"checkTime", "Int", "How often to check for new mail"},
      };
    return pinfo;
  }
}
