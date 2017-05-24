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
package org.apereo.portal.utils;

import com.google.common.base.Function;
import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Provides concurrent scanning of a directory tree. Clients provide a callback {@link Function} to
 * call for each found {@link File}. Optionally the function results can be collated and returned in
 * a Map with the file the result is for.
 *
 * <p>Note that the provided ExecutorService can never block on {@link
 * ExecutorService#execute(Runnable)}
 *
 */
public final class ConcurrentDirectoryScanner implements DirectoryScanner {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService executorService;

    private boolean processDirectories = false;
    private boolean recursive = true;
    private long maxWait = -1;
    private TimeUnit maxWaitTimeUnit = TimeUnit.MILLISECONDS;

    /** @param executorService Used to submit the file processing and directory recursion tasks */
    public ConcurrentDirectoryScanner(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * @param processDirectories If directories should be passed to the fileProcessor {@link
     *     Function}, defaults to false.
     */
    public void setProcessDirectories(boolean processDirectories) {
        this.processDirectories = processDirectories;
    }

    /** @param recursive If processing should recurse on directories, defaults to true. */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * @param maxWait Maximum wait for any one {@link Future}, defaults to -1 (forever)
     * @see Future#get(long, TimeUnit)
     */
    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    /**
     * @param maxWaitTimeUnit {@link TimeUnit} for the {@link #setMaxWait(long)} value.
     * @see Future#get(long, TimeUnit)
     */
    public void setMaxWaitTimeUnit(TimeUnit maxWaitTimeUnit) {
        this.maxWaitTimeUnit = maxWaitTimeUnit;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.DirectoryScanner#scanDirectoryWithResults(java.io.File, java.io.FileFilter, com.google.common.base.Function)
     */
    @Override
    public <T> Map<File, T> scanDirectoryWithResults(
            File directory, FileFilter fileFilter, Function<Resource, T> fileProcessor) {
        final ConcurrentMap<File, T> results = new ConcurrentHashMap<File, T>();
        this.scanDirectory(directory, results, fileFilter, fileProcessor);
        return results;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.DirectoryScanner#scanDirectoryNoResults(java.io.File, java.io.FileFilter, com.google.common.base.Function)
     */
    @Override
    public void scanDirectoryNoResults(
            File directory, FileFilter fileFilter, Function<Resource, ?> fileProcessor) {
        this.scanDirectory(directory, null, fileFilter, fileProcessor);
    }

    protected <T> void scanDirectory(
            File directory,
            ConcurrentMap<File, T> results,
            FileFilter fileFilter,
            Function<Resource, T> fileProcessor) {
        final Queue<Tuple<File, Future<T>>> futures =
                new ConcurrentLinkedQueue<Tuple<File, Future<T>>>();

        recurseOnDirectory(futures, results, directory, fileFilter, fileProcessor);

        waitForFutures(futures, results);
    }

    protected <T> void recurseOnDirectory(
            final Queue<Tuple<File, Future<T>>> futures,
            final ConcurrentMap<File, T> results,
            final File directory,
            final FileFilter fileFilter,
            final Function<Resource, T> fileProcessor) {

        logger.debug("processing directory: {}", directory);

        final File[] children = directory.listFiles(fileFilter);
        for (final File child : children) {
            //If the child is a directory and recursion is on recurse on it
            if (child.isDirectory()) {
                if (recursive) {
                    if (processDirectories) {
                        submitProcessFile(futures, child, fileProcessor);
                    }

                    submitDirectoryRecurse(futures, results, child, fileFilter, fileProcessor);
                }
            }
            //Otherwise pass the file into the fileHandler via the executor service
            else {
                submitProcessFile(futures, child, fileProcessor);
            }
        }

        //Clean up any completed futures from the queue
        cleanFutures(futures, results);
    }

    protected <T> void submitDirectoryRecurse(
            final Queue<Tuple<File, Future<T>>> futures,
            final ConcurrentMap<File, T> results,
            final File directory,
            final FileFilter fileFilter,
            final Function<Resource, T> fileProcessor) {

        final Future<T> dirFuture =
                this.executorService.submit(
                        new Callable<T>() {
                            @Override
                            public T call() throws Exception {
                                recurseOnDirectory(
                                        futures, results, directory, fileFilter, fileProcessor);
                                return null;
                            }
                        });

        logger.debug("queued directory recurse: {}", directory);
        futures.offer(new Tuple<File, Future<T>>(directory, dirFuture));
    }

    protected <T> void submitProcessFile(
            final Queue<Tuple<File, Future<T>>> futures,
            final File child,
            final Function<Resource, T> fileProcessor) {
        final Future<T> fileFuture =
                this.executorService.submit(
                        new Callable<T>() {
                            @Override
                            public T call() throws Exception {
                                logger.debug("processing file: {}", child);
                                return fileProcessor.apply(new FileSystemResource(child));
                            }
                        });

        logger.debug("queued file processing: {}", child);
        futures.offer(new Tuple<File, Future<T>>(child, fileFuture));
    }

    protected <T> void cleanFutures(
            final Queue<Tuple<File, Future<T>>> futures, final ConcurrentMap<File, T> results) {
        for (final Iterator<Tuple<File, Future<T>>> futureItr = futures.iterator();
                futureItr.hasNext();
                ) {
            final Tuple<File, Future<T>> future = futureItr.next();
            if (future.second.isDone()) {
                futureItr.remove();

                try {
                    processResult(results, future);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    return;
                }
            }
        }
    }

    protected <T> void waitForFutures(
            final Queue<Tuple<File, Future<T>>> futures, final ConcurrentMap<File, T> results) {
        while (!futures.isEmpty()) {
            final Tuple<File, Future<T>> future = futures.poll();
            try {
                processResult(results, future);
            } catch (InterruptedException e) {
                Thread.interrupted();
                return;
            }
        }
    }

    protected <T> void processResult(
            final ConcurrentMap<File, T> results, final Tuple<File, Future<T>> future)
            throws InterruptedException {
        final T result;
        try {
            if (maxWait < 0) {
                result = future.second.get();
            } else {
                result = future.second.get(maxWait, maxWaitTimeUnit);
            }

            logger.debug("processing complete: {}", future.first);
        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Exception processing for file: " + future.first, e.getCause());
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout waiting for file: " + future.first, e);
        }

        if (results != null && result != null) {
            results.put(future.first, result);
        }
    }
}
