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

package org.jasig.portal.portlet.marketplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletDescriptorKey;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marketplace portlet definitions provide user-specific views adding Marketplace-specific features
 * upon an underlying IPortletDefinition.
 *
 * Since uPortal 4.2, a given MarketplacePortletDefinition is scoped to the user for whom it was
 * instantiated.  Instances of MarketplacePortletDefinition MUST NOT be re-used between users
 * because they will reflect state (namely, related portlets) that may not be appropriate for the
 * subsequently serviced user.
 *
 * @since uPortal 4.1; substantially changed for uPortal 4.2.
 */
public class MarketplacePortletDefinition implements IPortletDefinition{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // This state underlies the Marketplace portlet definition instance.
    private IPortletDefinition portletDefinition;
    private IPerson person;

    // static constants
    public static final String MARKETPLACE_FNAME = "portletmarketplace";
    public static final int QUANTITY_RELATED_PORTLETS_TO_SHOW = 5;
    private static final String INITIAL_RELEASE_DATE_PREFERENCE_NAME="Initial_Release_Date";
    private static final String RELEASE_DATE_PREFERENCE_NAME="Release_Date";
    private static final String RELEASE_NOTE_PREFERENCE_NAME="Release_Notes";
    private static final String RELEASE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String MARKETPLACE_SHORT_LINK_PREF = "short_link";
    private static final DateTimeFormatter releaseDateFormatter = DateTimeFormat.forPattern(RELEASE_DATE_FORMAT);

    // depended-upon injected services
    private final IMarketplaceService marketplaceService;
    private IPortletCategoryRegistry portletCategoryRegistry;

    // state derived from the underlying IPortletDefinition.
    private List<ScreenShot> screenShots;
    private PortletReleaseNotes releaseNotes;
    private Set<PortletCategory> categories;
    private Set<PortletCategory> parentCategories;
    private String shortURL = null;

    // that MarketplacePortletDefinition contains more MarketplacePortletDefinition is okay only
    // so long as related portlets are lazy-initialized on access and are not always accessed.
    private Set<MarketplacePortletDefinition> relatedPortlets;

    /**
     * Constructs a Definition for use by no particular user, so with no related portlets.
     *
     * The resulting MarketplacePortletDefinition will have no
     * related portlets since cannot filter by BROWSE permissions of the unspecified user.
     *
     * @param portletDefinition the portlet definition to make this MarketplacePD
     * @param marketplaceService the Marketplace service
     * @param registry the registry you want to use for categories and related apps
     * Benefit: screenshot property is set if any are available.  This includes URL and caption
     *
     * @since uPortal 4.2
     */
    public MarketplacePortletDefinition(final IPortletDefinition portletDefinition,
        final IMarketplaceService marketplaceService, final IPortletCategoryRegistry registry) {

        Validate.notNull(marketplaceService,
            "MarketplacePortletDefinition requires an IMarketplaceService.");

        this.marketplaceService = marketplaceService;
        this.portletCategoryRegistry = registry;
        this.portletDefinition = portletDefinition;

        this.initDefinitions();
    }

    /**
     * Constructs a Definition for use by a given user.
     *
     * The resulting MarketplacePortletDefinition will only have related portlets BROWSEable by the
     * given user.
     *
     * @param portletDefinition the portlet definition to make this MarketplacePD
     * @param person the user for whom this Marketplace definition is tailored
     * @param marketplaceService the Marketplace service
     * @param registry the registry you want to use for categories and related apps
     *
     * @since uPortal 4.2
     */
    public MarketplacePortletDefinition(
        final IPortletDefinition portletDefinition, final IPerson person,
        final IMarketplaceService marketplaceService, final IPortletCategoryRegistry registry) {

        Validate.notNull(marketplaceService,
            "MarketplacePortletDefinition requires an IMarketplaceService.");

        this.marketplaceService = marketplaceService;
        this.portletCategoryRegistry = registry;
        this.portletDefinition = portletDefinition;

        this.person = person;

        initDefinitions();
    }

