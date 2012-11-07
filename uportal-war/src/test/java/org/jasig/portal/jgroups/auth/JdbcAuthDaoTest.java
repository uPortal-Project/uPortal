package org.jasig.portal.jgroups.auth;

import static junit.framework.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.test.BasePortalJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class JdbcAuthDaoTest extends BasePortalJpaDaoTest {

    @Autowired
    private JdbcAuthDao authDao;
    
    @Autowired
    private JdbcOperations jdbcOperations;
    
    @Test
    public void testSimpleGetCreate() throws UnknownHostException {
        final String service = "foo";
        
        //Create & Return
        final String token = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final String t = authDao.getAuthToken(service);
                assertEquals(1000, t.length());
                return t;
            }
        });
        
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final String t = authDao.getAuthToken(service);
                assertEquals(token, t);
            }
        });
   }
    

    @Test
    public void testConcurrentCreate() throws UnknownHostException {
        final String service = "foo";
        
        //Create & Return
        final String token = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final String t = authDao.getAuthToken(service);
                assertEquals(1000, t.length());
                return t;
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                authDao.createToken(service);
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final String t = authDao.getAuthToken(service);
                assertEquals(token, t);
            }
        });
   }
}