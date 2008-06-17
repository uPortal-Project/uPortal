/*
 * Created on May 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jasig.portal.channels.jsp.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Mark Boyd
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Model
{
    private static final Log LOG = LogFactory.getLog(Model.class);
    private static final int ACTION_LABEL_TYPE = 0;
    private static final int ASPECT_LABEL_TYPE = 1;
    private static final int NODE_LABEL_TYPE = 2;
    
    private Config cfg = null;
    private int unresolvableCount = 0;

    private ArrayList indentations = new ArrayList();


    /**
     * returns the node with the specified id in the root node. if the id passed
     * in to this method is the root node's id than the root node is returned.
     * 
     * @param id
     *        the id to search for.
     * @return the node with the specified id.
     * @author Alexander Boyd
     */
    public Node getNodeForId(String id)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getNodeForId('" + id + "')");
        return getNodeForId(id, root);
    }

    /**
     * returns the node with the specified id in the specified node. if the node
     * does not have children and the node's id is not the specified id
     * than <code>null</code> is returned. if the specified id is not found in
     * this node's hierarchy than null is returned. otherwise, the node with the
     * specified id is returned. if more than one node in this hierarchy is
     * found, it uses the first one it encounters.
     * 
     * @param id
     *        the id to search for.
     * @param nodeToSearch
     *        the node to search for id in.
     * @return the node with the specified id, or null if none is found.
     * @author Alexander Boyd
     */
    public static Node getNodeForId(String id, Node nodeToSearch)
    {
        if (id.equals(nodeToSearch.getId()))
            return nodeToSearch;
        if (! nodeToSearch.getHasChildren())
            return null;
        Node[] children = nodeToSearch.getChildren();
        if(children == null)
            return null;
        Node childWithId = null;
        
        for (int i = 0; childWithId == null && i < children.length; i++)
            childWithId = getNodeForId(id, children[i]);

        return childWithId;
    }

    /**
     * sets whether or not the specified node is expanded.
     * 
     * @param id
     *        the id of the container
     * @param expanded
     *        whether or not it is expanded.
     * @throws IllegalArgumentException
     *         if the node specified by id is not a container or if there is no
     *         node with the specified id.
     * @throws NullPointerException
     *         if id is null.
     * @author Alexander Boyd
     */
    public void setExpandedForId(String id, boolean expanded)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("setExpandedForId('" + id + "', " + expanded +")");
        Node n = getNodeForId(id);
        if (n == null || ! n.getHasChildren())
            return;
        n.setIsExpanded(expanded);
    }

    /**
     * Sets the root object to be translated into the root node of the tree.
     * 
     * @param root
     *        The root to set.
     */
    public void setRootDomainObject(Object root)
    {
        if (LOG.isDebugEnabled())
        {
            if (root == null)
                LOG.debug("setRootDomainObject(null)");
            else
                LOG.debug("setRootDomainObject(" + root.getClass().getName() +
                        ")");
        }
        this.root = resolveChild(root);
        this.root.setIsExpanded(true);
    }
    
    public Model(Config cfg)
    {
        this.cfg = cfg;
    }
    
    public Config getConfig()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getConfig()");
        return cfg;
    }
    
    /**
     * Translates a domain object into the TreeNode that represents it if a
     * suitable surrogate is found. Called by the containing node to resolve
     * its child nodes.
     * 
     * @param o
     * @return
     */
    Node resolveChild(Object o)
    {
        return resolveObject(o, false);
    }

    /**
     * Translates a domain object's aspect into the TreeNode that represents
     * that aspect in the tree if a suitable surrogate is found. Called by the
     * containing node to resolve its aspect nodes.
     * 
     * @param o
     * @return
     */
    Node resolveAspect(Object o)
    {
        return resolveObject(o, true);
    }

    /**
     * Performs the surrogate lookup and instantiation of corresponding 
     * TreeNode objects.
     * 
     * @param o
     * @param isAspect
     * @return
     */
    private Node resolveObject(Object o, boolean isAspect)
    {
        ISurrogate s = null;
        for(Iterator itr = cfg.getSurrogates().iterator(); s == null && itr.hasNext();)  
        {
            ISurrogate sgt = (ISurrogate) itr.next();
            if (sgt.canResolve(o))
                s = sgt;
        }
        if (s == null)
        {
            if (cfg.getIncludeUnresolveables())
            {
                String id = "tun_" + unresolvableCount++;
                Node node = new Node(this, id, o);
                if (isAspect)
                    node.setIsAspect(true);
                return node;
            }
            return null;
        }
        String id = s.getId(o);
        Node node = new Node(this, id, o, s);
        
        if (isAspect)
            node.setIsAspect(true);
        else if (! cfg.getLazilyLoad())
        {
            node.loadChildren();
            node.loadAspects();
        }
        return node;
    }
    
