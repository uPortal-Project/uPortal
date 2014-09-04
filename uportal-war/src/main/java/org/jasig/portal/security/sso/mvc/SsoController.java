/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security.sso.mvc;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.api.sso.SsoPersonLookupService;
import org.jasig.portal.api.sso.SsoTicket;
import org.jasig.portal.api.sso.SsoTicketService;
import org.jasig.portal.api.url.BuildUrlRequest;
import org.jasig.portal.api.url.UrlBuilderService;
import org.jasig.portal.security.SystemPerson;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Controller
@RequestMapping("/sso")
public final class SsoController {

	private static final String USERNAME_PARAMETER = "username";
	private static final String SCHOOL_ID_PARAMETER = "schoolId";
	private static final String SCHOOL_ID_PERSONDIR_LOGICAL_ATTR_NAME = "schoolId";
	private static final String FORMATTED_COURSE_PARAMETER = "formattedCourse";
	private static final String SECTION_CODE_PARAMETER = "sectionCode";
	private static final String STUDENT_SCHOOL_ID_PARAMETER = "studentSchoolId";
	private static final String STUDENT_USER_NAME_PARAMETER = "studentUserName";
	private static final String TERM_CODE_PARAMETER = "termCode";
	private static final String TOKEN_PARAMETER = "token";
	private static final String TIMESTAMP = "timeStamp";
	private static final String VIEW = "view";

	// For building the refUrl
	private static final Object ACTION_KEY_SIMPLE = "action";
	private static final Object ACTION_VALUE = "enterAlert";
	private static final String DEFAULT_TEMPLATE = "default";
	private static final String EARLY_ALERT_TEMPLATE_NEW = "ea.new";

	private static final String SUCCESS_FIELD = "success";
	private static final String URL_FIELD = "URL";
	private static final String MESSAGE_FIELD = "message";

	@Value("${org.jasig.portal.security.sso.mvc.SsoController.requireSecure}")
	private boolean requireSecure = true; // default

	@Value("${org.jasig.portal.security.sso.mvc.SsoController.sharedSecret}")
	private String sharedSecret = null;

	@Value("${org.jasig.portal.security.sso.mvc.SsoController.checkTimeStampRange}")
	private boolean checkTimeStampRange = true; // default

	@Value("${org.jasig.portal.security.sso.mvc.SsoController.signedUrlToLiveMinutes}")
	private int signedUrlToLiveMinutes = 5; // default

	@Value("${org.jasig.portal.api.sso.ApiSsoTicketService.secret}")
	private String ticketIssuingSecret;

	@Autowired
	private SsoTicketService ssoTicketService;

	@Autowired
	private UrlBuilderService urlBuilder;

