package org.jasig.portal.utils.uri;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

public class BlockedUriExceptionTest extends TestCase {

    /**
     * Test one of the chaining constructors of BlockedUriException, 
     * demonstrating that it exposes the blocked URI and that it captures the
     * provided cause.
     * @throws URISyntaxException - if the testcase itself fails for bad URI syntax in setup.
     */
    public void testChaining() throws URISyntaxException {
        Throwable cause = new Throwable();
        
        URI blockedUri = new URI("http://some.blocked.uri.com/");
        
        String reason = "Some good reason for blocking the URI.";
        
        BlockedUriException bue = new BlockedUriException(blockedUri, reason, cause);
        
        assertSame(cause, bue.getCause());
        assertEquals(blockedUri, bue.getBlockedUri());
        assertEquals(reason, bue.getReasonBlocked());
        
    }
    
}
