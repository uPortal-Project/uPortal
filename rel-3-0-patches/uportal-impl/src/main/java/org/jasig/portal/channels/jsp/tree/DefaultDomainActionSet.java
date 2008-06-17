package org.jasig.portal.channels.jsp.tree;


/**
 * Represents a trivial default implementation of IDomainActionSet that provides
 * no supported actions for the tree.
 *  
 * @author Mark Boyd
 */
public class DefaultDomainActionSet implements IDomainActionSet
{

    /**
     * Returns null indicating that no domain specific actions are supported 
     * for objects in the tree.
     */
    public String[] getSupportedActions()
    {
        return null;
    }

    /**
     * Returns null since it will never get called due to no domain actions 
     * being supported by this class.
     */
    public Object getLabelData(String action, Object domainObject)
    {
        return null;
    }

}
