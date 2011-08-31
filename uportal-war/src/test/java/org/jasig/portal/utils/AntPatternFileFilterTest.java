/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.utils;

import static junit.framework.Assert.assertTrue;

import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AntPatternFileFilterTest {
    @Test
    public void testWindowsPathMatching() {
        final Set<String> dataFileIncludes = ImmutableSet.of("**\\*.xml");
        final Set<String> dataFileExcludes = ImmutableSet.copyOf(DirectoryScanner.getDefaultExcludes());
        
        final AntPatternFileFilter antPatternFileFilter = new AntPatternFileFilter(false, true, dataFileIncludes, dataFileExcludes);
        
        final boolean accepted = antPatternFileFilter.accept(null, "C:\\up\\uPortal_trunk\\uportal-war\\src\\main\\data\\default_entities\\portlet\\ImportExportPortlet.portlet.xml");
        assertTrue(accepted);
    }
    
    @Ignore
    @Test
    public void testExactNameMatching() {
        final Set<String> dataFileIncludes = ImmutableSet.of("youtube.portlet.xml", "**/test.xml", "te?.xml", "te*.xml");
        final Set<String> dataFileExcludes = ImmutableSet.copyOf(DirectoryScanner.getDefaultExcludes());
        
        final AntPatternFileFilter antPatternFileFilter = new AntPatternFileFilter(false, true, dataFileIncludes, dataFileExcludes);
        
        final boolean accepted = antPatternFileFilter.accept(null, "/uportal-war/src/main/data/default_entities/portlet/youtube.portlet.xml");
        assertTrue(accepted);
    }

}
