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

import org.slf4j.Logger;

/**
 * LogLevel
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public enum LogLevel {
    TRACE {
        @Override
        public void log(Logger logger, String msg) {
            logger.trace(msg);
        }
    },
    DEBUG {
        @Override
        public void log(Logger logger, String msg) {
            logger.debug(msg);
        }
    },
    INFO {
        @Override
        public void log(Logger logger, String msg) {
            logger.info(msg);
        }
    },
    WARN {
        @Override
        public void log(Logger logger, String msg) {
            logger.warn(msg);
        }
    },
    ERROR {
        @Override
        public void log(Logger logger, String msg) {
            logger.error(msg);
        }
    };

    public abstract void log(Logger logger, String msg);
}