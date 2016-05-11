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
package org.jasig.portal.portlets.dynamicskin;

import org.springframework.util.Assert;

/**
 * {@link DynamicSkinCssFileNamer} class that uses a {@link DynamicSkinUniqueTokenGenerator} to create unique Dynamic 
 * Skin CSS file names.
 */
public class UniqueTokenBasedDynamicSkinCssFileNamer implements DynamicSkinCssFileNamer {

    private String prefix;
    private DynamicSkinUniqueTokenGenerator uniqueTokenGenerator;

    public UniqueTokenBasedDynamicSkinCssFileNamer(
            final String prefix, final DynamicSkinUniqueTokenGenerator uniqueTokenGenerator) {
        Assert.notNull(prefix);
        Assert.notNull(uniqueTokenGenerator);
        this.prefix = prefix;
        this.uniqueTokenGenerator = uniqueTokenGenerator;
    }

    /**
     * @see DynamicSkinCssFileNamer#generateCssFileName(DynamicSkinInstanceData)
     */
    @Override
    public String generateCssFileName(DynamicSkinInstanceData data) {
        return this.prefix + this.getUniqueToken(data) + ".css";
    }

    protected String getUniqueToken(DynamicSkinInstanceData data) {
        return this.uniqueTokenGenerator.generateToken(data.getPortletRequest().getPreferences());
    }

}
