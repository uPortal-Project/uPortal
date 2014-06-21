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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;


@Component("emailPasswordResetNotificationImpl")
public class EmailPasswordResetNotificationImpl implements IPasswordResetNotification {
    protected final Log log = LogFactory.getLog(getClass());

    private String i18nSubject = "reset.your.password";
    private JavaMailSender mailSender;
    private MessageSource messageSource;
    private String portalEmailAddress;
    private String templateDir = "properties/templates";
    private String templateName = "passwordReset";


    /**
     * Set the subject string.  Note that this is the i18n key, not a raw
     * text string.  eg. reset.your.password
     *
     * @param i18nSubject
     */
    public void setI18nSubject(String i18nSubject) {
        this.i18nSubject = i18nSubject;
    }


    @Autowired
    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }


    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @Resource(name="portalEmailAddress")
    public void setPortalEmailAddress(String portalEmailAddress) {
        this.portalEmailAddress = portalEmailAddress;
    }


    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }


    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }


    @Override
    public void sendNotification(URL resetUrl, ILocalAccountPerson account, Locale locale) {
        log.debug("Sending password reset instructions to user with url " + resetUrl.toString());

        List<String> recipients = getRecipientAddresses(account);
        String subject = formatSubject(account, locale);
        String body = formatBody(resetUrl, account, locale);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            for (String recipient : recipients) {
                helper.addTo(recipient);
            }

            helper.setText(body, true);
            helper.setSubject(subject);
            helper.setFrom(portalEmailAddress, messageSource.getMessage("portal.name", new Object[]{}, locale));

            log.debug("Sending message to " + recipients + " from " +
                    Arrays.toString(message.getFrom()) +  " subject " + message.getSubject());
            this.mailSender.send(helper.getMimeMessage());

        } catch(MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("Unable to send password reset email ", e);
        }
    }


    protected List<String> getRecipientAddresses(ILocalAccountPerson account) {
        return Arrays.asList((String)account.getAttributeValue(ILocalAccountPerson.ATTR_MAIL));
    }


    protected String formatBody(URL resetUrl, ILocalAccountPerson account, Locale locale) {
        final STGroup group = new STGroupDir(templateDir, '$', '$');
        final ST template = group.getInstanceOf(templateName);

        String name = findDisplayNameFromLocalAccountPerson(account);
        template.add("displayName", name);
        template.add("url", resetUrl.toString());

        return template.render();
    }


    protected String formatSubject(ILocalAccountPerson account, Locale locale) {
        return messageSource.getMessage(i18nSubject, new Object[]{}, locale);
    }


    protected String findDisplayNameFromLocalAccountPerson(ILocalAccountPerson person) {
        Object name = person.getAttributeValue(ILocalAccountPerson.ATTR_DISPLAY_NAME);
        if ((name instanceof String) && !StringUtils.isEmpty((String) name)) {
            return (String)name;
        }

        // if display name is not set, just return username.
        return person.getName();
    }
}
