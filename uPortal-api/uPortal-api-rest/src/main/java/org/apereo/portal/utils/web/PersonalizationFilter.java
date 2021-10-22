/*
 Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information regarding copyright ownership. Apereo
 licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License. You may obtain a copy of the License at the
 following location:

 <p>http://www.apache.org/licenses/LICENSE-2.0

 <p>Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied. See the License for the specific language governing permissions and
 limitations under the License.
*/
package org.apereo.portal.utils.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.utils.personalize.IPersonalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

/**
 * Personalize every /api GET request that returns JSON
 *
 * <p>See {@link IPersonalizer for further details}
 */
@Slf4j
public class PersonalizationFilter implements Filter {
    @Value("${org.apereo.portal.utils.web.PersonalizationFilter.enable:false}")
    private boolean enableFilter;

    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private IPersonalizer personalizer;

    @Autowired
    public void setPersonalizer(IPersonalizer personalizer) {
        this.personalizer = personalizer;
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
            throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) resp;
        log.debug("In PersonalizationFilter after the filterChain");

        if (!enableFilter) {
            log.debug("PersonalizationFilter is disabled - skipping.");
            filterChain.doFilter(req, resp);
            return;
        }

        if (!request.getMethod().equals(HttpMethod.GET.name())) {
            log.debug(
                    "Not a GET request - skipping the filter. Request URL: [{}] {}",
                    request.getMethod(),
                    request.getRequestURI());
            filterChain.doFilter(req, resp);
            return;
        }

        // Capture the response
        SimpleCharacterResponseWrapper wrapper = new SimpleCharacterResponseWrapper(response);

        filterChain.doFilter(req, wrapper);

        PrintWriter responseWriter = response.getWriter();

        if ((wrapper != null) && (wrapper.getContentType() != null) && wrapper.getContentType().contains("application/json")) {
            final IPerson person = this.personManager.getPerson(request);
            if (person == null) {
                log.warn(
                        "Person not found in Person Manager.  Not applying the personalization filter. Request URL: [{}] {}",
                        request.getMethod(),
                        request.getRequestURI());
                writeToResponse(response, responseWriter, wrapper.toString());
                return;
            }

            String originalContent = wrapper.toString();
            if (originalContent == null) {
                log.debug(
                        "Original content is null. Not applying the personalization filter. Request URL: [{}] {}",
                        request.getMethod(),
                        request.getRequestURI());
                return;
            }

            final String personalizedContent =
                    personalizer.personalize(person, originalContent, request.getSession());
            if (originalContent.equals(personalizedContent)) {
                log.debug(
                        "No personalization made to the content. Request URL: [{}] {}",
                        request.getMethod(),
                        request.getRequestURI());
            } else {
                log.debug(
                        "Personalized the content! Request URL: [{}] {}",
                        request.getMethod(),
                        request.getRequestURI());
            }
            writeToResponse(response, responseWriter, personalizedContent);
        } else {
            log.info(
                    "REST API response is not JSON - not applying the personalization filter. Request URL: [{}] {}",
                    request.getMethod(),
                    request.getRequestURI());
        }
        log.debug("Finished with PersonalizationFilter");
    }

    private void writeToResponse(
            HttpServletResponse response, PrintWriter responseWriter, String text) {
        response.setContentLength(text.length());
        responseWriter.write(text);
    }
}
