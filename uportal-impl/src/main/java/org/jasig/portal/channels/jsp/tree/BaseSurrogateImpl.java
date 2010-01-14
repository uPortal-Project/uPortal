/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.jsp.tree;

/**
 * A base class for surrogates providing default functionality that can be used
 * by subclasses if appropriate and alleviating them from having to implement 
 * identical methods to conform to the ISurrogate interface.
 * 
 * @author Mark Boyd
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public abstract class BaseSurrogateImpl implements ISurrogate
{

    /**
     * Returns null meaning that the domain object has no aspects.
     */
    public Object[] getAspects(Object o)
    {
        return null;
    }
    /**
     * Returns null meaning that the domain object has no children.
     */
    public Object[] getChildren(Object o)
    {
        return null;
    }
    /**
     * Returns null meaning that the domain object has no object specific label 
     * data for use in custome rendering.
     */
    public Object getLabelData(Object o)
    {
        return null;
    }
    /**
     * Returns false meaning that the domain object has no aspects.
     */
    public boolean hasAspects(Object o)
    {
        return false;
    }
    /**
     * Returns false meaning that the domain object has no children.
     */
    public boolean hasChildren(Object o)
    {
        return false;
    }
    /**
     * Returns false meaning that the domain object can't contain children.
     */
    public boolean canContainChildren(Object o)
    {
        return false;
    }
}
