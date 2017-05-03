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
package org.apereo.portal.portlet.marketplace;

import java.util.ArrayList;
import java.util.List;

public class ScreenShot {
    private String url;
    private List<String> captions;

    public ScreenShot(String url) {
        this.setUrl(url);
        this.setCaptions(new ArrayList<String>());
    }

    public ScreenShot(String url, List<String> captions) {
        this.setUrl(url);
        this.setCaptions(captions);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * @return the captions for a screen shot. Will not return null, might return empty list.
     */
    public List<String> getCaptions() {
        if (captions == null) {
            this.captions = new ArrayList<String>();
        }
        return captions;
    }

    private void setCaptions(List<String> captions) {
        this.captions = captions;
    }
}
