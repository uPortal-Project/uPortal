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

package org.jasig.portal.io.xml.permission;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jasig.portal.io.xml.AbstractJaxbDataHandler;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.io.xml.SimpleStringPortalData;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Handles import and export of Permission Owner data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PermissionOwnerImporterExporter extends
		AbstractJaxbDataHandler<ExternalPermissionOwner> {
	
	private PermissionOwnerPortalDataType portalDataType;
	private IPermissionOwnerDao permissionOwnerDao;
	
	@Autowired
	public void setPortalDataType(PermissionOwnerPortalDataType portalDataType) {
		this.portalDataType = portalDataType;
	}

	@Autowired
	public void setPermissionOwnerDao(IPermissionOwnerDao permissionOwnerDao) {
		this.permissionOwnerDao = permissionOwnerDao;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporter#getImportDataKeys()
	 */
	@Override
	public Set<PortalDataKey> getImportDataKeys() {
		return Collections.singleton(PermissionOwnerPortalDataType.IMPORT_40_DATA_KEY);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataExporter#getPortalDataType()
	 */
	@Override
	public IPortalDataType getPortalDataType() {
		return this.portalDataType;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataExporter#getPortalData()
	 */
	@Override
	public Iterable<? extends IPortalData> getPortalData() {
	    final List<IPermissionOwner> permissionOwners = this.permissionOwnerDao.getAllPermissionOwners();
	    
	    return Lists.transform(permissionOwners, new Function<IPermissionOwner, IPortalData>() {
            /* (non-Javadoc)
             * @see com.google.common.base.Function#apply(java.lang.Object)
             */
            @Override
            public IPortalData apply(IPermissionOwner permissionOwner) {
                return new SimpleStringPortalData(
                        permissionOwner.getFname(),
                        permissionOwner.getName(),
                        permissionOwner.getDescription());
            }
        });
	}

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporter#importData(java.lang.Object)
     */
    @Override
    @Transactional
    public void importData(ExternalPermissionOwner externalPermissionOwner) {
        final String name = externalPermissionOwner.getName();
        final String fname = externalPermissionOwner.getFname();
        final IPermissionOwner permissionOwner = this.permissionOwnerDao.getOrCreatePermissionOwner(name, fname);
        
        final String desc = externalPermissionOwner.getDesc();
        permissionOwner.setDescription(desc);
        
        for (final ExternalActivity externalActivity : externalPermissionOwner.getActivities()) {
            final String activityName = externalActivity.getName();
            final String activityFname = externalActivity.getFname();
            final String targetProvider = externalActivity.getTargetProvider();
            
            final IPermissionActivity permissionActivity = this.permissionOwnerDao.getOrCreatePermissionActivity(permissionOwner, activityName, activityFname, targetProvider);
            
            final String activityDesc = externalActivity.getDesc();
            permissionActivity.setDescription(activityDesc);
        }
        
        this.permissionOwnerDao.saveOwner(permissionOwner);
    }

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataExporter#exportData(java.lang.String)
	 */
	@Override
	public ExternalPermissionOwner exportData(String fname) {
	    final IPermissionOwner permissionOwner = this.permissionOwnerDao.getPermissionOwner(fname);
	    return convert(permissionOwner);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataExporter#getFileName(java.lang.Object)
	 */
	@Override
	public String getFileName(ExternalPermissionOwner data) {
	    return SafeFilenameUtils.makeSafeFilename(data.getFname());
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataDeleter#deleteData(java.lang.String)
	 */
	@Override
	public ExternalPermissionOwner deleteData(String id) {
		throw new UnsupportedOperationException();
	}
	
    protected ExternalPermissionOwner convert(final IPermissionOwner permissionOwner) {
        if (permissionOwner == null) {
            return null;
        }
        
        final ExternalPermissionOwner externalPermissionOwner = new ExternalPermissionOwner();
        
        externalPermissionOwner.setName(permissionOwner.getName());
        externalPermissionOwner.setFname(permissionOwner.getFname());
        externalPermissionOwner.setDesc(permissionOwner.getDescription());
        
        final List<ExternalActivity> externalActivities = externalPermissionOwner.getActivities();
        
        final Set<IPermissionActivity> activities = permissionOwner.getActivities();
        for (final IPermissionActivity permissionActivity : activities) {
            final ExternalActivity externalActivity = new ExternalActivity();
            externalActivity.setName(permissionActivity.getName());
            externalActivity.setFname(permissionActivity.getFname());
            externalActivity.setDesc(permissionActivity.getDescription());
            externalActivity.setTargetProvider(permissionActivity.getTargetProviderKey());
            
            externalActivities.add(externalActivity);
        }
        
        externalPermissionOwner.setVersion("4.0");
        
        return externalPermissionOwner;
    }


}
