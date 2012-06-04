package org.jasig.portal.utils.threading;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Logs a more useful error message when rejecting a runnable
 * 
 * @author Eric Dalquist
 */
public class LoggingAbortHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new RejectedExecutionException("Rejecting execution of " + r + 
                    ". activeCount=" + executor.getActiveCount() +
                    ". corePoolSize=" + executor.getCorePoolSize() +
                    ". poolSize=" + executor.getPoolSize() +
                    ". maxPoolSize=" + executor.getMaximumPoolSize() +
                    ". queueSize=" + executor.getQueue().size() +
                    ". taskCount=" + executor.getTaskCount() +
                    ". completedTaskCount=" + executor.getCompletedTaskCount()
                );
    }
}
