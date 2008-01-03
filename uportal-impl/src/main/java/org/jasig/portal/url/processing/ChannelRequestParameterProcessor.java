/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.processing;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FileCleaner;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.Constants;
import org.jasig.portal.Errors;
import org.jasig.portal.ExceptionHelper;
import org.jasig.portal.IUserInstance;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.MultipartDataSource;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UploadStatus;
import org.jasig.portal.UserInstanceManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.url.IWritableHttpServletRequest;
import org.jasig.portal.url.support.IChannelRequestParameterManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsFileUploadSupport;

/**
 * Does request parameter processing on any request that does not explicitly target a portlet.
 * Takes care of pulling out channel parameters and handling file uploads so the data can be
 * passed into the ChannelRuntimeData for the targeted channel.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ChannelRequestParameterProcessor extends CommonsFileUploadSupport implements IRequestParameterProcessor, DisposableBean {
    public static final String UPLOAD_STATUS = "up_upload_status";
    
    protected final Log logger = LogFactory.getLog(this.getClass());

    private IPortletRequestParameterManager portletRequestParameterManager;
    private IChannelRequestParameterManager channelRequestParameterManager;
    
    /**
     * @return the portletRequestParameterManager
     */
    public IPortletRequestParameterManager getPortletRequestParameterManager() {
        return portletRequestParameterManager;
    }
    /**
     * @param portletRequestParameterManager the portletRequestParameterManager to set
     */
    @Required
    public void setPortletRequestParameterManager(IPortletRequestParameterManager portletRequestParameterManager) {
        Validate.notNull(portletRequestParameterManager, "IPortletRequestParameterManager can not be null");
        this.portletRequestParameterManager = portletRequestParameterManager;
    }
    /**
     * @return the channelRequestParameterManager
     */
    public IChannelRequestParameterManager getChannelRequestParameterManager() {
        return channelRequestParameterManager;
    }
    /**
     * @param channelRequestParameterManager the channelRequestParameterManager to set
     */
    @Required
    public void setChannelRequestParameterManager(IChannelRequestParameterManager channelRequestParameterManager) {
        Validate.notNull(channelRequestParameterManager, "IChannelRequestParameterManager can not be null");
        this.channelRequestParameterManager = channelRequestParameterManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.processing.IRequestParameterProcessor#processParameters(org.jasig.portal.url.IWritableHttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
        try {
            //If this is a portlet request don't do any channel parameter processing
            if (this.portletRequestParameterManager.isAnyPortletTargeted(request)) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Request is targeting a portlet, channel parameter processing will not take place.");
                }
                
                //Mark the request as not having any channel specific parameters
                this.channelRequestParameterManager.setNoChannelParameters(request);

                //Processing is complete
                return true;
            }
        }
        catch (RequestParameterProcessingIncompleteException rppie) {
            //Need to wait for the portlet request processor to complete
            return false;
        }
        
        //Determine the targeted channel
        final String targetChannelId = this.getTargetChannelId(request);
        
        //If no channel is targeted mark the request as such in the manager and return
        if (targetChannelId == null) {
            this.channelRequestParameterManager.setNoChannelParameters(request);
            return true;
        }

        //Map to track channel parameters in
        final Map<String, Object[]> channelParameters = new HashMap<String, Object[]>();

        //Do multipart file upload request processing
        if (ServletFileUpload.isMultipartContent(new ServletRequestContext(request))) {
            //Used to communicate to clients of multipart data if the request processing worked correctly
            UploadStatus uploadStatus;
            
            final String encoding = this.determineEncoding(request);
            final FileUpload fileUpload = this.prepareFileUpload(encoding);
            try {
                //TODO this may not support multiple files for a single parameter :(
                final List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
                final MultipartParsingResult parsingResult = parseFileItems(fileItems, encoding);
                
                final Map<String, MultipartDataSource[]> multipartDataSources = this.getMultipartDataSources(parsingResult);
                channelParameters.putAll(multipartDataSources);
                
                final Map<String, String[]> multipartParameters = parsingResult.getMultipartParameters();
                channelParameters.putAll(multipartParameters);
                
                uploadStatus = new UploadStatus(UploadStatus.SUCCESS, this.getFileUpload().getFileSizeMax());
            }
            catch (FileUploadException fue) {
                this.logger.warn("Failed to parse multipart upload, processing will continue but not all parameters may be available.", fue);
                uploadStatus = new UploadStatus(UploadStatus.FAILURE, this.getFileUpload().getFileSizeMax());
                ExceptionHelper.genericTopHandler(Errors.bug, fue);
            }
            
            channelParameters.put(UPLOAD_STATUS, new UploadStatus[] { uploadStatus });
        }

        // process parameters on the request object
        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames != null && parameterNames.hasMoreElements()) {
            final String parameterName = parameterNames.nextElement();
            
            if (!parameterName.equals("uP_channelTarget") && !parameterName.equals("uP_fname")) {
                final String[] parameterValues = request.getParameterValues(parameterName);
                channelParameters.put(parameterName, parameterValues);
            }
        }

        //Set the parameters on the request
        this.channelRequestParameterManager.setChannelParameters(request, targetChannelId, channelParameters);
        
        //Processing is complete
        return true;
    }
    
    /** 
     * Ensures the temp files from uploads are cleaned up correctly.
     * 
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        FileCleaner.exitWhenFinished();        
    }
    
    
    /**
     * Determine the targeted channel ID for the request.
     * 
     * @param request Current request.
     * @return The targeted channel ID, null if no channel is targeted.
     */
    protected String getTargetChannelId(IWritableHttpServletRequest request) {
        String targetChannelId = null;
        
        final IUserInstance userInstance = UserInstanceManager.getUserInstance(request);
        final IUserPreferencesManager userPreferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManger = userPreferencesManager.getUserLayoutManager();

        // see if this is targeted at an fname channel. if so then it takes
        // precedence. This is done so that a baseActionURL can be used for
        // the basis of an fname targeted channel with the fname query parm
        // appended to direct all query parms to the fname channel
        final String fname = request.getParameter(Constants.FNAME_PARAM);
        if (fname != null) {
            // get a subscribe id for the fname
            try {
                targetChannelId = userLayoutManger.getSubscribeId(fname);
            }
            catch (PortalException pe) {
                this.logger.error("Unable to get subscribe ID for fname=" + fname, pe);
            }
        }
        
        // check if the uP_channelTarget parameter has been passed
        if (targetChannelId == null) {
            targetChannelId = request.getParameter("uP_channelTarget");
        }
        
        // determine target channel id
        if (targetChannelId == null) {
            final UPFileSpec upfs = new UPFileSpec(request);
            targetChannelId = upfs.getTargetNodeId();
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("targetChannelId='" + targetChannelId + "'.");
        }

        return targetChannelId;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.multipart.commons.CommonsFileUploadSupport#newFileUpload(org.apache.commons.fileupload.FileItemFactory)
     */
    @Override
    protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
        return new ServletFileUpload(fileItemFactory);
    }
    
    /**
     * Determine the encoding for the given request.
     * Can be overridden in subclasses.
     * <p>The default implementation checks the request encoding,
     * falling back to the default encoding specified for this resolver.
     * @param request current HTTP request
     * @return the encoding for the request (never <code>null</code>)
     * @see javax.servlet.ServletRequest#getCharacterEncoding
     * @see #setDefaultEncoding
     */
    protected String determineEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = getDefaultEncoding();
        }
        return encoding;
    }
    


    /**
     * Convert's Spring's MultipartFile objects to uPortal's MultipartDataSource objects.
     * 
     * @param parsingResult The results of the multipart request parsing
     * @return A Map of String parameter names to MultipartDataSource objects
     */
    protected Map<String, MultipartDataSource[]> getMultipartDataSources(final MultipartParsingResult parsingResult) {
        final Map<String, MultipartFile> multipartFiles = parsingResult.getMultipartFiles();
        
        final Map<String, MultipartDataSource[]> multipartDataSources = new HashMap<String, MultipartDataSource[]>(multipartFiles.size());
        for (final Map.Entry<String, MultipartFile> multipartFileEntry : multipartFiles.entrySet()) {
            final MultipartDataSource multipartDataSource = new MultipartDataSource(multipartFileEntry.getValue());
            multipartDataSources.put(multipartFileEntry.getKey(), new MultipartDataSource[] { multipartDataSource });
        }
        
        return multipartDataSources;
    }
}