//////////////////// Methods accessed by the JSP renderer ///////////////////
    
    public List getIndentImages()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getIndentImages() --> list.size()=" +
                    indentations.size());
        return indentations;
    }
    
    public void setPushIndent(String indentType)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("setPushIndent('" + indentType + "')");
        indentations.add(indentType);
    }

    public String getPopIndent()
    {
        if (indentations.size() > 0)
        {
            String indent=(String) indentations.remove(indentations.size() - 1);
            if (LOG.isDebugEnabled())
                LOG.debug("getPopIndent() --> '" + indent + "'");
            return indent;
        }
        if (LOG.isDebugEnabled())
            LOG.debug("getPopIndent() --> 'garbage' since empty");
        return "garbage"; // not important since not used but can't be empty
    }

    /**
     * Returns the object acquired from implementations of one of two plugged-in
     * interfaces depending on the label type being rendered. If label type is
     * the same as the value returned by getNodeLabelType() or 
     * getAspectLabelType() then the returned object is acquired from the 
     * instance of ISurrogate that resolved the Node represented by getNode(). 
     * If the label type is the same as the value returned by 
     * getActionLabelType() then the returned object is acquired from the 
     * instance of IDomainActionSet.
     * 
     * @return
     */
    public Object getLabelData()
    {
        Node node = getNode();
        if (getLabelType() == getActionLabelType())
        {
            IDomainActionSet set = cfg.getActionSet();
            
            if (set == null || node == null)
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("getLabelData() --> null (for actionLabelType)");
                return null;
            }
            Object obj = node.getDomainObject();
            String action = getDomainAction();
            if (LOG.isDebugEnabled())
                LOG.debug("getLabelData() --> IDomainActionSet.getLabelData('" + 
                        action + "', domainObject) for node id=" + node.getId());
            return set.getLabelData(action, obj);
        }
        if (node == null)
        {
            if (LOG.isDebugEnabled())
                LOG.debug("getLabelData() --> null (for null node)");
            return null;
        }
        if (LOG.isDebugEnabled())
            LOG.debug("getLabelData() --> node.getLabelData() for " +
                    "node id=" + node.getId());
        return node.getLabelData();
    }
    /**
     * Used in the tree rendering JSP to translate supported JSP Map semantics
     * to dynamic lookup of an expansion and collapse URLs for a passed in node
     * id. The following pattern in the JSP will cause this method to be called
     * to obtain the Map represented by "treeUrls".
     * 
     * <c:out value="${requestScope.model.treeUrls.expand[node.id]}"/>
     * 
     * @author Mark Boyd
     *  
     */
    public Map getTreeUrls()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getTreeUrls()");
        return cfg.getTreeUrlResolvers();
    }
    
    /**
     * The first time called this method marks this model as being in the midst 
     * of rendering and returns true. Thereafter, it returns false until 
     * stopRendering has been called. This method is called by the JSP to 
     * learn if rendering started as a result of making this call. The method
     * name was chosen for the resulting access in the JSP and is misleading 
     * when viewed here in the code.
     * 
     * @return Returns true if the JSP is rendering the model.
     */
    public synchronized boolean getStartRendering()
    {
        if (isRendering)
        {
            if (LOG.isDebugEnabled())
                LOG.debug("getStartRendering() --> false");
            return false;
        }
        if (LOG.isDebugEnabled())
            LOG.debug("getStartRendering() --> true");
        isRendering = true;
        return isRendering;
    }
    
    /**
     * Marks this model as not being in the midst of rendering. This method is
     * called by the JSP to indicate that rendering has completed The method
     * name was chosen for the resulting access in the JSP and is misleading
     * when viewed here in the code since it does have side affects from being
     * called.
     *  
     */
    public synchronized void setIsRendering(boolean b)
    {
        this.isRendering = b;
        if (LOG.isDebugEnabled())
            LOG.debug("setIsRenderering(" + b + ")");
    }
    
    /**
     * Returns the root node of the tree.
     * 
     * @return Returns the root.
     */
    public Node getRoot()
    {
        if (LOG.isDebugEnabled())
        {
            if (root == null)
                LOG.debug("getRoot() --> null");
            else
                LOG.debug("getRoot() --> id=" + root.getId());
        }
        return root;
    }

    /**
     * Returns the currently rendering node. Called by the JSP renderer.
     * 
     * @return Returns the node.
     */
    public Node getNode()
    {
        if (LOG.isDebugEnabled())
        {
            if (node == null)
                LOG.debug("getNode() --> null");
            else
                LOG.debug("getNode() --> id=" + node.getId());
        }
        return node;
    }

    /**
     * Sets the currently rendering node. Called by the JSP renderer.
     * 
     * @param node
     *        The node to set.
     */
    public void setNode(Node node)
    {
        if (LOG.isDebugEnabled())
        {
            if (node == null)
                LOG.debug("setNode(null)");
            else
                LOG.debug("setNode(id=" + node.getId() + ")");
        }
        this.node = node;
    }


    /**
     * Returns the current domain action being rendered by the tree. This is
     * called by the required label renderer JSP identified via
     * getLabelRenderer().
     * 
     * @return
     */
    public String getDomainAction()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getDomainAction() --> '" + action + "'");
        return action;
    }

    /**
     * Sets the current domain action being rendered by the tree. This is called
     * by tree renderer to pass to the label renderer the action whose label is
     * to be rendered. The required label renderer JSP is identified via
     * getLabelRenderer().
     * 
     * @return
     */
    public void setDomainAction(String action)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("setDomainAction('" + action + "')");
        this.action = action;
    }

    /**
     * Returns the current label type rendering being requested of the label
     * renderer by the tree renderer. This is called by
     * the required label renderer JSP identified via getLabelRenderer().
     * 
     * @return
     */
    public int getLabelType()
    {
        if (LOG.isDebugEnabled())
        {
            String type = "" + labelType + "?";
            if (labelType == NODE_LABEL_TYPE)
                type = "NODE";
            else if (labelType == ASPECT_LABEL_TYPE)
                type = "ASPECT";
            else if (labelType == ACTION_LABEL_TYPE)
                type = "ACTION_LABEL_TYPE";
            LOG.debug("getLabelType() --> " + type);
        }
        return labelType;
    }
    
    // the following three accessors are added so that the rendering JSP can 
    // access the static lable type fields.
    public int getActionLabelType()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getActionLabelType()");
        return ACTION_LABEL_TYPE;
    }
    public int getNodeLabelType()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getNodeLabelType()");
        return NODE_LABEL_TYPE;
    }
    public int getAspectLabelType()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getAspectLabelType()");
        return ASPECT_LABEL_TYPE;
    }

    /**
     * Sets the current label type being requested of the label renderer by the
     * tree renderer. This is called by
     * tree renderer to pass to the label renderer the label type to
     * be rendered. The required label renderer JSP is identified via 
     * getLabelRenderer().
     * 
     * @return
     */
    public void setLabelType(int labelType)
    {
        if (LOG.isDebugEnabled())
        {
            String type = "" + labelType + "?";
            if (labelType == NODE_LABEL_TYPE)
                type = "NODE";
            else if (labelType == ASPECT_LABEL_TYPE)
                type = "ASPECT";
            else if (labelType == ACTION_LABEL_TYPE)
                type = "ACTION_LABEL_TYPE";
            LOG.debug("setLabelType(" + type + ")");
            
        }
        this.labelType = labelType;
    }
    ///////// reviewed variables /////////
    
    private Node root = null;
    private boolean isRendering = false;
    private Node node = null;
    private String action = null;
    private int labelType = 0;
}
