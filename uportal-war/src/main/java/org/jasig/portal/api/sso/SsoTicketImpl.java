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
package org.jasig.portal.api.sso;

import java.util.Calendar;

public class SsoTicketImpl implements SsoTicket {

	private String uuid;
	private String username;
	private Calendar creationDate;

	@Override
	public String getUuid() {
		return uuid;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getUsername() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public Calendar getCreationDate() {
		return creationDate;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

}
