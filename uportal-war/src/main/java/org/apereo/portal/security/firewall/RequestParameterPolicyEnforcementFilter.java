/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security.firewall;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This is a Java Servlet Filter that examines specified Request Parameters as to whether they
 * contain specified characters and as to whether they are multivalued throws an Exception if they
 * do not meet configured rules.
 *
 * <p>This is a fork of the Filter offered at
 * https://github.com/Jasig/cas-server-security-filter/pull/6 .
 *
 * <p>This is forked into the uPortal project source code directly so that it can be used to patch
 * the uPortal usage of the Java CAS Client against CVE-2014-4172 without requiring upgrade of the
 * Java CAS Client library itself.
 *
 * <p>Configuration:
 *
 * <p>The filter defaults to checking all request parameters for the hash, percent, question mark,
 * and ampersand characters, and enforcing no-multi-valued-ness.
 *
 * <p>You can turn off multi-value checking by setting the init-param "allowMultiValuedParameters"
 * to "true". Setting it to "false" is a no-op retaining the default configuration. Setting this
 * parameter to any other value fails filter initialization.
 *
 * <p>You can change the set of request parameters being examined by setting the init-param
 * "parametersToCheck" to a whitespace delimited list of parameters to check. Setting it to the
 * special value "*" retains the default behavior of checking all. Setting it to a blank value fails
 * filter initialization. Setting it to a String containing the asterisk token and any additional
 * token fails filter initialization.
 *
 * <p>You can change the set of characters looked for by setting the init-param "charactersToForbid"
 * to a whitespace delimited list of characters to forbid. Setting it to the special value "none"
 * disables the illicit character blocking feature of this Filter (for the case where you only want
 * to use the mutli-valued-ness blocking). Setting it to a blank value fails filter initialization.
 * Setting it to a value that fails to parse perfectly (e.g., a value with multi-character Strings
 * between the whitespace delimiters) fails filter initialization. The default set of characters
 * disallowed is percent, hash, question mark, and ampersand.
 *
 * <p>Setting any other init parameter other than these recognized by this Filter will fail Filter
 * initialization. This is to protect the adopter from typos or misunderstandings in web.xml
 * configuration such that an intended configuration might not have taken effect, since that might
 * have security implications.
 *
 * <p>Setting the Filter to both allow multi-valued parameters and to disallow no characters would
 * make the Filter a no-op, and so fails Filter initialization since you probably meant the Filter
 * to be doing something.
 *
 * <p>The intent of this filter is rough, brute force blocking of unexpected characters in specific
 * CAS protocol related request parameters. This is one option as a workaround for patching in place
 * certain Java CAS Client versions that may be vulnerable to certain attacks involving crafted
 * request parameter values that may be mishandled. This is also suitable for patching certain CAS
 * Server versions to make more of an effort to detect and block out-of-spec CAS protocol requests.
 * Aside from the intent to be useful for those cases, there is nothing CAS-specific about this
 * Filter itself. This is a generic Filter for doing some pretty basic generic sanity checking on
 * request parameters. It might come in handy the next time this kind of issue arises.
 *
 * <p>This Filter is written to have no external .jar dependencies aside from the Servlet API
 * necessary to be a Filter.
 *
 * <p>This class is declared final because it is not designed for extension.
 *
 * @since 4.0.15 , 4.1.1
 */
public final class RequestParameterPolicyEnforcementFilter implements Filter {

    /**
     * The set of Characters blocked by default on checked parameters. Expressed as a whitespace
     * delimited set of characters.
     */
    public static final String DEFAULT_CHARACTERS_BLOCKED = "? & # %";

    /**
     * The name of the optional Filter init-param specifying what request parameters ought to be
     * checked. The value is a whitespace delimited set of parameters. The exact value '*' has the
     * special meaning of matching all parameters, and is the default behavior.
     */
    public static final String PARAMETERS_TO_CHECK = "parametersToCheck";

    /**
     * The name of the optional Filter init-param specifying what characters are forbidden in the
     * checked request parameters. The value is a whitespace delimited set of such characters.
     */
    public static final String CHARACTERS_TO_FORBID = "charactersToForbid";

