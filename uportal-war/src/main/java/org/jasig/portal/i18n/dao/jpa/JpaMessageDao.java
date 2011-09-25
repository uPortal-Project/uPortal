package org.jasig.portal.i18n.dao.jpa;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.i18n.Message;
import org.jasig.portal.i18n.dao.IMessageDao;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaMessageDao extends BasePortalJpaDao implements IMessageDao {
    
    private static final String FIND_MESSAGES_BY_CODE_CACHE_REGION = MessageImpl.class.getName()
            + ".query.FIND_MESSAGES_BY_CODE";
    
    private static final String FIND_MESSAGES_BY_LOCALE_CACHE_REGION = MessageImpl.class.getName()
            + ".query.FIND_MESSAGES_BY_LOCALE";
    
    private static final String FIND_MESSAGE_BY_CODE_AND_LOCALE_CACHE_REGION = MessageImpl.class.getName()
            + ".query.FIND_MESSAGE_BY_CODE_AND_LOCALE";
    
    private CriteriaQuery<MessageImpl> findMessageByCodeQuery;
    
    private CriteriaQuery<MessageImpl> findMessageByLocaleQuery;
    
    private ParameterExpression<String> codeParameter;
    
    private ParameterExpression<Locale> localeParameter;
    
    @Override
    protected void buildCriteriaQueries(CriteriaBuilder criteriaBuilder) {
        this.codeParameter = criteriaBuilder.parameter(String.class, "code");
        this.localeParameter = criteriaBuilder.parameter(Locale.class, "locale");
        
        this.initFindMessageByCodeQuery(criteriaBuilder);
        this.initFindMessageByLocaleQuery(criteriaBuilder);
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
        final NaturalIdQueryBuilder<MessageImpl> naturalIdQuery = this.createNaturalIdQuery(MessageImpl.class, FIND_MESSAGE_BY_CODE_AND_LOCALE_CACHE_REGION);
        naturalIdQuery.setNaturalIdParam(MessageImpl_.code, code);
        naturalIdQuery.setNaturalIdParam(MessageImpl_.locale, locale);
        
        return naturalIdQuery.execute();
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
}
