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
package org.apereo.portal.i18n.dao.jpa;

import com.google.common.base.Function;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.Validate;
import org.apereo.portal.i18n.Message;
import org.apereo.portal.i18n.dao.IMessageDao;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

@Repository
public class JpaMessageDao extends BasePortalJpaDao implements IMessageDao {
    private CriteriaQuery<MessageImpl> findMessageByCodeAndLocaleQuery;
    private CriteriaQuery<MessageImpl> findMessageByCodeQuery;
    private CriteriaQuery<MessageImpl> findMessageByLocaleQuery;
    private CriteriaQuery<String> findCodes;

    private ParameterExpression<String> codeParameter;
    private ParameterExpression<Locale> localeParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.codeParameter = this.createParameterExpression(String.class, "code");
        this.localeParameter = this.createParameterExpression(Locale.class, "locale");

        this.findMessageByCodeAndLocaleQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<MessageImpl>>() {
                            @Override
                            public CriteriaQuery<MessageImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<MessageImpl> criteriaQuery =
                                        cb.createQuery(MessageImpl.class);
                                final Root<MessageImpl> root =
                                        criteriaQuery.from(MessageImpl.class);
                                criteriaQuery.select(root);
                                criteriaQuery.where(
                                        cb.and(
                                                cb.equal(
                                                        root.get(MessageImpl_.code), codeParameter),
                                                cb.equal(
                                                        root.get(MessageImpl_.locale),
                                                        localeParameter)));
                                criteriaQuery.orderBy(
                                        cb.asc(root.get(MessageImpl_.code)),
                                        cb.asc(root.get(MessageImpl_.locale)));

                                return criteriaQuery;
                            }
                        });

        this.findMessageByCodeQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<MessageImpl>>() {
                            @Override
                            public CriteriaQuery<MessageImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<MessageImpl> criteriaQuery =
                                        cb.createQuery(MessageImpl.class);
                                final Root<MessageImpl> root =
                                        criteriaQuery.from(MessageImpl.class);
                                criteriaQuery.select(root);
                                criteriaQuery.where(
                                        cb.equal(root.get(MessageImpl_.code), codeParameter));
                                criteriaQuery.orderBy(cb.asc(root.get(MessageImpl_.locale)));

                                return criteriaQuery;
                            }
                        });

        this.findMessageByLocaleQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<MessageImpl>>() {
                            @Override
                            public CriteriaQuery<MessageImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<MessageImpl> criteriaQuery =
                                        cb.createQuery(MessageImpl.class);
                                final Root<MessageImpl> root =
                                        criteriaQuery.from(MessageImpl.class);
                                criteriaQuery.select(root);
                                criteriaQuery.where(
                                        cb.equal(root.get(MessageImpl_.locale), localeParameter));
                                criteriaQuery.orderBy(cb.asc(root.get(MessageImpl_.code)));

                                return criteriaQuery;
                            }
                        });

        this.findCodes =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<String>>() {
                            @Override
                            public CriteriaQuery<String> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<String> criteriaQuery =
                                        cb.createQuery(String.class);
                                final Root<MessageImpl> root =
                                        criteriaQuery.from(MessageImpl.class);
                                criteriaQuery.select(root.get(MessageImpl_.code));
                                criteriaQuery.groupBy(root.get(MessageImpl_.code));
                                return criteriaQuery;
                            }
                        });
    }

    @Override
    @PortalTransactional
    public Message createMessage(String code, Locale locale, String value) {
        Validate.notNull(code, "code can not be null");

        final Message msg = new MessageImpl(code, locale, value);

        this.getEntityManager().persist(msg);

        return msg;
    }

    @Override
    @PortalTransactional
    public Message updateMessage(Message message) {
        Validate.notNull(message, "message can not be null");

        this.getEntityManager().persist(message);

        return message;
    }

    @Override
    @PortalTransactional
    public void deleteMessage(Message message) {
        Validate.notNull(message, "message can not be null");

        final Message msg;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(message)) {
            msg = message;
        } else {
            msg = entityManager.merge(message);
        }
        entityManager.remove(msg);
    }

    @Override
    public Message getMessage(String code, Locale locale) {
        final TypedQuery<MessageImpl> query = createCachedQuery(findMessageByCodeAndLocaleQuery);
        query.setParameter(this.codeParameter, code);
        query.setParameter(this.localeParameter, locale);

        final List<MessageImpl> messages = query.getResultList();
        return DataAccessUtils.uniqueResult(messages);
    }

    @Override
    public Set<Message> getMessagesByLocale(Locale locale) {
        final TypedQuery<MessageImpl> query = createCachedQuery(findMessageByLocaleQuery);
        query.setParameter(localeParameter, locale);
        final List<MessageImpl> messages = query.getResultList();
        return new LinkedHashSet<Message>(messages);
    }

    @Override
    public Set<Message> getMessagesByCode(String code) {
        final TypedQuery<MessageImpl> query = createCachedQuery(findMessageByCodeQuery);
        query.setParameter(codeParameter, code);
        final List<MessageImpl> messages = query.getResultList();
        return new LinkedHashSet<Message>(messages);
    }

    @Override
    public Set<String> getCodes() {
        final TypedQuery<String> query = createCachedQuery(findCodes);
        final List<String> codes = query.getResultList();
        return new LinkedHashSet<String>(codes);
    }
}
