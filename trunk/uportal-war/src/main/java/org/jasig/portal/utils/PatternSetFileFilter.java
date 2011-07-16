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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

/**
 * FilenameFilter that uses a Set of regular expressions for testing. The file name
 * must match any one of the patterns
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PatternSetFileFilter implements FileFilter {
    private final boolean acceptDirectories;
    private final boolean ignoreHidden;
    private final Collection<Pattern> patterns;
    
    public PatternSetFileFilter(boolean acceptDirectories, boolean ignoreHidden, Pattern pattern) {
        Validate.notNull(pattern);
        this.patterns = Arrays.asList(pattern);
        this.acceptDirectories = acceptDirectories;
        this.ignoreHidden = ignoreHidden;
    }
    
    public PatternSetFileFilter(boolean acceptDirectories, boolean ignoreHidden, Set<Pattern> patterns) {
        Validate.notNull(patterns);
        this.patterns = new HashSet<Pattern>(patterns);
        this.acceptDirectories = acceptDirectories;
        this.ignoreHidden = ignoreHidden;
    }

    @Override
    public boolean accept(File pathname) {
        if (acceptDirectories && pathname.isDirectory()) {
            return true;
        }
        if (ignoreHidden && pathname.isHidden()) {
            return false;
        }
        
        final String name = pathname.getName();
        for (final Pattern pattern : this.patterns) {
            if (pattern.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }
}
