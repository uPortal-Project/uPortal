/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.security.firewall;

import java.io.IOException;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link org.jasig.portal.security.firewall.RequestParameterPolicyEnforcementFilter}.
 *
 * There are two kinds of testcases here.
 *
 * First there are testcases for the Filter as a whole against the Filter API.  The advantage of these is that they
 * are testing at the level we care about, the way the filter will actually be used,
 * against the API it really exposes to adopters.  So, great.  The disadvantage of these is that it's
 *
 * Then there are testcases for bits of the implementation of the filter (namely, configuration parsing and policy
 * enforcement).
 *
 * @since uPortal 4.0.15
 */
public final class RequestParameterPolicyEnforcementFilterTests {

    /* ========================================================================================================== */

    /* Tests for the Filter as a whole.
     */


    /**
     * Test that the Filter throws on init when unrecognized Filter init-param.
     * @throws ServletException on test success.
     */
    @Test(expected = ServletException.class)
    public void testUnrecognizedInitParamFailsFilterInit() throws ServletException {

        final Set<String> initParameterNames = new HashSet<String>();
        initParameterNames.add("unrecognizedInitParameterName");
        final Enumeration parameterNamesEnumeration = Collections.enumeration(initParameterNames);

        final FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        final RequestParameterPolicyEnforcementFilter filter = new RequestParameterPolicyEnforcementFilter();
        filter.init(filterConfig);
    }

    /**
     * Test that if you configure the filter to forbid no characters and also to allow multi-valued parameters,
     * filter init fails because the filter would be a no-op.
     */
    @Test(expected = ServletException.class)
    public void testNoOpConfigurationFailsFilterInit() throws ServletException {
        final RequestParameterPolicyEnforcementFilter filter = new RequestParameterPolicyEnforcementFilter();

        // mock up filter config.
        final Set<String> initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        final Enumeration parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        final FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
                .thenReturn("true");
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
                .thenReturn("none");
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
                .thenReturn(null);

        filter.init(filterConfig);



    }

    /**
     * Test that, in the default configuration, when presented with a multi-valued parameter that configured to check
     * and configured to require not multi valued, rejects request.
     */
    @Test(expected  = ServletException.class)
    public void testRejectsMultiValuedRequestParameter() throws IOException, ServletException {

        final RequestParameterPolicyEnforcementFilter filter = new RequestParameterPolicyEnforcementFilter();

        // mock up filter config.  Default configuration with no init-params for this use case.
        final Set<String> initParameterNames = new HashSet<String>();
        final Enumeration parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        final FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
                .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
                .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
                .thenReturn(null);


        // init the filter
        try {
            filter.init(filterConfig);
        } catch (Exception e) {
            Assert.fail("Should not have failed filter init.");
        }

        // mock up a request to filter

        final Map<String, String[]> requestParameterMap = new HashMap<String, String[]>();
        requestParameterMap.put("someName", new String[] {"someValue", "someOtherValue"});

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(requestParameterMap);

        final HttpServletResponse response = mock(HttpServletResponse.class);

        final FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

    }

    /**
     * Test that, when configured to allow multi-valued parameters,
     * when presented with a multi-valued parameter that configured to check
     * , rejects request.
     */
    @Test
    public void testAcceptsMultiValuedRequestParameter() throws IOException, ServletException {

        final RequestParameterPolicyEnforcementFilter filter = new RequestParameterPolicyEnforcementFilter();

        // mock up filter config. Configure to allow multi-valued parameters.
        final Set<String> initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        final Enumeration parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        final FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
                .thenReturn("true");
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
                .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
                .thenReturn(null);


        // init the filter
        try {
            filter.init(filterConfig);
        } catch (Exception e) {
            Assert.fail("Should not have failed filter init.");
        }

        // mock up a request to filter, with a multi-valued checked parameter

        final Map<String, String[]> requestParameterMap = new HashMap<String, String[]>();
        requestParameterMap.put("someName", new String[] {"someValue", "someOtherValue"});

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(requestParameterMap);

        final HttpServletResponse response = mock(HttpServletResponse.class);

        final FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

    }

