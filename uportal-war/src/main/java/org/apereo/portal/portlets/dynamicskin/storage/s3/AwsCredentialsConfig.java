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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class to encapsulate the AWS credential properties, with the properties read in from a properties file and 
 * auto-injected by Spring.  This class implements the {@link AWSCredentials} interface and can therefore be injected 
 * as credentials into a {@link AmazonS3Client}.  If a credential property has no value in the properties file, then 
 * it's value will be set to null so that this class can be returned by a {@link AWSCredentialsProvider} as part of a 
 * {@link AWSCredentialsProviderChain}, which expects null values to be returned when the provider is unable to find 
 * credentials.
 */
@Component
public class AwsCredentialsConfig implements AWSCredentials {

    @Value("${dynamic-skin.aws.access-key-id:#{null}}")
    private String accessKeyId;

    @Value("${dynamic-skin.aws.secret-access-key:#{null}}")
    private String secretAccessKey;

    public String getAccessKey() {
        return this.accessKeyId;
    }

    public String getSecretAccessKey() {
        return this.secretAccessKey;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public String getAWSAccessKeyId() {
        return this.accessKeyId;
    }

    @Override
    public String getAWSSecretKey() {
        return this.secretAccessKey;
    }

}
