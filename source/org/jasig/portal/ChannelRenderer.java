/**
 * This class renders channel content into a SAXBuffer.
 * Rendering is done in a separate thread.
 * @author Peter Kharchenko
 * @version $ Revision: 1.0 $
 */

package org.jasig.portal;


import org.xml.sax.*;

public class ChannelRenderer {
    protected IXMLChannel channel;
    protected SAXBufferImpl buffer;

    protected boolean rendering;
    protected boolean donerendering;

    protected Thread workerThread;
    protected Worker worker;

    protected long startTime;
    protected long timeOut=java.lang.Long.MAX_VALUE;

    /**
     * Default constructor.
     * @param chan Channel associated with this ChannelRenderer
     */
    public ChannelRenderer(IXMLChannel chan) {
	this.channel=chan;
	rendering=false;
    };

    /**
     * Set the timeout value
     * @param value timeout in milliseconds
     */
    public void setTimeout(long value) {
	timeOut=value; 
    }

    /**
     * Start rendering of the channel in a new thread.
     * Note that rendered information will be accumulated in a
     * buffer until outputRendering() function is called.
     * startRendering() is a non-blocking function.
     */
    public void startRendering() {
	// start the rendering thread
	buffer=new SAXBufferImpl();
	worker=new Worker(channel,buffer);
	workerThread=new Thread(this.worker);
	workerThread.start();
	rendering=true;
	startTime=System.currentTimeMillis();
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
    public int outputRendering(DocumentHandler out) {
	if(!rendering) 
	    this.startRendering();
	
	try {
	    long wait=timeOut-System.currentTimeMillis()+startTime;
	    if (wait>0) workerThread.join(wait);
	} catch (InterruptedException e) {
	    Logger.log(Logger.DEBUG,"ChannelRenderer::outputRendering() : thread waiting on the WorkerThread has been interrupted : "+e);
	}
	
	// kill the working thread
	// yes, this is terribly crude and unsafe, but I don't see an alternative
	workerThread.stop();
	
	if(worker.done()) {
	    if(worker.successful()) {
		// unplug the buffer :)
		try {
		    buffer.outputBuffer(out);
		} catch (SAXException e) {
		    Logger.log(Logger.ERROR,"ChannelRenderer::outputRendering() : following SAX exception occured : "+e); 
		    return 1;
		}
		return 0;
	    } else {
		// rendering was not successful
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
    protected void finalize() throws Throwable {
	if(workerThread.isAlive()) 
	    workerThread.stop();
	super.finalize();
    }


    protected class Worker implements Runnable {
	private boolean successful; 
	private boolean done;
	private IXMLChannel channel;
	private DocumentHandler documentHandler;

	public Worker(IXMLChannel ch, DocumentHandler dh) {
	    this.channel=ch; this.documentHandler=dh;
	}

	public void run() {
	    successful=false;
	    done=false;
	    channel.renderXML(documentHandler);
	    successful=true;
	    done=true;
	}
	
	public boolean successful() { return this.successful; }
	public boolean done() { return this.done; }
    };
	

}
