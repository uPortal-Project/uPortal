package org.jasig.portal.portlets.translator;

import java.io.Serializable;

import org.jasig.portal.portlet.om.IPortletDefinition;

/**
 * This domain class is used in order to represent the localization of portlet definition. This
 * class is the same as o.j.p.portlet.dao.jpa.PortletLocalizationData but since JPA class should be
 * package private, another implementation is used.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
class LocalizedPortletDefinition implements Serializable {
    
    private static final long serialVersionUID = 7680610266419980922L;
    
    private String name;
    
    private String title;
    
    private String description;
    
    /**
     * Default constructor.
     */
    public LocalizedPortletDefinition() {
    }
    
    /**
     * Initialize the state using default translation of portlet definition.
     * 
     * @param definition portlet definition to use.
     */
    public LocalizedPortletDefinition(IPortletDefinition definition) {
        this(definition, null);
    }
    
    /**
     * Initialize the state using portlet definition. Class fields will be retrieved from portlet
     * definition using specified locale.
     * 
     * @param definition portelt definition to use (must not be null).
     * @param locale specifies the localization to use.
     */
    public LocalizedPortletDefinition(IPortletDefinition definition, String locale) {
        assert definition != null;
        
        if (locale != null) {
            this.name = definition.getName(locale);
            this.title = definition.getTitle(locale);
            this.description = definition.getDescription(locale);
        } else {
            this.name = definition.getName(locale);
            this.title = definition.getTitle(locale);
            this.description = definition.getDescription(locale);
        }
    }
    
    /**
     * Get the name for this locale.
     * 
     * @return localized name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name for this locale.
     * 
     * @param name localized name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the title for this locale.
     * 
     * @return localized title.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the title for this locale.
     * 
     * @param title localized title.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Get the description for this locale.
     * 
     * @return localized description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description for this locale.
     * 
     * @param description localized description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
