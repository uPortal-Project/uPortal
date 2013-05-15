package org.jasig.portal.url;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * @Author James Wennmacher, jwennmacher@unicon.net
 */
public class CasLoginRefUrlEncoderTest {

    CasLoginRefUrlEncoder encoder;
    MockHttpServletRequest request;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        encoder = new CasLoginRefUrlEncoder();
        encoder.setCasLoginUrl("https://cas:80/cas/login?service=myhost:8080/uPortal/Login");
        request = new MockHttpServletRequest("GET", "http://myhost:8080/uPortal/p/fname");
        request.setCharacterEncoding("UTF-8");
    }

    @Test
    public void testSimpleDestination() throws Exception {
        assertEquals("https://cas:80/cas/login?service=myhost:8080/uPortal/Login%3FrefUrl%3Dhttp%3A%2F%2Fmyhost%3A8080%2FuPortal%2Fp%2Ffname",
                encoder.encodeLoginAndRefUrl(request));
    }

    @Test
    public void testWithParam() throws Exception {
        request = new MockHttpServletRequest("GET", "http://myhost:8080/uPortal/p/fname");
        request.setQueryString("pP_announcementId=1834");
        request.setCharacterEncoding("UTF-8");
        assertEquals("https://cas:80/cas/login?service=myhost:8080/uPortal/Login%3FrefUrl%3Dhttp%3A%2F%2Fmyhost%3A8080%2FuPortal%2Fp%2Ffname%253FpP_announcementId%253D1834",
                encoder.encodeLoginAndRefUrl(request));
    }

    @Test
    public void testWithTwoParams() throws Exception {
        request = new MockHttpServletRequest("GET", "http://myhost:8080/uPortal/p/fname");
        request.setQueryString("pP_action=displayFullAnnouncement&pP_announcementId=1834");
        request.setCharacterEncoding("UTF-8");
        assertEquals("https://cas:80/cas/login?service=myhost:8080/uPortal/Login%3FrefUrl%3Dhttp%3A%2F%2Fmyhost%3A8080%2FuPortal%2Fp%2Ffname%253FpP_action%253DdisplayFullAnnouncement%2526pP_announcementId%253D1834",
                encoder.encodeLoginAndRefUrl(request));
    }
}
