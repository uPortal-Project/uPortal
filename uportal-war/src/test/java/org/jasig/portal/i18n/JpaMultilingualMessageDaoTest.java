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
package org.jasig.portal.i18n;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.i18n.dao.IMessageDao;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:jpaPortalTestApplicationContext.xml"})
public class JpaMultilingualMessageDaoTest extends BaseJpaDaoTest {
    
    @Autowired
    private IMessageDao messageDao;
    
    @PersistenceContext(unitName = "uPortalPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Test
    public void testAllMethods() {
        execute(new Callable<Object>() {
            
            @Override
            public Object call() throws Exception {
                final String code = "Test message";
                
                // test #createMessage
                final Message msgUS = messageDao.createMessage(code, new Locale("en_US"), "Test message");
                final Message msgUS2 = messageDao.createMessage("Test message 2", new Locale("en_US"), "Test message2");
                final Message msgLV = messageDao.createMessage(code, new Locale("lv_LV"), "Testa ziņojums");
                final Message msgDE = messageDao.createMessage(code, new Locale("de_DE"), "Testnachricht");
                
                // test #getMessage
                final Message actual1 = messageDao.getMessage(code, new Locale("lv_LV"));
                assertEquals(msgLV, actual1);
                
                // test #updateMessage
                msgLV.setValue("Labots testa ziņojums");
                messageDao.updateMessage(msgLV);
                
                final Message actual2 = messageDao.getMessage(code, new Locale("lv_LV"));
                assertEquals(msgLV, actual2);
                
                // test #getMessagesByCode
                final Set<Message> expected3 = new LinkedHashSet<Message>();
                expected3.add(msgUS);
                expected3.add(msgLV);
                expected3.add(msgDE);
                final Set<Message> actual3 = messageDao.getMessagesByCode(code);
                assertEquals(expected3, actual3);
                
                // test #deleteMessage
                messageDao.deleteMessage(msgDE);
                final Set<Message> actual4 = messageDao.getMessagesByCode(code);
                assertEquals(2, actual4.size());
                
                // test #getMessagesByLocale
                final Set<Message> expected5 = new LinkedHashSet<Message>();
                expected5.add(msgUS);
                expected5.add(msgUS2);
                final Set<Message> actual5 = messageDao.getMessagesByLocale(new Locale("en_US"));
                assertEquals(expected5, actual5);
                
                // test #getCodes
                final Set<String> expected6 = new LinkedHashSet<String>();
                expected6.add("Test message");
                expected6.add("Test message 2");
                final Set<String> actual6 = messageDao.getCodes();
                assertEquals(expected6, actual6);
                
                return null;
            }
        });
    }
}
