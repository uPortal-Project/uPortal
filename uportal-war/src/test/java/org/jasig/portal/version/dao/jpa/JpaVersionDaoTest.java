package org.jasig.portal.version.dao.jpa;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.test.BasePortalJpaDaoTest;
import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class JpaVersionDaoTest extends BasePortalJpaDaoTest {

    @Autowired
    private VersionDao versionDao;

    @Test
    public void testVersionLifecycle() {
        final String productName = "TEST_VERSION";
        
        //Doesn't exist
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final Version version = versionDao.getVersion(productName);
                assertNull(version);
            }
        });

        //Create
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                Version version = versionDao.getVersion(productName);
                assertNull(version);
                version = versionDao.setVersion(productName, 1, 2, 3);
                assertNotNull(version);
                assertEquals(1, version.getMajor());
                assertEquals(2, version.getMinor());
                assertEquals(3, version.getPatch());
            }
        });

        //Update
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                Version version = versionDao.getVersion(productName);
                assertNotNull(version);
                assertEquals(1, version.getMajor());
                assertEquals(2, version.getMinor());
                assertEquals(3, version.getPatch());
                

                version = versionDao.setVersion(productName, 4, 5, 6);
                assertNotNull(version);
                assertEquals(4, version.getMajor());
                assertEquals(5, version.getMinor());
                assertEquals(6, version.getPatch());
            }
        });

        //verify
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                Version version = versionDao.getVersion(productName);
                assertNotNull(version);
                assertEquals(4, version.getMajor());
                assertEquals(5, version.getMinor());
                assertEquals(6, version.getPatch());
            }
        });
    }
}
