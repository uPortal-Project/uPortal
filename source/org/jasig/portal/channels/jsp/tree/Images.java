package org.jasig.portal.channels.jsp.tree;


import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jasig.portal.PortalException;

/**
 * Represents a set of well known keys and the default URL paths for images used
 * in rendering the tree. These are loaded from configuration using the values
 * of the constants defined in this class having names of XXX_IMG. The matching
 * constants having just the XXX names hold the values of the keys into the
 * table. These keys are used in the tree control's renderer.jsp to embed the
 * URL from the map for that key into the rendered html tree.
 * 
 * Applications that use the tree control can provide their own maps of image
 * names to image URLs using the XXX constant names for the keys and providing
 * their own URL values in their map objects. These customized maps can then be
 * passed to the Config object for use by the renderer. If only a portion of the
 * images are to be overridden they can obtain the default URLs from the
 * configuration system for the unchanged images using the XXX_IMG constants.
 * 
 * <pre>
 *    FOLDER_OPEN             = &quot;folderOpen&quot;
 *    FOLDER_CLOSED           = &quot;folderClosed&quot;
 *    START_OF_CONTAINER      = &quot;startOfContainer&quot;
 *    END_OF_CONTAINER        = &quot;endOfContainer&quot;
 *    MIDDLE_BRANCH_COLLAPSED = &quot;middleBranchCollapsed&quot;
 *    MIDDLE_BRANCH_EXPANDED  = &quot;middleBranchExpanded&quot;
 *    LAST_BRANCH_COLLAPSED   = &quot;lastBranchCollapsed&quot;
 *    LAST_BRANCH_EXPANDED    = &quot;lastBranchExpanded&quot;
 *    NO_BRANCH               = &quot;noBranch&quot;
 *    TRANSPARENT_POINT       = &quot;transparentPoint&quot;
 *    MIDDLE_CHILD            = &quot;middleChild&quot;
 *    SKIPPED_CHILD_RPT       = &quot;skippedChildRpt&quot;
 *    SKIPPED_CHILD           = &quot;skippedChild&quot;
 *    LAST_CHILD              = &quot;lastChild&quot;
 *    SHOW_ASPECTS            = &quot;showAspects&quot;
 *    HIDE_ASPECTS            = &quot;hideAspects&quot;
 * </pre>
 * 
 * @author Mark Boyd
 */
public class Images
{
    private static final Properties cCfg = loadConfiguration();
        // restore i18n piece made available 
        //Configuration.lookup(Images.class);
    private static final String cfgPrefix = Images.class.getName() + ".";

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * open container node in the tree. A typical default image for containers 
     * is a folder.
     */
    public static final String FOLDER_OPEN = "folderOpen";
    
    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #FOLDER_OPEN} image.
     */
    public static final String FOLDER_OPEN_IMG = cfgPrefix + FOLDER_OPEN;

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * closed container node in the tree.
     */
    public static final String FOLDER_CLOSED = "folderClosed";
    
    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #FOLDER_CLOSED} image.
     */
    public static final String FOLDER_CLOSED_IMG = cfgPrefix + FOLDER_CLOSED;

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * containment start image in the tree when containment is being used. Such 
     * an image implies containment. A typical default is an open paren. These
     * images can be used to render logical expressions via the tree renderer.
     */
    public static final String START_OF_CONTAINER = "startOfContainer";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #START_OF_CONTAINER} image.
     */
    public static final String START_OF_CONTAINER_IMG 
        = cfgPrefix + START_OF_CONTAINER;
    /**
     * Represents the key used by the renderer to obtain the URL for the
     * containment end image in the tree when containment is being used. A 
     * typical default is a close paren.
     */
    public static final String END_OF_CONTAINER = "endOfContainer";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #END_OF_CONTAINER} image.
     */
    public static final String END_OF_CONTAINER_IMG 
        = cfgPrefix + END_OF_CONTAINER;

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * image depicting a collapsed branch point in the tree where there are 
     * additional following sibling nodes. For example:
     * 
     * <pre>
     *    middleBranchCollapsed
     *        |            
     *      +---+
     *      | + |--- node image and label
     *      +---+
     *        |                           
     *        |   
     * </pre>
     */
    public static final String MIDDLE_BRANCH_COLLAPSED = "middleBranchCollapsed";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #MIDDLE_BRANCH_COLLAPSED} image.
     */
    public static final String MIDDLE_BRANCH_COLLAPSED_IMG 
        = cfgPrefix + MIDDLE_BRANCH_COLLAPSED;

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * image depicting an expanded branch point in the tree where there are 
     * additional following sibling nodes. For example:
     * 
     * <pre>
     *    middleBranchExpanded
     *        | 
     *      +---+
     *      | - +--- node image and label
     *      +---+     |
     *        |       +-- child 
     *        |       |
     * </pre>
     */
    public static final String MIDDLE_BRANCH_EXPANDED = "middleBranchExpanded";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #MIDDLE_BRANCH_EXPANDED} image.
     */
    public static final String MIDDLE_BRANCH_EXPANDED_IMG 
        = cfgPrefix + MIDDLE_BRANCH_EXPANDED;

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * image depicting a collapsed branch point in the tree where there are no
     * additional following sibling nodes. For example:
     * 
     * <pre>
     *   lastBranchCollapsed
     *       |  
     *     +---+
     *     | + |--- node image and label
     *     +---+ 
     *    
     * </pre>
     */
    public static final String LAST_BRANCH_COLLAPSED = "lastBranchCollapsed";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #LAST_BRANCH_COLLAPSED} image.
     */
    public static final String LAST_BRANCH_COLLAPSED_IMG 
        = cfgPrefix + LAST_BRANCH_COLLAPSED;

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * image depicting an expanded branch point in the tree where there are no
     * additional following sibling nodes. For example:
     * 
     * <pre>
     *   lastBranchExpanded
     *       | 
     *     +---+
     *     | - +--- node image and label
     *     +---+     |
     *               +--- child 
     *               |
     * </pre>
     */
    public static final String LAST_BRANCH_EXPANDED = "lastBranchExpanded";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #LAST_BRANCH_EXPANDED} image.
     */
    public static final String LAST_BRANCH_EXPANDED_IMG 
        = cfgPrefix + LAST_BRANCH_EXPANDED;

