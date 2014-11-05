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
package org.jasig.portal.security.sso;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "UP_SSO_TICKET")
@SequenceGenerator(
        name="UP_SSO_TICKET_GEN",
        sequenceName="UP_SSO_TICKET_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_SSO_TICKET_GEN",
        pkColumnValue="UP_SSO_TICKET",
        allocationSize=5
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class JpaSsoTicket implements ISsoTicket, Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_SSO_TICKET_GEN")
    @Column(name="TICKET_ID")
    private final long id;

    @NaturalId
    @Column(name = "UUID", updatable = false, nullable = false)
    private final String uuid;

    @Column(name = "USER_NAME", updatable = false, nullable = false, length = 50)
    private final String username;

    @Column(name = "CREATION_DATE", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar creationDate;

    public JpaSsoTicket() {
        this.id = -1;
        this.uuid = "";
        this.username = "";
    }

    public JpaSsoTicket(String uuid, String username) {
        this.id = -1;
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Calendar getCreationDate() {
        return creationDate;
    }

    @PrePersist
    @SuppressWarnings("unused")
    private void setCreationDate() {
        this.creationDate =  new GregorianCalendar();
    }

}