    private void initDefinitions(){
        this.initScreenShots();
        this.initPortletReleaseNotes();
        this.initCategories();
        this.initShortURL();

        // deliberately does *not* init related portlets, to avoid infinite recursion of initing related portlets.
        // TODO: use a caching registry so that get some cache hits when referencing MarketplacePortletDefinitions
    }

    private void initShortURL() {
        List<IPortletPreference> portletPreferences = this.portletDefinition.getPortletPreferences();
        for(IPortletPreference pref : portletPreferences) {
            if(MARKETPLACE_SHORT_LINK_PREF.equalsIgnoreCase(pref.getName())
                    && pref.getValues().length == 1) {
                setShortURL(pref.getValues()[0]);
                break;
            }
        }
    }

    private void initScreenShots(){
        List<IPortletPreference> portletPreferences = this.portletDefinition.getPortletPreferences();
        List<IPortletPreference> urls =  new ArrayList<IPortletPreference>(portletPreferences.size());
        List<IPortletPreference> captions = new ArrayList<IPortletPreference>(portletPreferences.size());
        List<ScreenShot> screenShots = new ArrayList<ScreenShot>();

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
        for(int i=0; i<urls.size(); i++){
            if(urls.get(i)!=null){
                if(captions.size()>i && captions.get(i)!=null){
                    screenShots.add(new ScreenShot(urls.get(i).getValues()[0], Arrays.asList(captions.get(i).getValues())));
                }else{
                    screenShots.add(new ScreenShot(urls.get(i).getValues()[0]));
                }
            }
        }
        this.setScreenShots(screenShots);
    }

    private void initPortletReleaseNotes(){
        PortletReleaseNotes temp = new PortletReleaseNotes();
        for(IPortletPreference portletPreference:this.portletDefinition.getPortletPreferences()){
            if(MarketplacePortletDefinition.RELEASE_DATE_PREFERENCE_NAME.equalsIgnoreCase(portletPreference.getName())){
                try {
                    DateTime dt = releaseDateFormatter.parseDateTime(portletPreference.getValues()[0]);
                    temp.setReleaseDate(dt);
                } catch (Exception e){
                    logger.warn("Issue with parsing "+ RELEASE_DATE_PREFERENCE_NAME + ". Should be in format " + RELEASE_DATE_FORMAT, e);
                }
                continue;
            }
            if(MarketplacePortletDefinition.RELEASE_NOTE_PREFERENCE_NAME.equalsIgnoreCase(portletPreference.getName())){
                temp.setReleaseNotes(Arrays.asList(portletPreference.getValues()));
                continue;
            }
            if(MarketplacePortletDefinition.INITIAL_RELEASE_DATE_PREFERENCE_NAME.equalsIgnoreCase(portletPreference.getName())){
                try {
                    DateTime dt = releaseDateFormatter.parseDateTime(portletPreference.getValues()[0]);
                    temp.setInitialReleaseDate(dt);
                } catch (Exception e){
                    logger.warn("Issue with parsing "+ INITIAL_RELEASE_DATE_PREFERENCE_NAME + ". Should be in format " + RELEASE_DATE_FORMAT, e);
                }
                continue;
            }
        }
        this.setPortletReleaseNotes(temp);
    }

    /**
     * private method that sets the parentCategories field and the categories field
     * This will ensure that the public methods {@link #getParentCategories() getParentCategories()}
     * and {@link #getCategories() getCategories()} will not return null.
     * Empty sets are allowed
     */
    private void initCategories(){
        Set<PortletCategory> allCategories = new HashSet<PortletCategory>();
        this.setParentCategories(this.portletCategoryRegistry.getParentCategories(this));
        for(PortletCategory childCategory:this.parentCategories){
            allCategories.add(childCategory);
            allCategories.addAll(this.portletCategoryRegistry.getAllParentCategories(childCategory));
        }
        this.setCategories(allCategories);
    }

