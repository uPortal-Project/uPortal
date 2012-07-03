package org.jasig.portal.concurrency.locking;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import org.jasig.portal.concurrency.locking.IClusterLockService.LockStatus;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ClusterLockServiceImplTest {
	@InjectMocks private final ClusterLockServiceImpl clusterLockService = new ClusterLockServiceImpl();
	@Mock private IClusterLockDao clusterLockDao;
	@Mock private ExecutorService lockMonitorExecutorService;
	
	@Test
	public void testLastRunDelay() throws InterruptedException {
		String mutextName = "TEST";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutextName);
		when(clusterLockDao.getClusterMutex(mutextName)).thenReturn(clusterMutex);
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutextName, LockOptions.builder().lastRunDelay(100),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(@Nullable ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.SKIPPED_LAST_RUN, result.getLockStatus());
		assertNull(result.getResult());
		assertFalse(result.isExecuted());
		
	}
}
