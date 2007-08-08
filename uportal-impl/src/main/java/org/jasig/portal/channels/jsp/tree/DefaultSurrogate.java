package org.jasig.portal.channels.jsp.tree;

/**
 * Provides default resolution of domain objects for presenting in a tree. The
 * class uses object hashCodes for the identifiers of objects and the results of 
 * toString() for label data.
 * 
 * @author Mark Boyd
 */
public class DefaultSurrogate extends BaseSurrogateImpl
{
    /**
     * Always returns true since it can resolve any domain object using basic
     * java language constructs for representing the objects in the tree.
     */
    public boolean canResolve(Object o)
    {
        return true;
    }

    /**
     * Returns the String version of the hash code of the passed-in object.
     */
    public String getId(Object o)
    {
        return "" + o.hashCode();
    }

    /**
     * Returns the results of calling toString() on the passed in object.
     */
    public Object getLabelData(Object o)
    {
        return null;
    }
}
