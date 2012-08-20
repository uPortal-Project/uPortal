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
package org.jasig.portal.concurrency.locking;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jasig.portal.IPortalInfoProvider;
import org.jasig.portal.concurrency.locking.IClusterLockService.LockStatus;
import org.jasig.portal.concurrency.locking.IClusterLockService.TryLockFunctionResult;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;

//@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ClusterLockServiceImplTest {
	@InjectMocks private final ClusterLockServiceImpl clusterLockService = new ClusterLockServiceImpl();
	@Mock private IClusterLockDao clusterLockDao;
	@Mock private IPortalInfoProvider portalInfoProvider;
	private ExecutorService lockMonitorExecutorService;
	
	@Before
	public void setup() {
		lockMonitorExecutorService = Executors.newSingleThreadExecutor();
		clusterLockService.setLockMonitorExecutorService(this.lockMonitorExecutorService);
	}
	
	@After
	public void teardown() {
		this.lockMonitorExecutorService.shutdownNow();
	}
	
	@Test
	public void testWithinLastRunDelay() throws InterruptedException {
		final String mutexName = "TEST";
		final String serverName = "server_1";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutexName);
		clusterMutex.lock(serverName);
		clusterMutex.unlock();
		
		when(clusterLockDao.getClusterMutex(mutexName)).thenReturn(clusterMutex);
		when(clusterLockDao.getLock(mutexName)).thenAnswer(new Answer<ClusterMutex>() {
			@Override
			public ClusterMutex answer(InvocationOnMock invocation) throws Throwable {
				clusterMutex.lock(serverName);
				return clusterMutex;
			}
		});
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutexName, LockOptions.builder().lastRunDelay(1000),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.SKIPPED_LAST_RUN, result.getLockStatus());
		assertNull(result.getResult());
		assertFalse(result.isExecuted());
		
	}
	
	@Test
	public void testOutsideLastRunDelay() throws InterruptedException {
		final String mutexName = "TEST";
		final String serverName = "server_1";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutexName);
		clusterMutex.lock(serverName);
		clusterMutex.unlock();
		
		Thread.sleep(10);
		
		when(clusterLockDao.getClusterMutex(mutexName)).thenReturn(clusterMutex);
		when(clusterLockDao.getLock(mutexName)).thenAnswer(new Answer<ClusterMutex>() {
			@Override
			public ClusterMutex answer(InvocationOnMock invocation) throws Throwable {
				clusterMutex.lock(serverName);
				return clusterMutex;
			}
		});
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutexName, LockOptions.builder().lastRunDelay(10),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.EXECUTED, result.getLockStatus());
		assertTrue(result.getResult());
		assertTrue(result.isExecuted());
		
	}
	
	@Test
	public void testWithinServerBiasDelayDifferentServer() throws InterruptedException {
		final String mutexName = "TEST";
		final String serverName = "server_1";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutexName);
		clusterMutex.lock("server_2");
		clusterMutex.unlock();
		when(portalInfoProvider.getUniqueServerName()).thenReturn(serverName);
		when(clusterLockDao.getClusterMutex(mutexName)).thenReturn(clusterMutex);
		when(clusterLockDao.getLock(mutexName)).thenAnswer(new Answer<ClusterMutex>() {
			@Override
			public ClusterMutex answer(InvocationOnMock invocation) throws Throwable {
				clusterMutex.lock(serverName);
				return clusterMutex;
			}
		});
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutexName, LockOptions.builder().serverBiasDelay(1000),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.SKIPPED_SERVER_BIAS, result.getLockStatus());
		assertNull(result.getResult());
		assertFalse(result.isExecuted());
		
	}
	
	@Test
	public void testWithinServerBiasDelaySameServer() throws InterruptedException {
		final String mutexName = "TEST";
		final String serverName = "server_1";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutexName);
		clusterMutex.lock(serverName);
		clusterMutex.unlock();
		when(portalInfoProvider.getUniqueServerName()).thenReturn(serverName);
		when(clusterLockDao.getClusterMutex(mutexName)).thenReturn(clusterMutex);
		when(clusterLockDao.getLock(mutexName)).thenAnswer(new Answer<ClusterMutex>() {
			@Override
			public ClusterMutex answer(InvocationOnMock invocation) throws Throwable {
				clusterMutex.lock(serverName);
				return clusterMutex;
			}
		});
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutexName, LockOptions.builder().serverBiasDelay(1000),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.EXECUTED, result.getLockStatus());
		assertTrue(result.getResult());
		assertTrue(result.isExecuted());
	}
	
	@Test
	public void testOutsideServerBiasDelayDifferentServer() throws InterruptedException {
		final String mutexName = "TEST";
		final String serverName = "server_1";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutexName);
		clusterMutex.lock("server_2");
		clusterMutex.unlock();
		
		Thread.sleep(10);
		when(portalInfoProvider.getUniqueServerName()).thenReturn(serverName);
		when(clusterLockDao.getClusterMutex(mutexName)).thenReturn(clusterMutex);
		when(clusterLockDao.getLock(mutexName)).thenAnswer(new Answer<ClusterMutex>() {
			@Override
			public ClusterMutex answer(InvocationOnMock invocation) throws Throwable {
				clusterMutex.lock(serverName);
				return clusterMutex;
			}
		});
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutexName, LockOptions.builder().serverBiasDelay(10),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.EXECUTED, result.getLockStatus());
		assertTrue(result.getResult());
		assertTrue(result.isExecuted());
		
	}
	
	@Test
	public void testOutsideServerBiasDelaySameServer() throws InterruptedException {
		final String mutexName = "TEST";
		final String serverName = "server_1";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutexName);
		clusterMutex.lock(serverName);
		clusterMutex.unlock();
		
		Thread.sleep(10);
		when(portalInfoProvider.getUniqueServerName()).thenReturn(serverName);
		when(clusterLockDao.getClusterMutex(mutexName)).thenReturn(clusterMutex);
		when(clusterLockDao.getLock(mutexName)).thenAnswer(new Answer<ClusterMutex>() {
			@Override
			public ClusterMutex answer(InvocationOnMock invocation) throws Throwable {
				clusterMutex.lock(serverName);
				return clusterMutex;
			}
		});
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutexName, LockOptions.builder().serverBiasDelay(10),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.EXECUTED, result.getLockStatus());
		assertTrue(result.getResult());
		assertTrue(result.isExecuted());
		
	}
	
	@Test
	public void testLockThreadPoolDead() throws InterruptedException {
		final String mutexName = "TEST";
		final String serverName = "server_1";
		
		final ClusterMutex clusterMutex = new ClusterMutex(mutexName);
		clusterMutex.lock(serverName);
		clusterMutex.unlock();
		
		when(portalInfoProvider.getUniqueServerName()).thenReturn(serverName);
		when(clusterLockDao.getClusterMutex(mutexName)).thenReturn(clusterMutex);
		when(clusterLockDao.getLock(mutexName)).thenAnswer(new Answer<ClusterMutex>() {
			@Override
			public ClusterMutex answer(InvocationOnMock invocation) throws Throwable {
				clusterMutex.lock(serverName);
				return clusterMutex;
			}
		});
		
		ExecutorService fakeLockMonitorExecutorService = mock(ExecutorService.class);
		final Future dbLockWorkerFuture = mock(Future.class);
		when(fakeLockMonitorExecutorService.submit(any(Callable.class))).thenReturn(dbLockWorkerFuture);
		this.clusterLockService.setLockMonitorExecutorService(fakeLockMonitorExecutorService);
		this.clusterLockService.setDbLockTimeout(Duration.millis(500));
		
		final TryLockFunctionResult<Boolean> result = this.clusterLockService
				.doInTryLock(mutexName, LockOptions.builder().serverBiasDelay(10),
						new Function<ClusterMutex, Boolean>() {
							@Override
							public Boolean apply(ClusterMutex input) {
								return Boolean.TRUE;
							}
						});
		
		assertNotNull(result);
		assertEquals(LockStatus.SKIPPED_LOCKED, result.getLockStatus());
		assertNull(result.getResult());
		assertFalse(result.isExecuted());
	}
	
	//test exec serv not actually execing
}
