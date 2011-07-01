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

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import com.google.common.base.Functions;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ConcurrentDirectoryScannerTest {
    private ThreadPoolExecutorFactoryBean executorServiceFactory;
    private ConcurrentDirectoryScanner directoryScanner;
    private File testDirectory;
    
    @Before
    public void setup() throws Exception {
        testDirectory = File.createTempFile("ConcurrentDirectoryScannerTest_", "_dir");
        testDirectory.delete();
        testDirectory.mkdirs();
        
        final InputStream dataZipStream = this.getClass().getResourceAsStream("/org/jasig/portal/utils/DirScannerData.zip");
        ZipUtils.extract(dataZipStream, testDirectory);
        IOUtils.closeQuietly(dataZipStream);
        
        executorServiceFactory = new ThreadPoolExecutorFactoryBean();
        executorServiceFactory.setCorePoolSize(0);
        executorServiceFactory.setMaxPoolSize(1);
        executorServiceFactory.setQueueCapacity(500);
        executorServiceFactory.setKeepAliveSeconds(30);
        executorServiceFactory.setAllowCoreThreadTimeOut(true);
        executorServiceFactory.setDaemon(true);
        executorServiceFactory.afterPropertiesSet();
        
        this.directoryScanner = new ConcurrentDirectoryScanner(executorServiceFactory.getObject());
    }
    
    @After
    public void teardown() throws Exception {
        executorServiceFactory.destroy();
        FileUtils.deleteDirectory(testDirectory);
    }
    
    @Test
    public void testDirectoryScanner() {
        final AntPatternFileFilter fileFilter = new AntPatternFileFilter(true, true, Collections.<String>singleton("**"), Collections.<String>emptySet());
        
        final Map<File, Resource> results = directoryScanner.scanDirectoryWithResults(testDirectory, fileFilter, Functions.<Resource>identity());
            
        assertEquals(7, results.size());
    }
}

