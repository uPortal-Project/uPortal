/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.portlets.dynamicskin.storage.s3;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletPreferences;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import net.sf.ehcache.Cache;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinException;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinInstanceData;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinUniqueTokenGenerator;
import org.apereo.portal.portlets.dynamicskin.storage.AbstractDynamicSkinService;
import org.apereo.portal.portlets.dynamicskin.storage.DynamicSkinCssFileNamer;
import org.apereo.portal.portlets.dynamicskin.storage.DynamicSkinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * {@link DynamicSkinService} implementation that saves the CSS to an AWS S3 bucket.
 */
@Service("awsS3DynamicSkinService")
public class AwsS3DynamicSkinService extends AbstractDynamicSkinService {

    private static final String ATTEMPTING_TO_GET_FILE_METADATA_FROM_AWS_S3_LOG_MSG = "Attempting to get file metadata from AWS S3: bucket[{}]; key[{}]";
    private static final String FILE_METADATA_RETRIEVED_FROM_AWS_S3_LOG_MSG = "File metadata retrieved from AWS S3 with no reported errors: bucket[{}]; key[{}]";

    private static final String ATTEMPTING_TO_SAVE_FILE_TO_AWS_S3_LOG_MSG = "Attempting to save file to AWS S3: bucket[{}]; key[{}]";
    private static final String FILE_SAVED_TO_AWS_S3_LOG_MSG = "File saved to AWS S3 with no reported errors: bucket[{}]; key[{}]";

    public static final String CONTENT_CACHE_CONTROL_PORTLET_PREF_NAME = "contentCacheControl";
    public static final String SKIN_UNIQUE_TOKEN_METADATA_KEY = "dynamicSkinUniqueToken";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private AmazonS3 amazonS3Client;
    private DynamicSkinAwsS3BucketConfig awsS3BucketConfig;
    private Map<String,String> awsObjectUserMetadata;

    @Autowired
    public AwsS3DynamicSkinService(
            final AmazonS3 client,
            final DynamicSkinAwsS3BucketConfig config,
            final DynamicSkinUniqueTokenGenerator uniqueTokenGenerator,
            final DynamicSkinCssFileNamer namer,
            @Qualifier("org.apereo.portal.skinManager.failureCache") final Cache failureCache) {
        super(uniqueTokenGenerator, namer, failureCache);
        Assert.notNull(client);
        Assert.notNull(config);
        this.amazonS3Client = client;
        this.awsS3BucketConfig = config;
        log.info("DynamicSkinAwsS3BucketConfig provided: {}", config);
    }

    @Override
    public String getSkinCssPath(DynamicSkinInstanceData data) {
        final String bucketUrl = this.awsS3BucketConfig.getBucketUrl();
        if (bucketUrl.endsWith("/")) {
            return bucketUrl + this.getCssObjectKey(data);
        } else {
            return bucketUrl + "/" + this.getCssObjectKey(data);
        }
    }

    private String getCssObjectKey(DynamicSkinInstanceData data) {
        return this.awsS3BucketConfig.getObjectKeyPrefix() + "/" + this.getSkinCssFilename(data);
    }

    @Override
    protected boolean supportsRetainmentOfNonCurrentCss() {
        return false;
    }

    @Override
    protected boolean innerSkinCssFileExists(DynamicSkinInstanceData data) {
        final String objectKey = this.getCssObjectKey(data);
        log.info(ATTEMPTING_TO_GET_FILE_METADATA_FROM_AWS_S3_LOG_MSG, this.awsS3BucketConfig.getBucketName(), objectKey);
        final ObjectMetadata metadata = this.getMetadataFromAwsS3Bucket(objectKey);
        log.info(FILE_METADATA_RETRIEVED_FROM_AWS_S3_LOG_MSG, this.awsS3BucketConfig.getBucketName(), objectKey);
        if (metadata == null) {
            return false;
        } else {
            final String uniqueTokenFromS3 = metadata.getUserMetaDataOf(SKIN_UNIQUE_TOKEN_METADATA_KEY);
            return this.getUniqueToken(data).equals(uniqueTokenFromS3);
        }
    }

    private ObjectMetadata getMetadataFromAwsS3Bucket(final String objectKey) {
        final GetObjectMetadataRequest request =
                new GetObjectMetadataRequest(this.awsS3BucketConfig.getBucketName(), objectKey);
        try {
            return this.amazonS3Client.getObjectMetadata(request);
        } catch (AmazonServiceException ase) {
            if (ase.getStatusCode() == 404) {
                return null;
            }
            this.logAmazonServiceException(ase, request);
            throw new DynamicSkinException("AWS S3 'get object metadata' failure for: " + request, ase);
        } catch (AmazonClientException ace) {
            this.logAmazonClientException(ace, request);
            throw new DynamicSkinException("AWS S3 'get object metadata' failure for: " + request, ace);
        }
    }

    @Override
    protected void moveCssFileToFinalLocation(DynamicSkinInstanceData data, File tempCssFile) {
        final String objectKey = this.getCssObjectKey(data);
        final String content = this.readFileContentAsString(tempCssFile);
        this.saveContentToAwsS3Bucket(objectKey, content, data);
    }