	@Autowired
	private SsoPersonLookupService ssoPersonLookupService;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@RequestMapping
	public ModelAndView issueUrl(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		if (null != sharedSecret && !("".equals(sharedSecret))) {

			// Verify secure connection if we must have one
			if (requireSecure && !req.isSecure()) {
				return sendClientError(res, HttpServletResponse.SC_FORBIDDEN,
						"The SSO handshake requires a secure connection (SSL)");
			}

			// Inputs
			Inputs inputs = null;
			try {
				inputs = Inputs.parse(req, checkTimeStampRange);
			} catch (Exception e) {
				return sendClientError(res, HttpServletResponse.SC_BAD_REQUEST,
						"One or more required inputs was not specified", e);
			}

			// Verify the request is authorized
			try {
				if (!validateToken(inputs)) {
					return sendClientError(res,
							HttpServletResponse.SC_FORBIDDEN, "Not authorized");
				}
			} catch (Exception e) {
				return sendServerError(res,
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Authorization check error", e);
			}

			// Verify if TimeStamp range needs to be checked
			if (checkTimeStampRange) {
				try {
					if (!validateTimeStampRange(inputs.getTimeStamp())) {
						return sendClientError(res,
								HttpServletResponse.SC_FORBIDDEN,
								"Timestamp out of range");
					}
				} catch (ParseException e) {
					return sendClientError(res,
							HttpServletResponse.SC_BAD_REQUEST,
							"Timestamp parse failure", e);
				} catch (Exception e) {
					return sendServerError(res,
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Authorization check error", e);
				}
			}

			// Generate a ticket
			String username = null;
			try {
				username = resolveToValidUsername(inputs);
			} catch (Exception e) {
				return sendServerError(res,
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"End user lookup error", e);
			}

			if (username == null) {
				return sendClientError(res, HttpServletResponse.SC_BAD_REQUEST,
						"Missing or invalid end user identifier(s)");
			}

			log.info("Generating SSO ticket for username '{}'", username);
			final SsoTicket ssoTicket = ssoTicketService.issueTicket(username, ticketIssuingSecret);

			@SuppressWarnings("unchecked")
			Enumeration<String> iter = req.getParameterNames();
			Map<String, String> parameters = new HashMap<String, String>();
			while (iter.hasMoreElements()) {
				String key = iter.nextElement();
				parameters.put(key, req.getParameter(key));
			}

			// Deal with name conflicts between SsoController and the
			// SSP EarlyAlert Portlet. SsoController treats "schoolId"
			// as the *end user's* schoolId. EarlyAlert treats that
			// same param as the targeted *student's* schoolId. So
			// we replace the end user's identifier in that param, if
			// present, with the student's schoolId.
			if ( parameters.containsKey(SCHOOL_ID_PARAMETER) ) {
				parameters.remove(SCHOOL_ID_PARAMETER);
			}
			if ( parameters.containsKey(STUDENT_SCHOOL_ID_PARAMETER) ) {
				parameters.put(SCHOOL_ID_PARAMETER, parameters.get(STUDENT_SCHOOL_ID_PARAMETER));
			}

			parameters.put(ACTION_KEY_SIMPLE.toString(),
					ACTION_VALUE.toString());

			final String view = StringUtils.isBlank(inputs.getView()) ? DEFAULT_TEMPLATE : inputs.getView().trim();
			BuildUrlRequest buildRequest = new BuildUrlRequest(
					parameters, view, ssoTicket);

			final Map<String, Object> rslt = new HashMap<String, Object>();
			rslt.put(SUCCESS_FIELD, true);
			rslt.put(URL_FIELD, urlBuilder.buildUrl(buildRequest));
			return new ModelAndView("json", rslt);
		} else {
			return sendServerError(res, HttpServletResponse.SC_FORBIDDEN,
					"SSO key not configured");
		}
	}

	/**
	 * 
	 * @param inputs
	 * @return
	 */
	private String resolveToValidUsername(Inputs inputs) {
		if (inputs.hasUsername()) {
			// Hisorically we didn't validate that the user actually exists
			// before generating the ticket, but once we introduced the schoolId
			// option, we went ahead and started validating the underlying
			// user's existence in both cases for symmetry
			return resolveCanonicalUsername(inputs.getUsername());
		} else if (inputs.hasSchoolId()) {
			return resolveCanonicalUsernameFromSchoolId(inputs.getSchoolId());
		}
		return null;
	}

	private String resolveCanonicalUsername(String username) {
		final Map<String, Object> ssoPersonByUsername = ssoPersonLookupService.findSsoPersonByUsername(username);
		return ssoPersonByUsername == null ? null : (String)ssoPersonByUsername.get(SsoPersonLookupService.USERNAME_ATTRIBUTE_NAME);
	}

	private String resolveCanonicalUsernameFromSchoolId(String schoolId) {
		final List<Map<String, Object>> ssoPersons =
				ssoPersonLookupService.findSsoPerson(SCHOOL_ID_PERSONDIR_LOGICAL_ATTR_NAME, schoolId);
		if ( ssoPersons == null || ssoPersons.isEmpty() ) {
			return null;
		}
		if (ssoPersons.size() > 1) {
			throw new IllegalStateException("SchoolId search on [" + schoolId + "] matched too many users ["
					+ ssoPersons.size() + "]");
		}
		return (String) ssoPersons.get(0).get(SsoPersonLookupService.USERNAME_ATTRIBUTE_NAME);
	}

	/**
	 * Selects either {@code username} or {@code schoolId} from the given
	 * {@link Inputs}.
	 * 
	 * <p>
	 * <em>Implementation note: make sure the preference order here matches
	 * {@link #normalizeUserIdentifier(org.jasig.portal.security.sso.mvc.SsoController.Inputs)}</em>
	 * </p>
	 * 
	 * @param inputs
	 * @return
	 */
	private String getUserIdentifier(Inputs inputs) {
		return inputs.hasUsername() ? inputs.getUsername() : inputs
				.getSchoolId();
	}

	private ModelAndView sendServerError(final HttpServletResponse res,
			final int status, final String message) {
		return sendServerError(res, status, message, null);
	}

	private ModelAndView sendServerError(final HttpServletResponse res,
			final int status, final String message, final Exception cause) {
		// Unexpected server-side failure, so go ahead and fill up the logs
		if (cause != null) {
			log.error("Sending server error response {}: {}", new Object[] {
					status, message, cause });
		}
		return sendError(res, status, message);
	}

	private ModelAndView sendClientError(final HttpServletResponse res,
			final int status, final String message) {
		return sendClientError(res, status, message, null);
	}

	private ModelAndView sendClientError(final HttpServletResponse res,
			final int status, final String message, final Exception cause) {
		// This is for client errors, so only need to log at 'info'. I.e. this
		// isn't an unexpected server-side failure,
		// so filling up the logs is pointless in most cases
		if (cause != null) {
			log.info("Sending client error response {}: {}", new Object[] {
					status, message, cause });
		}
		return sendError(res, status, message);
	}

	private ModelAndView sendError(final HttpServletResponse res,
			final int status, final String message) {
		final Map<String, Object> rslt = new HashMap<String, Object>();
		res.setStatus(status);
		rslt.put(SUCCESS_FIELD, false);
		rslt.put(MESSAGE_FIELD, message);
		return new ModelAndView("json", rslt);
	}

	private boolean validateToken(Inputs input)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return validateToken(getUserIdentifier(input), input.getToken(),
				input.getTimeStamp());
	}

	private boolean validateToken(final String userIdentifier,
			final String token, final String timeStamp)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		final MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(userIdentifier.getBytes("UTF-8"));
		// Since timeStamp is optional, include in md5 calc only if present; if
		// not present, don't include
		if (null != timeStamp) {
			md.update(timeStamp.getBytes("UTF-8"));
		}
		md.update(sharedSecret.getBytes("UTF-8"));
		final byte[] hash = md.digest();
		final String md5 = new String(Hex.encode(hash));
		return md5.equalsIgnoreCase(token);
	}