    /**
     * Test that, in the default configuration, when presented with a request parameter with an illicit character in
     * it, blocks the request.
     * @throws IOException
     * @throws ServletException
     */
    @Test(expected = ServletException.class)
    public void testRejectsRequestWithIllicitCharacterInCheckedParameter() throws IOException, ServletException {

        final RequestParameterPolicyEnforcementFilter filter = new RequestParameterPolicyEnforcementFilter();

        // mock up filter config.  Default configuration with no init-params for this use case.
        final Set<String> initParameterNames = new HashSet<String>();
        final Enumeration parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        final FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
                .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
                .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
                .thenReturn(null);


        // init the filter
        try {
            filter.init(filterConfig);
        } catch (Exception e) {
            Assert.fail("Should not have failed filter init.");
        }

        // mock up a request to filter

        final Map<String, String[]> requestParameterMap = new HashMap<String, String[]>();
        // percent character is illicit by default, so, illicit character in this parameter value
        requestParameterMap.put("someName", new String[] {"someValue%40gmail.com"});

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(requestParameterMap);

        final HttpServletResponse response = mock(HttpServletResponse.class);

        final FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

    }

    /**
     * Test that when configured to only check some parameters, does not throw on forbidden character in unchecked
     * parameter.
     */
    @Test
    public void testAllowsUncheckedParametersToHaveIllicitCharacters() throws IOException, ServletException {

        final RequestParameterPolicyEnforcementFilter filter = new RequestParameterPolicyEnforcementFilter();

        // mock up filter config.  Default configuration with no init-params for this use case.
        final Set<String> initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        final Enumeration parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        final FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
                .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
                .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
                .thenReturn("ticket");


        // init the filter
        try {
            filter.init(filterConfig);
        } catch (Exception e) {
            Assert.fail("Should not have failed filter init.");
        }

        // mock up a request to filter

        final Map<String, String[]> requestParameterMap = new HashMap<String, String[]>();
        // percent character is illicit by default, so, illicit character in this parameter value
        // but this parameter name is unchecked
        requestParameterMap.put("uncheckedName", new String[] {"someValue%40gmail.com"});

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(requestParameterMap);

        final HttpServletResponse response = mock(HttpServletResponse.class);

        final FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

    }


    /* ========================================================================================================== */

    /* Tests for throwIfUnrecognizedInitParamNames()
     * These test that the fail-safe on unrecognized (and thus un-honored) configuration works as intended.
     */

    /**
     * Tests that the method checking for unrecognized parameters accepts the expected parameters.
     * @throws ServletException on test failure
     */
    @Test
    public void testAcceptsExpectedParameterNames() throws ServletException {

        final Set<String> parameterNames = new HashSet<String>();
        parameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        final Enumeration parameterNamesEnumeration = Collections.enumeration(parameterNames);

         RequestParameterPolicyEnforcementFilter.throwIfUnrecognizedParamName(parameterNamesEnumeration);
    }

    /**
     * Test that the method checking for unrecognized parameters throws on an unrecognized parameter.
     * @throws ServletException on test success
     */
    @Test(expected = ServletException.class)
    public void testRejectsUnExpectedParameterName() throws ServletException {

        final Set<String> parameterNames = new HashSet<String>();
        parameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        parameterNames.add("unexpectedParameterName");
        final Enumeration parameterNamesEnumeration = Collections.enumeration(parameterNames);

        RequestParameterPolicyEnforcementFilter.throwIfUnrecognizedParamName(parameterNamesEnumeration);
    }



    /* ========================================================================================================== */

    /* Tests for parseStringToBooleanDefaultingToFalse()
     * These test that the boolean init parameter parsing works properly.
     */

    /**
     * Test that true parses to true.
     */
    @Test
    public void testParsesTrueToTrue() {

        Assert.assertTrue(RequestParameterPolicyEnforcementFilter.parseStringToBooleanDefaultingToFalse("true"));

    }

    /**
     * Test that false parses to false.
     */
    @Test
    public void testParsesFalseToFalse() {

        Assert.assertFalse(RequestParameterPolicyEnforcementFilter.parseStringToBooleanDefaultingToFalse("false"));

    }

