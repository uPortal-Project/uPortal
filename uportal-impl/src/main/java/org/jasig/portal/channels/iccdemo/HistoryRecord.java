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

package org.jasig.portal.channels.iccdemo;


import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A trivial class for keeping a queue of N strings.
 * This class is bound to jndi "chan-obj" context by CHistory, so
 * that CViewer (or other channels) could add to the history list.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class HistoryRecord {
    LinkedList history=new LinkedList();
    int maxRecords=10;

    public HistoryRecord() {};
    public HistoryRecord(int maxRecords) {
        this.maxRecords=maxRecords;
    }

    public void addHistoryRecord(String newRecord) {
        history.addFirst(newRecord);
        for(int i=0;i<history.size()-maxRecords;i++) {
            history.removeLast();
        }
    }

    public Iterator constIterator() {
        return (Collections.unmodifiableList(history)).iterator();
    }

    public String get(int i) {
        return (String)history.get(i);
    }
}