	private boolean validateTimeStampRange(final String timeStamp)
			throws ParseException {
		boolean rc = true;
		TimeZone utc = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
		df.setTimeZone(utc);

		long nowMinus5Raw = System.currentTimeMillis()
				- (signedUrlToLiveMinutes * 60000);
		long nowPlus5Raw = System.currentTimeMillis()
				+ (signedUrlToLiveMinutes * 60000);

		Date ts = df.parse(timeStamp);
		long tsMillis = ts.getTime();
		if (nowMinus5Raw > tsMillis || tsMillis > nowPlus5Raw) {
			// Return false if timestamp is outside +/- 5 signedUrlToLiveMinutes
			// of current time
			rc = false;
		}
		return rc;
	}

	/*
	 * Nested Types
	 */

	private static final class Inputs {

		private final String username;
		private final String schoolId;
		private final String formattedCourse;
		private final String sectionCode;
		private final String studentSchoolId;
		private final String studentUserName;
		private final String token;
		private final String timeStamp;
		private final String termCode;
		private final String view;

		public static Inputs parse(HttpServletRequest req,
				boolean checkTimeStampRange) {

			final String username = req.getParameter(USERNAME_PARAMETER);
			final String schoolId = req.getParameter(SCHOOL_ID_PARAMETER);

			final String formattedCourse = req
					.getParameter(FORMATTED_COURSE_PARAMETER);
			final String sectionCode = req.getParameter(SECTION_CODE_PARAMETER);
			final String studentSchoolId = req
					.getParameter(STUDENT_SCHOOL_ID_PARAMETER);
			final String studentUserName = req
					.getParameter(STUDENT_USER_NAME_PARAMETER);
			final String termCode = req.getParameter(TERM_CODE_PARAMETER);
			final String token = req.getParameter(TOKEN_PARAMETER);
			final String timeStamp = req.getParameter(TIMESTAMP);
			final String view = req.getParameter(VIEW);

			return new Inputs(username, schoolId, formattedCourse, sectionCode,
					studentSchoolId, studentUserName, token, timeStamp,
					termCode, view, checkTimeStampRange);

		}

