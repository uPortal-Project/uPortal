/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.processing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.UserInstance;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.security.IPerson;
import org.xml.sax.ContentHandler;

/**
 * Handles chaining of layout parameter request processing for the
 * DistributedLayoutManager object and provides assistance during generation of
 * the SAX event stream during rendering by wrapping the ultimate ContentHandler
 * to which all events will be pushed. Conceptually, the pipe performs as if
 * sitting between UserInstance and the DistributedLayoutManager instance as
 * shown below. The call to processLayoutParameters and the call to render the
 * layout via the getUserLayout call pass through the pipe along with the
 * resulting SAX event stream. This is conceptual only. The pipe is actually
 * embedded within the DistributedLayoutManager.
 *
 * <pre>
 *
 *              time proceeds forward moving downward
 *                              |
 *                              |
 *                              V
 *                process                        optionally      +-------------+
 *   +----------+ Layout           +-----------+ specific layout | Distributed |
 *   | User     | Parameters       | Processor | action calls to | Layout      |
 *   | Instance |  (...)           | Pipe      | the manager     | Manager     |
 *   |          |-----------------&gt;|           |----------------&gt;|             |
 *   |          |                  |           |                 |             |
 *   |          | getUserLayout()  |           | getUserLayout() |             |
 *   |          |-----------------&gt;|           |----------------&gt;|             |
 *   |          |                  |           |                 |             |
 *   |          | SAX Events flow  |           | SAX Events flow |             |
 *   |          | back to User     |           | back to Pipe's  |             |
 *   |          | Instance's       |           | ContentHandler  |             |
 *   |          | ContentHandler   |           |                 |             |
 *   |          |&lt;-----------------|           |&lt;----------------|             |
 *   |          |                  |           |                 |             |
 *   +----------+                  +-----------+                 +-------------+
 *
 *
 * </pre>
 *
 * This pipe allows us to do specific URL parameter handling that may be unique
 * to a structure and theme stylesheet set. A special parameter uP_dlmPrc is
 * also looked for which causes an optional processor to be embedded within the
 * pipe until a further occurrance of this parameter either replaces that
 * processor with a new processor or removes it by specifying an empty value.
 * This allows a specific dlm stylesheet set to use custom processing for things
 * like adding channel targets, adding column or tab targets, adding targets for
 * moving channels and a different policy for adding targets for moving columns
 * or tabs, etc.
 *
 * There are two types of processors supported, a fixed set which does not
 * change and an optional set from which a single processor can be added to the
 * pipe via the uP_dlmPrc parameter. All are configured via the
 * "/properties/dlmContext.xml" file.
 *
 * The pipe provides a "front" ContentHandler to the DistributedLayoutManager
 * representing the front of the pipe for SAX events generated in the
 * getUserLayout() method. The ContentHandler passed in from UserInstance via
 * the getUserLayout() method is set as the "back" ContentHandler to which all
 * events exiting the pipe are pushed.
 *
 * As such this pipe can act on incoming layout parameters and then influence
 * the perceived layout seen by UserInstance by altering the SAX stream
 * accordingly as it flows back toward UserInstance's ContentHandler.
 *
 * @author mark.boyd@sungardhe.com
 */
public class ProcessingPipe implements IParameterProcessor
{
    /**
     * Log object for logging.
     */
    private static final Log LOG = LogFactory.getLog(ProcessingPipe.class);

    /**
     * Holds the configured array of fixed processors if any in the order that
     * they are allowed to process layout parameters and optionally filter the
     * SAX event stream if they are IProcessingHandlers.
     */
    private List fixedProcessors = null;

    /**
     * Holds a configured map of optional processors. One of these can be
     * selected at a time and can participate in layout parameter processing
     * and SAX event stream handling. These are selected by a request parameter
     * of uP_dlmPrc containing one of the keys in this map. If uP_dlmPrc is
     * specified and a corresponding key is not found a IllegalArgumentException
     * is thrown. An optional processor remains in place until a subsequent
     * request clears the optional processor by specifying an empty string for
     * the uP_dlmPrc value or specified a different processor. In the request
     * in which the optional processor is cleared or replaced the old processor
     * does not receive that request for processing.
     */
    private Map optionalProcessors = null;

