/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
 */
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
