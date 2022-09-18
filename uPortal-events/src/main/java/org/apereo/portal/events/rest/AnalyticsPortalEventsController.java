package org.apereo.portal.events.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsPortalEventsController {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private AnalyticsPortalEventsService service;

	public AnalyticsPortalEventsController(AnalyticsPortalEventsService service) {
		this.service = service;
	}

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAnalytics() {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "SUCCESS");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> postAnalytics(@RequestBody Map<String, Object> analyticsData,
			HttpServletRequest request) {
		service.publishEvent(request, analyticsData);
		return new ResponseEntity<>("CREATED", HttpStatus.CREATED);
	}

	@ExceptionHandler({ IllegalArgumentException.class })
	public ResponseEntity<String> handleException(IllegalArgumentException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}
}