    /**
     * Holds the currently selected optional processor as selected by some
     * previous request specifying a non empty parameter of uP_dlmPrc. This can
     * be either in implementation of IParameterProcessor or ISaxProcessor.
     */
    private Object optionalProcessor = null;

    /**
     * Holds the key of the currently selected optional processor.
     */
    private String optionalProcessorKey;

    /**
     * Holds the last processor in the configured list of fixed processors if
     * any that processes the SAX event stream.
     */
    private ISaxProcessor lastSaxProcessor = null;

    /**
     * Holds the first processor in the configured list of fixed processors if
     * any that processes the SAX event stream.
     */
    private ISaxProcessor firstSaxProcessor = null;

    /**
     * Holds the back ContentHandler to which all SAX events will be pushed
     * including changes made to the event stream by the pipe.
     */
    private ContentHandler exitContentHandler;

    /**
     * The parameter watched for by this pipe in the request whose value if
     * included should be an empty String or a value that matches one of the
     * keys in the optionalProcessors Map. An empty String value removes the
     * currently selected optional processor if any from pipe processing. A
     * value matching a key sets the corresponding processor as the optional
     * procesor prior to performing regular pipe processing. This means that the
     * request in which a processor is made the currently selected optional
     * processor is the first request that that processor will be allowed to
     * participate in processing. Furthermore, when a currently selected
     * optional processor is replaced it does not take part in processing of the
     * request during which it was replaced.
     */
    public static final String CHANGE_PROCESSOR_PARAM = "uP_dlmPrc";

    /**
     * Holds the classpath location of the context file for loading instances
     * of this class.
     */
    public static final String PIPE_CONFIG_FILE = "/properties/dlmContext.xml";

    /**
     * Holds the name of the bean configured in the context file that is
     * a factory for instances of this class. The factory must return a new
     * instance of this class and all configured, contained processors for each
     * call to the factory to get an instance of this class.
     */
    public static final String PROCESSING_PIPE_BEAN_ID = "dlmProcessingPipe";

    /**
     * Sets the configured set of fixed processors in the order that
     * they are allowed to process layout parameters and filter the SAX event
     * stream. These processors will take part in either parameter processing
     * or SAX event stream modification depending on which interfaces they
     * implement. See IParmeterProcessor and ISaxProcessor.
     *
     * @param fixedProcessors
     */
    public void setFixedProcessors(List fixedProcessors)
    {
        firstSaxProcessor = null;
        lastSaxProcessor = null;

        if (fixedProcessors != null && fixedProcessors.size()>0)
        {
            this.fixedProcessors = fixedProcessors;

            if (fixedProcessors.size() == 1)
            {
                // see if it handles SAX events as well
                if (fixedProcessors.get(0) instanceof ISaxProcessor)
                {
                    firstSaxProcessor = (ISaxProcessor) fixedProcessors.get(0);
                    lastSaxProcessor = firstSaxProcessor;
                }
            }
            else
            {
                // now link up processors that participate in SAX event.
                ISaxProcessor lastSaxProc = null;
                for (Iterator itr = fixedProcessors.iterator(); itr.hasNext();)
                {
                    if (lastSaxProc == null)
                    {
                        Object proc = itr.next();
                        if (proc instanceof ISaxProcessor)
                        {
                            firstSaxProcessor = (ISaxProcessor) proc;
                            lastSaxProc = firstSaxProcessor;
                        }
                    }
                    else
                    {
                        Object next = itr.next();
                        if (next instanceof ISaxProcessor)
                        {
                            ISaxProcessor nextSaxProc = (ISaxProcessor)next;
                            lastSaxProc.setExitContentHandler(
                                    nextSaxProc.getEntryContentHandler());
                            lastSaxProc = nextSaxProc;
                        }
                    }
                }
                lastSaxProcessor = lastSaxProc;
            }
        }
    }
    /**
     * Sets or clears the optional processor and correspondingly alters the
     * pipeline through which SAX events pass. If there are no fixed processors
     * then the termination processor is the value for the option processor. If
     * there are fixed processors then the termination processor is the last
     * processor in the fixed list when the options processor is cleared and it
     * is the optional processor when it is set to a non-null value.
     *
     * @param optionalProcessor
     */
    private void setOptionalProcessor(Object optionalProcessor)
    {
        this.optionalProcessor = optionalProcessor;

        // see if we need to adjust fixed pipe
        if (optionalProcessor != null &&
                (this.optionalProcessor instanceof ISaxProcessor))
        {
            if (lastSaxProcessor != null)
            {
                ISaxProcessor sp = (ISaxProcessor)optionalProcessor;
                lastSaxProcessor.setExitContentHandler(
                        sp.getEntryContentHandler());
            }
        }
    }

