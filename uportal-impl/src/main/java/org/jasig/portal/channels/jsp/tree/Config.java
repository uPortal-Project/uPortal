package org.jasig.portal.channels.jsp.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channels.jsp.Deployer;
import org.jasig.portal.properties.PropertiesManager;

/**
 * Represents configuration of the JSP Tree control.
 * 
 * @author Mark Boyd
 *
 */
public class Config
{
    private static final Log LOG = LogFactory.getLog(Config.class);
    private static final List DEFAULT_SURROGATES = getDefaultSurrogates();
    private static final String TREE_RENDERER = getTreeRendererPath();
    
    private boolean includeUnresolveables = false;
    private boolean lazilyLoad = true;
    private boolean showBranches = true;
    private boolean viewContainment = false;

    private IDomainActionSet actionSet = null;
    private Map treeUrlResolvers = null;
    private Map images;
    private List surrogates = null;
    private String labelRenderer = null;

    /**
     * Returns an instance of Config with suitable defaults for rendering a
     * very limited view of the tree hierarchy. Most default settings will be
     * overridden for all implementations. By default there are no supported
     * domain actions. The default surrogate uses hash codes for identifiers
     * and returns the value of toString() for labels. Default label rendering
     * treats the value returned from getlabelData and any registered 
     * implementations of ISurrogate or IDomainActionSet as a String and embeds
     * that value as-is in the generated tree. The default image set will most
     * likely not be accessible since its image paths are relative to these 
     * classes and do not take into consideration the web application 
     * architecture in which the tree is being rendered. By default branches are
     * shown from parents to children, unresolveable domain objects are not
     * show in the tree, containment is not portrayed, and domain objects are
     * resolved through lazy loading.
     */
    public Config()
    {
        // set up default utilities
        String path = Images.class.getName();
        path = path.replace('.', '/');
        path = path.substring(0, path.lastIndexOf('/'));
        labelRenderer = "/WEB-INF/" + path + "defaultLabelRenderer.jsp";
        actionSet = new DefaultDomainActionSet();
        surrogates = DEFAULT_SURROGATES;
        includeUnresolveables = false;
        lazilyLoad = true;
        viewContainment = false;
        showBranches = true;
    }

    /**
     * Determines the path to the tree renderer JSP based upon the value of the
     * configured deployment location for jsp channel JSPs in portal.properties.
     * 
     * @return
     */
    private static String getTreeRendererPath() {
        String ctxRelativePath 
            = PropertiesManager.getProperty(Deployer.JSP_DEPLOY_DIR_PROP, 
                    "/WEB-INF/classes");
        if (ctxRelativePath.endsWith("/") || ctxRelativePath.endsWith("\\"))
            ctxRelativePath = ctxRelativePath.substring(0, ctxRelativePath
                    .length() - 1);
        
        String cls = Config.class.getName();
        String pkg = cls.substring( 0, cls.lastIndexOf( '.' ) );
        String jspPath = pkg.replace( '.', '/' ) + "/renderer.jsp";
        return ctxRelativePath + '/' + jspPath;
    }

