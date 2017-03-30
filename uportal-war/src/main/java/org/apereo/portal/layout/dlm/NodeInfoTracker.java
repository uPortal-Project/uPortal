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
package org.apereo.portal.layout.dlm;

import java.util.List;
import org.apereo.portal.xml.XmlUtilitiesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Helps {@link PLFIntegrator} track the number of {@link NodeInfo} objects created for a single
 * layout. There is some evidence in the community that there is a bug within the <code>
 * mergePLFintoILF</code> process that can occur with certain data. When triggered, it pulls the
 * process into an infinite(?) loop, causing it to instantiate {@link NodeInfo} objects until the
 * JVM runs out of memory. This class tracks the number these objects created and fails noisily when
 * the specific threshold is crossed.
 */
/* package-private */ final class NodeInfoTracker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The maximum number of {@link NodeInfo} objects that may be created in processing a single
     * layout.
     */
    private static final int MAX_NUMBER = 1000;

    private int count;

    public void track(List<NodeInfo> order, Element compViewParent, Element positionSet) {
        ++count;
        if (count > MAX_NUMBER) {
            final String msg = "Maximum number of NodeInfo objects for this layout exceeded";
            logger.error(msg);
            logger.error("count=" + count);
            logger.error("order=" + order);
            logger.error("compViewParent=" + XmlUtilitiesImpl.toString(compViewParent));
            logger.error("positionSet=" + XmlUtilitiesImpl.toString(positionSet));
            throw new RuntimeException(msg);
        }
    }
}
