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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class to encapsulate the AWS S3 bucket properties for content.  This class should be injected as a dependency into 
 * classes that need Content AWS S3 bucket properties values.
 */
@Component
public class DynamicSkinAwsS3BucketConfig {

    @Value("${dynamic-skin.service.aws.s3.bucket.url}")
    private String bucketUrl;

    @Value("${dynamic-skin.service.aws.s3.bucket.name}")
    private String bucketName;

    @Value("${dynamic-skin.service.aws.s3.bucket.object-key-prefix}")
    private String objectKeyPrefix;

    @Value("${dynamic-skin.service.aws.s3.bucket.object-cache-control}")
    private String objectCacheControl;

    public String getBucketUrl() {
        return this.bucketUrl;
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public String getObjectKeyPrefix() {
        return this.objectKeyPrefix;
    }

    public String getObjectCacheControl() {
        return this.objectCacheControl;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