    /**
     * Represents the key used by the renderer to obtain the URL for the
     * image depicting an area of the tree not containing a branch. For example
     * the lastBranchExpanded example has been included again below but with 
     * an empty representation of this image below the lastBranchExpanded image.
     * 
     * <pre>
     *   lastBranchExpanded
     *       | 
     *     +---+
     *     | - +-- node image and label
     *     +---+     |
     *     +---+     +-- child
     *     |   |     |
     *     +---+     |
     * </pre>
     */
    public static final String NO_BRANCH = "noBranch";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #NO_BRANCH} image.
     */
    public static final String NO_BRANCH_IMG = cfgPrefix + NO_BRANCH;

    /**
     * Represents the key used by the renderer to obtain the URL for a single 
     * pixel transparent image suitable for stretching to fill areas of labels 
     * for spacing.
     * 
     * </pre>
     */
    public static final String TRANSPARENT_POINT = "transparentPoint";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #TRANSPARENT_POINT} image.
     */
    public static final String TRANSPARENT_POINT_IMG 
        = cfgPrefix + TRANSPARENT_POINT;

    /**
     * Represents the key used by the renderer to obtain the URL for the image 
     * representing the non expandable branch of a child node with additional 
     * following sibling nodes.
     * 
     * <pre>
     *   middleBranch
     *     |        
     *     +-- child image and label
     *     |
     * </pre>
     */
    public static final String MIDDLE_CHILD = "middleChild";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #MIDDLE_CHILD} image.
     */
    public static final String MIDDLE_CHILD_IMG = cfgPrefix + MIDDLE_CHILD;

    /**
     * Represents the key used by the renderer to obtain the URL for a 
     * vertically repeatable version of an image representing the branch 
     * leading from the parent of an exposed child to the parent's following 
     * sibling. By using this repeatable version as a repeated background image
     * the text in rows can wrap and the tree branches will accomodate the 
     * expansion by appearing to expand and collapse vertically as needed.
     * 
     * <pre>
     *   skippedChild
     *     |   |
     *     |   +-- child of parallel branch skipped by parent's sibling branch
     *     |    
     *     +-- child's parent's following sibling
     *     
     * </pre>
     */
    public static final String SKIPPED_CHILD_RPT = "skippedChildRpt";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #SKIPPED_CHILD_RPT} image.
     */
    public static final String SKIPPED_CHILD_RPT_IMG 
        = cfgPrefix + SKIPPED_CHILD_RPT;

    /**
     * Represents the key used by the renderer to obtain the URL for an image 
     * representing the branch leading from the parent of an exposed child to 
     * the parent's following sibling. This may be the same as the repeatable
     * version or may differ if needed. 
     * 
     * <pre>
     *   skippedChild
     *     |   |
     *     |   +-- child of parallel branch skipped by parent's sibling branch
     *     |    
     *     +-- child's parent's following sibling
     *     
     * </pre>
     */
    public static final String SKIPPED_CHILD = "skippedChild";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #SKIPPED_CHILD} image.
     */
    public static final String SKIPPED_CHILD_IMG 
        = cfgPrefix + SKIPPED_CHILD;

