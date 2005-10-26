/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SetCheckInSemaphore;
import org.jasig.portal.utils.SoftHashMap;
import org.jasig.portal.utils.threading.BaseTask;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Future;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.TimeoutException;

/**
 * This class takes care of initiating channel rendering thread, 
 * monitoring it for timeouts, retreiving cache, and returning 
 * rendering results and status.
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class ChannelRenderer
    implements IChannelRenderer
{
    
    protected final Log log = LogFactory.getLog(getClass());
    
    /**
     * Default value for CACHE_CHANNELS.
     * This value will be used when the corresponding property cannot be loaded.
     */
    private static final boolean DEFAULT_CACHE_CHANNELS = false;
    
    public static final boolean CACHE_CHANNELS=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.ChannelRenderer.cache_channels", DEFAULT_CACHE_CHANNELS);
  
    public static final String[] renderingStatus={"successful","failed","timed out"};

    protected IChannel channel;
    protected ChannelRuntimeData rd;
    protected Map channelCache;
    protected Map cacheTables;

    protected boolean rendering;
    protected boolean donerendering;

    protected Thread workerThread;

    protected Worker worker;
    protected Future workTracker;

    protected long startTime;
    protected long timeOut = java.lang.Long.MAX_VALUE;

    protected boolean ccacheable;

    protected static ExecutorService tp=null;
    protected static Map systemCache=null;

    protected SetCheckInSemaphore groupSemaphore;
    protected Object groupRenderingKey;

    /**
     * Default contstructor
     *
     * @param chan an <code>IChannel</code> value
     * @param runtimeData a <code>ChannelRuntimeData</code> value
     * @param threadPool a <code>ThreadPool</code> value
     */
    public ChannelRenderer (IChannel chan,ChannelRuntimeData runtimeData, ExecutorService threadPool) {
        this.channel=chan;
        this.rd=runtimeData;
        this.rendering = false;
        this.ccacheable=false;
        tp = threadPool;

        if(systemCache==null) {
            systemCache=ChannelManager.systemCache;
        }

        this.groupSemaphore=null;
        this.groupRenderingKey=null;
    }


    /**
     * Default contstructor
     *
     * @param chan an <code>IChannel</code> value
     * @param runtimeData a <code>ChannelRuntimeData</code> value
     * @param threadPool a <code>ThreadPool</code> value
     * @param groupSemaphore a <code>SetCheckInSemaphore</code> for the current rendering group
     * @param groupRenderingKey an <code>Object</code> to be used for check ins with the group semaphore
     */
    public ChannelRenderer (IChannel chan,ChannelRuntimeData runtimeData, ExecutorService threadPool, SetCheckInSemaphore groupSemaphore, Object groupRenderingKey) {
        this(chan,runtimeData,threadPool);
        this.groupSemaphore=groupSemaphore;
        this.groupRenderingKey=groupRenderingKey;
    }


    /**
     * Sets the channel on which ChannelRenderer is to operate.
     *
     * @param channel an <code>IChannel</code>
     */
    public void setChannel(IChannel channel) {
        if (log.isDebugEnabled())
            log.debug("ChannelRenderer::setChannel() : channel is being reset!");        
        this.channel=channel;
        if(this.worker!=null) {
            this.worker.setChannel(channel);
        }
        // clear channel chace
        this.channelCache=null;
    }
    
    /**
     * Obtains a content cache specific for this channel instance.
     *
     * @return a key->rendering map for this channel
     */
    // XXX is this thread safe?
    Map getChannelCache() {
        if(this.channelCache==null) {
            if((this.channelCache=(SoftHashMap)this.cacheTables.get(this.channel))==null) {
                this.channelCache=new SoftHashMap(1);
                this.cacheTables.put(this.channel,this.channelCache);
            }
        }
        return this.channelCache;
    }


    /**
     * Set the timeout value
     * @param value timeout in milliseconds
     */
    public void setTimeout (long value) {
        this.timeOut = value;
    }

    public void setCacheTables(Map cacheTables) {
        this.cacheTables=cacheTables;
    }

    /**
     * Informs IChannelRenderer that a character caching scheme
     * will be used for the current rendering.
     * @param setting a <code>boolean</code> value
     */
    public void setCharacterCacheable(boolean setting) {
        this.ccacheable=setting;
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

    this.worker = new Worker (this.channel,this.rd);

    this.workTracker = tp.submit(this.worker); // XXX is execute okay?
    this.rendering = true;
    this.startTime = System.currentTimeMillis ();
  }

    public void startRendering(SetCheckInSemaphore groupSemaphore, Object groupRenderingKey) {
        this.groupSemaphore=groupSemaphore;
        this.groupRenderingKey=groupRenderingKey;
        this.startRendering();
    }

    /**
     * <p>Cancels the rendering job.
     **/
    public void cancelRendering()
    {
        if (null != this.workTracker) {
            this.workTracker.cancel(true);
        }
    }

  /**
   * Output channel rendering through a given ContentHandler.
   * Note: call of outputRendering() without prior call to startRendering() is equivalent to
   * sequential calling of startRendering() and then outputRendering().
   * outputRendering() is a blocking function. It will return only when the channel completes rendering
   * or fails to render by exceeding allowed rendering time.
   * @param out Document Handler that will receive information rendered by the channel.
   * @return error code. 0 - successful rendering; 1 - rendering failed; 2 - rendering timedOut;
   */
    public int outputRendering (ContentHandler out) throws Throwable {
        int renderingStatus=completeRendering();
        if(renderingStatus==RENDERING_SUCCESSFUL) {
            SAX2BufferImpl buffer;
            if ((buffer=this.worker.getBuffer())!=null) {
                // unplug the buffer :)
                try {
                    buffer.setAllHandlers(out);
                    buffer.outputBuffer();
                    return RENDERING_SUCCESSFUL;
                } catch (SAXException e) {
                    // worst case scenario: partial content output :(
                    log.error( "ChannelRenderer::outputRendering() : following SAX exception occured : "+e);
                    throw e;
                }
            } else {
                log.error( "ChannelRenderer::outputRendering() : output buffer is null even though rendering was a success?! trying to rendering for ccaching ?"); 
                throw new PortalException("unable to obtain rendering buffer");
            }
        }
        return renderingStatus;
    }


    /**
     * Requests renderer to complete rendering and return status.
     * This does exactly the same things as outputRendering except for the
     * actual stream output.
     *
     * @return an <code>int</code> return status value
     */

    public int completeRendering() throws Throwable {
        if (!this.rendering) {
            this.startRendering ();
        }
        boolean abandoned=false;
        long timeOutTarget = this.startTime + this.timeOut;
      
      
        // separate waits caused by rendering group
        if(this.groupSemaphore!=null) {
            while(!this.worker.isSetRuntimeDataComplete() && System.currentTimeMillis() < timeOutTarget && !this.workTracker.isDone()) {
                long wait=timeOutTarget-System.currentTimeMillis();
                if(wait<=0) { wait=1; }
                try {
                    synchronized(this.groupSemaphore) {
                        this.groupSemaphore.wait(wait);
                    }
                } catch (InterruptedException ie) {}
            }
            if(!this.worker.isSetRuntimeDataComplete() && !this.workTracker.isDone()) {
                this.workTracker.cancel(true);
                abandoned=true;
                if (log.isDebugEnabled())
                    log.debug("ChannelRenderer::outputRendering() : killed. " +
                            "(key="+this.groupRenderingKey.toString()+")");
            } else {
                this.groupSemaphore.waitOn();
            }
            // reset timer for rendering
            timeOutTarget=System.currentTimeMillis()+this.timeOut;
        }
      
        if(!abandoned) {
            try {
                this.workTracker.get(this.timeOut, TimeUnit.MILLISECONDS);
            } catch (TimeoutException te) {
                log.debug("ChannelRenderer::outputRendering() : timed out", te);
            }
          
            if(!this.workTracker.isDone()) {
                this.workTracker.cancel(true);
                abandoned=true;
                if (log.isDebugEnabled())
                    log.debug("ChannelRenderer::outputRendering() : killed.");
            } else {
                boolean successful = this.workTracker.isDone() && !this.workTracker.isCancelled() && this.worker.getException() == null;
                abandoned=!successful;
            }
          
        }
      
        if (!abandoned && this.worker.done ()) {
            if (this.worker.successful() && (((this.worker.getBuffer())!=null) || (this.ccacheable && this.worker.cbuffer!=null))) {
                return RENDERING_SUCCESSFUL;

            } else {
                // rendering was not successful
                Throwable e;
                if((e=this.worker.getException())!=null) throw new InternalPortalException(e);
                // should never get there, unless thread.stop() has seriously messed things up for the worker thread.
                return RENDERING_FAILED;
            }
        } else {
            Throwable e = null;
            if (this.worker != null) {
              e = this.worker.getException();
            }
            
            if (e != null) {
                throw new InternalPortalException(e);
            } else {
                // Assume rendering has timed out
                return RENDERING_TIMED_OUT;
            }
        }
    }


    /**
     * Returns rendered buffer.
     * This method does not perform any status checks, so make sure to call completeRendering() prior to invoking this method.
     *
     * @return rendered buffer
     */
    public SAX2BufferImpl getBuffer() {
        return this.worker != null ? this.worker.getBuffer() : null;
    }

    /**
     * Returns a character output of a channel rendering.
     */
    public String getCharacters() {
        if(this.worker!=null) {
            return this.worker.getCharacters();
        }
        
        if (log.isDebugEnabled()) {
            log.debug("ChannelRenderer::getCharacters() : worker is null already !");
        }

        return null;
    }


    /**
     * Sets a character cache for the current rendering.
     */
    public void setCharacterCache(String chars) {
        if(this.worker!=null) {
            this.worker.setCharacterCache(chars);
        }
    }

    /**
     * This method suppose to take care of the runaway rendering threads.
     * This method will be called from ChannelManager explictly.
     */
    protected void kill() {
        if(this.workTracker!=null && !this.workTracker.isDone())
            this.workTracker.cancel(true);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("ChannelRenderer ");
        sb.append("channel = [").append(this.channel).append("] ");
        sb.append("rd = [").append(this.rd).append("] ");
        sb.append("rendering=").append(this.rendering).append(" ");
        sb.append("donerendering=").append(this.donerendering).append(" ");
        sb.append("startTime=").append(this.startTime).append(" ");
        sb.append("timeOut=").append(this.timeOut).append(" ");
        
        return sb.toString();
    }
    

    protected class Worker extends BaseTask {
        private boolean successful;
        private boolean done;
        private boolean setRuntimeDataComplete;
        private IChannel channel;
        private ChannelRuntimeData rd;
        private SAX2BufferImpl buffer;
        private String cbuffer;

        public Worker (IChannel ch, ChannelRuntimeData runtimeData) {
            this.channel=ch;  this.rd=runtimeData;
            successful = false; done = false; setRuntimeDataComplete=false;
            buffer=null; cbuffer=null;
        }

        public void setChannel(IChannel ch) {
            this.channel=ch;
        }

        public boolean isSetRuntimeDataComplete() {
            return this.setRuntimeDataComplete;
        }

        public void execute () throws Exception {
            try {
                if(rd!=null) {
                    channel.setRuntimeData(rd);
                }
                setRuntimeDataComplete=true;
                
                if(groupSemaphore!=null) {
                    groupSemaphore.checkInAndWaitOn(groupRenderingKey);
                }

                if(CACHE_CHANNELS) {
                    // try to obtain rendering from cache
                    if(channel instanceof ICacheable ) {
                        ChannelCacheKey key=((ICacheable)channel).generateKey();
                        if(key!=null) {
                            if(key.getKeyScope()==ChannelCacheKey.SYSTEM_KEY_SCOPE) {
                                ChannelCacheEntry entry=(ChannelCacheEntry)systemCache.get(key.getKey());
                                if(entry!=null) {
                                    // found cached page
                                    // check page validity
                                    if(((ICacheable)channel).isCacheValid(entry.validity) && (entry.buffer!=null)) {
                                        // use it
                                        if(ccacheable && (entry.buffer instanceof String)) {
                                            cbuffer=(String)entry.buffer;
                                            if (log.isDebugEnabled()) {
                                                log.debug("ChannelRenderer.Worker::run() : retrieved system-wide cached character content based on a key \""+key.getKey()+"\"");
                                            }
                                        } else if(entry.buffer instanceof SAX2BufferImpl) {
                                            buffer=(SAX2BufferImpl) entry.buffer;
                                            if (log.isDebugEnabled()) {
                                                log.debug("ChannelRenderer.Worker::run() : retrieved system-wide cached content based on a key \""+key.getKey()+"\"");
                                            }
                                        }
                                    } else {
                                        // remove it
                                        systemCache.remove(key.getKey());
                                        if (log.isDebugEnabled()) {
                                            log.debug("ChannelRenderer.Worker::run() : removed system-wide unvalidated cache based on a key \""+key.getKey()+"\"");
                                        }
                                    }
                                }
                            } else {
                                // by default we assume INSTANCE_KEY_SCOPE
                                ChannelCacheEntry entry=(ChannelCacheEntry)getChannelCache().get(key.getKey());
                                if(entry!=null) {
                                    // found cached page
                                    // check page validity
                                    if(((ICacheable)channel).isCacheValid(entry.validity) && (entry.buffer!=null)) {
                                        // use it
                                        if(ccacheable && (entry.buffer instanceof String)) {
                                            cbuffer=(String)entry.buffer;
                                            if (log.isDebugEnabled()) {
                                                log.debug("ChannelRenderer.Worker::run() : retrieved instance-cached character content based on a key \""+key.getKey()+"\"");
                                            }

                                        } else if(entry.buffer instanceof SAX2BufferImpl) {
                                            buffer=(SAX2BufferImpl) entry.buffer;
                                            if (log.isDebugEnabled()) {
                                                log.debug("ChannelRenderer.Worker::run() : retrieved instance-cached content based on a key \""+key.getKey()+"\"");
                                            }
                                        }
                                    } else {
                                        // remove it
                                        getChannelCache().remove(key.getKey());
                                        if (log.isDebugEnabled()) {
                                        	log.debug("ChannelRenderer.Worker::run() : removed unvalidated instance-cache based on a key \""+key.getKey()+"\"");
                                        }
                                    }
                                }
                            }
                        }

                        // future work: here we should synchronize based on a particular cache key.
                        // Imagine a VERY popular cache entry timing out, then portal will attempt
                        // to re-render the page in many threads (serving many requests) simultaneously.
                        // If one was to synchronize on writing cache for a particular key, one thread
                        // would render and others would wait for it to complete. 

                        // check if need to render
                        if((ccacheable && cbuffer==null && buffer==null) || ((!ccacheable) && buffer==null)) {
                            if (ccacheable && channel instanceof ICharacterChannel) {
                                StringWriter sw = new StringWriter(100);
                                PrintWriter pw = new PrintWriter(sw);
                                ((ICharacterChannel)channel).renderCharacters(pw);
                                pw.flush();
                                cbuffer = sw.toString();
                                // save cache
                                if (key != null) {
                                    if (key.getKeyScope() == ChannelCacheKey.SYSTEM_KEY_SCOPE) {
                                        systemCache.put(key.getKey(), new ChannelCacheEntry(cbuffer, key.getKeyValidity()));
                                        if (log.isDebugEnabled()) {
                                            log.debug("ChannelRenderer.Worker::run() : recorded system character cache based on a key \"" + key.getKey() + "\"");
                                        }
                                    } else {
                                        getChannelCache().put(key.getKey(), new ChannelCacheEntry(cbuffer, key.getKeyValidity()));
                                        if (log.isDebugEnabled()) {
                                            log.debug("ChannelRenderer.Worker::run() : recorded instance character cache based on a key \"" + key.getKey() + "\"");
                                        }
                                    }
                                }
                            } else {
                                // need to render again and cache the output
                                buffer = new SAX2BufferImpl ();
                                buffer.startBuffering();
                                channel.renderXML(buffer);

                                // save cache
                                if(key!=null) {

                                    if(key.getKeyScope()==ChannelCacheKey.SYSTEM_KEY_SCOPE) {
                                        systemCache.put(key.getKey(),new ChannelCacheEntry(buffer,key.getKeyValidity()));
                                        if (log.isDebugEnabled()) {
                                            log.debug("ChannelRenderer.Worker::run() : recorded system cache based on a key \""+key.getKey()+"\"");
                                        }
                                    } else {
                                        getChannelCache().put(key.getKey(),new ChannelCacheEntry(buffer,key.getKeyValidity()));
                                        if (log.isDebugEnabled()) {
                                            log.debug("ChannelRenderer.Worker::run() : recorded instance cache based on a key \""+key.getKey()+"\"");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (ccacheable && channel instanceof ICharacterChannel) {
                            StringWriter sw = new StringWriter(100);
                            PrintWriter pw = new PrintWriter(sw);
                            ((ICharacterChannel)channel).renderCharacters(pw);
                            pw.flush();
                            cbuffer = sw.toString();
                        } else {
                            buffer = new SAX2BufferImpl ();
                            buffer.startBuffering();
                            channel.renderXML(buffer);
                        }
                    }
                } else  {
                    // in the case when channel cache is not enabled
                    buffer = new SAX2BufferImpl ();
                    buffer.startBuffering();
                    channel.renderXML (buffer);
                }
                successful = true;
            } catch (Exception e) {
                if(groupSemaphore!=null) {
                    groupSemaphore.checkIn(groupRenderingKey);
                }
                this.setException(e);
            }
            done = true;
        }

        public boolean successful () {
            return this.successful;
        }

        public SAX2BufferImpl getBuffer() {
            return this.buffer;
        }

        /**
         * Returns a character output of a channel rendering.
         */
        public String getCharacters() {
            if(ccacheable) {
                return this.cbuffer;
            } else {
                log.error("ChannelRenderer.Worker::getCharacters() : attempting to obtain character data while character caching is not enabled !");
                return null;
            }
        }
       

        /**
         * Sets a character cache for the current rendering.
         */
        public void setCharacterCache(String chars) {
            cbuffer=chars;
            if(CACHE_CHANNELS) {
                // try to obtain rendering from cache
                if(channel instanceof ICacheable ) {
                    ChannelCacheKey key=((ICacheable)channel).generateKey();
                    if(key!=null) {
                        if (log.isDebugEnabled()) {
                            log.debug("ChannelRenderer::setCharacterCache() : called on a key \""+key.getKey()+"\"");
                        }
                        ChannelCacheEntry entry=null;
                        if(key.getKeyScope()==ChannelCacheKey.SYSTEM_KEY_SCOPE) {
                            entry=(ChannelCacheEntry)systemCache.get(key.getKey());
                            if(entry==null) {
                                if (log.isDebugEnabled()) {
                                    log.debug("ChannelRenderer::setCharacterCache() : setting character cache buffer based on a system key \""+key.getKey()+"\"");
                                }
                                entry=new ChannelCacheEntry(chars,key.getKeyValidity());
                            } else {
                                entry.buffer=chars;
                            }
                            systemCache.put(key.getKey(),entry);
                        } else {
                            // by default we assume INSTANCE_KEY_SCOPE
                            entry=(ChannelCacheEntry)getChannelCache().get(key.getKey());
                            if(entry==null) {
                                if (log.isDebugEnabled()) {
                                    log.debug("ChannelRenderer::setCharacterCache() : no existing cache on a key \""+key.getKey()+"\"");
                                }
                                entry=new ChannelCacheEntry(chars,key.getKeyValidity());
                            } else {
                                entry.buffer=chars;
                            }
                            getChannelCache().put(key.getKey(),entry);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                        	log.debug("ChannelRenderer::setCharacterCache() : channel cache key is null.");
                        }
                    }
                }
            }
        }

        public boolean done () {
            return this.done;
        }
    }
}