    /**
     * Test that null parses to false.
     */
    @Test
    public void testParsesNullToFalse() {

        Assert.assertFalse(RequestParameterPolicyEnforcementFilter.parseStringToBooleanDefaultingToFalse(null));

    }

    /**
     * Test that maybe parses to illegal argument exception.
     */
    @Test(expected = Exception.class)
    public void testParsingMaybeYieldsException() {

        RequestParameterPolicyEnforcementFilter.parseStringToBooleanDefaultingToFalse("maybe");

    }


    /* ========================================================================================================== */
    /* Tests for parseParametersToCheck().
     * Ensure that the Filter properly understands which parameters it ought to be checking.
     */

    /**
     * Test that a null parameter value parses to the empty set.
     */
    @Test
    public void testParsesNullToEmptySet() {

        final Set<String> returnedSet  = RequestParameterPolicyEnforcementFilter.parseParametersToCheck(null);

        Assert.assertTrue(returnedSet.isEmpty());

    }

    /**
     * Test that a whitespace delimited list of parameter names parses as expected.
     */
    @Test
    public void testParsesWhiteSpaceDelimitedStringToSet() {

        final String parameterValue = "service renew gateway";

        final Set<String> expectedSet = new HashSet<String>();
        expectedSet.add("service");
        expectedSet.add("renew");
        expectedSet.add("gateway");

        final Set<String> returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersToCheck(parameterValue);

        Assert.assertEquals(expectedSet, returnedSet);
    }

    /**
     * Test that blank parses to exception.
     */
    @Test(expected = Exception.class)
    public void testParsingBlankParametersToCheckThrowsException() {

        RequestParameterPolicyEnforcementFilter.parseParametersToCheck("   ");

    }

    /**
     * Test the special parsing behavior of star parses to empty Set.
     */
    @Test
    public void testAsteriskParsesToEmptySetOfParametersToCheck() {

        final Set<String> expectedSet = new HashSet<String>();

        final Set<String> returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersToCheck("*");

        Assert.assertEquals(expectedSet, returnedSet);

    }

    /**
     * Test that encountering the star token among other tokens yields exception.
     */
    @Test(expected = Exception.class)
    public void testParsingAsteriskWithOtherTokensThrowsException() {

        RequestParameterPolicyEnforcementFilter.parseParametersToCheck("renew * gateway");

    }


    /* ========================================================================================================== */
    /* Tests for parseCharactersToForbid().
     * Ensure that the Filter properly understands which characters it ought to be forbidding.
     */

    /**
     * Test that when the parameter is not set (is null) parses to a default set of character.
     */
    @Test
    public void testParsingNullYieldsPercentHashAmpersandAndQuestionMark() {

        final Set<Character> expected = new HashSet<Character>();
        expected.add('%');
        expected.add('#');
        expected.add('&');
        expected.add('?');

        final Set<Character> actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid(null);

        Assert.assertEquals(expected, actual);

    }

    /**
     * Test that when the parameter is set but blank throws an exception.
     */
    @Test(expected = Exception.class)
    public void testParsingBlankThrowsException() {
        RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("   ");
    }

