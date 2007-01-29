/*
 * Created on Oct 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jasig.portal.channels.jsp.tree;

/**
 * If a tree is to support expanding and collapsing portions of the tree then
 * an implementation of this interface must be provided to the tree to translate
 * user actions into URLs suitable for the domain in which the tree is being
 * used and which will cause the appropriate methods on the appropriate tree
 * nodes to be called.
 * 
 * @author Mark Boyd
 */
public interface ITreeActionUrlResolver
{
    public static final int SHOW_CHILDREN = 0;
    public static final int HIDE_CHILDREN = 1;
    public static final int SHOW_ASPECTS = 2;
    public static final int HIDE_ASPECTS = 3;
    
    public String getTreeActionUrl(int type, String nodeId);
}
