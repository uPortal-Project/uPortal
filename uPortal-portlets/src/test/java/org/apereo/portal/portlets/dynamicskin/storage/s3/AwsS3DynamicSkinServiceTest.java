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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import net.sf.ehcache.Cache;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinInstanceData;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinUniqueTokenGenerator;
import org.apereo.portal.portlets.dynamicskin.storage.DynamicSkinCssFileNamer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class AwsS3DynamicSkinServiceTest {

    private static final String TOKEN = "tok-123";

    @Mock private S3Client s3Client;
    @Mock private DynamicSkinAwsS3BucketConfig config;
    @Mock private DynamicSkinUniqueTokenGenerator tokenGenerator;
    @Mock private DynamicSkinCssFileNamer namer;
    @Mock private Cache failureCache;
    @Mock private DynamicSkinInstanceData data;
    @Mock private PortletRequest portletRequest;
    @Mock private PortletPreferences preferences;

    private AwsS3DynamicSkinService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        given(this.config.getBucketName()).willReturn("skin-bucket");
        given(this.config.getObjectKeyPrefix()).willReturn("skins");
        given(this.namer.generateCssFileName(this.data)).willReturn("myskin.css");
        given(this.tokenGenerator.generateToken(this.data)).willReturn(TOKEN);
        given(this.data.getPortletRequest()).willReturn(this.portletRequest);
        given(this.portletRequest.getPreferences()).willReturn(this.preferences);
        this.service =
                new AwsS3DynamicSkinService(
                        this.s3Client,
                        this.config,
                        this.tokenGenerator,
                        this.namer,
                        this.failureCache);
    }

    @Test
    public void putsCssWithExpectedKeyContentTypeMetadataAndCacheControl() throws Exception {
        given(this.config.getObjectCacheControl()).willReturn("public, max-age=3600");
        given(
                        this.preferences.getValue(
                                AwsS3DynamicSkinService.CONTENT_CACHE_CONTROL_PORTLET_PREF_NAME,
                                null))
                .willReturn(null);
        final String content = "body { color: red; }";
        final File tempCss = File.createTempFile("skin", ".css");
        tempCss.deleteOnExit();
        Files.write(tempCss.toPath(), content.getBytes(StandardCharsets.UTF_8));

        this.service.moveCssFileToFinalLocation(this.data, tempCss);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(this.s3Client).putObject(captor.capture(), any(RequestBody.class));
        PutObjectRequest request = captor.getValue();
        assertEquals("skin-bucket", request.bucket());
        assertEquals("skins/myskin.css", request.key());
        assertEquals("text/css", request.contentType());
        assertEquals("public, max-age=3600", request.cacheControl());
        assertEquals(Long.valueOf(content.length()), request.contentLength());
        assertEquals(
                TOKEN,
                request.metadata().get(AwsS3DynamicSkinService.SKIN_UNIQUE_TOKEN_METADATA_KEY));
    }

    @Test
    public void cssFileExistsWhenHeadObjectReturnsMatchingToken() {
        // S3 returns user-metadata keys lower-cased.
        given(this.s3Client.headObject(any(HeadObjectRequest.class)))
                .willReturn(
                        HeadObjectResponse.builder()
                                .metadata(Collections.singletonMap("dynamicskinuniquetoken", TOKEN))
                                .build());

        assertTrue(this.service.innerSkinCssFileExists(this.data));
    }

    @Test
    public void cssFileDoesNotExistWhenObjectMissing() {
        given(this.s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().build());

        assertFalse(this.service.innerSkinCssFileExists(this.data));
    }
}
