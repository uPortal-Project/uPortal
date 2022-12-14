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
package org.apereo.portal.events.analytics;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.rest.utils.ErrorResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsPortalEventsController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final FastDateFormat df =
            FastDateFormat.getInstance(DATE_FORMAT, TimeZone.getTimeZone("UTC"));

    @Autowired private IAnalyticsPortalEventService service;

    public AnalyticsPortalEventsController(IAnalyticsPortalEventService service) {
        this.service = service;
    }

    @RequestMapping(value = "/level", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getAnalyticsLevel() {
        Map<String, String> response = new HashMap<>();
        response.put("level", service.getLogLevel());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> postAnalytics(
            @RequestBody Map<String, Object> analyticsData, HttpServletRequest request) {
        service.publishEvent(request, analyticsData);
        return new ResponseEntity<>(new HashMap<>(), HttpStatus.CREATED);
    }

    @PreAuthorize(
            "hasPermission('ALL', 'java.lang.String', new org.apereo.portal.spring.security.evaluator.AuthorizableActivity('UP_SYSTEM', 'ALL_PERMISSIONS'))")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<PortalEvent>> getAnalytics(
            @RequestParam(name = "startDate") String startDateStr,
            @RequestParam(name = "endDate") String endDateStr,
            @RequestParam(name = "eventType", required = false) String eventType,
            @RequestParam(name = "userId", required = false) String userId) {
        DateTime startDate = parseDateTime(startDateStr);
        // by default a new date will be midnight; this goes to the last second of the day instead
        DateTime endDate = parseDateTime(endDateStr).plusDays(1).minusSeconds(1);
        List<PortalEvent> response = service.getAnalytics(startDate, endDate, eventType, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleException(IllegalArgumentException e) {
        final ErrorResponse response = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private DateTime parseDateTime(String dateTimeStr) {
        try {
            Date date = df.parse(dateTimeStr);
            return new DateTime(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "Query Date ["
                            + dateTimeStr
                            + "] is formatted incorrectly, correct format is ["
                            + DATE_FORMAT
                            + "]");
        }
    }
}
