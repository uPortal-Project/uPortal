package org.jasig.portal.rendering.predicates;

import com.google.common.base.Predicate;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.url.UrlState;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * Answers whether a given HttpServletRequest represents one that the rendering pipeline will focus on rendering just
 * one portlet (e.g., maximized, or exclusive).
 * @since uPortal 4.2
 */
public class FocusedOnOnePortletPredicate
    implements Predicate<HttpServletRequest> {

    // auto-wired.
    private IUrlSyntaxProvider urlSyntaxProvider;

    @Override
    public boolean apply(final HttpServletRequest request) {

        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);

        // true when there is a portal request and that request is not for a portal in NORMAL state
        // i.e. portal is in some other state, like Maximized or Exclusive or Detached.
        // false otherwise. False when portalRequestInfo is null because unknown portal state is not
        // focused-on-one-portlet portal state.
        return (null != portalRequestInfo &&
                ! (UrlState.NORMAL.equals(portalRequestInfo.getUrlState())));
    }

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
}
