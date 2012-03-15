package org.jasig.portal.rendering.xslt;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;

/**
 * Maps a user attribute to a skin. The user's attribute named by {@link #setSkinAttributeName(String)} is used to
 * look up a skin name via the {@link #setAttributeToSkinMap(Map)} map and the skin name is set to in the transformer
 * using the {@link #setSkinParameterName(String)} parameter.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class SkinMappingTransformerConfigurationSource extends TransformerConfigurationSourceAdapter implements BeanNameAware {
    private String skinNameAttribute;
    private ResourcesElementsProvider resourcesElementsProvider;
    private String skinParameterName = "skin";
    private boolean cacheSkinResolution = true;

    /**
     * The name of the transformer parameter used for the skin name, defaults to "skin"
     */
    public void setSkinParameterName(String skinParameterName) {
        this.skinParameterName = skinParameterName;
    }
    
    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    /**
     * If true the result of {@link #getSkinName(HttpServletRequest)} will be cached in the user's session and
     * re-used. If false {@link #getSkinName(HttpServletRequest)} will be called on every execution.
     */
    public void setCacheSkinResolution(boolean cacheSkinResolution) {
        this.cacheSkinResolution = cacheSkinResolution;
    }
    

    @Override
    public void setBeanName(String name) {
        this.skinNameAttribute = name + ".SKIN_NAME";
    }

    @Override
    public final Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final String skinName = this.getSkinNameInternal(request);
        if (skinName == null) {
            return Collections.emptyMap();
        }
        
        return ImmutableMap.<String, Object>of(this.skinParameterName, skinName);
    }

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final String skinName = this.getSkinNameInternal(request);
        return new CacheKey(this.getClass().getName(), skinName);
    }
    
    private String getSkinNameInternal(HttpServletRequest request) {
        if (!this.cacheSkinResolution || Included.PLAIN == this.resourcesElementsProvider.getDefaultIncludedType()) {
            return this.getSkinName(request);
        }
        
        final HttpSession session = request.getSession();
        SkinNameHolder skinNameHolder = (SkinNameHolder)session.getAttribute(skinNameAttribute);
        if (skinNameHolder == null) {
            final String skinName = this.getSkinName(request);
            skinNameHolder = new SkinNameHolder(skinName);
            session.setAttribute(skinNameAttribute, skinNameHolder);
        }
        
        return skinNameHolder.skinName;
    }
    
    /**
     * @return The skin name to use for this request
     */
    protected abstract String getSkinName(HttpServletRequest request);
    
    private static final class SkinNameHolder implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String skinName;

        public SkinNameHolder(String skinName) {
            this.skinName = skinName;
        }
    }
}
