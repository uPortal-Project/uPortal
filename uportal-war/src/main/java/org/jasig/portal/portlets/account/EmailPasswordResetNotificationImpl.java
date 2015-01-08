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
package org.jasig.portal.portlets.account;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;


@Component("emailPasswordResetNotificationImpl")
public class EmailPasswordResetNotificationImpl implements IPasswordResetNotification {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private String subjectMessageKey = "reset.your.password";
    private JavaMailSender mailSender;
    private MessageSource messageSource;
    private String portalEmailAddress;
    private String templateDir = "properties/templates";
    private String templateName = "passwordReset";


    /**
     * Set the subject string.  Note that this is the i18n key, not a raw
     * text string.  eg. reset.your.password
     *
     * @param subjectMessageKey the message key to use as a subject line.  Defaults to "reset.your.password"
     */
    public void setSubjectMessageKey(final String subjectMessageKey) {
        this.subjectMessageKey = subjectMessageKey;
    }


    @Autowired
    public void setMailSender(final JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }


    @Autowired
    public void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @Resource(name="portalEmailAddress")
    public void setPortalEmailAddress(final String portalEmailAddress) {
        this.portalEmailAddress = portalEmailAddress;
    }


    public void setTemplateDir(final String templateDir) {
        this.templateDir = templateDir;
    }


    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }


    @Override
    public void sendNotification(URL resetUrl, ILocalAccountPerson account, Locale locale) {
        log.debug("Sending password reset instructions to user with url {}", resetUrl.toString());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String email = (String)account.getAttributeValue(ILocalAccountPerson.ATTR_MAIL);
            String subject = messageSource.getMessage(subjectMessageKey, new Object[]{}, locale);
            String body = formatBody(resetUrl, account, locale);

            helper.addTo(email);
            helper.setText(body, true);
            helper.setSubject(subject);
            helper.setFrom(portalEmailAddress, messageSource.getMessage("portal.name", new Object[]{}, locale));

            log.debug("Sending message to {} from {} subject {}", email,
                    Arrays.toString(message.getFrom()), message.getSubject());
            this.mailSender.send(helper.getMimeMessage());

        } catch(Exception e) {
            log.error("Unable to send password reset email", e);
        }
    }


    /**
     * Get the body content of the email.
     *
     * @param resetUrl the password reset URL
     * @param account the user account that has had its password reset
     * @param locale the locale of the user who reset the password
     * @return The message body as a string.
     */
    private String formatBody(URL resetUrl, ILocalAccountPerson account, Locale locale) {
        final STGroup group = new STGroupDir(templateDir, '$', '$');
        final ST template = group.getInstanceOf(templateName);

        String name = findDisplayNameFromLocalAccountPerson(account);
        template.add("displayName", name);
        template.add("url", resetUrl.toString());

        return template.render();
    }


    /**
     * Determine the name to use in the password reset email that identifies the
     * users whose password has been reset.
     *
     * @param person the account that has had its password reset
     * @return the account holders name
     */
    protected String findDisplayNameFromLocalAccountPerson(ILocalAccountPerson person) {
        Object name = person.getAttributeValue(ILocalAccountPerson.ATTR_DISPLAY_NAME);
        if ((name instanceof String) && !StringUtils.isEmpty((String) name)) {
            return (String)name;
        }

        // if display name is not set, just return username.
        return person.getName();
    }
}