		private Inputs(final String username, final String schoolId,
				final String formattedCourse, final String sectionCode,
				final String studentSchoolId, final String studentUserName,
				final String token, final String timeStamp,
				final String termCode, final String view,
				boolean checkTimeStampRange) {

			Assert.state(username != null || schoolId != null,
					"Must specify either 'username' or 'schoolId'");
			Assert.hasText(token, "Required parameter 'token' is missing");
			if (checkTimeStampRange) {
				Assert.hasText(timeStamp,
						"Required parameter 'timeStamp' is missing");
			}

			this.username = username;
			this.schoolId = schoolId;
			this.token = token;
			this.timeStamp = timeStamp;
			this.termCode = termCode;
			this.view = view;
			// Check the incoming EA params if ea.new is indicated
			if (!(StringUtils.isBlank(this.view)) && this.view.equals(EARLY_ALERT_TEMPLATE_NEW)) {
				this.formattedCourse = formattedCourse;
				this.studentSchoolId = studentSchoolId;
				this.sectionCode = sectionCode;
				this.studentUserName = studentUserName;
				Boolean hasCourse = this.hasFormattedCourse();
				Boolean hasSec = this.hasSectionCode();
				Boolean hasCourseInformation = hasCourse || hasSec;
				Boolean hasStudentInformation = hasStudentSchoolId()
						|| hasStudentUserName();
				Assert.isTrue(hasCourseInformation,
						"Required parameter 'formattedCourse' is missing");
				Assert.isTrue(hasStudentInformation,
						"Required parameter 'studentSchoolId' is missing");
			} else {
				this.formattedCourse = null;
				this.studentSchoolId = null;
				this.sectionCode = null;
				this.studentUserName = null;
			}
		}

		public String getUsername() {
			return username;
		}

		public String getSchoolId() {
			return schoolId;
		}

		public String getFormattedCourse() {
			return formattedCourse;
		}

		public String getStudentSchoolId() {
			return studentSchoolId;
		}

		public String getSectionCode() {
			return sectionCode;
		}

		public String getStudentUserName() {
			return studentUserName;
		}

		public boolean hasUsername() {
			return StringUtils.isNotBlank(username);
		}

		public boolean hasSchoolId() {
			return StringUtils.isNotBlank(schoolId);
		}

		public boolean hasFormattedCourse() {
			return StringUtils.isNotBlank(formattedCourse);
		}

		public boolean hasStudentSchoolId() {
			return StringUtils.isNotBlank(studentSchoolId);
		}

		public boolean hasSectionCode() {
			return StringUtils.isNotBlank(sectionCode);
		}

		public boolean hasStudentUserName() {
			return StringUtils.isNotBlank(studentUserName);
		}

		public boolean hasTermCode() {
			return StringUtils.isNotBlank(termCode);
		}

		public String getTermCode() {
			return termCode;
		}

		public String getToken() {
			return token;
		}

		public String getTimeStamp() {
			return timeStamp;
		}

		public String getView() {
			return view;
		}

	}

}
