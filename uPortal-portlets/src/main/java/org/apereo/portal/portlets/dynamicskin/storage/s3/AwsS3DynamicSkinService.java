/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlets.dynamicskin.storage.s3;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** {@link DynamicSkinService} implementation that saves the CSS to an AWS S3 bucket. */
@Service("awsS3DynamicSkinService")
public class AwsS3DynamicSkinService extends AbstractDynamicSkinService {

    private static final String ATTEMPTING_TO_GET_FILE_METADATA_FROM_AWS_S3_LOG_MSG =
            "Attempting to get file metadata from AWS S3: bucket[{}]; key[{}]";
    private static final String FILE_METADATA_RETRIEVED_FROM_AWS_S3_LOG_MSG =
            "File metadata retrieved from AWS S3 with no reported errors: bucket[{}]; key[{}]";

    private static final String ATTEMPTING_TO_SAVE_FILE_TO_AWS_S3_LOG_MSG =
            "Attempting to save file to AWS S3: bucket[{}]; key[{}]";
    private static final String FILE_SAVED_TO_AWS_S3_LOG_MSG =
            "File saved to AWS S3 with no reported errors: bucket[{}]; key[{}]";

    public static final String CONTENT_CACHE_CONTROL_PORTLET_PREF_NAME = "contentCacheControl";
    public static final String SKIN_UNIQUE_TOKEN_METADATA_KEY = "dynamicSkinUniqueToken";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private S3Client amazonS3Client;
    private DynamicSkinAwsS3BucketConfig awsS3BucketConfig;
    private Map<String, String> awsObjectUserMetadata;

    @Autowired
    public AwsS3DynamicSkinService(
            final S3Client client,
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
        log.info(
                ATTEMPTING_TO_GET_FILE_METADATA_FROM_AWS_S3_LOG_MSG,
                this.awsS3BucketConfig.getBucketName(),
                objectKey);
        final HeadObjectResponse metadata = this.getMetadataFromAwsS3Bucket(objectKey);
        log.info(
                FILE_METADATA_RETRIEVED_FROM_AWS_S3_LOG_MSG,
                this.awsS3BucketConfig.getBucketName(),
                objectKey);
        if (metadata == null) {
            return false;
        } else {
            // S3 returns user-metadata keys lower-cased, so look up with the lower-cased key.
            final String uniqueTokenFromS3 =
                    metadata.metadata()
                            .get(SKIN_UNIQUE_TOKEN_METADATA_KEY.toLowerCase(Locale.ROOT));
            return this.getUniqueToken(data).equals(uniqueTokenFromS3);
        }
    }