    /**
     * Returns the web application context relative path to the tree renderer
     * JSP that knows how to render a tree using the classes in this package.
     * 
     * @return 
     */
    public String getRenderer()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("getRenderer() --> " + TREE_RENDERER);
        }
        return TREE_RENDERER;
    }

    /**
     * Creates a default surrogates list for very simplistic resolution of 
     * domain objects. See DefaultSurrogate.
     * 
     * @return
     */
    private static final List getDefaultSurrogates()
    {
       List l = new ArrayList();
       l.add(new DefaultSurrogate());
       return Collections.unmodifiableList(l);
    }
    
    /**
     * Returns the set of supported domain action identifiers.
     * 
     * @return Returns the actions.
     */
    public String[] getDomainActions()
    {
        if (LOG.isDebugEnabled())
        {
            if (actionSet == null)
                LOG.debug("getDomainActions() --> null");
            else
                LOG.debug("getDomainActions() --> length=" + 
                        actionSet.getSupportedActions().length);
        }
        return actionSet.getSupportedActions();
    }
    
    /**
     * Sets the required, web application context relative path to a JSP for
     * rendering action, node, and aspect labels. To this JSP will be passed the
     * TreeModel, the current TreeNode, the type of label being rendered, and 
     * for actions the current action. These are accessible in the called JSP 
     * via:
     * 
     * <pre>
     *   TreeModel = ${requestScope.model}
     *   Current TreeNode = ${requestScope.model.node}
     *   Label Type = ${requestScope.model.labelType} = ['action'|'node'|'aspect']
     *   Current Action = ${requestScope.model.action}
     * </pre>
     * 
     * @param labelRenderer
     */
    public void setLabelRenderer(String labelRenderer)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("setLabelRenderer(" + labelRenderer + ")");
        }
        this.labelRenderer = labelRenderer;
    }

    /**
     * Returns the web application context relative path to a JSP for rendering
     * node and action labels for the tree. Called by the tree renderer JSP to 
     * include the contents of the rendered label.
     * 
     * @return
     */
    public String getLabelRenderer()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("getLabelRenderer() --> " + labelRenderer);
        }
        return labelRenderer;
    }

    /**
     * Adds a surrogate suitable for handling translation of tree semantics to
     * calls to a domain object of specific types.
     * 
     * @param surrogate
     */
    public void addSurrogate(ISurrogate surrogate)
    {
        if (surrogates == DEFAULT_SURROGATES)
            surrogates = new ArrayList();
        if (LOG.isDebugEnabled())
        {
            if (surrogate == null)
                LOG.debug("addSurrogate(null)");
            else
                LOG.debug("addSurrogate(" + surrogate.getClass().getName() + ")");
        }
        surrogates.add(surrogate);
    }

    List getSurrogates()
    {
        if (LOG.isDebugEnabled())
        {
            if (surrogates == DEFAULT_SURROGATES)
                LOG.debug("getSurrogates() --> DEFAULT_SURROGATES");
            else
                LOG.debug("getSurrogates() --> list.size()=" + surrogates.size());
        }
        return surrogates;
    }
    public Map getImages()
    {
        if (LOG.isDebugEnabled())
        {
            if (images == null)
                LOG.debug("getImages() --> null");
            else
                LOG.debug("getImages() --> map.size()=" + images.size());
        }
        return images;
    }

    public void setImages(Map types)
    {
        if (LOG.isDebugEnabled())
        {
            if (types == null)
                LOG.debug("setImages(null)");
            else
                LOG.debug("setImages(map.size()=" + types.size() + ")");
        }
        images = types;
    }

    public boolean getViewContainment()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getViewContainment() --> " + viewContainment);
        return viewContainment;
    }

    public void setViewContainment(boolean b)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("setViewContainment(" + b + ")");
        viewContainment = b;
    }

    /**
     * @return Returns the showBranches.
     */
    public boolean getShowBranches()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getShowBranches() --> " + showBranches);
        return showBranches;
    }

    /**
     * @param showBranches
     *        The showBranches to set.
     */
    public void setShowBranches(boolean showBranches)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("setShowBranches(" + showBranches + ")");
        this.showBranches = showBranches;
    }

    /**
     * Returns the plugged-in implementation of IDomainActionSet.
     * @return
     */
    public IDomainActionSet getActionSet()
    {
        if (LOG.isDebugEnabled())
        {
            if (actionSet == null)
                LOG.debug("getActionSet() --> null");
            else
                LOG.debug("getActionSet() --> " + actionSet.getClass().getName());
        }
        return actionSet;
    }
    public void setActionSet(IDomainActionSet actionSet)
    {
        if (LOG.isDebugEnabled())
        {
            if (actionSet == null)
                LOG.debug("setActionSet(null)");
            else
                LOG.debug("setActionSet(" + actionSet.getClass().getName() + ")");
        }
        this.actionSet = actionSet;
    }
    public Map getTreeUrlResolvers()
    {
        if (treeUrlResolvers == null)
        {
            String message = "No instance of " +
            "ITreeActionUrlResolver was registered. Rendering can " +
            "not take place without an implementation of this " +
            "interface.";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        if (LOG.isDebugEnabled())
            LOG.debug("getTreeUrlResolvers() --> map.size()=" + treeUrlResolvers.size());
        return treeUrlResolvers;
    }
    public void setTreeUrlResolver(ITreeActionUrlResolver resolver)
    {
        if (LOG.isDebugEnabled()) 
        {
            if (resolver == null)
                LOG.debug("setTreeUrlResolver(null)");
            else
                LOG.debug("setTreeUrlResolver(" + resolver.getClass().getName() + ")");
        }
        treeUrlResolvers = new HashMap();
        treeUrlResolvers.put("expand", 
                new UrlResolver(resolver, ITreeActionUrlResolver.SHOW_CHILDREN));
        treeUrlResolvers.put("collapse", 
                new UrlResolver(resolver, ITreeActionUrlResolver.HIDE_CHILDREN));
        treeUrlResolvers.put("showAspects", 
                new UrlResolver(resolver, ITreeActionUrlResolver.SHOW_ASPECTS));
        treeUrlResolvers.put("hideAspects", 
                new UrlResolver(resolver, ITreeActionUrlResolver.HIDE_ASPECTS));
    }
    public void setIncludeUnresolveables(boolean includeUnresolveables)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("setIncludeUnresolveables(" + includeUnresolveables + ")");
        this.includeUnresolveables = includeUnresolveables;
    }

    public boolean getIncludeUnresolveables()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getIncludeUnresolveables() --> " + includeUnresolveables);
        return includeUnresolveables;
    }

    public boolean getLazilyLoad()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("getLazilyLoad() --> " + lazilyLoad);
        return lazilyLoad;
    }
    
    public void setLazilyLoad(boolean b)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("setLazilyLoad(" + b + ")");
        this.lazilyLoad = b;
    }
}
