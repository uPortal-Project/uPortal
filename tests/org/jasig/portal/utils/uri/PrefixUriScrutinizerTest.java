/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.utils.uri;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

/**
 * Testcase for PrefixUriScrutinizer.  Tests argument checking and normalization
 * and the scrutinization logic.
 * @since uPortal 2.5.1
 */
public class PrefixUriScrutinizerTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNullAllowPrefixArray() {
        try {
            new PrefixUriScrutinizer(null, new String[0]);
        } catch (IllegalArgumentException iae) {
            // good, threw expected exception.
            return;
        }
        fail("Should have thrown IllegalArgumentException preventing " +
        		"construction with null allow prefix list.");
    }
    
    public void testNullInAllowPrefixArray() {
        try {
            String[] nullContainingPrefixes = {"http://", null, "htps://"};
            new PrefixUriScrutinizer(nullContainingPrefixes, new String[0]);
        } catch (IllegalArgumentException iae) {
            // good, threw expected exception.
            return;
        }
        fail("Should have thrown IllegalArgumentException preventing " +
        		"construction with null in allow prefix array.");
    }
    
    public void testNullDenyPrefixesArray() {
        try {
            new PrefixUriScrutinizer(new String[0], null);
        } catch (IllegalArgumentException iae) {
            // good, threw expected exception.
            return;
        }
        fail("Should have thrown IllegalArgumentException preventing " +
        		"construction with null allow prefix list.");
    }
    
    public void testNullInDenyPrefixArray() {
        try {
            String[] nullContainingPrefixes = {"http://", null, "htps://"};
            new PrefixUriScrutinizer(new String[0], nullContainingPrefixes);
        } catch (IllegalArgumentException iae) {
            // good, threw expected exception.
            return;
        }
        fail("Should have thrown IllegalArgumentException preventing " +
        		"construction with null in deny prefix array.");
    }
    
    public void testNullUri() {
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(new String[0], new String[0]);
        
        try {
            testMe.scrutinize(null);
        } catch (IllegalArgumentException iae) {
            // good, threw expected exception
            return;
        }
        fail ("Should have thrown IllegalArgumentException.");
    }
    
    public void testAllowedHttpUri() {
        String[] allowedPrefixes = {"http://", "https://" };
        
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(allowedPrefixes, new String[0]);
        
        URI httpUri = null;
        try {
            httpUri = new URI("http://www.ja-sig.org");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        // testcase will fail if scrutinize throws
        testMe.scrutinize(httpUri);
        
        URI httpsUri = null;
        try {
            httpsUri = new URI("https://secure.its.yale.edu");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        // testcase will fail if scrutinize throws
        testMe.scrutinize(httpsUri);
        
    }
    
    public void testAllowIgnoresCase() {
        String[] allowedPrefixes = {"HTTP://", "HtTPs://" };
        
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(allowedPrefixes, new String[0]);
        
        URI httpUri = null;
        try {
            httpUri = new URI("http://www.ja-sig.org");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        // testcase will fail if scrutinize throws
        testMe.scrutinize(httpUri);
        
        URI httpsUri = null;
        try {
            httpsUri = new URI("HttpS://secure.its.yale.edu");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        // testcase will fail if scrutinize throws
        testMe.scrutinize(httpsUri);
        
    }
    
    public void testNotAllowedUri() {
        String[] allowedPrefixes = {"http://", "https://" };
        
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(allowedPrefixes, new String[0]);
        
        URI httpUri = null;
        try {
            httpUri = new URI("file:/etc/.passwd");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        try {
            testMe.scrutinize(httpUri);
        } catch(BlockedUriException bue) {
            // good, blocked URI not bearing an allowed prefix
            return;
        }
        fail("Scrutinize should have blocked URI failing to bear allowed prefix.");
        
    }
    
    public void testEplicitlyBlockedUri() {
        String[] allowedPrefixes = {"http://", "https://" };
        
        String[] blockedPrefixes = {"https://secure.its.yale.edu"};
        
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(allowedPrefixes, blockedPrefixes);
        
        URI httpUri = null;
        try {
            httpUri = new URI("https://secure.its.yale.edu/cas/");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        try {
            testMe.scrutinize(httpUri);
        } catch(BlockedUriException bue) {
            // good, blocked URI matching an allowed prefix but also matching
            // a blocked prefix
            return;
        }
        fail("Scrutinize should have blocked URI bearing a blocked prefix.");
        
    }
    
    public void testBlockingIgnoresCase() {
        String[] allowedPrefixes = {"http://", "https://" };
        
        String[] blockedPrefixes = {"HTTPS://secure.its.yale.edu"};
        
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(allowedPrefixes, blockedPrefixes);
        
        URI httpUri = null;
        try {
            httpUri = new URI("https://secure.its.yale.edu/cas/");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        try {
            testMe.scrutinize(httpUri);
        } catch(BlockedUriException bue) {
            // good, blocked URI matching an allowed prefix but also matching
            // a blocked prefix
            return;
        }
        fail("Scrutinize should have blocked URI bearing a blocked prefix.");
        
    }
    
    public void testNormalizedMatchingAllow() {
        String[] allowedPrefixes = {"http://", "https://", "file:/portal/" };
        
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(allowedPrefixes, new String[0]);
        
        URI httpUri = null;
        try {
            httpUri = new URI("file:/portal/../etc/shadow");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        try {
            testMe.scrutinize(httpUri);
        } catch(BlockedUriException bue) {
            // good, blocked URI which when normalized does not bear an
            // allowed prefix
            return;
        }
        fail("Scrutinize should have blocked URI failing to bear allowed prefix.");

    }
    
    public void testNormalizeMatchingDeny() {
        String[] allowedPrefixes = {"http://", "https://" };
        
        String[] blockedPrefixes = {"http://www.uportal.org/private/"};
        
        PrefixUriScrutinizer testMe = 
            new PrefixUriScrutinizer(allowedPrefixes, blockedPrefixes);
        
        URI httpUri = null;
        try {
            httpUri = new URI("http://www.uportal.org/public/../private/secret.html");
        } catch (URISyntaxException e) {
            fail("testcase broken" + e);
        }
        
        try {
            testMe.scrutinize(httpUri);
        } catch(BlockedUriException bue) {
            // good, blocked URI matching an allowed prefix but also matching
            // a blocked prefix after normalization
            return;
        }
        fail("Scrutinize should have blocked URI bearing a blocked prefix.");
        
    }
    
}
