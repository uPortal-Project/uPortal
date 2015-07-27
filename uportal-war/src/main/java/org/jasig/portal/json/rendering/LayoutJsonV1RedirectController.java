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
package org.jasig.portal.json.rendering;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.rest.layout.LayoutJsonV1RenderingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Eric Dalquist
 */
@Controller
public class LayoutJsonV1RedirectController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Redirect the old endpoint to the new endpoint.
    @RequestMapping(value="/layout.json", method = RequestMethod.GET)
    public String v1RenderRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return ("redirect:/api" + LayoutJsonV1RenderingController.URL);
    }

}
