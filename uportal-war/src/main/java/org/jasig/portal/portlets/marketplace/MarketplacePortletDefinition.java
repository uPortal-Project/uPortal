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

package org.jasig.portal.portlets.marketplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletDescriptorKey;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketplacePortletDefinition implements IPortletDefinition{
	
	private IPortletDefinition portletDefinition;
	private List<ScreenShot> screenShots;
	public static final String MARKETPLACE_FNAME = "portletmarketplace";
	private static final String RELEASE_DATE_PREFERENCE_NAME="Release_Date";
	private static final String RELEASE_NOTE_PREFERENCE_NAME="Release_Notes";
	private static final String RELEASE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final DateTimeFormatter releaseDateFormatter = DateTimeFormat.forPattern(RELEASE_DATE_FORMAT);
	private PortletReleaseNotes releaseNotes;
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 
	 * @param portletDefinition the portlet definition to make this MarketplacePD
	 * Benefit: screenshot property is set if any are available.  This includes URL and caption
	 */
	public MarketplacePortletDefinition(IPortletDefinition portletDefinition){
		this.portletDefinition = portletDefinition;
		this.setUpDefinitions(portletDefinition);
	}
	
	private void setUpDefinitions(IPortletDefinition portletDefinition){
		this.setScreenShots(portletDefinition);
		this.setPortletReleaseNotes(portletDefinition);
	}
	
	private void setPortletReleaseNotes(IPortletDefinition portlet){
		if(releaseNotes == null) 
			releaseNotes = new PortletReleaseNotes();
		for(IPortletPreference portletPreference: portlet.getPortletPreferences()){
			if(MarketplacePortletDefinition.RELEASE_DATE_PREFERENCE_NAME.equalsIgnoreCase(portletPreference.getName())){
				try { 
					DateTime dt = releaseDateFormatter.parseDateTime(portletPreference.getValues()[0]);
					releaseNotes.setReleaseDate(dt);
				} catch (Exception e){
					logger.warn("Issue with parsing "+ RELEASE_DATE_PREFERENCE_NAME + ". Should be in format " + RELEASE_DATE_FORMAT, e);
				}
				continue;
			}
			if(MarketplacePortletDefinition.RELEASE_NOTE_PREFERENCE_NAME.equalsIgnoreCase(portletPreference.getName())){
				releaseNotes.setReleaseNotes(Arrays.asList(portletPreference.getValues()));
				continue;
			}
		}
	}
	
	private void setScreenShots(IPortletDefinition portlet){
		List<IPortletPreference> portletPreferences = portlet.getPortletPreferences();
		List<IPortletPreference> urls =  new ArrayList<IPortletPreference>(portletPreferences.size());
		List<IPortletPreference> captions = new ArrayList<IPortletPreference>(portletPreferences.size());
		List<ScreenShot> screenshots = new ArrayList<ScreenShot>();
		
		//Creates a list of captions and list of urls
		for(int i=0; i<portletPreferences.size(); i++){
			//Most screenshots possible is i
			urls.add(null);
			captions.add(null);
			for(int j=0; j<portletPreferences.size(); j++){
				if(portletPreferences.get(j).getName().equalsIgnoreCase("screen_shot"+Integer.toString(i+1))){
					urls.set(i, portletPreferences.get(j));
				}
				if(portletPreferences.get(j).getName().equalsIgnoreCase("screen_shot"+Integer.toString(i+1)+"_caption")){
					captions.set(i, portletPreferences.get(j));
				}
			}			
		}
		
		//
		for(int i=0; i<urls.size(); i++){
			if(urls.get(i)!=null){
				if(captions.size()>i && captions.get(i)!=null){
					screenshots.add(new ScreenShot(urls.get(i).getValues()[0], Arrays.asList(captions.get(i).getValues())));
				}else{
					screenshots.add(new ScreenShot(urls.get(i).getValues()[0]));
				}
			}
		}
		this.screenShots = screenshots;
	}
	
	/**
	 * @return the screenShotList
	 */
	public List<ScreenShot> getScreenShots() {
		return screenShots;
	}
	
	/**
	 * @return the releaseNotes
	 */
	public PortletReleaseNotes getPortletReleaseNotes() {
		return releaseNotes;
	}

	@Override
	public String getDataId() {
		return this.portletDefinition.getDataId();
	}

	@Override
	public String getDataTitle() {
		return this.portletDefinition.getDataTitle();
	}

	@Override
	public String getDataDescription(){
		return this.portletDefinition.getDataDescription();
	}

	@Override
	public IPortletDefinitionId getPortletDefinitionId() {
		return this.portletDefinition.getPortletDefinitionId();
	}

	@Override
	public List<IPortletPreference> getPortletPreferences() {
		return this.portletDefinition.getPortletPreferences();
	}

	@Override
	public boolean setPortletPreferences(List<IPortletPreference> portletPreferences) {
			return this.portletDefinition.setPortletPreferences(portletPreferences);
	}

	@Override
	public PortletLifecycleState getLifecycleState() {
		return this.portletDefinition.getLifecycleState();
	}

	@Override
	public String getFName() {
		return this.portletDefinition.getFName();
	}

	@Override
	public String getName() {
		return this.portletDefinition.getName();
	}

	@Override
	public String getDescription() {
		return this.portletDefinition.getDescription();
	}

	@Override
	public IPortletDescriptorKey getPortletDescriptorKey() {
		return this.portletDefinition.getPortletDescriptorKey();
	}

	@Override
	public String getTitle() {
		return this.portletDefinition.getTitle();
	}

	@Override
	public int getTimeout() {
		return this.getTimeout();
	}

	@Override
	public Integer getActionTimeout() {
		return this.portletDefinition.getActionTimeout();
	}

	@Override
	public Integer getEventTimeout() {
		return this.portletDefinition.getEventTimeout();
	}

	@Override
	public Integer getRenderTimeout() {
		return this.getRenderTimeout();
	}

	@Override
	public Integer getResourceTimeout() {
		return this.portletDefinition.getResourceTimeout();
	}

	@Override
	public IPortletType getType() {
		return this.portletDefinition.getType();
	}

	@Override
	public int getPublisherId() {
		return this.portletDefinition.getPublisherId();
	}

	@Override
	public int getApproverId() {
		return this.portletDefinition.getApproverId();
	}

	@Override
	public Date getPublishDate() {
		return this.portletDefinition.getPublishDate();
	}

	@Override
	public Date getApprovalDate() {
		return this.portletDefinition.getApprovalDate();
	}

	@Override
	public int getExpirerId() {
		return this.portletDefinition.getExpirerId();
	}

	@Override
	public Date getExpirationDate() {
		return this.portletDefinition.getExpirationDate();
	}

	@Override
	public Set<IPortletDefinitionParameter> getParameters() {
		return this.portletDefinition.getParameters();
	}

	@Override
	public IPortletDefinitionParameter getParameter(String key) {
		return this.portletDefinition.getParameter(key);
	}

	@Override
	public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap() {
		return this.portletDefinition.getParametersAsUnmodifiableMap();
	}

	@Override
	public String getName(String locale) {
		return this.portletDefinition.getName();
	}

	@Override
	public String getDescription(String locale) {
		return this.portletDefinition.getDescription();
	}

	@Override
	public String getTitle(String locale) {
		return this.portletDefinition.getTitle(locale);
	}

	@Override
	public void setFName(String fname){
		this.portletDefinition.setFName(fname);
	}

	@Override
	public void setName(String name) {
		this.portletDefinition.setName(name);
		
	}

	@Override
	public void setDescription(String descr) {
		this.portletDefinition.setDescription(descr);
	}

	@Override
	public void setTitle(String title) {
		this.portletDefinition.setTitle(title);
	}

	@Override
	public void setTimeout(int timeout) {
		this.portletDefinition.setTimeout(timeout);
	}

	@Override
	public void setActionTimeout(Integer actionTimeout) {
		this.portletDefinition.setActionTimeout(actionTimeout);
	}

	@Override
	public void setEventTimeout(Integer eventTimeout) {
		this.portletDefinition.setEventTimeout(eventTimeout);
	}

	@Override
	public void setRenderTimeout(Integer renderTimeout) {
		this.portletDefinition.setRenderTimeout(renderTimeout);
	}

	@Override
	public void setResourceTimeout(Integer resourceTimeout) {
		this.portletDefinition.setResourceTimeout(resourceTimeout);
	}

	@Override
	public void setType(IPortletType channelType) {
		this.portletDefinition.setType(channelType);
	}

	@Override
	public void setPublisherId(int publisherId) {
		this.portletDefinition.setPublisherId(publisherId);
	}

	@Override
	public void setApproverId(int approvalId) {
		this.portletDefinition.setApproverId(approvalId);
	}

	@Override
	public void setPublishDate(Date publishDate) {
		this.portletDefinition.setPublishDate(publishDate);
	}

	@Override
	public void setApprovalDate(Date approvalDate) {
		this.portletDefinition.setApprovalDate(approvalDate);
	}

	@Override
	public void setExpirerId(int expirerId) {
		this.portletDefinition.setExpirerId(expirerId);
	}

	@Override
	public void setExpirationDate(Date expirationDate) {
		this.portletDefinition.setExpirationDate(expirationDate);
	}

	@Override
	public void setParameters(Set<IPortletDefinitionParameter> parameters) {
		this.portletDefinition.setParameters(parameters);
	}

	@Override
	public void addLocalizedTitle(String locale, String chanTitle) {
		this.portletDefinition.addLocalizedTitle(locale, chanTitle);
	}

	@Override
	public void addLocalizedName(String locale, String chanName) {
		this.portletDefinition.addLocalizedName(locale, chanName);
	}

	@Override
	public void addLocalizedDescription(String locale, String chanDesc) {
		this.portletDefinition.addLocalizedDescription(locale, chanDesc);
	}

	@Override
	public EntityIdentifier getEntityIdentifier() {
		return this.portletDefinition.getEntityIdentifier();
	}

	@Override
	public void addParameter(IPortletDefinitionParameter parameter) {
		this.portletDefinition.addParameter(parameter);
	}

	@Override
	public void addParameter(String name, String value) {
		this.portletDefinition.addParameter(name, value);
	}

	@Override
	public void removeParameter(IPortletDefinitionParameter parameter) {
		this.portletDefinition.removeParameter(parameter);
	}

	@Override
	public void removeParameter(String name) {
		this.portletDefinition.removeParameter(name);
	}
}
