/*
 * Created on Dec 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jasig.portal.channels.jsp.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Boyd
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Node
{
    private static final Node[] TREE_NODE_ARRAY = new Node[] {};

    private String id = null;
    private boolean isAspect = false;
    private boolean hasNextSibling = false;
    private ISurrogate surrogate = null;
    private Object domainObject = null;
    private boolean isExpanded = false;
    private boolean isShowingAspects = false;
    private Node[] children = null;
    private Model model = null;
    private boolean isUnresolveable = false;

    private Node[] aspects;
    
    private Node()
    {
    }

    Node(Model model, String id, Object domainObject)
    {
        this(model, id, domainObject, null);
        this.isUnresolveable = true;
    }
    
    Node(Model model, String id, Object domainObject, ISurrogate s)
    {
        this.model = model;
        this.id = id;
        this.domainObject = domainObject;
        this.surrogate = s;
    }
    
    Object getLabelData()
    {
        if (surrogate == null)
            return null;
        return surrogate.getLabelData(domainObject);
    }
    /**
     * Returns the domain object represented by this node.
     * @return
     */
    Object getDomainObject()
    {
        return domainObject;
    }
    public boolean isUnresolveable()
    {
        return isUnresolveable;
    }
    public String getId()
    {
        return id;
    }
    
    public boolean getIsAspect()
    {
        return isAspect;
    }
    
    void setIsAspect(boolean b)
    {
        this.isAspect = b;
    }
    
    /**
     * Set by the tree renderer JSP during rendering.
     * @param b
     */
    public void setHasNextSibling(boolean b)
    {
        this.hasNextSibling = b;
    }
    
    /**
     * Used by the tree renderer JSP during rendering.
     * @param b
     */
    public boolean getHasNextSibling()
    {
        return this.hasNextSibling;
    }
    
    public boolean getCanContainChildren()
    {
        if (isAspect)
            return false;
        return this.surrogate.canContainChildren(this.domainObject);
    }
    
    public boolean getHasChildren()
    {
        if (isAspect)
            return false;
        if (children != null && children.length > 0)
            return true;
        return this.surrogate.hasChildren(this.domainObject);
    }
    
    public boolean getIsExpanded()
    {
        return this.isExpanded;
    }
    
    public void setIsExpanded(boolean b) 
    {
        this.isExpanded = b;
        if (isExpanded && children == null)
            loadChildren();
    }
    
    public boolean getHasAspects()
    {
        if (this.isAspect)
            return false;
        return this.surrogate.hasAspects(this.domainObject);
    }
    
    public boolean getIsShowingAspects() 
    {
        return this.isShowingAspects;
    }
    
    public void setIsShowingAspects(boolean b)
    {
        this.isShowingAspects = b;
        if (isShowingAspects && aspects == null)
            loadAspects();
    }
    
    public Node[] getChildren()
    {
        return this.children;
    }
    
    void loadChildren()
    {
        Object[] domainObjects = null;
        domainObjects = this.surrogate.getChildren(this.domainObject);
        List nodes = new ArrayList();
        
        if (domainObjects != null && domainObjects.length > 0)
        {
            for (int i=0; i<domainObjects.length; i++)
            {
                Node node = this.model.resolveChild(domainObjects[i]);
                if (node != null)
                    nodes.add(node);
            }
        }
        this.children = (Node[]) nodes.toArray(TREE_NODE_ARRAY);
    }
    void loadAspects()
    {
        Object[] domainObjects = null;
        domainObjects = this.surrogate.getAspects(this.domainObject);
        List nodes = new ArrayList();
        
        if (domainObjects != null && domainObjects.length > 0)
        {
            for (int i=0; i<domainObjects.length; i++)
            {
                Node node = this.model.resolveAspect(domainObjects[i]);
                if (node != null)
                    nodes.add(node);
            }
        }
        this.aspects = (Node[]) nodes.toArray(TREE_NODE_ARRAY);
    }
    
    public Node[] getAspects()
    {
        return this.aspects;
    }
}
