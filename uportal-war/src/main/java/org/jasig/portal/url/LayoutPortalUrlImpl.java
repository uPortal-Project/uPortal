/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implementation of a portal URL
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class LayoutPortalUrlImpl extends AbstractPortalUrl implements ILayoutPortalUrl {
    private final String targetFolderId;
    protected final Map<String, List<String>> layoutParameters = new LinkedHashMap<String, List<String>>();
    private boolean action = false;
    
    public LayoutPortalUrlImpl(HttpServletRequest request, IUrlGenerator urlGenerator, String targetFolderId) {
        super(request, urlGenerator);
        Validate.notNull(targetFolderId, "targetFolderId can not be null");
        
        this.targetFolderId = targetFolderId;
    }
    
    public String getTargetFolderId() {
        return this.targetFolderId;
    }

    public Map<String, List<String>> getLayoutParameters() {
        return this.layoutParameters;
    }

    public final void addLayoutParameter(String name, String... values) {
        Validate.notNull(name, "name can not be null");
        Validate.noNullElements(values, "values can not be null or contain null elements");
        
        List<String> valuesList = this.layoutParameters.get(name);
        if (valuesList == null) {
            valuesList = new ArrayList<String>(values.length);
        }
        
        for (final String value : values) {
            valuesList.add(value);
        }
        
        this.layoutParameters.put(name, valuesList);
    }

    public final void setLayoutParameter(String name, String... values) {
        Validate.notNull(name, "name can not be null");
        Validate.noNullElements(values, "values can not be null or contain null elements");
        
        final List<String> valuesList = new ArrayList<String>(values.length);
        for (final String value : values) {
            valuesList.add(value);
        }
        
        this.layoutParameters.put(name, valuesList);
    }

    public final void setLayoutParameters(Map<String, List<String>> parameters) {
        this.layoutParameters.clear();
        this.layoutParameters.putAll(parameters);
    }
    
    public boolean isAction() {
        return this.action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }

    public String getUrlString() {
        return this.urlGenerator.generateLayoutUrl(this.request, this);
    }

    @Override
    public String toString() {
        return "LayoutPortalUrl [targetFolderId=" + this.targetFolderId + ", action=" + this.action
                + ", layoutParameters=" + this.layoutParameters + ", portalParameters=" + this.portalParameters + "]";
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-942605321, 2130461357)
            .appendSuper(super.hashCode())
            .append(this.targetFolderId)
            .append(this.layoutParameters)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof LayoutPortalUrlImpl)) {
            return false;
        }
        LayoutPortalUrlImpl rhs = (LayoutPortalUrlImpl) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.targetFolderId, rhs.targetFolderId)
            .append(this.layoutParameters, rhs.layoutParameters)
            .isEquals();
    }
}
