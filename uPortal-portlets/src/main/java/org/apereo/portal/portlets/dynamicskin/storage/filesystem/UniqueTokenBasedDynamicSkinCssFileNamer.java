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
package org.apereo.portal.portlets.dynamicskin.storage.filesystem;

import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinException;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinInstanceData;
import org.apereo.portal.portlets.dynamicskin.DynamicSkinUniqueTokenGenerator;
import org.apereo.portal.portlets.dynamicskin.storage.DynamicSkinCssFileNamer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * {@link DynamicSkinCssFileNamer} class that uses a {@link DynamicSkinUniqueTokenGenerator} to create unique Dynamic
 * Skin CSS file names.  A file prefix can be provided, otherwise the skin name will be used as the prefix.
 */
@Service
public class UniqueTokenBasedDynamicSkinCssFileNamer implements DynamicSkinCssFileNamer {

    private String prefix;
    private DynamicSkinUniqueTokenGenerator uniqueTokenGenerator;

    public UniqueTokenBasedDynamicSkinCssFileNamer(final DynamicSkinUniqueTokenGenerator generator) {
        Assert.notNull(generator);
        this.uniqueTokenGenerator = generator;
    }

    @Autowired
    public UniqueTokenBasedDynamicSkinCssFileNamer(
            @Value("${dynamic-skin.skin-prefix:skin}") final String prefix,
            final DynamicSkinUniqueTokenGenerator generator) {
        this(generator);
        Assert.notNull(prefix);
        this.prefix = prefix;
    }

    /**
     * @see DynamicSkinCssFileNamer#generateCssFileName(DynamicSkinInstanceData)
     */
    @Override
    public String generateCssFileName(DynamicSkinInstanceData data) {
        final String prefixToUse = this.prefix == null ? data.getSkinName() : this.prefix;
        return prefixToUse + this.getUniqueToken(data) + ".css";
    }

    protected String getUniqueToken(DynamicSkinInstanceData data) {
        final String result = this.uniqueTokenGenerator.generateToken(data);
        if (StringUtils.isBlank(result)) {
            throw new DynamicSkinException("Dynamic Skin unique token cannot be null or empty.");
        }
        return result;
    }

}
