package org.jasig.portal.portlet.container.cache;

import java.util.concurrent.TimeUnit;

import javax.portlet.CacheControl;

import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;

/**
 * CacheControl where calls to {@link #setExpirationTime(int)}, {@link #setPublicScope(boolean)}, and
 * {@link #setETag(String)} result setting headers Last-Modified, CacheControl and ETag
 * 
 * @author Eric Dalquist
 */
public class HeaderSettingCacheControl implements CacheControl {
    private final CacheControl cacheControl;
    private final PortletResourceOutputHandler portletResourceOutputHandler;
    private final long lastModified = System.currentTimeMillis();
    
    public HeaderSettingCacheControl(CacheControl cacheControl,
            PortletResourceOutputHandler portletResourceOutputHandler) {
        this.cacheControl = cacheControl;
        this.portletResourceOutputHandler = portletResourceOutputHandler;
    }
    
    private void setCacheHeaders(int time, boolean publicScope) {
        this.portletResourceOutputHandler.setDateHeader("Last-Modified", lastModified);
        
        if (publicScope) {
            this.portletResourceOutputHandler.setHeader("CacheControl", "public");
        }
        else {
            this.portletResourceOutputHandler.setHeader("CacheControl", "private");
        }
        
        if (time > 0) {
            this.portletResourceOutputHandler.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(time));
            this.portletResourceOutputHandler.addHeader("CacheControl", "max-age=" + time);
        }
    }

    @Override
    public int getExpirationTime() {
        return this.cacheControl.getExpirationTime();
    }

    @Override
    public void setExpirationTime(int time) {
        this.cacheControl.setExpirationTime(time);
        this.setCacheHeaders(time, this.isPublicScope());
    }

    @Override
    public boolean isPublicScope() {
        return this.cacheControl.isPublicScope();
    }

    @Override
    public void setPublicScope(boolean publicScope) {
        this.cacheControl.setPublicScope(publicScope);
        this.setCacheHeaders(this.getExpirationTime(), publicScope);
    }

    @Override
    public String getETag() {
        return this.cacheControl.getETag();
    }

    @Override
    public void setETag(String token) {
        this.cacheControl.setETag(token);
        this.portletResourceOutputHandler.setHeader("ETag", token);
    }

    @Override
    public boolean useCachedContent() {
        return this.cacheControl.useCachedContent();
    }

    @Override
    public void setUseCachedContent(boolean useCachedContent) {
        this.cacheControl.setUseCachedContent(useCachedContent);
    }

}
