/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.NodeList;

/**
 * Implementation of {@link ConfigurationLoader} that loads 
 * {@link FragmentDefinition} objects from the database using JPA/Hibernate.  
 * You can enable this feature by setting the <code>ConfigurationLoader.impl</code> 
 * property in dlm.xml.
 * 
 * @author awills
 */
public class RDBMConfigurationLoader extends LegacyConfigurationLoader {
    // Instance Members.
    private IFragmentDefinitionDao fragmentDao = null;

    /**
     * @param fragmentDao the fragmentDao to set
     */
    public void setFragmentDao(IFragmentDefinitionDao fragmentDao) {
        this.fragmentDao = fragmentDao;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dlm.LegacyConfigurationLoader#getFragments()
     */
    @Override
    public List<FragmentDefinition> getFragments() {
        final List<FragmentDefinition> allFragments = this.fragmentDao.getAllFragments();
        // lastly sort according to precedence
        Collections.sort(allFragments, new FragmentComparator() );
        return Collections.unmodifiableList(allFragments);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dlm.LegacyConfigurationLoader#getFragments(org.w3c.dom.NodeList)
     */
    @Override
    protected List<FragmentDefinition> getFragments(NodeList frags) {
        //Return null to just ignore the data from the dlm XML file
        return null;
    }
}
