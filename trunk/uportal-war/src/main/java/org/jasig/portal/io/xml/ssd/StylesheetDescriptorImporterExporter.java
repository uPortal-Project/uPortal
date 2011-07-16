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

package org.jasig.portal.io.xml.ssd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jasig.portal.io.xml.AbstractJaxbDataHandler;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.dao.jpa.LayoutAttributeDescriptorImpl;
import org.jasig.portal.layout.dao.jpa.OutputPropertyDescriptorImpl;
import org.jasig.portal.layout.dao.jpa.StylesheetParameterDescriptorImpl;
import org.jasig.portal.layout.om.ILayoutAttributeDescriptor;
import org.jasig.portal.layout.om.IOutputPropertyDescriptor;
import org.jasig.portal.layout.om.IStylesheetData;
import org.jasig.portal.layout.om.IStylesheetData.Scope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StylesheetDescriptorImporterExporter extends AbstractJaxbDataHandler<ExternalStylesheetDescriptor> {
    private StylesheetDescriptorPortalDataType stylesheetDescriptorPortalDataType;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
    
    @Autowired
    public void setStylesheetDescriptorPortalDataType(StylesheetDescriptorPortalDataType stylesheetDescriptorPortalDataType) {
        this.stylesheetDescriptorPortalDataType = stylesheetDescriptorPortalDataType;
    }

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporterExporter#getImportDataKey()
     */
    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return Collections.singleton(StylesheetDescriptorPortalDataType.IMPORT_40_DATA_KEY);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporterExporter#getPortalDataType()
     */
    @Override
    public IPortalDataType getPortalDataType() {
        return this.stylesheetDescriptorPortalDataType;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporterExporter#getPortalData()
     */
    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        return this.stylesheetDescriptorDao.getStylesheetDescriptors();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporterExporter#importData(java.lang.Object)
     */
    @Transactional
    @Override
    public void importData(ExternalStylesheetDescriptor data) {
        final String stylesheetName = data.getName();
        final String uri = data.getUri();
        
        IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptorByName(stylesheetName);
        if (stylesheetDescriptor == null) {
            stylesheetDescriptor = this.stylesheetDescriptorDao.createStylesheetDescriptor(stylesheetName, uri);
        }
        else {
            stylesheetDescriptor.setStylesheetResource(uri);
        }
        
        stylesheetDescriptor.setUrlNodeSyntaxHelperName(data.getUrlSyntaxHelper());
        stylesheetDescriptor.setDescription(data.getDescription());
        
        final List<ExternalOutputPropertyDescriptor> extOutputProperties = data.getOutputProperties();
        final List<IOutputPropertyDescriptor> outputPropertyDescriptors = new ArrayList<IOutputPropertyDescriptor>(extOutputProperties.size());
        for (final ExternalOutputPropertyDescriptor extOutputProperty : extOutputProperties) {
            final String name = extOutputProperty.getName();
            final Scope scope = Scope.valueOf(extOutputProperty.getScope().name());
            final OutputPropertyDescriptorImpl outputPropertyDescriptor = new OutputPropertyDescriptorImpl(name, scope);
            outputPropertyDescriptor.setDefaultValue(extOutputProperty.getDefaultValue());
            outputPropertyDescriptor.setDescription(extOutputProperty.getDescription());
            
            outputPropertyDescriptors.add(outputPropertyDescriptor);
        }
        stylesheetDescriptor.setOutputPropertyDescriptors(outputPropertyDescriptors);
        
        
        final List<ExternalStylesheetParameterDescriptor> extStylesheetParameters = data.getStylesheetParameters();
        final List<IStylesheetParameterDescriptor> stylesheetParameterDescriptors = new ArrayList<IStylesheetParameterDescriptor>(extOutputProperties.size());
        for (final ExternalStylesheetParameterDescriptor extStylesheetParameter : extStylesheetParameters) {
            final String name = extStylesheetParameter.getName();
            final Scope scope = Scope.valueOf(extStylesheetParameter.getScope().name());
            final StylesheetParameterDescriptorImpl stylesheetParameterDescriptor = new StylesheetParameterDescriptorImpl(name, scope);
            stylesheetParameterDescriptor.setDefaultValue(extStylesheetParameter.getDefaultValue());
            stylesheetParameterDescriptor.setDescription(extStylesheetParameter.getDescription());
            
            stylesheetParameterDescriptors.add(stylesheetParameterDescriptor);
        }
        stylesheetDescriptor.setStylesheetParameterDescriptors(stylesheetParameterDescriptors);
        
        
        final List<ExternalLayoutAttributeDescriptor> extLayoutAttributes = data.getLayoutAttributes();
        final List<ILayoutAttributeDescriptor> layoutAttributeDescriptors = new ArrayList<ILayoutAttributeDescriptor>(extOutputProperties.size());
        for (final ExternalLayoutAttributeDescriptor extLayoutAttribute : extLayoutAttributes) {
            final String name = extLayoutAttribute.getName();
            final Scope scope = Scope.valueOf(extLayoutAttribute.getScope().name());
            final LayoutAttributeDescriptorImpl layoutAttributeDescriptor = new LayoutAttributeDescriptorImpl(name, scope);
            layoutAttributeDescriptor.setDefaultValue(extLayoutAttribute.getDefaultValue());
            layoutAttributeDescriptor.setDescription(extLayoutAttribute.getDescription());
            layoutAttributeDescriptor.setTargetElementNames(new LinkedHashSet<String>(extLayoutAttribute.getTargetElements()));
            
            layoutAttributeDescriptors.add(layoutAttributeDescriptor);
        }
        stylesheetDescriptor.setLayoutAttributeDescriptors(layoutAttributeDescriptors);
        
        this.stylesheetDescriptorDao.updateStylesheetDescriptor(stylesheetDescriptor);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporterExporter#exportData(java.lang.String)
     */
    @Override
    public ExternalStylesheetDescriptor exportData(String name) {
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptorByName(name);
        if (stylesheetDescriptor == null) {
            return null;
        }
        
        return convert(stylesheetDescriptor);
    }
    
    @Override
    public String getFileName(ExternalStylesheetDescriptor data) {
        return SafeFilenameUtils.makeSafeFilename(data.getName());
    }

    /**
     * Treats the {@link String} id argument as the stylesheet name.
     * 
     * (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImporter#deleteData(java.lang.String)
     */
    @Override
	public ExternalStylesheetDescriptor deleteData(String name) {
    	final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptorByName(name);
        if (stylesheetDescriptor == null) {
            return null;
        }
        
    	ExternalStylesheetDescriptor result = convert(stylesheetDescriptor);
    	this.stylesheetDescriptorDao.deleteStylesheetDescriptor(stylesheetDescriptor);
    	return result;
	}

	protected void copyProperties(IStylesheetData source, ExternalStylesheetData dest) {
        dest.setName(source.getName());
        dest.setDefaultValue(source.getDefaultValue());
        dest.setDescription(source.getDescription());
        dest.setScope(ExternalStylesheetDataScope.valueOf(source.getScope().name()));
    }

	/**
	 * Convert the {@link IStylesheetDescriptor} to an {@link ExternalStylesheetDescriptor}.
	 * 
	 * @param stylesheetDescriptor 
	 * @return converted object, never null
	 */
	protected ExternalStylesheetDescriptor convert(IStylesheetDescriptor stylesheetDescriptor) {
		final ExternalStylesheetDescriptor externalStylesheetDescriptor = new ExternalStylesheetDescriptor();
        externalStylesheetDescriptor.setVersion("4.0");
        
        externalStylesheetDescriptor.setName(stylesheetDescriptor.getName());
        externalStylesheetDescriptor.setUrlSyntaxHelper(stylesheetDescriptor.getUrlNodeSyntaxHelperName());
        externalStylesheetDescriptor.setDescription(stylesheetDescriptor.getDescription());
        externalStylesheetDescriptor.setUri(stylesheetDescriptor.getStylesheetResource());
        
        final Collection<IOutputPropertyDescriptor> outputPropertyDescriptors = stylesheetDescriptor.getOutputPropertyDescriptors();
        final List<ExternalOutputPropertyDescriptor> extOutputPropertyDescriptors = externalStylesheetDescriptor.getOutputProperties();
        for (final IOutputPropertyDescriptor outputPropertyDescriptor : outputPropertyDescriptors) {
            final ExternalOutputPropertyDescriptor extOutputPropertyDescriptor = new ExternalOutputPropertyDescriptor();
            copyProperties(outputPropertyDescriptor, extOutputPropertyDescriptor);
            extOutputPropertyDescriptors.add(extOutputPropertyDescriptor);
        }
        
        final Collection<IStylesheetParameterDescriptor> stylesheetParameterDescriptors = stylesheetDescriptor.getStylesheetParameterDescriptors();
        final List<ExternalStylesheetParameterDescriptor> extStylesheetParameterDescriptors = externalStylesheetDescriptor.getStylesheetParameters();
        for (final IStylesheetParameterDescriptor stylesheetParameterDescriptor : stylesheetParameterDescriptors) {
            final ExternalStylesheetParameterDescriptor extStylesheetParameterDescriptor = new ExternalStylesheetParameterDescriptor();
            copyProperties(stylesheetParameterDescriptor, extStylesheetParameterDescriptor);
            extStylesheetParameterDescriptors.add(extStylesheetParameterDescriptor);
        }
        
        final Collection<ILayoutAttributeDescriptor> layoutAttributeDescriptors = stylesheetDescriptor.getLayoutAttributeDescriptors();
        final List<ExternalLayoutAttributeDescriptor> extLayoutAttributeDescriptors = externalStylesheetDescriptor.getLayoutAttributes();
        for (final ILayoutAttributeDescriptor layoutAttributeDescriptor : layoutAttributeDescriptors) {
            final ExternalLayoutAttributeDescriptor extLayoutAttributeDescriptor = new ExternalLayoutAttributeDescriptor();
            copyProperties(layoutAttributeDescriptor, extLayoutAttributeDescriptor);
            extLayoutAttributeDescriptor.getTargetElements().addAll(layoutAttributeDescriptor.getTargetElementNames());
            extLayoutAttributeDescriptors.add(extLayoutAttributeDescriptor);
        }
        
        return externalStylesheetDescriptor;
	}
}