    /**
     * Initialize related portlets.
     * This must be called lazily so that MarketplacePortletDefinitions instantiated as related
     * portlets off of a MarketplacePortletDefinition do not always instantiate their related
     * MarketplacePortletDefinitions, ad infinitem.
     */
    private Set<MarketplacePortletDefinition> computeRelatedPortlets(){

        if (person == null) {
            return new HashSet<>();
        }

        return Sets.intersection(
            marketplaceService.marketplacePortletDefinitionsRelatedTo(this),
            marketplaceService.marketplacePortletDefinitionsBrowseableBy(person));
    }

    private void setScreenShots(List<ScreenShot> screenShots){
        this.screenShots = screenShots;
    }

    /**
     * @return the screenShotList. Will not be null.
     */
    public List<ScreenShot> getScreenShots() {
        if(screenShots==null){
            this.initScreenShots();
        }
        return screenShots;
    }

    private void setPortletReleaseNotes(PortletReleaseNotes portletReleaseNotes){
        this.releaseNotes = portletReleaseNotes;
    }

    /**
     * @return the releaseNotes. Will not be null.
     */
    public PortletReleaseNotes getPortletReleaseNotes() {
        if(releaseNotes==null){
            this.initPortletReleaseNotes();
        }
        return releaseNotes;
    }

    private void setCategories(Set<PortletCategory> categories){
        this.categories = categories;
    }

    /**
     * 
     * @return a set of categories that includes all the categories that this definition is in
     * and all parent categories of the portlet's category.  Will not return null.  Might return
     * empty set.
     */
    public Set<PortletCategory> getCategories() {
        if(this.categories == null){
            this.initCategories();
        }
        return this.categories;
    }

    private void setParentCategories(Set<PortletCategory> parentCategories) {
        this.parentCategories = parentCategories;
    }
    
    /**
     * @return a set of categories that this portlet directly belongs too.  Will
     * not traverse up the category tree and return grandparent categories and
     * farther.  Will not return null.  Might return empty set.
     */
    public Set<PortletCategory> getParentCategories() {
        if(this.parentCategories == null){
            this.initCategories();
        }
        return this.parentCategories;
    }

    /**
     * Gets a Set of BROWSEable definitions representing portlets related to this portlet.
     *
     * Related currently means in the categories of this portlet, traversing those categories
     * deeply into child categories.
     *
     * If this MarketplacePortletDefinition was constructed without specifying a user, then there
     * are no related portlets.
     *
     * @return a set of related MarketplacePortletDefinition s.
     * Will not return null. Will not include self in set.  Might return an empty set.
     */
    public Set<MarketplacePortletDefinition> getRelatedPortlets() {
        // lazy init is essential to avoid infinite recursion in graphing related portlets.
        if(this.relatedPortlets==null){
            this.relatedPortlets = computeRelatedPortlets();
        }
        return this.relatedPortlets;
    }

    /**
     * Returns a random set of up to QUANTITY_RELATED_PORTLETS_TO_SHOW related portlets that are
     * BROWSEable by the user for whom this MarketplacePortletDefinition was instantiated.
     *
     * If this MarketplacePortletDefinition was not instantiated for any particular user, then
     * returns empty Set since cannot filter to BROWSEable related portlets.
     *
     * (This method also returns different data randomly on each invocation.)
     *
     * Currently, related means contained in a category that this portlet is contained in, with
     * deep traversal of sub-categories.
     *
     * On failure returns an empty Set rather than propagating an Exception.
     *
     * @return a non-null potentially empty Set of related BROWSEable portlets
     */
    public Set<MarketplacePortletDefinition> getRandomSamplingRelatedPortlets(){

        if (this.person == null) {
            // MarketplacePortletDefinition instances instantiated for no particular person
            // will have no sampling of related portlets because without a person cannot filter
            // by BROWSE permission
            return new HashSet<>();
        }

        try {

            // lazy init is essential to avoid infinite recursion in graphing related portlets.
            if (this.relatedPortlets==null) {
               this.relatedPortlets = computeRelatedPortlets();
            }
            if (this.relatedPortlets.isEmpty()) {
                return this.relatedPortlets;
            }

            final Set<MarketplacePortletDefinition> browseableMarketplacePortletDefinitions =
                marketplaceService.marketplacePortletDefinitionsBrowseableBy(person);


            final Set<MarketplacePortletDefinition> browseableRelatedPortlets =
                Sets.intersection (browseableMarketplacePortletDefinitions, this.relatedPortlets);

            List<MarketplacePortletDefinition> tempList = new ArrayList<>(browseableRelatedPortlets);
            Collections.shuffle(tempList);

            final int count = Math.min(QUANTITY_RELATED_PORTLETS_TO_SHOW, tempList.size());
            return new HashSet<>(tempList.subList(0, count));

        } catch (Exception e) {
            logger.error("Problem computing random sampling of BROWSEable related portlets.", e);
            // Behaving as if no related portlets is preferable to propagating failure.
            return new HashSet<>();
        }
    }