    /**
     * The name of the optional Filter init-param specifying whether the checked request parameters
     * are allowed to have multiple values. Allowable values for this init parameter are `true` and
     * `false`.
     */
    public static final String ALLOW_MULTI_VALUED_PARAMETERS = "allowMultiValuedParameters";

    /**
     * Set of parameter names to check. Empty set represents special behavior of checking all
     * parameters.
     */
    private Set<String> parametersToCheck;

    /**
     * Set of characters to forbid in the checked request parameters. Empty set represents not
     * forbidding any characters.
     */
    private Set<Character> charactersToForbid;

    /** Should checked parameters be permitted to have multiple values. */
    private boolean allowMultiValueParameters = false;

    /* ========================================================================================================== */
    /* Filter methods */

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

        // verify there are no init parameters configured that are not recognized
        // since an unrecognized init param might be the adopter trying to configure this filter in an important way
        // and accidentally ignoring that intent might have security implications.
        final Enumeration initParamNames = filterConfig.getInitParameterNames();
        throwIfUnrecognizedParamName(initParamNames);

        final String initParamAllowMultiValuedParameters =
                filterConfig.getInitParameter(ALLOW_MULTI_VALUED_PARAMETERS);
        final String initParamParametersToCheck =
                filterConfig.getInitParameter(PARAMETERS_TO_CHECK);
        final String initParamCharactersToForbid =
                filterConfig.getInitParameter(CHARACTERS_TO_FORBID);

        try {
            this.allowMultiValueParameters =
                    parseStringToBooleanDefaultingToFalse(initParamAllowMultiValuedParameters);
        } catch (final Exception e) {
            throw new ServletException(
                    "Error parsing request parameter ["
                            + ALLOW_MULTI_VALUED_PARAMETERS
                            + "] with value ["
                            + initParamAllowMultiValuedParameters
                            + "]",
                    e);
        }

        try {
            this.parametersToCheck = parseParametersToCheck(initParamParametersToCheck);
        } catch (final Exception e) {
            throw new ServletException(
                    "Error parsing request parameter "
                            + PARAMETERS_TO_CHECK
                            + " with value ["
                            + initParamParametersToCheck
                            + "]",
                    e);
        }

        try {
            this.charactersToForbid = parseCharactersToForbid(initParamCharactersToForbid);
        } catch (final Exception e) {
            throw new ServletException(
                    "Error parsing request parameter "
                            + CHARACTERS_TO_FORBID
                            + " with value ["
                            + initParamCharactersToForbid
                            + "]",
                    e);
        }

