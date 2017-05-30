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
package org.apereo.portal.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FilenameFilter that uses a Set of regular expressions for testing. The file name must match any
 * one of the patterns
 *
 */
public class AntPatternFileFilter implements FileFilter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean acceptDirectories;
    private final boolean ignoreHidden;
    private final Collection<String> includes;
    private final Collection<String> excludes;

    public AntPatternFileFilter(
            boolean acceptDirectories,
            boolean ignoreHidden,
            String include,
            Collection<String> excludes) {
        Validate.notNull(include);
        Validate.notNull(excludes);
        this.includes = ImmutableSet.of(fixAntPattern(include));
        this.excludes = fixAntPatterns(excludes);
        this.acceptDirectories = acceptDirectories;
        this.ignoreHidden = ignoreHidden;
    }

    public AntPatternFileFilter(
            boolean acceptDirectories,
            boolean ignoreHidden,
            Collection<String> includes,
            Collection<String> excludes) {
        Validate.notNull(includes);
        Validate.notNull(excludes);
        this.includes = fixAntPatterns(includes);
        this.excludes = fixAntPatterns(excludes);
        this.acceptDirectories = acceptDirectories;
        this.ignoreHidden = ignoreHidden;
    }

    protected Collection<String> fixAntPatterns(Collection<String> patterns) {
        final Builder<String> fixedPatterns = ImmutableSet.builder();

        for (String pattern : patterns) {
            pattern = fixAntPattern(pattern);
            fixedPatterns.add(pattern);
        }

        return fixedPatterns.build();
    }

    private String fixAntPattern(String pattern) {
        pattern = FilenameUtils.separatorsToSystem(pattern);
        if (!SelectorUtils.hasWildcards(pattern)) {
            pattern = "**" + File.separatorChar + pattern;
        }
        return pattern;
    }

    @Override
    public boolean accept(File pathname) {
        if (ignoreHidden && pathname.isHidden()) {
            return false;
        }

        final String path;
        try {
            path = pathname.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not determine canonical path of: " + pathname, e);
        }

        return accept(pathname, path);
    }

    protected boolean accept(File pathname, final String path) {
        logger.debug("checking path: {}", path);
        for (final String include : this.includes) {
            if ((acceptDirectories && pathname.isDirectory())
                    || SelectorUtils.matchPath(include, path, false)
                    || SelectorUtils.match(include, path, false)) {

                logger.debug("{} matches include {}", path, include);
                for (final String exclude : this.excludes) {
                    if (SelectorUtils.matchPath(exclude, path, false)) {
                        logger.debug("{} matches exclude {}", path, exclude);
                        logger.debug("denied path: {}", path);
                        return false;
                    } else if (logger.isTraceEnabled()) {
                        logger.trace("{} doesn't match exclude {}", path, exclude);
                    }
                }

                logger.debug("acepted path: {}", path);
                return true;
            } else if (logger.isTraceEnabled()) {
                logger.trace("{} doesn't match include {}", path, include);
            }
        }

        logger.debug("denied path: {}", path);
        return false;
    }
}
