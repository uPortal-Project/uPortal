package org.jasig.portal.i18n;

import java.text.MessageFormat;
import java.util.Locale;

import org.jasig.portal.i18n.dao.IMessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;

/**
 * <p>
 * DB based {@link MessageSource}. This is used for enabling i18n on database entities that does not
 * support it directly. This message source is more convinient for tranlation of such entities
 * because of the same dynamic nature - tab titles, group names, and other entities can be changed
 * very frequently, hence it does not make sense to edit message files to follow these changes
 * (althought they are reloaded on regular basis).
 * </p>
 * <p>
 * Note that this message source should be used in conjunction with file-based message sources, who
 * provide translations for messages in other files (like XSL). It can be achieved by setting this
 * message source as parent to resource bundle message source, thus primary source would be resource
 * bundle, but if message could not be found there, this implementation would be used to look up for
 * a message. Of course, it can be set up the other way round - if resource bundle message source
 * will be set as parent to this message source, then this message source will become the primary
 * one.
 * </p>
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
public class DatabaseMessageSource extends AbstractMessageSource implements MessageSource {
    
    private IMessageDao messageDao;
    
    @Autowired
    public void setMessageDao(IMessageDao messageDao) {
        this.messageDao = messageDao;
    }
    
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        if (locale == null) {
            return null;
        }
        
        Message message = messageDao.getMessage(code, locale.toString());
        return message != null ? new MessageFormat(message.getValue(), locale) : null;
    }
    
    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        if (locale == null) {
            return null;
        }
        
        Message message = messageDao.getMessage(code, locale.toString());
        return message != null ? message.getValue() : null;
    }
}
