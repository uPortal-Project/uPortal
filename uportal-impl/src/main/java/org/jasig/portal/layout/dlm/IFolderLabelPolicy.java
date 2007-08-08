package org.jasig.portal.layout.dlm;

import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;

/**
 * Implementations define how localized versions of labels for layout folders
 * are handled. This allows different policies to be used depending on the 
 * specific structure being used. At some point if multiple profiles are 
 * supported this would have to be augmented to pass that information as well
 * or provide a mechanism to obtain a structure specific implementation of this
 * interface.
 *  
 * @author Mark Boyd
 *
 */
public interface IFolderLabelPolicy
{
    /**
     * Scans the passed in layout and purges all labels that appear in the 
     * configuration system for folders in this layout. This will typically be 
     * called as part of removing a user or resetting their layout.
     * 
     * @param userId
     * @param isFragmentOwner
     * @param layout
     */
    public void purgeFolderLabels(int userId, boolean isFragmentOwner);
    
    /**
     * Scans the passed in layout and coordinates the labels that 
     * appear in the configuration system according to the needs of the specific
     * structure and theme being used. For example, for tab/column structure
     * only visible, regular folders of the root folder map to tabs and need
     * have localized names. No other folder name appears in the UI. This 
     * method is called when a user's or fragment layout is loaded.
     * 
     * @param userId
     * @param layout
     */
    public void coordinateFolderLabels(int userId, boolean isFramentOwner, 
            Document layout);

    /**
     * Handles removal of locale specific labels for a node during a delete 
     * operation.
     * 
     * @param nodeLayoutId the ILF nodeId
     * @param nodePlfId the ILF dlm:plfId attribute of the node if applicable
     * @param userId the user's uPortal integer id
     * @param isFragmentOwner indicates if they are a fragment owner or not
     */
    public void deleteNodeLabel(String nodeLayoutId, 
            String nodePlfId, boolean applyToAll,  
            int userId, boolean isFragmentOwner);
    
    /**
     * Handles adding locale specific labels for a newly added node. This 
     * should not be called for an existing node. Use updateNodeLabel() for an
     * existing node.
     * 
     * @param nodeId the ILF nodeId
     * @param parentId the node id of the parent
     * @param layout the DOM layout of the user
     * @param userId the user's uPortal integer id
     * @param isFragmentOwner indicates if they are a fragment owner or not
     * @param value the value for the label
     */
    public void addNodeLabel(String nodeId, String parentId, Document layout, 
             int userId, boolean isFragmentOwner, String value);
    
    /**
     * Handles updating locale specific labels for a node during a node update.
     * Should only be called if the node is owned by the user or if it is owned
     * by a fragment and the editAllowed attribute is nonexitent or has a value
     * of true. In either case the plfId, the id used to persist that node in
     * the DB is used. For user owned nodes this is the same as the ilf id.
     * 
     * @param plfId the id of the node in the PLF
     * @param userId the user's uPortal integer id
     * @param isFragmentOwner indicates if they are a fragment owner or not
     * @param value the value for the label
     */
    public void updateNodeLabel(String plfId, 
            int userId, boolean isFragmentOwner, String value);
    
    /**
     * Handles retrieval of locale specific labels for a node including handling
     * overrides of fragment node labels if applicable.
     * 
     * @param nodeLayoutId the ILF nodeId
     * @param nodePlfId the ILF dlm:plfId attribute of the node if applicable
     * @param editAllowed the ILF dlm:editAllowed attribute of the node if applicable
     * @param userId the user's uPortal integer id
     * @param isFragmentOwner indicates if they are a fragment owner or not
     * @param value the default value to use if one is not found
     */
    public String getNodeLabel(String nodeLayoutId, 
            String nodePlfId, boolean editAllowed, 
            int userId, boolean isFragmentOwner, String value);
}
