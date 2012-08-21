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
package org.jasig.portal.utils.jdbc;

import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.PoolProperties.InterceptorDefinition;
import org.apache.tomcat.jdbc.pool.Validator;
import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class TomcatDataSourceFactory extends AbstractFactoryBean<DataSource>
		implements BeanNameAware, PoolConfiguration {
	
	private final PoolConfiguration poolConfiguration = new PoolProperties();
	private MBeanServer mBeanServer;
	private String baseObjectName;
	private DelayedValidationQueryResolver delayedValidationQueryResolver;

	private ObjectName objectName;

	@Override
	public void setBeanName(String name) {
		this.poolConfiguration.setName(name);
	}

	@Autowired(required = false)
	public void setmBeanServer(MBeanServer mBeanServer) {
		this.mBeanServer = mBeanServer;
	}

	@Autowired(required = false)
	public void setDelayedValidationQueryResolver(
			DelayedValidationQueryResolver delayedValidationQueryResolver) {
		this.delayedValidationQueryResolver = delayedValidationQueryResolver;
	}

	public void setBaseObjectName(String baseObjectName) {
		this.baseObjectName = baseObjectName;
	}

	@Override
	public Class<?> getObjectType() {
		return DataSource.class;
	}

	@Override
	protected DataSource createInstance() throws Exception {
		final DataSource dataSource = new DataSource(this.poolConfiguration);

		if (this.mBeanServer != null) {
			try {
				final ConnectionPool pool = dataSource.createPool();
				final org.apache.tomcat.jdbc.pool.jmx.ConnectionPool jmxPool = pool
						.getJmxPool();
	
				
				this.objectName = ObjectName.getInstance(this.baseObjectName + this.poolConfiguration.getName());
				logger.info("Registering DataSource " + this.poolConfiguration.getName() + " in MBeanServer under name: " + this.objectName);
				
				final ObjectInstance instance = this.mBeanServer.registerMBean(
						jmxPool, this.objectName);
				this.objectName = instance.getObjectName();
			}
			catch (Exception e) {
				logger.warn("Failed to register connection pool with MBeanServer. JMX information will not be available for: " + this.poolConfiguration.getName(), e);
			}
		}
		

		if (dataSource.getValidationQuery() == null && this.delayedValidationQueryResolver != null) {
			logger.info("Attempting to resolve validation query for: " + this.poolConfiguration.getName());
			try {
				this.delayedValidationQueryResolver.registerValidationQueryCallback(dataSource, new FunctionWithoutResult<String>() {
					@Override
					protected void applyWithoutResult(String input) {
						logger.info("Resolved validation query '" + input + "' for " + poolConfiguration.getName());
						dataSource.setValidationQuery(input);
					}
				});
			}
			catch (Exception e) {
				logger.warn("Failed to resolve validation query for: " + this.poolConfiguration.getName(), e);
			}
		}

		return dataSource;
	}

	@Override
	protected void destroyInstance(DataSource instance) throws Exception {
		if (this.objectName != null) {
			this.mBeanServer.unregisterMBean(this.objectName);
		}
		instance.close();
	}

	@Override
	public void setAbandonWhenPercentageFull(int percentage) {
		this.poolConfiguration.setAbandonWhenPercentageFull(percentage);
	}

	@Override
	public int getAbandonWhenPercentageFull() {
		return this.poolConfiguration.getAbandonWhenPercentageFull();
	}

	@Override
	public boolean isFairQueue() {
		return this.poolConfiguration.isFairQueue();
	}

	@Override
	public void setFairQueue(boolean fairQueue) {
		this.poolConfiguration.setFairQueue(fairQueue);
	}

	@Override
	public boolean isAccessToUnderlyingConnectionAllowed() {
		return this.poolConfiguration.isAccessToUnderlyingConnectionAllowed();
	}

	@Override
	public void setAccessToUnderlyingConnectionAllowed(
			boolean accessToUnderlyingConnectionAllowed) {
		this.poolConfiguration
				.setAccessToUnderlyingConnectionAllowed(accessToUnderlyingConnectionAllowed);
	}

	@Override
	public String getConnectionProperties() {
		return this.poolConfiguration.getConnectionProperties();
	}

	@Override
	public void setConnectionProperties(String connectionProperties) {
		this.poolConfiguration.setConnectionProperties(connectionProperties);
	}

	@Override
	public Properties getDbProperties() {
		return this.poolConfiguration.getDbProperties();
	}

	@Override
	public void setDbProperties(Properties dbProperties) {
		this.poolConfiguration.setDbProperties(dbProperties);
	}

	@Override
	public Boolean isDefaultAutoCommit() {
		return this.poolConfiguration.isDefaultAutoCommit();
	}

	@Override
	public Boolean getDefaultAutoCommit() {
		return this.poolConfiguration.getDefaultAutoCommit();
	}

	@Override
	public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
		this.poolConfiguration.setDefaultAutoCommit(defaultAutoCommit);
	}

	@Override
	public String getDefaultCatalog() {
		return this.poolConfiguration.getDefaultCatalog();
	}

	@Override
	public void setDefaultCatalog(String defaultCatalog) {
		this.poolConfiguration.setDefaultCatalog(defaultCatalog);
	}

	@Override
	public Boolean isDefaultReadOnly() {
		return this.poolConfiguration.isDefaultReadOnly();
	}

	@Override
	public Boolean getDefaultReadOnly() {
		return this.poolConfiguration.getDefaultReadOnly();
	}

	@Override
	public void setDefaultReadOnly(Boolean defaultReadOnly) {
		this.poolConfiguration.setDefaultReadOnly(defaultReadOnly);
	}

	@Override
	public int getDefaultTransactionIsolation() {
		return this.poolConfiguration.getDefaultTransactionIsolation();
	}

	@Override
	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		this.poolConfiguration
				.setDefaultTransactionIsolation(defaultTransactionIsolation);
	}

	@Override
	public String getDriverClassName() {
		return this.poolConfiguration.getDriverClassName();
	}

	@Override
	public void setDriverClassName(String driverClassName) {
		this.poolConfiguration.setDriverClassName(driverClassName);
	}

	@Override
	public int getInitialSize() {
		return this.poolConfiguration.getInitialSize();
	}

	@Override
	public void setInitialSize(int initialSize) {
		this.poolConfiguration.setInitialSize(initialSize);
	}

	@Override
	public boolean isLogAbandoned() {
		return this.poolConfiguration.isLogAbandoned();
	}

	@Override
	public void setLogAbandoned(boolean logAbandoned) {
		this.poolConfiguration.setLogAbandoned(logAbandoned);
	}

	@Override
	public int getMaxActive() {
		return this.poolConfiguration.getMaxActive();
	}

	@Override
	public void setMaxActive(int maxActive) {
		this.poolConfiguration.setMaxActive(maxActive);
	}

	@Override
	public int getMaxIdle() {
		return this.poolConfiguration.getMaxIdle();
	}

	@Override
	public void setMaxIdle(int maxIdle) {
		this.poolConfiguration.setMaxIdle(maxIdle);
	}

	@Override
	public int getMaxWait() {
		return this.poolConfiguration.getMaxWait();
	}

	@Override
	public void setMaxWait(int maxWait) {
		this.poolConfiguration.setMaxWait(maxWait);
	}

	@Override
	public int getMinEvictableIdleTimeMillis() {
		return this.poolConfiguration.getMinEvictableIdleTimeMillis();
	}

	@Override
	public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		this.poolConfiguration
				.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	}

	@Override
	public int getMinIdle() {
		return this.poolConfiguration.getMinIdle();
	}

	@Override
	public void setMinIdle(int minIdle) {
		this.poolConfiguration.setMinIdle(minIdle);
	}

	@Override
	public String getName() {
		return this.poolConfiguration.getName();
	}

	@Override
	public void setName(String name) {
		this.poolConfiguration.setName(name);
	}

	@Override
	public int getNumTestsPerEvictionRun() {
		return this.poolConfiguration.getNumTestsPerEvictionRun();
	}

	@Override
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this.poolConfiguration
				.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
	}

	@Override
	public String getPassword() {
		return this.poolConfiguration.getPassword();
	}

	@Override
	public void setPassword(String password) {
		this.poolConfiguration.setPassword(password);
	}

	@Override
	public String getPoolName() {
		return this.poolConfiguration.getPoolName();
	}

	@Override
	public String getUsername() {
		return this.poolConfiguration.getUsername();
	}

	@Override
	public void setUsername(String username) {
		this.poolConfiguration.setUsername(username);
	}

	@Override
	public boolean isRemoveAbandoned() {
		return this.poolConfiguration.isRemoveAbandoned();
	}

	@Override
	public void setRemoveAbandoned(boolean removeAbandoned) {
		this.poolConfiguration.setRemoveAbandoned(removeAbandoned);
	}

	@Override
	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		this.poolConfiguration
				.setRemoveAbandonedTimeout(removeAbandonedTimeout);
	}

	@Override
	public int getRemoveAbandonedTimeout() {
		return this.poolConfiguration.getRemoveAbandonedTimeout();
	}

	@Override
	public boolean isTestOnBorrow() {
		return this.poolConfiguration.isTestOnBorrow();
	}

	@Override
	public void setTestOnBorrow(boolean testOnBorrow) {
		this.poolConfiguration.setTestOnBorrow(testOnBorrow);
	}

	@Override
	public boolean isTestOnReturn() {
		return this.poolConfiguration.isTestOnReturn();
	}

	@Override
	public void setTestOnReturn(boolean testOnReturn) {
		this.poolConfiguration.setTestOnReturn(testOnReturn);
	}

	@Override
	public boolean isTestWhileIdle() {
		return this.poolConfiguration.isTestWhileIdle();
	}

	@Override
	public void setTestWhileIdle(boolean testWhileIdle) {
		this.poolConfiguration.setTestWhileIdle(testWhileIdle);
	}

	@Override
	public int getTimeBetweenEvictionRunsMillis() {
		return this.poolConfiguration.getTimeBetweenEvictionRunsMillis();
	}

	@Override
	public void setTimeBetweenEvictionRunsMillis(
			int timeBetweenEvictionRunsMillis) {
		this.poolConfiguration
				.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	}

	@Override
	public String getUrl() {
		return this.poolConfiguration.getUrl();
	}

	@Override
	public void setUrl(String url) {
		this.poolConfiguration.setUrl(url);
	}

	@Override
	public String getValidationQuery() {
		return this.poolConfiguration.getValidationQuery();
	}

	@Override
	public void setValidationQuery(String validationQuery) {
		this.poolConfiguration.setValidationQuery(validationQuery);
	}

	@Override
	public String getValidatorClassName() {
		return this.poolConfiguration.getValidatorClassName();
	}

	@Override
	public void setValidatorClassName(String className) {
		this.poolConfiguration.setValidatorClassName(className);
	}

	@Override
	public Validator getValidator() {
		return this.poolConfiguration.getValidator();
	}

	@Override
	public void setValidator(Validator validator) {
		this.poolConfiguration.setValidator(validator);
	}

	@Override
	public long getValidationInterval() {
		return this.poolConfiguration.getValidationInterval();
	}

	@Override
	public void setValidationInterval(long validationInterval) {
		this.poolConfiguration.setValidationInterval(validationInterval);
	}

	@Override
	public String getInitSQL() {
		return this.poolConfiguration.getInitSQL();
	}

	@Override
	public void setInitSQL(String initSQL) {
		this.poolConfiguration.setInitSQL(initSQL);
	}

	@Override
	public boolean isTestOnConnect() {
		return this.poolConfiguration.isTestOnConnect();
	}

	@Override
	public void setTestOnConnect(boolean testOnConnect) {
		this.poolConfiguration.setTestOnConnect(testOnConnect);
	}

	@Override
	public String getJdbcInterceptors() {
		return this.poolConfiguration.getJdbcInterceptors();
	}

	@Override
	public void setJdbcInterceptors(String jdbcInterceptors) {
		this.poolConfiguration.setJdbcInterceptors(jdbcInterceptors);
	}

	@Override
	public InterceptorDefinition[] getJdbcInterceptorsAsArray() {
		return this.poolConfiguration.getJdbcInterceptorsAsArray();
	}

	@Override
	public boolean isJmxEnabled() {
		return this.poolConfiguration.isJmxEnabled();
	}

	@Override
	public void setJmxEnabled(boolean jmxEnabled) {
		this.poolConfiguration.setJmxEnabled(jmxEnabled);
	}

	@Override
	public boolean isPoolSweeperEnabled() {
		return this.poolConfiguration.isPoolSweeperEnabled();
	}

	@Override
	public boolean isUseEquals() {
		return this.poolConfiguration.isUseEquals();
	}

	@Override
	public void setUseEquals(boolean useEquals) {
		this.poolConfiguration.setUseEquals(useEquals);
	}

	@Override
	public long getMaxAge() {
		return this.poolConfiguration.getMaxAge();
	}

	@Override
	public void setMaxAge(long maxAge) {
		this.poolConfiguration.setMaxAge(maxAge);
	}

	@Override
	public boolean getUseLock() {
		return this.poolConfiguration.getUseLock();
	}

	@Override
	public void setUseLock(boolean useLock) {
		this.poolConfiguration.setUseLock(useLock);
	}

	@Override
	public void setSuspectTimeout(int seconds) {
		this.poolConfiguration.setSuspectTimeout(seconds);
	}

	@Override
	public int getSuspectTimeout() {
		return this.poolConfiguration.getSuspectTimeout();
	}

	@Override
	public void setDataSource(Object ds) {
		this.poolConfiguration.setDataSource(ds);
	}

	@Override
	public Object getDataSource() {
		return this.poolConfiguration.getDataSource();
	}

	@Override
	public void setDataSourceJNDI(String jndiDS) {
		this.poolConfiguration.setDataSourceJNDI(jndiDS);
	}

	@Override
	public String getDataSourceJNDI() {
		return this.poolConfiguration.getDataSourceJNDI();
	}

	@Override
	public boolean isAlternateUsernameAllowed() {
		return this.poolConfiguration.isAlternateUsernameAllowed();
	}

	@Override
	public void setAlternateUsernameAllowed(boolean alternateUsernameAllowed) {
		this.poolConfiguration
				.setAlternateUsernameAllowed(alternateUsernameAllowed);
	}

	@Override
	public void setCommitOnReturn(boolean commitOnReturn) {
		this.poolConfiguration.setCommitOnReturn(commitOnReturn);
	}

	@Override
	public boolean getCommitOnReturn() {
		return this.poolConfiguration.getCommitOnReturn();
	}

	@Override
	public void setRollbackOnReturn(boolean rollbackOnReturn) {
		this.poolConfiguration.setRollbackOnReturn(rollbackOnReturn);
	}

	@Override
	public boolean getRollbackOnReturn() {
		return this.poolConfiguration.getRollbackOnReturn();
	}

	@Override
	public void setUseDisposableConnectionFacade(
			boolean useDisposableConnectionFacade) {
		this.poolConfiguration
				.setUseDisposableConnectionFacade(useDisposableConnectionFacade);
	}

	@Override
	public boolean getUseDisposableConnectionFacade() {
		return this.poolConfiguration.getUseDisposableConnectionFacade();
	}

	@Override
	public void setLogValidationErrors(boolean logValidationErrors) {
		this.poolConfiguration.setLogValidationErrors(logValidationErrors);
	}

	@Override
	public boolean getLogValidationErrors() {
		return this.poolConfiguration.getLogValidationErrors();
	}

	@Override
	public boolean getPropagateInterruptState() {
		return this.poolConfiguration.getPropagateInterruptState();
	}

	@Override
	public void setPropagateInterruptState(boolean propagateInterruptState) {
		this.poolConfiguration
				.setPropagateInterruptState(propagateInterruptState);
	}

}
