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

package org.jasig.portal;

import org.xml.sax.*;

/**
 * This class renders channel content into a SAXBuffer.
 * Rendering is done in a separate thread.
 * @author Peter Kharchenko
 * @version $ Revision: 1.0 $
 */
public class ChannelRenderer
{
  protected IChannel channel;
  protected SAXBufferImpl buffer;

  protected boolean rendering;
  protected boolean donerendering;

  protected Thread workerThread;
  protected Worker worker;

  protected long startTime;
  protected long timeOut = java.lang.Long.MAX_VALUE;

  /**
   * Default constructor.
   * @param chan Channel associated with this ChannelRenderer
   */
  public ChannelRenderer (IChannel chan)
  {
    this.channel=chan;
    rendering = false;
  }

  /**
   * Set the timeout value
   * @param value timeout in milliseconds
   */
  public void setTimeout (long value)
  {
    timeOut = value;
  }

  /**
   * Start rendering of the channel in a new thread.
   * Note that rendered information will be accumulated in a
   * buffer until outputRendering() function is called.
   * startRendering() is a non-blocking function.
   */
  public void startRendering ()
  {
    // start the rendering thread
    buffer = new SAXBufferImpl ();
    worker = new Worker (channel,buffer);
    workerThread = new Thread (this.worker);
    workerThread.start ();
    rendering = true;
    startTime = System.currentTimeMillis ();
  }

  /**
   * Output channel rendering through a given DocumentHandler.
   * Note: call of outputRendering() without prior call to startRendering() is equivalent to
   * sequential calling of startRendering() and then outputRendering().
   * outputRendering() is a blocking function. It will return only when the channel completes rendering
   * or fails to render by exceeding allowed rendering time.
   * @param out Document Handler that will receive information rendered by the channel.
   * @return error code. 0 - successful rendering; 1 - rendering failed; 2 - rendering timedOut; 
   */
  public int outputRendering (DocumentHandler out) throws Exception
  {
    if (!rendering)
      this.startRendering ();

    try
    {
      long wait = timeOut - System.currentTimeMillis () + startTime;

      if (wait > 0)
        workerThread.join (wait);
    }
    catch (InterruptedException e)
    {
      Logger.log (Logger.DEBUG, "ChannelRenderer::outputRendering() : thread waiting on the WorkerThread has been interrupted : "+e);
    }

    // kill the working thread
    // yes, this is terribly crude and unsafe, but I don't see an alternative
    workerThread.stop ();

    if (worker.done ())
    {
      if (worker.successful ())
      {
        // unplug the buffer :)
        try
        {
          buffer.outputBuffer (out);
	  return 0;
        }
        catch (SAXException e) {
	    // worst case scenario: partial content output :(
	    Logger.log (Logger.ERROR, "ChannelRenderer::outputRendering() : following SAX exception occured : "+e);
	    throw e;
        }
      } else {
	  // rendering was not successful
	  Exception e;
	  if((e=worker.getException())!=null) throw new InternalPortalException(e);
	  // should never get there, unless thread.stop() has seriously messed things up for the worker thread.
	  return 1;
      }
    } else {
	// rendering has timed out
	return 2;
    }
  }

  /**
   * I am not really sure if this will take care of the runaway rendering threads.
   * The alternative is kill them explicitly in ChannelManager.
   */
    protected void finalize () throws Throwable
    {
	if (workerThread.isAlive ())
	    workerThread.stop ();
	
	super.finalize ();
    }

    protected class Worker implements Runnable {
	private boolean successful;
	private boolean done;
	private IChannel channel;
	private DocumentHandler documentHandler;
	private Exception exc=null;
	
	public Worker (IChannel ch, DocumentHandler dh) {
	    this.channel=ch; this.documentHandler=dh;
	}
	
	public void run () {
	    successful = false;
	    done = false;
	    
	    try {
		channel.renderXML (documentHandler);
		successful = true;
	    } catch (Exception e) {
		this.exc=e;
	    }
	    done = true;
	}
	
	public boolean successful () {
	    return this.successful;
	}
	
	public boolean done () {
	    return this.done;
	}
	
	public Exception getException() {
	    return exc;
	}

    }
}
