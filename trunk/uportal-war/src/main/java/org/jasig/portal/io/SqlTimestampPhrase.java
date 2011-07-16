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

package org.jasig.portal.io;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class SqlTimestampPhrase implements Phrase {

    protected final Log logger = LogFactory.getLog(this.getClass());
	private static final String SQL_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	
    // Instance Members.
    private Phrase timestamp;

    /*
     * Public API.
     */

    public static final Reagent TIMESTAMP = new SimpleReagent("TIMESTAMP", "descendant-or-self::text()", 
                    ReagentType.PHRASE, String.class, "A String that expresses a java.sql.Timestamp " +
                    "in 'yyyy-MM-dd HH:mm:ss.SSS' format.  A null value generates a null response.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {TIMESTAMP};
        return new SimpleFormula(getClass(), reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.timestamp = (Phrase) config.getValue(TIMESTAMP);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {
        
        Timestamp rslt = null;  // the default...

    	String tsString = (String) timestamp.evaluate(req, res);
    	if (tsString != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(SQL_TIMESTAMP_FORMAT);
                rslt = new Timestamp((sdf.parse(tsString)).getTime());
            } catch(ParseException ex) {
                /* If the date can't be parsed, it's probably invalid anyway. */
                String msg = "Couldn't parse SQL timestamp:  " + tsString;
                logger.error(msg);
                throw new RuntimeException(msg, ex);
            }
    	}
    	
    	return rslt;
    	
    }
    
}