    /**
     * Sets the map of optional processors that can be included in the
     * processing and SAX event pipe via inclusion of the uP_dlmPrc parameter
     * being included with a value corresponding to a key in this map.
     *
     * @param optionalProcessors
     */
    public void setOptionalProcessors(Map optionalProcessors)
    {
        optionalProcessor = null;
        this.optionalProcessors = optionalProcessors;
    }

    /**
     * Hands the passed-in instances or IPerson and DistributedLayoutManager
     * to all configured fixed and optional processors. Hence, this should be
     * called after setFixedProcessors() and setOptionalProcessors().
     *
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#setResources(org.jasig.portal.security.IPerson, org.jasig.portal.layout.dlm.DistributedLayoutManager)
     */
    public void setResources(IPerson person, DistributedLayoutManager dlm)
    {
        if (fixedProcessors != null)
        {
            initProcessorResources(fixedProcessors.iterator(), person, dlm);
        }
        if (optionalProcessors != null)
        {
            initProcessorResources(optionalProcessors.values().iterator(),
                    person, dlm);
        }
    }

    /**
     * Passes the person and DLM objects to those processors that implement the
     * IParameterProcessor interface.
     *
     * @param itr
     * @param person
     * @param dlm
     */
    private void initProcessorResources(Iterator itr, IPerson person,
            DistributedLayoutManager dlm)
    {
        for (; itr.hasNext();)
        {
            Object obj = itr.next();
            if (obj instanceof IParameterProcessor)
            {
                IParameterProcessor pp = (IParameterProcessor) obj;
                pp.setResources(person, dlm);
            }
        }
    }

    /**
     * Passes a request to the set of processors that are currently part of the
     * pipe allowing them to act on query or post parameters embedded in the
     * request. This will include all fixed processors if any followed by the
     * optional processor if one is set.
     *
     * If this request includes the uP_dlmPrc parameter then prior to passing
     * the request to any processors the optional processor state is updated
     * according to the value of uP_dlmPrc. If it is an empty String then the
     * optional processor is removed from the pipe. If non-null then the value
     * must match a key in the optional processors map. If no matching key is
     * found then an InvalidArgumentException is logged and processing continues
     * with no optional processor. If a matching key is found then the
     * corresponding optional processor becomes part of the pipe possibly
     * replacing an existing optional processor set when uP_dlmPrc was last
     * specified in a request.
     *
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#processParameters(org.jasig.portal.UserPreferences,
     *      javax.servlet.http.HttpServletRequest)
     */
    public void processParameters(UserPreferences prefs,
            HttpServletRequest request)
    {
        handleOptionalProcessorSelection(request);
        pushRequestToProcessors(prefs, request);
    }

    /**
     * Calls the processParameters() method on all fixed processors followed by
     * the currently selected optional processor if any allowing them to act on
     * submitted parameters accordingly to their implemented policy.
     *
     * @param prefs
     * @param request
     */
    private void pushRequestToProcessors(UserPreferences prefs,
            HttpServletRequest request)
    {
        if (fixedProcessors != null)
        {
            for (Iterator itr = fixedProcessors.iterator(); itr.hasNext();)
            {
                Object obj = itr.next();
                if (obj instanceof IParameterProcessor)
                {
                    IParameterProcessor pp = (IParameterProcessor) obj;
                    pp.processParameters(prefs, request);
                }
            }
        }
        if (optionalProcessor != null &&
                optionalProcessor instanceof IParameterProcessor)
        {
            ((IParameterProcessor) optionalProcessor).processParameters(prefs,
                    request);
            if (optionalProcessor instanceof IOptionalParameterProcessor &&
                ((IOptionalParameterProcessor) optionalProcessor).isFinished())
                setOptionalProcessor(null);

        }
    }

