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

package org.jasig.portal.security.sso;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("jpaSsoTicketDao")
public class JpaSsoTicketDao implements ISsoTicketDao {

    private static final long MILLIS_IN_ONE_MINUTE = 1000L * 60L;
    private static final String GET_TICKET_JPQL = "SELECT t FROM JpaSsoTicket t WHERE t.uuid = :uuid";
    private static final String GET_EXPIRED_TICKETS_JPQL = "SELECT t FROM JpaSsoTicket t WHERE t.creationDate < :expiration";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${org.jasig.portal.security.sso.JpaSsoTicketDao.timeToLiveMinutes}")
    private int timeToLiveMinutes = 5;  // default

    private EntityManager entityManager;

    @PersistenceContext(unitName = "PortalDb")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ISsoTicket issueTicket(String username) {
        final String uuid = UUID.randomUUID().toString();
        final JpaSsoTicket rslt = new JpaSsoTicket(uuid, username);
        entityManager.persist(rslt);
        return rslt;
    }

    @Override
    @Transactional
    public String redeemTicketForUsername(String uuid) {
        final ISsoTicket ticket = getTicket(uuid);
        if ( ticket == null ) {
            return null;
        }
        final String username = ticket.getUsername();
        entityManager.remove(ticket);
        return username;
    }

    private ISsoTicket getTicket(String uuid) {
        ISsoTicket rslt = null;  // default
        final Query query = entityManager.createQuery(GET_TICKET_JPQL);
        query.setParameter("uuid", uuid);
        @SuppressWarnings("unchecked")
        ISsoTicket ticket = (ISsoTicket) DataAccessUtils.uniqueResult(query.getResultList());
        if (ticket != null) {
            long expires = ticket.getCreationDate().getTimeInMillis() + (MILLIS_IN_ONE_MINUTE * (long) timeToLiveMinutes);
            if (System.currentTimeMillis() < expires) {
                rslt = ticket;
            }
        }
        return rslt;
    }

    @Override
    @Transactional
    public void purgeExpiredSsoTickets() {
        log.info("Pruning expired SSO tickets");
        final Query query = entityManager.createQuery(GET_EXPIRED_TICKETS_JPQL);
        long time = System.currentTimeMillis() - (MILLIS_IN_ONE_MINUTE * (long) timeToLiveMinutes);
        Calendar expiration = GregorianCalendar.getInstance();
        expiration.setTimeInMillis(time);
        query.setParameter("expiration", expiration);
        @SuppressWarnings("unchecked")
        List<ISsoTicket> expiredTickets = query.getResultList();
        for (ISsoTicket ticket : expiredTickets) {
            entityManager.remove(ticket);
        }
    }

}