    /**
     * Test that when the parameter is set to the special value "none"  returns empty Set.
     */
    @Test
    public void testParsesLiteralNoneToEmptySet() {

        final Set<Character> expected = new HashSet<Character>();

        final Set<Character> actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("none");

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test that parsing some characters works as expected.
     */
    @Test
    public void testParsingSomeCharactersWorks() {
        final Set<Character> expected = new HashSet<Character>();
        expected.add('&');
        expected.add('%');
        expected.add('*');
        expected.add('#');
        expected.add('@');

        final Set<Character> actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("& % * # @");

        Assert.assertEquals(expected, actual);
    }

    /**
     * The tokens are supposed to be single characters.  If they are longer than that, the deployer may be confused as
     * to how to configure this filter.
     */
    @Test(expected = Exception.class)
    public void testParsingMulticharacterTokensThrows() {
        RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("& %*# @");
    }


    /* ========================================================================================================== */

    /* Tests for requireNotMultiValued().
     * Ensure that runtime enforcement of not-multi-valued-ness work properly.
     */




    /**
     * Test that enforcing no-multi-value detects multi-valued parameter and throws.
     */
    @Test(expected = IllegalStateException.class)
    public void testRequireNotMultiValueBlocksMultiValue() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        // set up a parameter map with a multi-valued parameter
        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[] {"Johan", "Cubby"});

        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    /**
     * Test that enforcing no-multi-value allows single-valued parameter.
     */
    @Test
    public void testRequireNotMultiValuedAllowsSingleValued() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        // set up a parameter map with single-valued parameter
        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[] {"Abbie"});

        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);

    }

    /**
     * Test that enforcing no-multi-value allows not-present parameter.
     */
    @Test
    public void testRequireNotMultiValuedIgnoresMissingParameter() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        // set up a parameter map with no entries
        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();

        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);

    }

    /**
     * Test that enforcing no-multi-value allows multi-valued parameters not among those to check.
     */
    @Test
    public void testRequireNotMultiValueAllowsUncheckedMultiValue() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        // set up a parameter map with a multi-valued parameter with a name not matching those to check
        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[] {"Reggie", "Shenanigans"});

        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    /* ========================================================================================================== */

    /* Tests for enforceParameterContentCharacterRestrictions().
     * Ensure that runtime enforcement of what characters parameters contain works properly.
     */

    /**
     * Test that allows parameters not containing forbidden characters.
     */
    @Test
    public void testAllowsParametersNotContainingForbiddenCharacters() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        final Set<Character> charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('%');

        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[] {"Reggie", "Shenanigans"});
        parameterMap.put("dogName", new String[] {"Brutus", "Johan", "Cubby", "Abbie"});
        parameterMap.put("plantName", new String[] {"Rex"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
                charactersToForbid, parameterMap);

    }

    /**
     * Test that when a checked parameter contains a forbidden character, throws.
     */
    @Test(expected = Exception.class)
    public void testThrowsOnParameterContainingForbiddenCharacter() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("plantName");

        final Set<Character> charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('%');

        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[] {"Reggie", "Shenanigans"});
        parameterMap.put("dogName", new String[] {"Brutus", "Johan", "Cubby", "Abbie"});
        // plantName is checked, and contains a forbidden character
        parameterMap.put("plantName", new String[] {"Rex&p0wned=true"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
                charactersToForbid, parameterMap);

    }

    /**
     * Test that when a checked parameter contains a forbidden character in a non-first value, still throws.
     */
    @Test(expected = Exception.class)
    public void testThrowsOnMultipleParameterContainingForbiddenCharacter() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        final Set<Character> charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('!');
        charactersToForbid.add('$');

        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[] {"Reggie", "Shenanigans"});
        // dogName is checked, and contains a forbidden character
        parameterMap.put("dogName", new String[] {"Brutus", "Johan", "Cub!by", "Abbie"});
        parameterMap.put("plantName", new String[] {"Rex"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
                charactersToForbid, parameterMap);

    }

    /**
     * Test that when an unchecked parameter contains a character that would be forbidden were the parameter checked,
     * does not throw.
     */
    @Test
    public void testAllowsUncheckedParameterContainingForbiddenCharacter() {

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        final Set<Character> charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('$');

        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[] {"Reggie", "Shenanigans"});
        parameterMap.put("dogName", new String[] {"Brutus", "Johan", "Cubby", "Abbie"});

        // plantName is not checked
        parameterMap.put("plantName", new String[] {"Rex&ownage=true"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
                charactersToForbid, parameterMap);

    }

    /**
     * Test that not all parametersToCheck need be present on the request.
     */
    @Test
    public void testAllowsCheckedParameterNotPresent() {
        // this test added in response to a stupid NullPointerException defect, to prevent regression.

        final Set<String> parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        final Set<Character> charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('$');

        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[] {"Brutus", "Johan", "Cubby", "Abbie"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
                charactersToForbid, parameterMap);

    }

}
