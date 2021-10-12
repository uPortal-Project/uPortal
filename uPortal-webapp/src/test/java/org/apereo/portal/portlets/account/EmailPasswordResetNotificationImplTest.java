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
package org.apereo.portal.portlets.account;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;

@RunWith(MockitoJUnitRunner.class)
public class EmailPasswordResetNotificationImplTest {
    @Mock MessageSource messageSource;
    @Mock JavaMailSender mailSender;
    @Captor ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    @InjectMocks
    EmailPasswordResetNotificationImpl service = new EmailPasswordResetNotificationImpl();

    @Before
    public void setUp() {
        // dummy up message source to just return the key value...
        when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenAnswer(
                        new Answer<String>() {
                            @Override
                            public String answer(InvocationOnMock invocationOnMock)
                                    throws Throwable {
                                return (String) invocationOnMock.getArguments()[0];
                            }
                        });
    }

    @Test
    public void testNotification() throws Exception {
        final String fromAddress = "portal@test.com";
        final String toAddress = "to@test.com";
        final String subject = "i18nSubject";
        final String resetUrl = "http://localhost/testing";
        final String displayName = "displayName";

        ILocalAccountPerson person = mock(ILocalAccountPerson.class);
        when(person.getAttributeValue(eq(ILocalAccountPerson.ATTR_DISPLAY_NAME)))
                .thenReturn(displayName);
        when(person.getAttributeValue(eq(ILocalAccountPerson.ATTR_MAIL))).thenReturn(toAddress);

        MimeMessage mockedMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockedMimeMessage);

        URL url = new URL(resetUrl);

        service.setSubjectMessageKey(subject);
        service.setPortalEmailAddress(fromAddress);
        service.sendNotification(url, person, Locale.getDefault());

        // verify send request was made...
        verify(mailSender).send(mimeMessageCaptor.capture());

        // verify basic email contents...
        ArgumentCaptor<InternetAddress> fromCaptor = ArgumentCaptor.forClass(InternetAddress.class);
        ArgumentCaptor<InternetAddress> toCaptor = ArgumentCaptor.forClass(InternetAddress.class);
        ArgumentCaptor<Multipart> bodyCaptor = ArgumentCaptor.forClass(Multipart.class);

        verify(mockedMimeMessage).setFrom(fromCaptor.capture());
        verify(mockedMimeMessage).addRecipient(eq(RecipientType.TO), toCaptor.capture());
        verify(mockedMimeMessage).setSubject(eq(subject));
        verify(mockedMimeMessage).setContent(bodyCaptor.capture());

        assertThat(fromCaptor.getValue().getAddress(), equalTo(fromAddress));
        assertThat(toCaptor.getValue().getAddress(), equalTo(toAddress));
        assertThat(getBodyHtml(bodyCaptor.getValue()), containsString(resetUrl));
    }

    private String getBodyHtml(Multipart msg) throws Exception {
        for (int i = 0; i < msg.getCount(); i++) {
            BodyPart part = msg.getBodyPart(i);
            String type = part.getContentType();

            if ("text/plain".equals(type) || "text/html".equals(type)) {
                DataHandler handler = part.getDataHandler();
                String val = IOUtils.toString(handler.getInputStream());
                return val;
            }
        }

        return null;
    }
}
