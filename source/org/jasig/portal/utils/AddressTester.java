/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class checks a URL or a webserver hosting a URL. It only allows a specific time allocated for
 * connecting to the URL rather than waiting for a timeout.
 * This class uses the <code>java.util.Timer</code> to schedule a task which is cancelling the attempt of calling
 * httpURLConnection.
 *
 * @author <a href="mailto:kazemnaderi@yahoo.ca">Kazem Naderi</a>
 * @version $Revision$
 * @since uPortal 2.2
 */

public class AddressTester {
    
    private static final Log log = LogFactory.getLog(AddressTester.class);
    
    /**The timer object that takes a timerTask as a parameter when constructed*/
    private static final Timer timer = new Timer();

    /**The connection thread inwhich the connection attempt is made*/
    Thread connectionThread;

    /* urlConnectio to be used */
    private HttpURLConnection urlConnect = null;

    /**The connectioncode returned from connetion attempt*/
    private int connectionCode = 0;

    /**This the url to try. the value is set through the class constructor*/
    private String urlToTry = "";

    /**The amount of time connection attempt can take, the default is 100 ms*/
    int timeToWait = 100;
    static final int defaultTimeToWait = 100;

    /** Get header data only **/
    private boolean headOnly = false;


    /** Debug the code */
    private static boolean DEBUG = false;

    static {
      Runtime.getRuntime().addShutdownHook(new Thread(
      "AddressTester JVM shutdown hook") {
        public void run() {
          timer.cancel();
        }
      });
    }


    /**
     * Constructor
     * @param milliSeconds the number of milliseconds to let the connectioon attempt run
     * @param urlString the String representing a URL
     * @param getHead use setRequestMathod("HEAD")
     */
    public AddressTester(final String urlString, final int milliSeconds, final boolean getHead) throws Exception{
      urlToTry = urlString;
      timeToWait = milliSeconds;
      connectionThread = new Thread() {
        public void run() {
          try {
            URL url = new URL(urlToTry);
            if (DEBUG) {
              System.out.println("URL to try is " + urlToTry);
            }

            RemindTask rt = new RemindTask();
            try {
              timer.schedule(rt, timeToWait);
              urlConnect = (HttpURLConnection) url.openConnection();
              if (headOnly) {
                urlConnect.setRequestMethod("HEAD");
              }
              urlConnect.setInstanceFollowRedirects(false);
              connectionCode = urlConnect.getResponseCode();
            } catch (InterruptedIOException iie) { /* Thread Interrupt */
              if (DEBUG) {
                System.out.println("timed out on " + urlToTry);
              }
              log.info(
                                        "AddressTest::checkURL(): timed out on " +
                                        urlToTry);

            } catch (Exception e) { /* Something went wrong */
              if (DEBUG) {
                System.out.println(urlToTry + " generated exception: " +
                                   e.getMessage());
              }
              log.info(
                                        "AddressTest::checkURL(): "
                                        + urlToTry + " generated exception: " +
                                        e.getMessage());
            } finally {
              rt.cancel();
            }

          } catch (MalformedURLException mue) { /* Garbage In */
            if (DEBUG) {
              System.out.println("Bad URL: " + urlToTry);
            }
            log.error(
                                      "AddressTest::checkURL(): Bad URL: " +
                                      urlToTry);
          }
        }
      };

      connectionThread.start();
    }

    /**
     * Constructor
     * @param urlString
     * @param getHead
     * @throws java.lang.Exception
     */
    public AddressTester(String urlString, boolean getHead) throws Exception {
      this(urlString, defaultTimeToWait, getHead);  }

    /**
     * Constructor
     * @param milliSeconds - the number of milliseconds to let the connectioon attempt run
     * @param urlString - the String representing a URL
     */
    public AddressTester(int milliSeconds, String urlString) throws Exception {
      this(urlString, milliSeconds, false);
    }

    /**
     * Constructor
     * @param urlString the String representing a URL
     */
    public AddressTester(String urlString) throws Exception{
      this(urlString, defaultTimeToWait, false);
    }

    /**
     * This method returns the response code that was set in checkURL ()
     * @return the response code
     */
    public int getResponseCode()
    {
       //make sure we wait for connection thread to finish
       if(connectionThread.isAlive()){
           try{
               connectionThread.join();
           }catch(InterruptedException e){
               return 0;
           }
       }
        return this.connectionCode;
    }


    /**
     * Get the (valid) URL connection
     * @return URL connection
     */
    public URLConnection getConnection() {
      //make sure we wait for connection thread to finish
      if (connectionThread.isAlive()) {
        try {
          connectionThread.join();
        } catch (InterruptedException e) {
          return null;
        }
      }
      return urlConnect;
    }

    /**
     * Shut down the connection
     */
    public void disconnect() {
      if (urlConnect != null) {
        urlConnect.disconnect();
      }
    }

    /**
     *
     * @return <code>false</code> if the address is not available. <code>True</code> otherwise
     */
    public boolean URLAvailable()
    {
       //make sure we wait for connection thread to finish
       if(connectionThread.isAlive()){
           try{
               connectionThread.join();
           }catch(InterruptedException e){
               return false;
           }
       }
       return connectionCode == HttpURLConnection.HTTP_OK;
     }

    /**
     * Class RemidTask
     * @author knaderi
     *
     * This is a TimerTask class that interuupts the connectionThread
     * After the timer scheduled time.
     *
     */
    class RemindTask extends TimerTask {
      public void run() {
        if (connectionThread.isAlive()) {
          if (DEBUG) {
            System.out.println("Canceled");
          }
          connectionThread.interrupt();
        }
      }
    }

    /**
     * This is the main method and is left as a usage sample
     * @param args
     */
    public static void main(String[] args) {
      String[] testUrl = new String[] {
        "http://www.cbcsrc.ca/cbcsrc/1010991/11566",
        "http://www.theweathernetwork.com/weatherbutton/test.js",
        "http://localhost:666/a.nothere",
        "http://www.linuxmandrake.com",
        "https://localhost:443/a.nothere"};
      for (int i = 0; i < testUrl.length; i++) {
        try {
          System.out.println("About to schedule task: " + testUrl[i]);
          AddressTester myReminder = new AddressTester(testUrl[i], false);
          System.out.println("  Running the test URL");
          System.out.println("available: " + myReminder.URLAvailable() + ", HTTP code: " + myReminder.getResponseCode());
        } catch (Exception e) {}
      }
    }

}

