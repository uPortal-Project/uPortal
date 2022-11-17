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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.rest.utils.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsPortalEventsController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired private IAnalyticsPortalEventService service;

    public AnalyticsPortalEventsController(AnalyticsPortalEventsService service) {
        this.service = service;
    }

    @RequestMapping(value = "/level", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getAnalyticsLevel() {
        Map<String, String> response = new HashMap<>();
        response.put("level", service.getLogLevel());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_UTF8_VALUE,
                MediaType.TEXT_PLAIN_VALUE
            })
    public ResponseEntity postAnalytics(
            @RequestBody String analyticsData, HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map =
                    objectMapper.readValue(
                            analyticsData, new TypeReference<Map<String, Object>>() {});
            service.publishEvent(request, map);
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.warn("Failed to parse analytics data: " + e.getMessage());
            final ErrorResponse response =
                    new ErrorResponse(
                            "Post data was not in a JSON format, or the required attributes were not present.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleException(IllegalArgumentException e) {
        final ErrorResponse response = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
