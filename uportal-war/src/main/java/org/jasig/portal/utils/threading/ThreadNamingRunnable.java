package org.jasig.portal.utils.threading;

/**
 * Adds a suffix to the current thread name for the duration of the wrapped runnable execution
 * 
 * @author Eric Dalquist
 */
public class ThreadNamingRunnable implements Runnable {
    private final String threadNameSuffix;
    private final Runnable runnable;
    
    public ThreadNamingRunnable(String threadNameSuffix, Runnable runnable) {
        this.threadNameSuffix = threadNameSuffix;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        final Thread currentThread = Thread.currentThread();
        final String name = currentThread.getName();
        try {
            currentThread.setName(name + threadNameSuffix);
            runnable.run();
        }
        finally {
            currentThread.setName(name);
        }
    }
}
