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

package org.apereo.portal.rest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by stalele on 6/21/17.
 */
public class AjaxSuccessControllerTest {
    //final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
   // final HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

    @InjectMocks
    private AjaxSuccessController ajaxSuccessController;

    @Before
    public void setup() throws Exception {
        ajaxSuccessController = new AjaxSuccessController();
    }

    @Test
    public void testSendJsonSuccess() {

        String body = ajaxSuccessController.sendJsonSuccess(null,null);
        String expectedResponse = "{ 'success': 'true' }";
        assertEquals(expectedResponse,body);
    }
}
