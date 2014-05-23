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

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.security.sso.ISsoTicket;
import org.jasig.portal.security.sso.ISsoTicketDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Calendar;

/**
 * See design notes in {@link SsoTicketService}, esp re the {@code secret}
 * field/arg.
 */
@Service
public class ApiSsoTicketService implements SsoTicketService, InitializingBean {

	@Autowired
	private ISsoTicketDao ssoTicketDao;

	@Value("${org.jasig.portal.api.sso.ApiSsoTicketService.secret}")
	private String secret;

	private int secretHash;

	private boolean isSecretSet;

	@Override
	public void afterPropertiesSet() throws Exception {
		if ( StringUtils.isBlank(secret) ) {
			isSecretSet = false;
		} else {
			isSecretSet = true;
			secretHash = secret.hashCode();
		}
		secret = null;
	}

	@Override
	public SsoTicket issueTicket(String username, String secret) {
		if ( !(isSecretSet) ) {
			throw new SecurityException("Must configure a secret");
		}
		if ( StringUtils.isBlank(secret) ) {
			throw new SecurityException("Must specify a secret");
		}
		if ( !(secretHash == secret.hashCode()) ) {
			throw new SecurityException("Mismatched keys");
		}
		return asDto(ssoTicketDao.issueTicket(username));
	}

	private SsoTicket asDto(ISsoTicket entity) {
		SsoTicketImpl dto = new SsoTicketImpl();
		dto.setUuid(entity.getUuid());
		dto.setUsername(entity.getUsername());
		dto.setCreationDate((Calendar)entity.getCreationDate().clone());
		return dto;
	}
}
