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
package org.jasig.portal.i18n.dao.jpa;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.i18n.Message;
import org.jasig.portal.i18n.dao.IMessageDao;
import org.jasig.portal.jpa.BaseJpaDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaMessageDao extends BaseJpaDao implements IMessageDao {
    
    private static final String FIND_MESSAGES_BY_CODE_CACHE_REGION = MessageImpl.class.getName()
            + ".query.FIND_MESSAGES_BY_CODE";
    
    private static final String FIND_MESSAGES_BY_LOCALE_CACHE_REGION = MessageImpl.class.getName()
            + ".query.FIND_MESSAGES_BY_LOCALE";
    
    private static final String FIND_MESSAGE_BY_CODE_AND_LOCALE_CACHE_REGION = MessageImpl.class.getName()
            + ".query.FIND_MESSAGE_BY_CODE_AND_LOCALE";
    
    private static final String FIND_MESSAGE_CODES_CACHE_REGION = MessageImpl.class.getName()
            + ".query.FIND_MESSAGE_CODES";
    
    private CriteriaQuery<MessageImpl> findMessageByCodeAndLocaleQuery;
    private CriteriaQuery<MessageImpl> findMessageByCodeQuery;
    private CriteriaQuery<MessageImpl> findMessageByLocaleQuery;
    private CriteriaQuery<String> findCodes;
    
    private ParameterExpression<String> codeParameter;
    private ParameterExpression<Locale> localeParameter;
    private EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Override
    protected void buildCriteriaQueries(CriteriaBuilder criteriaBuilder) {
        this.codeParameter = criteriaBuilder.parameter(String.class, "code");
        this.localeParameter = criteriaBuilder.parameter(Locale.class, "locale");
        
        this.initFindMessageByCodeAndLocaleQuery(criteriaBuilder);
        this.initFindMessageByCodeQuery(criteriaBuilder);
        this.initFindMessageByLocaleQuery(criteriaBuilder);
        this.initFindCodes(criteriaBuilder);
    }
    
    protected void initFindMessageByCodeAndLocaleQuery(CriteriaBuilder cb) {
        final CriteriaQuery<MessageImpl> criteriaQuery = cb.createQuery(MessageImpl.class);
        final Root<MessageImpl> root = criteriaQuery.from(MessageImpl.class);
        criteriaQuery.select(root);
        criteriaQuery.where(cb.and(cb.equal(root.get(MessageImpl_.code), this.codeParameter),
                                   cb.equal(root.get(MessageImpl_.locale), this.localeParameter)));
        criteriaQuery.orderBy(cb.asc(root.get(MessageImpl_.code)), cb.asc(root.get(MessageImpl_.locale)));
        
        this.findMessageByCodeAndLocaleQuery = criteriaQuery;
    }
    
    protected void initFindMessageByCodeQuery(CriteriaBuilder criteriaBuilder) {
        final CriteriaQuery<MessageImpl> criteriaQuery = criteriaBuilder.createQuery(MessageImpl.class);
        final Root<MessageImpl> root = criteriaQuery.from(MessageImpl.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get(MessageImpl_.code), this.codeParameter));
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(MessageImpl_.locale)));
        
        this.findMessageByCodeQuery = criteriaQuery;
    }
    
    protected void initFindMessageByLocaleQuery(CriteriaBuilder criteriaBuilder) {
        final CriteriaQuery<MessageImpl> criteriaQuery = criteriaBuilder.createQuery(MessageImpl.class);
        final Root<MessageImpl> root = criteriaQuery.from(MessageImpl.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get(MessageImpl_.locale), this.localeParameter));
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(MessageImpl_.code)));
        
        this.findMessageByLocaleQuery = criteriaQuery;
    }
    
    protected void initFindCodes(CriteriaBuilder criteriaBuilder) {
        final CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
        final Root<MessageImpl> root = criteriaQuery.from(MessageImpl.class);
        criteriaQuery.select(root.get(MessageImpl_.code));
        criteriaQuery.groupBy(root.get(MessageImpl_.code));
        this.findCodes = criteriaQuery;
    }
    
    @Override
    @Transactional
    public Message createMessage(String code, Locale locale, String value) {
        Validate.notNull(code, "code can not be null");
        
        final Message msg = new MessageImpl(code, locale, value);
        
        this.entityManager.persist(msg);
        
        return msg;
    }
    
    @Override
    @Transactional
    public Message updateMessage(Message message) {
        Validate.notNull(message, "message can not be null");
        
        this.entityManager.persist(message);
        
        return message;
    }
    
    @Override
    @Transactional
    public void deleteMessage(Message message) {
        Validate.notNull(message, "message can not be null");
        
        final Message msg;
        if (this.entityManager.contains(message)) {
            msg = message;
        } else {
            msg = this.entityManager.merge(message);
        }
        this.entityManager.remove(msg);
    }
    
    @Override
    public Message getMessage(String code, Locale locale) {
        final TypedQuery<MessageImpl> query = createQuery(findMessageByCodeAndLocaleQuery,
                                                      FIND_MESSAGE_BY_CODE_AND_LOCALE_CACHE_REGION);
        query.setParameter(this.codeParameter, code);
        query.setParameter(this.localeParameter, locale);
        
        final List<MessageImpl> messages = query.getResultList();
        return DataAccessUtils.uniqueResult(messages);
    }
    
    @Override
    public Set<Message> getMessagesByLocale(Locale locale) {
        final TypedQuery<MessageImpl> query = createQuery(findMessageByLocaleQuery, FIND_MESSAGES_BY_LOCALE_CACHE_REGION);
        query.setParameter(localeParameter, locale);
        final List<MessageImpl> messages = query.getResultList();
        return new LinkedHashSet<Message>(messages);
    }
    
    @Override
    public Set<Message> getMessagesByCode(String code) {
        final TypedQuery<MessageImpl> query = createQuery(findMessageByCodeQuery, FIND_MESSAGES_BY_CODE_CACHE_REGION);
        query.setParameter(codeParameter, code);
        final List<MessageImpl> messages = query.getResultList();
        return new LinkedHashSet<Message>(messages);
    }
    
    @Override
    public Set<String> getCodes() {
        final TypedQuery<String> query = createQuery(findCodes, FIND_MESSAGE_CODES_CACHE_REGION);
        final List<String> codes = query.getResultList();
        return new LinkedHashSet<String>(codes);
    }
}
