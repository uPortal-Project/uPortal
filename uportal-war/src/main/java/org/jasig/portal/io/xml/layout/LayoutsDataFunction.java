/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.io.xml.layout;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.SimpleStringPortalData;
import org.jasig.portal.layout.dlm.ConfigurationLoader;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Lists each fragment owner in the portal
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LayoutsDataFunction implements Function<IPortalDataType, Iterable<? extends IPortalData>>, InitializingBean {
    private ConfigurationLoader configurationLoader;
    private DataSource dataSource;
    
    private NamedParameterJdbcOperations jdbcOperations;
    
    @Resource(name="PortalDb")
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Autowired
    public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }
    
    /* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.jdbcOperations = new NamedParameterJdbcTemplate(this.dataSource);
	}

	@Override
    public Iterable<? extends IPortalData> apply(IPortalDataType input) {
		final List<FragmentDefinition> fragments = this.configurationLoader.getFragments();
		final Set<String> fragmentOwners = new LinkedHashSet<String>();
		for (final FragmentDefinition fragmentDefinition : fragments) {
			fragmentOwners.add(fragmentDefinition.getOwnerId());
		}
		
    	final List<String> userList = this.jdbcOperations.queryForList(
    			"SELECT USER_NAME FROM UP_USER WHERE USER_NAME NOT IN (:userNames)", 
    			Collections.singletonMap("userNames", fragmentOwners), 
    			String.class);
	    
	    return Lists.transform(userList, new Function<String, IPortalData>() {
            @Override
            public IPortalData apply(final String userName) {
                return new SimpleStringPortalData(userName, null, null);
            }
        });
    }
}
