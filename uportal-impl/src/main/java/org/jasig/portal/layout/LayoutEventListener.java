/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout;


/**
 * An interface implemented by objects interested in 
 * monitoring layout events.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
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
