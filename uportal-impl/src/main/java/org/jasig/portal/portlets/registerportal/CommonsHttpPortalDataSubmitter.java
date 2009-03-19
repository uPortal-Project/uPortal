/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CommonsHttpPortalDataSubmitter implements IPortalDataSubmitter {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String submitUrl = null;
    
    /**
     * @param submitUrl URL to POST registration to
     */
    public void setSubmitUrl(String submitUrl) {
        this.submitUrl = submitUrl;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataSubmitter#submitPortalData(org.jasig.portal.portlets.registerportal.PortalRegistrationData)
     */
    public boolean submitPortalData(PortalRegistrationData portalRegistrationData) {
        final HttpClient client = new HttpClient();
        final HttpClientParams httpClientParams = client.getParams();
        httpClientParams.setSoTimeout(5000);
        
        String postUrl = this.submitUrl;
        for (int redirectCounter = 0; redirectCounter < 10; redirectCounter++) {
            final PostMethod post = new PostMethod(postUrl);
            
            //Add static parameters
            final String deployerAddress = portalRegistrationData.getDeployerAddress();
            if (deployerAddress != null) {
                post.addParameter("deployerAddress", deployerAddress);
            }
            
            final String deployerName = portalRegistrationData.getDeployerName();
            if (deployerName != null) {
                post.addParameter("deployerName", deployerName);
            }
            
            final String institutionName = portalRegistrationData.getInstitutionName();
            if (institutionName != null) {
                post.addParameter("institutionName", institutionName);
            }
            
            final String portalName = portalRegistrationData.getPortalName();
            if (portalName != null) {
                post.addParameter("portalName", portalName);
            }
            
            final String portalUrl = portalRegistrationData.getPortalUrl();
            if (portalUrl != null) {
                post.addParameter("portalUrl", portalUrl);
            }
            
            post.addParameter("shareInfo", Boolean.toString(portalRegistrationData.isShareInfo()));
            
            //Add gathered data
            final Map<String, Map<String, String>> collectedData = portalRegistrationData.getCollectedData();
            for (final Map.Entry<String, Map<String, String>> collectedDataEntry : collectedData.entrySet()) {
                final String dataKey = collectedDataEntry.getKey();
                
                for (final Map.Entry<String, String> dataValueEntry : collectedDataEntry.getValue().entrySet()) {
                    final String valueKey = dataValueEntry.getKey();
                    final String value = dataValueEntry.getValue();
                    if (value != null) {
                        post.addParameter(dataKey + "_" + valueKey, value);
                    }
                }
            }
            
            Integer result = null;
            try {
                result = client.executeMethod(post);
                
                if (result == 200) {
                    this.logger.info("Portal registration data successfully submitted to " + this.submitUrl);
                    return true;
                }
                else if (result >= 300 && result <= 399) {
                    final Header newLocation = post.getResponseHeader("location");
                    postUrl = newLocation.getValue();
                    this.logger.info("Handling redirect to " + postUrl);
                    continue;
                }
            }
            catch (HttpException e) {
                this.logger.warn("Portal registration data failed to submit due to a HTTP exception", e);
                return false;
            }
            catch (IOException e) {
                this.logger.warn("Portal registration data failed to submit due to an IO exception", e);
                return false;
            }
            
            this.logger.warn("Portal registration data failed to submit with return code " + result);
            return false;
        }
        
        this.logger.warn("Portal registration data failed to submit due to too many redirects");
        return false;
    }

}
