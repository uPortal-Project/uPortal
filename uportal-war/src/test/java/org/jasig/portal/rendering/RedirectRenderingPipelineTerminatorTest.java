package org.jasig.portal.rendering;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for RedirectRenderingPipelineTerminator.
 * @since uPortal 4.2
 */
public class RedirectRenderingPipelineTerminatorTest {

    @Mock private HttpServletRequest mockRequest;

    @Mock private HttpServletResponse mockResponse;

    @Before
    public void beforeTests() {
        initMocks(this);
    }

    /**
     * Test that on the happy path, sends a redirect to the properly configured redirectTo path.
     * @throws ServletException never, this would be a test failure
     * @throws IOException never, this would be a test failure
     */
    @Test
    public void redirectsToConfiguredPath()
            throws ServletException, IOException {

        final RedirectRenderingPipelineTerminator terminator = new RedirectRenderingPipelineTerminator();
        terminator.setRedirectTo("/web");

        terminator.renderState(mockRequest, mockResponse);

        verify(mockResponse).sendRedirect("/web");
    }

    /**
     * Test that RedirectRenderingPipelineTerminator throws IllegalStateException if
     * invoked without the redirect target having been set.
     * @throws ServletException never, this would be a test failure.
     * @throws IOException never, this would be a test failure.
     */
    @Test(expected =  IllegalStateException.class)
    public void throwsIllegalStateExceptionWhenRedirectToNotSet()
            throws ServletException, IOException {

        final RedirectRenderingPipelineTerminator unconfiguredTerminator = new RedirectRenderingPipelineTerminator();
        // forget to setRedirectTo

        unconfiguredTerminator.renderState(mockRequest, mockResponse);
    }

    /**
     * Test that attempts to set the redirect path to null on a RedirectRenderingPipelineTerminator
     * are rebuffed with an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionOnSettingRedirectPathToNull() {

        final RedirectRenderingPipelineTerminator badlyConfiguredTerminator = new RedirectRenderingPipelineTerminator();
        badlyConfiguredTerminator.setRedirectTo(null);

    }
}
