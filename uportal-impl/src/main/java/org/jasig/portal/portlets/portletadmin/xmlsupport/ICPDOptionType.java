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

package org.jasig.portal.portlets.portletadmin.xmlsupport;

public interface ICPDOptionType {

	public String getBase();

	public void setBase(String base);

	public String getInput();

	public void setInput(String input);

	public String getDisplay();

	public void setDisplay(String display);

	public String getLength();

	public void setLength(String length);

	public String getMaxlength();

	public void setMaxlength(String maxlength);

	public ICPDOptionTypeRestriction getRestriction();

}
