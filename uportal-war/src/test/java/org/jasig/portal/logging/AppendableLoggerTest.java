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

package org.jasig.portal.logging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Formatter;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AppendableLoggerTest {
    @Test
    public void testAppendableLogger() {
        final Logger logger = Mockito.mock(Logger.class);
        
        final Formatter f = new Formatter(new AppendableLogger(logger, LogLevel.INFO));

        f.format("%9s | %16s | %16s%n", "Data Type", "Export Supported", "Import Supported");
        verify(logger).info("Data Type | Export Supported | Import Supported");
        
        f.format("%9s | %16s | %16s%n", "portlet-type", true, true);
        verify(logger).info("portlet-type |             true |             true");

        f.format("%9s | %16s | %16s%n", "portlet-definition", true, false);
        verify(logger).info("portlet-definition |             true |            false");

        f.format("%9s | %16s | %16s%n", "layout", true, false);
        verify(logger).info("   layout |             true |            false");
        
        verifyNoMoreInteractions(logger);
    }
}