    private HeadObjectResponse getMetadataFromAwsS3Bucket(final String objectKey) {
        final HeadObjectRequest request =
                HeadObjectRequest.builder()
                        .bucket(this.awsS3BucketConfig.getBucketName())
                        .key(objectKey)
                        .build();
        try {
            return this.amazonS3Client.headObject(request);
        } catch (NoSuchKeyException nske) {
            return null;
        } catch (S3Exception se) {
            if (se.statusCode() == 404) {
                return null;
            }
            this.logAwsServiceException(se, request);
            throw new DynamicSkinException("AWS S3 'head object' failure for: " + request, se);
        } catch (SdkException sdke) {
            this.logSdkException(sdke, request);
            throw new DynamicSkinException("AWS S3 'head object' failure for: " + request, sdke);
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
        final PutObjectRequest putObjectRequest =
                this.createPutObjectRequest(objectKey, content, data);
        log.info(
                ATTEMPTING_TO_SAVE_FILE_TO_AWS_S3_LOG_MSG,
                this.awsS3BucketConfig.getBucketName(),
                objectKey);
        this.saveContentToAwsS3Bucket(putObjectRequest, content);
        log.info(FILE_SAVED_TO_AWS_S3_LOG_MSG, this.awsS3BucketConfig.getBucketName(), objectKey);
    }

    private PutObjectRequest createPutObjectRequest(
            final String objectKey, final String content, final DynamicSkinInstanceData data) {
        final byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        final PutObjectRequest.Builder builder =
                PutObjectRequest.builder()
                        .bucket(this.awsS3BucketConfig.getBucketName())
                        .key(objectKey)
                        .contentType("text/css")
                        .contentLength((long) contentBytes.length)
                        .contentMD5(this.calculateBase64EncodedMd5Digest(contentBytes))
                        .metadata(this.buildUserMetadata(data));
        final String cacheControl = this.resolveCacheControl(data);
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        return builder.build();
    }

    private void saveContentToAwsS3Bucket(
            final PutObjectRequest putObjectRequest, final String content) {
        try {
            this.amazonS3Client.putObject(
                    putObjectRequest, RequestBody.fromString(content, StandardCharsets.UTF_8));
        } catch (S3Exception se) {
            this.logAwsServiceException(se, putObjectRequest);
            throw new DynamicSkinException(
                    "AWS S3 'put object' failure for: " + putObjectRequest, se);
        } catch (SdkException sdke) {
            this.logSdkException(sdke, putObjectRequest);
            throw new DynamicSkinException(
                    "AWS S3 'put object' failure for: " + putObjectRequest, sdke);
        }
    }

    private String calculateBase64EncodedMd5Digest(final byte[] content) {
        final byte[] md5DigestAs16ElementByteArray = DigestUtils.md5(content);
        return new String(Base64.encodeBase64(md5DigestAs16ElementByteArray));
    }

    private Map<String, String> buildUserMetadata(final DynamicSkinInstanceData data) {
        final Map<String, String> userMetadata = new HashMap<>();
        if (this.awsObjectUserMetadata != null) {
            userMetadata.putAll(this.awsObjectUserMetadata);
        }
        userMetadata.put(SKIN_UNIQUE_TOKEN_METADATA_KEY, this.getUniqueToken(data));
        return userMetadata;
    }

    /**
     * Resolves the cache-control value to apply: the bucket config value when non-empty, overridden
     * by the per-portlet preference when that preference is set. Returns null when neither applies.
     */
    private String resolveCacheControl(final DynamicSkinInstanceData data) {
        String cacheControl = null;
        final String configCacheControl = this.awsS3BucketConfig.getObjectCacheControl();
        if (StringUtils.isNotEmpty(configCacheControl)) {
            cacheControl = configCacheControl;
        }
        final String contentCacheControl =
                data.getPortletRequest()
                        .getPreferences()
                        .getValue(CONTENT_CACHE_CONTROL_PORTLET_PREF_NAME, null);
        if (contentCacheControl != null) {
            cacheControl = contentCacheControl;
        }
        return cacheControl;
    }

    private void logSdkException(final SdkException exception, final SdkRequest request) {
        log.info(
                "Caught an SdkException, which means the client encountered a serious internal problem "
                        + "while trying to communicate with S3, such as not being able to access the network.");
        log.info("Error Message: {}", exception.getMessage());
    }

    private void logAwsServiceException(
            final AwsServiceException exception, final SdkRequest request) {
        log.info(
                "Caught an AwsServiceException, which means your request made it "
                        + "to Amazon S3, but was rejected with an error response for some reason.");
        log.info("Error Message:    {}", exception.getMessage());
        log.info("HTTP Status Code: {}", exception.statusCode());
        log.info(
                "AWS Error Code:   {}",
                exception.awsErrorDetails() != null
                        ? exception.awsErrorDetails().errorCode()
                        : null);
        log.info("Request ID:       {}", exception.requestId());
    }

    public void setAwsObjectUserMetadata(final Map<String, String> metadata) {
        this.awsObjectUserMetadata = new HashMap<String, String>(metadata.size());
        this.awsObjectUserMetadata.putAll(metadata);
    }
}