        if (this.allowMultiValueParameters && this.charactersToForbid.isEmpty()) {
            throw new ServletException(
                    "Configuration to allow multi-value parameters and forbid no characters makes "
                            + getClass().getSimpleName()
                            + " a no-op, which is probably not what you want, "
                            + "so failing Filter init.");
        }
    }

    @Override
    public void doFilter(
            final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        try {
            if (request instanceof HttpServletRequest) {
                final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

                // immutable map from String param name --> String[] parameter values
                final Map parameterMap = httpServletRequest.getParameterMap();

                // which parameters *on this request* ought to be checked.
                final Set<String> parametersToCheckHere;

                if (this.parametersToCheck.isEmpty()) {
                    // the special meaning of empty is to check *all* the parameters, so
                    parametersToCheckHere = parameterMap.keySet();
                } else {
                    parametersToCheckHere = this.parametersToCheck;
                }

                if (!this.allowMultiValueParameters) {
                    requireNotMultiValued(parametersToCheckHere, parameterMap);
                }

                enforceParameterContentCharacterRestrictions(
                        parametersToCheckHere, this.charactersToForbid, parameterMap);
            }
        } catch (final Exception e) {
            // translate to a ServletException to meet the typed expectations of the Filter API.
            throw new ServletException(
                    getClass().getSimpleName()
                            + " is blocking this request.  Examine the cause in"
                            + " this stack trace to understand why.",
                    e);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // do nothing
    }

    /* ========================================================================================================== */
    /* Init parameter parsing */

    /**
     * Examines the Filter init parameter names and throws ServletException if they contain an
     * unrecognized init parameter name.
     *
     * <p>This is a stateless static method.
     *
     * <p>This method is an implementation detail and is not exposed API. This method is only
     * non-private to allow JUnit testing.
     *
     * @param initParamNames init param names, in practice as read from the FilterConfig.
     * @throws ServletException if unrecognized parameter name is present
     */
    static void throwIfUnrecognizedParamName(Enumeration initParamNames) throws ServletException {
        final Set<String> recognizedParameterNames = new HashSet<String>();
        recognizedParameterNames.add(ALLOW_MULTI_VALUED_PARAMETERS);
        recognizedParameterNames.add(PARAMETERS_TO_CHECK);
        recognizedParameterNames.add(CHARACTERS_TO_FORBID);

        while (initParamNames.hasMoreElements()) {
            final String initParamName = (String) initParamNames.nextElement();
            if (!recognizedParameterNames.contains(initParamName)) {
                throw new ServletException(
                        "Unrecognized init parameter ["
                                + initParamName
                                + "].  Failing safe.  Typo"
                                + " in the web.xml configuration? "
                                + " Misunderstanding about the configuration "
                                + RequestParameterPolicyEnforcementFilter.class.getSimpleName()
                                + " expects?");
            }
        }
    }

    /**
     * Parse a String to a boolean.
     *
     * <p>"true" --> true "false" --> false null --> false Anything else --> throw
     * IllegalArgumentException.
     *
     * <p>This is a stateless static method.
     *
     * <p>This method is an implementation detail and is not exposed API. This method is only
     * non-private to allow JUnit testing.
     *
     * @param stringToParse a String to parse to a boolean as specified
     * @return true or false
     * @throws IllegalArgumentException if the String is not true, false, or null.
     */
    static boolean parseStringToBooleanDefaultingToFalse(final String stringToParse) {

        if ("true".equals(stringToParse)) {
            return true;
        } else if ("false".equals(stringToParse)) {
            return false;
        } else if (null == stringToParse) {
            return false;
        }

        throw new IllegalArgumentException(
                "String ["
                        + stringToParse
                        + "] could not parse to a boolean because it was not precisely 'true' or 'false'.");
    }

    /**
     * Parse the whitespace delimited String of parameters to check.
     *
     * <p>If the String is null, return the empty set. If the whitespace delimited String contains
     * no tokens, throw IllegalArgumentException. If the sole token is an asterisk, return the empty
     * set. If the asterisk token is encountered among other tokens, throw IllegalArgumentException.
     *
     * <p>This method returning an empty Set has the special meaning of "check all parameters".
     *
     * <p>This is a stateless static method.
     *
     * <p>This method is an implementation detail and is not exposed API. This method is only
     * non-private to allow JUnit testing.
     *
     * @param initParamValue null, or a non-blank whitespace delimited list of parameters to check
     * @return a Set of String names of parameters to check, or an empty set representing
     *     check-them-all.
     * @throws IllegalArgumentException when the init param value is out of spec
     */
    static Set<String> parseParametersToCheck(final String initParamValue) {

        final Set<String> parameterNames = new HashSet<String>();

        if (null == initParamValue) {
            return parameterNames;
        }

        final String[] tokens = initParamValue.split("\\s+");

        if (0 == tokens.length) {
            throw new IllegalArgumentException(
                    "["
                            + initParamValue
                            + "] had no tokens but should have had at least one token.");
        }

        if (1 == tokens.length && "*".equals(tokens[0])) {
            return parameterNames;
        }

        for (final String parameterName : tokens) {

            if ("*".equals(parameterName)) {
                throw new IllegalArgumentException(
                        "Star token encountered among other tokens in parsing ["
                                + initParamValue
                                + "]");
            }

            parameterNames.add(parameterName);
        }

        return parameterNames;
    }

    /**
     * Parse a whitespace delimited set of Characters from a String.
     *
     * <p>If the String is null (init param was not set) default to DEFAULT_CHARACTERS_BLOCKED. If
     * the String is "none" parse to empty set meaning block no characters. If the String is empty
     * throw, to avoid configurer accidentally configuring not to block any characters.
     *
     * @param paramValue value of the init param to parse
     * @return non-null Set of zero or more Characters to block
     */
    static Set<Character> parseCharactersToForbid(String paramValue) {

        final Set<Character> charactersToForbid = new HashSet<Character>();

        if (null == paramValue) {
            paramValue = DEFAULT_CHARACTERS_BLOCKED;
        }

        if ("none".equals(paramValue)) {
            return charactersToForbid;
        }

        final String[] tokens = paramValue.split("\\s+");

        if (0 == tokens.length) {
            throw new IllegalArgumentException(
                    "Expected tokens when parsing ["
                            + paramValue
                            + "] but found no tokens."
                            + " If you really want to configure no characters, use the magic value 'none'.");
        }

        for (final String token : tokens) {

            if (token.length() > 1) {
                throw new IllegalArgumentException(
                        "Expected tokens of length 1 but found ["
                                + token
                                + "] when "
                                + "parsing ["
                                + paramValue
                                + "]");
            }

            final Character character = token.charAt(0);
            charactersToForbid.add(character);
        }

        return charactersToForbid;
    }

    /* ========================================================================================================== */
    /* Filtering requests */

    /**
     * For each parameter to check, verify that it has zero or one value.
     *
     * <p>The Set of parameters to check MAY be empty. The parameter map MAY NOT contain any given
     * parameter to check.
     *
     * <p>This method is an implementation detail and is not exposed API. This method is only
     * non-private to allow JUnit testing.
     *
     * <p>Static, stateless method.
     *
     * @param parametersToCheck non-null potentially empty Set of String names of parameters
     * @param parameterMap non-null Map from String name of parameter to String[] values
     * @throws IllegalStateException if a parameterToCheck is present in the parameterMap with
     *     multiple values.
     */
    static void requireNotMultiValued(final Set<String> parametersToCheck, final Map parameterMap) {

        for (final String parameterName : parametersToCheck) {
            if (parameterMap.containsKey(parameterName)) {
                final String[] values = (String[]) parameterMap.get(parameterName);
                if (values.length > 1) {
                    throw new IllegalStateException(
                            "Parameter ["
                                    + parameterName
                                    + "] had multiple values ["
                                    + values
                                    + "] but at most one value is allowable.");
                }
            }
        }
    }

    /**
     * For each parameter to check, for each value of that parameter, check that the value does not
     * contain forbidden characters.
     *
     * <p>This is a stateless static method.
     *
     * <p>This method is an implementation detail and is not exposed API. This method is only
     * non-private to allow JUnit testing.
     *
     * @param parametersToCheck Set of String request parameter names to look for
     * @param charactersToForbid Set of Character characters to forbid
     * @param parameterMap String --> String[] Map, in practice as read from ServletRequest
     */
    static void enforceParameterContentCharacterRestrictions(
            final Set<String> parametersToCheck,
            final Set<Character> charactersToForbid,
            final Map parameterMap) {

        if (charactersToForbid.isEmpty()) {
            // short circuit
            return;
        }

        for (final String parameterToCheck : parametersToCheck) {

            final String[] parameterValues = (String[]) parameterMap.get(parameterToCheck);

            if (null != parameterValues) {

                for (final String parameterValue : parameterValues) {

                    for (final Character forbiddenCharacter : charactersToForbid) {

                        final StringBuilder characterAsStringBuilder = new StringBuilder();
                        characterAsStringBuilder.append(forbiddenCharacter);

                        if (parameterValue.contains(characterAsStringBuilder)) {
                            throw new IllegalArgumentException(
                                    "Disallowed character ["
                                            + forbiddenCharacter
                                            + "] found in value ["
                                            + parameterValue
                                            + "] of parameter named ["
                                            + parameterToCheck
                                            + "]");
                        }

                        // that forbiddenCharacter was not in this parameterValue
                    }

                    // none of the charactersToForbid were in this parameterValue
                }

                // none of the values of this parameterToCheck had a forbidden character
            } // or this parameterToCheck had null value
        }

        // none of the values of any of the parametersToCheck had a forbidden character
        // hurray! allow flow to continue without throwing an Exception.
    }
}
