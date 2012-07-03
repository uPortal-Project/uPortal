package org.jasig.portal.concurrency.locking;

import org.jasig.portal.IPortalInfoProvider;

/**
 * Options available when getting a lock
 */
public final class LockOptions {
	private long lastRunDelay = 0;
	private long serverBiasDelay = 0;
	
	/**
	 * Create a new LockOptions
	 */
	public static LockOptions builder() {
		return new LockOptions();
	}
	
	/**
	 * If set to a value greater than 0 the service will not attempt to acquire the lock if it was last held
	 * within the last lastRunDelay milliseconds.
	 * <br/>
	 * Useful for rate-limiting work that happens within a lock across a cluster.
	 */
	public LockOptions lastRunDelay(long lastRunDelay) {
		this.lastRunDelay = lastRunDelay;
		return this;
	}
	
	/**
	 * If set to a value greater than 0 the service will not attempt to acquire the lock if it was last held
	 * by a different server within the last serverBiasDelay milliseconds.
	 * <br/>
	 * Useful for biasing work that happens within a lock to run on the same server on each successive run.
	 * <br/>
	 * Different Server is defined using {@link IPortalInfoProvider#getUniqueServerName()}
	 */
	public LockOptions serverBiasDelay(long serverBiasDelay) {
		this.serverBiasDelay = serverBiasDelay;
		return this;
	}
	
	/**
	 * @see #lastRunDelay(long)
	 */
	public long getLastRunDelay() {
		return lastRunDelay;
	}

	/**
	 * @see #lastRunDelay(long)
	 */
	public void setLastRunDelay(long lastRunDelay) {
		this.lastRunDelay = lastRunDelay;
	}

	/**
	 * @see #serverBiasDelay(long)
	 */
	public long getServerBiasDelay() {
		return serverBiasDelay;
	}

	/**
	 * @see #serverBiasDelay(long)
	 */
	public void setServerBiasDelay(long serverBiasDelay) {
		this.serverBiasDelay = serverBiasDelay;
	}
}