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

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Cache;

/**
 * File system based implementation of services for the Skin Manager.
 *
 * @since 4.1.0
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Service("fileSystemDynamicSkinService")
public class FileSystemDynamicSkinService extends AbstractDynamicSkinService {

    @Autowired
    public FileSystemDynamicSkinService(
            final DynamicSkinUniqueTokenGenerator uniqueTokenGenerator,
            final DynamicSkinCssFileNamer namer,
            @Qualifier("org.jasig.portal.skinManager.failureCache") final Cache failureCache) {
        super(uniqueTokenGenerator, namer, failureCache);
    }

    @Override
    public String getSkinCssPath(DynamicSkinInstanceData data) {
        return this.localRelativeRootPath + "/" + this.getSkinCssFilename(data);
    }

    @Override
    protected boolean innerSkinCssFileExists(DynamicSkinInstanceData data) {
        return new File(this.getAbsoluteSkinCssPath(data)).exists();
    }

    @Override
    protected void moveCssFileToFinalLocation(final DynamicSkinInstanceData data, final File tempCssFile) {
        final String absolutePath = this.getAbsoluteSkinCssPath(data);
        boolean succeeded = tempCssFile.renameTo(new File(absolutePath));
        if (!succeeded) {
            throw new DynamicSkinException("Unable to create file: " + absolutePath);
        }
    }

    private String getAbsoluteSkinCssPath(final DynamicSkinInstanceData data) {
        final String relativePath = this.getSkinCssPath(data);
        return data.getPortletAbsolutePathRoot() + relativePath;
    }

}
