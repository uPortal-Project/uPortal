package org.jasig.portal;

import javax.servlet.http.*;

public class PortalControlStructures {
    protected UserLayoutManager ulm;
    protected HttpServletRequest req;
    protected HttpServletResponse res;
    protected ChannelManager cm;

    public UserLayoutManager getUserLayoutManager() { return ulm; }
    public HttpServletRequest getHttpServletRequest() { return req;}
    public HttpServletResponse getHttpServletResponse() { return res; }
    public ChannelManager getChannelManager() {return cm; }

    public void setUserLayoutManager(UserLayoutManager lm) { ulm=lm; }
    public void setHttpServletRequest(HttpServletRequest r) { req=r; }
    public void setHttpServletResponse(HttpServletResponse r) { res=r; }
    public void setChannelManager(ChannelManager m) { cm=m; }
}
