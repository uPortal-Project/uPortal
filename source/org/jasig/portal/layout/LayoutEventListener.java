/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;


/**
 * An interface implemented by objects interested in 
 * monitoring layout events.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public interface LayoutEventListener extends java.util.EventListener {
    public void channelAdded(LayoutEvent ev);
    public void channelUpdated(LayoutEvent ev);
    public void channelMoved(LayoutMoveEvent ev);
    public void channelDeleted(LayoutMoveEvent ev);

    public void folderAdded(LayoutEvent ev);
    public void folderUpdated(LayoutEvent ev);
    public void folderMoved(LayoutMoveEvent ev);
    public void folderDeleted(LayoutMoveEvent ev);

    public void layoutLoaded();
    public void layoutSaved();
}