    private String readFileContentAsString(final File file) {
        try {
            return IOUtils.toString(new FileReader(file));
        } catch (IOException ioe) {
            throw new DynamicSkinException(ioe);
        }
    }

    private void saveContentToAwsS3Bucket(
            final String objectKey, final String content, final DynamicSkinInstanceData data) {
        final InputStream inputStream = IOUtils.toInputStream(content);
        final ObjectMetadata objectMetadata =
                this.createObjectMetadata(content, data);
        final PutObjectRequest putObjectRequest = this.createPutObjectRequest(objectKey, inputStream, objectMetadata);
        log.info(ATTEMPTING_TO_SAVE_FILE_TO_AWS_S3_LOG_MSG, this.awsS3BucketConfig.getBucketName(), objectKey);
        this.saveContentToAwsS3Bucket(putObjectRequest);
        log.info(FILE_SAVED_TO_AWS_S3_LOG_MSG, this.awsS3BucketConfig.getBucketName(), objectKey);
    }

    private ObjectMetadata createObjectMetadata(final String content, final DynamicSkinInstanceData data) {
        final ObjectMetadata metadata = new ObjectMetadata();
        this.addContentMetadata(metadata, content);
        this.addUserMetatadata(metadata);
        this.addPortletPreferenceMetadata(metadata, data.getPortletRequest().getPreferences());
        this.addDynamicSkinMetadata(metadata, data);
        return metadata;
    }

    private PutObjectRequest createPutObjectRequest(
            final String objectKey, final InputStream inputStream, final ObjectMetadata objectMetadata) {
        return new PutObjectRequest(this.awsS3BucketConfig.getBucketName(), objectKey, inputStream, objectMetadata);
    }

    private void saveContentToAwsS3Bucket(final PutObjectRequest putObjectRequest) {
        try {
            this.amazonS3Client.putObject(putObjectRequest);
        } catch (AmazonServiceException ase) {
            this.logAmazonServiceException(ase, putObjectRequest);
            throw new DynamicSkinException("AWS S3 'put object' failure for: " + putObjectRequest, ase);
        } catch (AmazonClientException ace) {
            this.logAmazonClientException(ace, putObjectRequest);
            throw new DynamicSkinException("AWS S3 'put object' failure for: " + putObjectRequest, ace);
        }
    }

    private void addContentMetadata(final ObjectMetadata metadata, final String content) {
        metadata.setContentMD5(this.calculateBase64EncodedMd5Digest(content));
        metadata.setContentLength(content.length());
        metadata.setContentType("text/css");
        final String cacheControl = this.awsS3BucketConfig.getObjectCacheControl();
        if (StringUtils.isNotEmpty(cacheControl)) {
            metadata.setCacheControl(cacheControl);
        }
    }

    private String calculateBase64EncodedMd5Digest(final String content) {
        final byte[] md5DigestAs16ElementByteArray = DigestUtils.md5(content);
        return new String(Base64.encodeBase64(md5DigestAs16ElementByteArray));
    }

    private void addUserMetatadata(final ObjectMetadata metadata) {
        if (this.awsObjectUserMetadata != null) {
            for (Entry<String, String> entry : this.awsObjectUserMetadata.entrySet()) {
                metadata.addUserMetadata(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addPortletPreferenceMetadata(
            final ObjectMetadata metadata, final PortletPreferences portletPreferences) {
        final String contentCacheControl = portletPreferences.getValue(CONTENT_CACHE_CONTROL_PORTLET_PREF_NAME, null);
        if (contentCacheControl != null) {
            metadata.setCacheControl(contentCacheControl);
        }
    }

    private void addDynamicSkinMetadata(final ObjectMetadata metadata, final DynamicSkinInstanceData data) {
        metadata.addUserMetadata(SKIN_UNIQUE_TOKEN_METADATA_KEY, this.getUniqueToken(data));
    }

    private void logAmazonClientException(final AmazonClientException exception, final AmazonWebServiceRequest request) {
        log.info("Caught an AmazonClientException, which means the client encountered a serious internal problem "
                + "while trying to communicate with S3, such as not being able to access the network.");
        log.info("Error Message: {}", exception.getMessage());
    }

    private void logAmazonServiceException(final AmazonServiceException exception, final AmazonWebServiceRequest request) {
        log.info("Caught an AmazonServiceException, which means your request made it "
                + "to Amazon S3, but was rejected with an error response for some reason.");
        log.info("Error Message:    {}", exception.getMessage());
        log.info("HTTP Status Code: {}", exception.getStatusCode());
        log.info("AWS Error Code:   {}", exception.getErrorCode());
        log.info("Error Type:       {}", exception.getErrorType());
        log.info("Request ID:       {}", exception.getRequestId());
    }

    public void setAwsObjectUserMetadata(final Map<String, String> metadata) {
        this.awsObjectUserMetadata  = new HashMap<String, String>(metadata.size());
        this.awsObjectUserMetadata.putAll(metadata);
    }

}