    /**
     * Represents the key used by the renderer to obtain the URL for the branch
     * of a non-expandable child node with no additional following sibling 
     * nodes.
     * 
     * <pre>
     *   lastBranch
     *     |        
     *     +-- child image and label
     *     
     * </pre>
     */
    public static final String LAST_CHILD = "lastChild";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #LAST_CHILD} image.
     */
    public static final String LAST_CHILD_IMG = cfgPrefix + LAST_CHILD;

    /**
     * Represents the key used by the renderer to obtain the URL for an image 
     * used to show aspects of a node in the tree. Aspects do not receive a 
     * horizontal branch connected to the vertical child branch of a parent 
     * node thus implying that they are aspects of the parent node and not 
     * child nodes.
     * 
     * <pre>
     *   an expanded middle branch
     *     | 
     *   +---+                         +------------------+
     *   | - +-- node image and label  | showAspectsImage |
     *   +---+     |                   +------------------+
     *     |       +-- child 
     *     |       |
     *   
     * </pre>
     */
    public static final String SHOW_ASPECTS = "showAspects";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #SHOW_ASPECTS} image.
     */
    public static final String SHOW_ASPECTS_IMG = cfgPrefix + SHOW_ASPECTS;

    /**
     * Represents the key used by the renderer to obtain the URL for an image 
     * used to hide aspects of a node in the tree. Aspects do not receive a 
     * horizontal branch connected to the vertical child branch of a parent 
     * node thus implying that they are aspects of the parent node and not 
     * child nodes.
     * 
     * <pre>
     *   an expanded middle branch
     *     | 
     *   +---+                         +------------------+
     *   | - +-- node image and label  | hideAspectsImage |
     *   +---+     | aspect 1          +------------------+
     *     |       | aspect 2
     *     |       | aspect 3
     *     |       +-- child 
     *     |       |
     *   
     * </pre>
     */
    public static final String HIDE_ASPECTS = "hideAspects";

    /**
     * Defines the property name used within the configuration system to obtain
     * the URL for the tree's default {@link #HIDE_ASPECTS} image.
     */
    public static final String HIDE_ASPECTS_IMG = cfgPrefix + HIDE_ASPECTS;

    /**
     * Builds a Map of the default images used in the tree control that can then
     * be customized by an application to override some or all of the URLs for
     * the images.
     * 
     * @return
     */
    public static Map getDefaultSet()
    {
        Map<String, String> map = new HashMap<String, String>();

        map.put(FOLDER_OPEN,             cCfg.getProperty(FOLDER_OPEN_IMG));
        map.put(FOLDER_CLOSED,           cCfg.getProperty(FOLDER_CLOSED_IMG));
        map.put(START_OF_CONTAINER,      cCfg.getProperty(START_OF_CONTAINER_IMG));
        map.put(END_OF_CONTAINER,        cCfg.getProperty(END_OF_CONTAINER_IMG));
        map.put(MIDDLE_BRANCH_COLLAPSED, cCfg.getProperty(MIDDLE_BRANCH_COLLAPSED_IMG));
        map.put(MIDDLE_BRANCH_EXPANDED,  cCfg.getProperty(MIDDLE_BRANCH_EXPANDED_IMG));
        map.put(LAST_BRANCH_COLLAPSED,   cCfg.getProperty(LAST_BRANCH_COLLAPSED_IMG));
        map.put(LAST_BRANCH_EXPANDED,    cCfg.getProperty(LAST_BRANCH_EXPANDED_IMG));
        map.put(MIDDLE_CHILD,            cCfg.getProperty(MIDDLE_CHILD_IMG));
        map.put(SKIPPED_CHILD,           cCfg.getProperty(SKIPPED_CHILD_IMG));
        map.put(SKIPPED_CHILD_RPT,       cCfg.getProperty(SKIPPED_CHILD_RPT_IMG));
        map.put(LAST_CHILD,              cCfg.getProperty(LAST_CHILD_IMG));
        map.put(NO_BRANCH,               cCfg.getProperty(NO_BRANCH_IMG));
        map.put(SHOW_ASPECTS,            cCfg.getProperty(SHOW_ASPECTS_IMG));
        map.put(HIDE_ASPECTS,            cCfg.getProperty(HIDE_ASPECTS_IMG));
        map.put(TRANSPARENT_POINT,       cCfg.getProperty(TRANSPARENT_POINT_IMG));
        return map;
    }

    /*
     * Remove after i18n config tools are made available.
     */
    private static Properties loadConfiguration()
    {
        URL config = Images.class.getResource("org/jasig/portal/channels/jsp/tree/Images.properties");
        if (config == null)
            throw new PortalException("Unable to locate default images list file.");
        Properties defaults = new Properties();
        try
        {
            defaults.load(config.openStream());
        } catch (IOException e)
        {
            throw new PortalException("Unable to load default images list file.", e);
        }
        return defaults;
    }
}
