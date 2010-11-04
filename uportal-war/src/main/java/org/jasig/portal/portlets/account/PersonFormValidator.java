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

package org.jasig.portal.portlets.account;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

public class PersonFormValidator {

    private ILocalAccountDao accountDao;

    @Autowired(required = true)
    public void setLocalAccountDao(ILocalAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void validateEditLocalAccount(PersonForm person, MessageContext context) {
        
        // ensure that this username isn't already taken
        if (person.getId() < 0 && accountDao.getPerson(person.getUsername()) != null) {
            context.addMessage(new MessageBuilder().error().source("username")
                    .code("username.in.use").build());
        } 
        
        if (StringUtils.isNotBlank(person.getPassword()) || StringUtils.isNotBlank(person.getConfirmPassword())) {
            if (person.getPassword() == null || !person.getPassword().equals(person.getConfirmPassword())) {
                context.addMessage(new MessageBuilder().error().source("password")
                        .code("passwords.must.match").build());
            }
        }
        
    }
    
}