    /**
     * Watch for the uP_dlmPrc parameter and swap the optional processor
     * accordingly.
     *
     * @param request
     */
    private void handleOptionalProcessorSelection(HttpServletRequest request)
    {
        String key = request.getParameter(CHANGE_PROCESSOR_PARAM);

        if (key != null) // one specified
        {
            if (key.equals("")) // clear currently selected optional processor
            {
                setOptionalProcessor(null);
            }
            else if (optionalProcessors == null || optionalProcessors.isEmpty())
            {
                LOG.error("Optional Processor for " + CHANGE_PROCESSOR_PARAM +
                        "=" + key + " requested but there are no optional " +
                        "processors registered.");
            }
            else
            {
                Object proc = optionalProcessors.get(key);
                if (proc == null)
                {
                    LOG.error("Optional Processor for " +
                            CHANGE_PROCESSOR_PARAM + "=" + key +
                            " not found in registered optional processors.");
                }
                else
                    setOptionalProcessor(proc);
            }
        }
    }

    /**
     * Returns a cache key indicative of the affect that the pipe has on the
     * SAX event stream. If there is no effect on the SAX event stream then an
     * empty String is returned.
     *
     * @return String cache key
     */
    public String getCacheKey()
    {
        // only contribute to the key if the pipe alters the SAX stream
        if (fixedProcessors == null && optionalProcessors == null)
            return "";

        StringBuffer buf = new StringBuffer();

        if (fixedProcessors != null && firstSaxProcessor != null)
        {
            ISaxProcessor sProc  = null;
            for (Iterator itr = fixedProcessors.iterator(); itr.hasNext();)
            {
                Object obj = itr.next();
                if (obj instanceof ISaxProcessor)
                {
                    if (sProc == null)
                        buf.append("FX[");
                    else
                        buf.append(':');
                    sProc = (ISaxProcessor) obj;
                    buf.append(sProc.getCacheKey());
                }
            }
            if (sProc != null)
                buf.append("]");
        }
        if (optionalProcessor != null &&
                optionalProcessor instanceof ISaxProcessor)
        {
            buf.append("OP[");
            buf.append(optionalProcessorKey);
            buf.append(':');
            buf.append(((ISaxProcessor)optionalProcessor).getCacheKey());
            buf.append(']');
        }
        return buf.toString();
    }

    /**
     * Sets the ContentHandler to which all SAX events passing through the pipe
     * will be passed including any modifications made to that stream by any of
     * the fixed processors if any or the currently selected optional processor
     * if any. If there are no fixed processors and no currently selected
     * optional processor then the "front" and "back" ContentHandler will be
     * the same object. Accordingly, this method must be called prior to
     * calling getFrontContentHandler().
     *
     */
    public void setExitContentHandler(ContentHandler handler)
    {
        exitContentHandler = handler;

        // now plug it into the pipe if it exists
        if (optionalProcessor != null &&
                optionalProcessor instanceof ISaxProcessor)
            ((ISaxProcessor)optionalProcessor).setExitContentHandler(handler);
        else if (lastSaxProcessor != null)
            lastSaxProcessor.setExitContentHandler(handler);
    }

    /**
     * Returns the ContentHandler into which SAX events should be pushed after
     * flowing through all ContentHandlers that are part of the pipe. This
     * method should only be called after calling setBackContentHandler passing
     * a valid ContentHandler since that ContentHandler will be returned by
     * this method of there are no fixed processors or a currently selected
     * optional processor.
     *
     * @return ContentHandler
     */
    public ContentHandler getEntryContentHandler()
    {
        if (firstSaxProcessor != null)
            return firstSaxProcessor.getEntryContentHandler();
        if (optionalProcessor != null &&
                optionalProcessor instanceof ISaxProcessor)
            return ((ISaxProcessor)optionalProcessor).getEntryContentHandler();
        return exitContentHandler;
    }
}
