/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class checks a URL or a webserver hosting a URL. It only allows a specific time allocated for
 * connecting to the URL rather than waiting for a timeout.
 * This class user the java.util.timer to schedule a task which is cancelling the attempt of calling
 * httpURLConnection.
 *
 * @author <a href="mailto:kazemnaderi@yahoo.ca">Kazem Naderi</a>
 * @version $Revision$
 * @since uPortal 2.2
 */

public class AddressTester {
    /**The timer object that takes a timerTask as a parameter when constructed*/
    Timer timer;
    
    /**The connection thread inwhich the connection attempt is made*/
    Thread connectionThread;
    
    /**The connectioncode returned from connetion attempt*/
    private int connectionCode = 0;
    
    /**This the url to try. the value is set through the class constructor*/
    private String urlToTry = "";
    
    /**The amount of time connection attempt can take, the deffault is 100 ms*/
    int timeToWait = 100;

    /** The code 200 that corresponds to HttpURlConnection.HTTP_OK*/
    private int HTTP_OK = HttpURLConnection.HTTP_OK; 
 

	/**
	 * Constructor
	 * @param milliSeconds the number of milliseconds to let the connectioon attempt run 
	 * @param urlString the String representing a URL
	 */
    public AddressTester(int milliSeconds, String urlString) throws Exception{
		urlToTry = urlString;
		timeToWait = milliSeconds;		
		checkURL();
    }
    
        /**
         * Constructor
         * @param urlString the String representing a URL
         */
    public AddressTester(String urlString) throws Exception{
                urlToTry = urlString;
                checkURL();
    }

    /**
     * This method uses an annymous inner thread class and attepmts 
     * receving some response codes from the specified URLString 
     * @throws Exception
     */
    public void checkURL() throws Exception
    {
		connectionThread = (new Thread()
		{
				public void run() {
				  try {
					URL url = new URL(urlToTry);
					//System.out.println("URL totry is " + urlToTry);
					URLConnection urlConnect = url.openConnection();
					HttpURLConnection httpUrlConnect = (HttpURLConnection) urlConnect;
					connectionCode = httpUrlConnect.getResponseCode();								
				  }
				  catch(Exception e) {
				     //System.out.println(e.getMessage());
                                    connectionCode = 0; 
                                  }
				}
		 });
		 
		connectionThread.start();        
		timer = new Timer();
		timer.schedule(new RemindTask(), timeToWait);    	
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
       if(this.connectionCode == HTTP_OK)
           return true;	
       else
    	   return false;
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
           	
			//System.out.println("Canceled");          
			timer.cancel(); //Terminate the timer thread		
			connectionThread.interrupt();            
		}
	}
	    
    /**
     * This is the main method and is left as a usage sample
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("About to schedule task.");       
        try{
        
        	AddressTester myReminder = new AddressTester("http://data.my.ubc.ca/verf/cbc.html");         
        
        System.out.println("Task scheduled.");
		System.out.println("Running the test URL");
		//myReminder.checkURL();
		System.out.println("is URL available " + myReminder.getResponseCode());
        System.out.println("is URL available " + myReminder.URLAvailable());
		}catch(Exception e){}
    }
    
}

