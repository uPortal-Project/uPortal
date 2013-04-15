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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.security.sso.ISsoTicket;
import org.jasig.portal.security.sso.ISsoTicketDao;
import org.jasig.portal.security.sso.RemoteUserFilterBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/sso")
public final class SsoController {

    private static final String USERNAME_PARAMETER = "username";
    private static final String FORMATTED_COURSE_PARAMETER = "formattedCourse";
    private static final String STUDENT_SCHOOL_ID_PARAMETER = "studentSchoolId";
    private static final String TOKEN_PARAMETER = "token";
    private static final String TIMESTAMP = "timeStamp";
    private static final String VIEW = "view";

    // For building the refUrl
    private static final String EA_PORTLET_PATH = "/p/early-alert";
    private static final Object ACTION_KEY = "pP_action";
    private static final Object ACTION_VALUE = "enterAlert";
    private static final Object SCHOOL_ID_KEY = "pP_schoolId";
    private static final Object FORMATTED_COURSE_ID_KEY = "pP_formattedCourse";
    private static final String EA_VIEW = "ea.new";

    // For building the loginUrl
    private static final String LOGIN_SERVLET_PATH = "/Login";

    private static final String SUCCESS_FIELD = "success";
    private static final String URL_FIELD = "URL";
    private static final String MESSAGE_FIELD = "message";

    @Value("${org.jasig.portal.security.sso.mvc.SsoController.requireSecure}")
    private boolean requireSecure = true;  // default

    @Value("${org.jasig.portal.security.sso.mvc.SsoController.sharedSecret}")
    private String sharedSecret = null;

    @Value("${org.jasig.portal.security.sso.mvc.SsoController.checkTimeStampRange}")
    private boolean checkTimeStampRange = true;  //default

    @Value("${org.jasig.portal.security.sso.mvc.SsoController.signedUrlToLiveMinutes}")
    private int signedUrlToLiveMinutes = 5;  //default

    @Autowired
    private ISsoTicketDao ticketDao;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping
    public ModelAndView issueUrl(HttpServletRequest req, HttpServletResponse res) throws IOException {

        if(null != sharedSecret && !("".equals(sharedSecret))){

            // Verify secure connection if we must have one
            if (requireSecure && !req.isSecure()) {
                return sendError(res, HttpServletResponse.SC_FORBIDDEN,
                    "The SSO handshake requires a secure connection (SSL)");
            }

            // Inputs
            Inputs inputs = null;
            try {
                inputs = Inputs.parse(req);
            } catch (Exception e) {
                return sendError(res, HttpServletResponse.SC_BAD_REQUEST,
                    "One or more required inputs was not specified");
            }

            // Verify the request is authorized
            try {
                if (!validateToken(inputs.getUsername(), inputs.getToken(), inputs.getTimeStamp())) {
                    return sendError(res, HttpServletResponse.SC_FORBIDDEN, "Not authorized");
                }
            } catch (Exception e) {
                return sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authorization check error");
            }

            //Verify if TimeStamp range needs to be checked
            if(checkTimeStampRange){
                try {
                    if (!validateTimeStampRange(inputs.getTimeStamp())) {
                        return sendError(res, HttpServletResponse.SC_FORBIDDEN, "Timestamp out of range");
                    }
                } catch (Exception e) {
                    return sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authorization check error");
                }
            }

            // Generate a ticket
            log.info("Generating SSO ticket for user '{}'", inputs.getUsername());
            ISsoTicket ticket = ticketDao.issueTicket(inputs.getUsername());

            // refUrl (redirect destination after login)
            final StringBuilder redirect = new StringBuilder();
            if(null != inputs.getView() && EA_VIEW.equals(inputs.getView())){
                redirect.append(req.getContextPath()).append(EA_PORTLET_PATH)
                .append("?").append(ACTION_KEY).append("=").append(ACTION_VALUE)
                .append("&").append(SCHOOL_ID_KEY).append("=").append(URLEncoder.encode(inputs.getStudentSchoolId(), "UTF-8"))
                .append("&").append(FORMATTED_COURSE_ID_KEY).append("=").append(URLEncoder.encode(inputs.getFormattedCourse(), "UTF-8"));
            }else{
                redirect.append(req.getContextPath());
            }

            // loginUrl
            final URL contextUrl = new URL(req.getRequestURL().toString());
            final URL loginUrl = new URL(contextUrl, req.getContextPath() + LOGIN_SERVLET_PATH);
            final StringBuilder login = new StringBuilder();
            login.append(loginUrl.toExternalForm())
                .append("?").append(RemoteUserFilterBean.TICKET_PARAMETER).append("=").append(ticket.getUuid())
                .append("&").append(LoginController.REFERER_URL_PARAM).append("=").append(URLEncoder.encode(redirect.toString(), "UTF-8"));

            final Map<String,Object> rslt = new HashMap<String,Object>();
            rslt.put(SUCCESS_FIELD, true);
            rslt.put(URL_FIELD, login.toString());
            return new ModelAndView("json", rslt);
        }else{
            return sendError(res, HttpServletResponse.SC_FORBIDDEN, "SSO key not configured");
        }
    }

