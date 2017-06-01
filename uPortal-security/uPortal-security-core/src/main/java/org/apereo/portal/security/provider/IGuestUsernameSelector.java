package org.apereo.portal.security.provider;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface supports pluggable strategies for multiple guest user accounts. Zero instances of
 * this interface are required, in which case the behavior is just like uPortal 4 (one guest user
 * account). Instances are sorted in natural order.
 *
 * @since 5.0
 */
public interface IGuestUsernameSelector extends Comparable<IGuestUsernameSelector> {

    String selectGuestUsername(HttpServletRequest req);
}
