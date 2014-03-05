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

package org.jasig.portal.portlets.dynamicskin;

/**
 * DTO for the skin preferences values.  TODO refactor the CONFIG interface to
 * take arbitrary preferences as LESS variables.
 *
 * @since 4.1.0
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public class SkinPreferencesDto {

    private String color1;
    private String color2;
    private String color3;

    public String getColor1() {
        return color1;
    }

    public void setColor1(String color1) {
        this.color1 = color1;
    }

    public String getColor2() {
        return color2;
    }

    public void setColor2(String color2) {
        this.color2 = color2;
    }

    public String getColor3() {
        return color3;
    }

    public void setColor3(String color3) {
        this.color3 = color3;
    }

    @Override
    public String toString() {
        return "SkinPreferencesDto [color1=" + color1 + ", color2=" + color2
                + ", color3=" + color3 + "]";
    }

}