    /*
     * Implementation
     */

    private ModelAndView sendError(final HttpServletResponse res, final int status, final String message) {
        final Map<String,Object> rslt = new HashMap<String,Object>();
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        rslt.put(SUCCESS_FIELD, false);
        rslt.put(MESSAGE_FIELD, message);
        return new ModelAndView("json", rslt);
    }

    private boolean validateToken(final String username, final String token,
        final String timeStamp) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(username.getBytes("UTF-8"));
        md.update(timeStamp.getBytes("UTF-8"));
        md.update(sharedSecret.getBytes("UTF-8"));
        final byte[] hash = md.digest();
        final String md5 = new String(Hex.encode(hash));
        return md5.equalsIgnoreCase(token);
    }

    private boolean validateTimeStampRange(final String timeStamp){
        boolean rc = true;
        TimeZone utc = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
        df.setTimeZone(utc);

        long nowMinus5Raw = System.currentTimeMillis() - (signedUrlToLiveMinutes * 60000);
        long nowPlus5Raw = System.currentTimeMillis() + (signedUrlToLiveMinutes * 60000);

        Date ts = null;

        try{
            ts = df.parse(timeStamp);
            long tsMillis = ts.getTime();
            if(nowMinus5Raw > tsMillis ||
                tsMillis > nowPlus5Raw){
                //Return false if timestamp is outside +/- 5 signedUrlToLiveMinutes of current time
                rc = false;
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        return rc;
    }

    /*
     * Nested Types
     */

    private static final class Inputs {

        private final String username;
        private final String formattedCourse;
        private final String studentSchoolId;
        private final String token;
        private final String timeStamp;
        private final String view;

        public static Inputs parse(HttpServletRequest req) {

            final String username = req.getParameter(USERNAME_PARAMETER);
            final String formattedCourse = req.getParameter(FORMATTED_COURSE_PARAMETER);
            final String studentSchoolId = req.getParameter(STUDENT_SCHOOL_ID_PARAMETER);
            final String token = req.getParameter(TOKEN_PARAMETER);
            final String timeStamp = req.getParameter(TIMESTAMP);
            final String view = req.getParameter(VIEW);

            return new Inputs(username, formattedCourse, studentSchoolId,
                    token, timeStamp, view);

        }

        private Inputs(final String username, final String formattedCourse,
                final String studentSchoolId, final String token, final String timeStamp, final String view) {

            Assert.hasText(username, "Required parameter 'username' is missing");
            Assert.hasText(token, "Required parameter 'token' is missing");
            Assert.hasText(timeStamp, "Required parameter 'timeStamp' is missing");

            this.username = username;
            this.token = token;
            this.timeStamp = timeStamp;
            this.view = view;
            //Check the incoming EA params iff ea.new is indicated
            if(null != this.view && this.view.equals(EA_VIEW)){
                Assert.hasText(formattedCourse, "Required parameter 'formattedCourse' is missing");
                Assert.hasText(studentSchoolId, "Required parameter 'studentSchoolId' is missing");
                this.formattedCourse = formattedCourse;
                this.studentSchoolId = studentSchoolId;
            }else{
                this.formattedCourse = null;
                this.studentSchoolId = null;
            }
        }

        public String getUsername() {
            return username;
        }

        public String getFormattedCourse() {
            return formattedCourse;
        }

        public String getStudentSchoolId() {
            return studentSchoolId;
        }

        public String getToken() {
            return token;
        }

        public String getTimeStamp() {
            return timeStamp;
        }
        public String getView(){
            return view;
        }

    }

}