    /**
     * Get the person for whom this Definition is tailored, or null if none.
     * @return potentially null IPerson for whom this Definition is tailored
     */
    public IPerson getPerson() {
        return person;
    }

    /**
     * Convenience method for getting a bookmarkable URL for rendering the defined portlet,
     *
     * Normally this is the portal rendering of the portlet, addressed by fname, and in that case
     * this method will *ONLY* work when invoked in the context of a Spring-managed
     * HttpServletRequest available via RequestContextHolder.
     *
     * When there is an alternativeMaximizedLink, this method returns that instead.
     *
     * WARNING: This method does not consider whether the requesting user has permission to render
     * the target portlet, so this might be getting a URL that the user can't actually use.
     *
     * @return URL for rendering the portlet
     * @throws IllegalStateException when context does not allow computing the URL
     */
    public String getRenderUrl() {

        final String alternativeMaximizedUrl = getAlternativeMaximizedLink();
        if (null != alternativeMaximizedUrl) {
            return alternativeMaximizedUrl;
        }

        final String contextPath = PortalWebUtils.currentRequestContextPath();

        // TODO: stop abstraction violation of relying on knowledge of uPortal URL implementation details
        return contextPath + "/p/" + getFName() + "/render.uP";

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
		return this.portletDefinition.getTimeout();
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
		return this.portletDefinition.getRenderTimeout();
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
    public String getAlternativeMaximizedLink() {
        return this.portletDefinition.getAlternativeMaximizedLink();
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


	@Override
	public Double getRating() {
		return this.portletDefinition.getRating();
	}


	@Override
	public void setRating(Double rating) {
		this.portletDefinition.setRating(rating);
	}


	@Override
	public Long getUsersRated() {
		return this.portletDefinition.getUsersRated();
	}


	@Override
	public void setUsersRated(Long usersRated) {
		this.portletDefinition.setUsersRated(usersRated);
	}

    public String getShortURL() {
        return shortURL;
    }

    public void setShortURL(String shortURL) {
        this.shortURL = shortURL;
    }
    
    public IPortletDefinitionParameter getPortletDefinitionParameter(String parameterKey) {
        return this.portletDefinition.getParameter(parameterKey);
    }

    @Override
    public String getTarget() {
      return this.portletDefinition.getTarget();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("fname", getFName())
            .toString();
    }

    /*
     * Marketplace portlet definitions are definitively identified by the fname of their underlying
     * portlet publication and the user for whom they were instantiated, so the fname and the
     * person contribute to the hash code.
     * @since uPortal 4.2
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getFName())
            .append(getPerson())
            .toHashCode();
    }

    /*
     * Equal where the other object is a MarketplacePortletDefinition with the same fname AND
     * is tailored to the same user if any.
     * This is important so that Set operations work properly.
     * @since uPortal 4.2
     */
    @Override
    public boolean equals(Object other) {
        if (null == other) { return false; }
        if (this == other) { return true; }
        if (getClass() != other.getClass()) {
            return false;
        }
        final MarketplacePortletDefinition otherDefinition = (MarketplacePortletDefinition) other;
        return new EqualsBuilder()
            .append(getFName(), otherDefinition.getFName())
            .append(getPerson(), otherDefinition.getPerson())
            .isEquals();
    }


}
