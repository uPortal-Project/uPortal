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
package org.apereo.portal.utils.threading;

public final class ReadResult<T> {
    private static final ReadResult<Object> READ_RESULT_NO_WRITE_NULL =
            new ReadResult<Object>(false);
    private static final ReadResult<Object> READ_RESULT_DO_WRITE_NULL =
            new ReadResult<Object>(true);

    @SuppressWarnings("unchecked")
    public static <T> ReadResult<T> create(boolean doWriteLock) {
        if (doWriteLock) {
            return (ReadResult<T>) READ_RESULT_DO_WRITE_NULL;
        }

        return (ReadResult<T>) READ_RESULT_NO_WRITE_NULL;
    }

    public static <T> ReadResult<T> create(boolean doWriteLock, T result) {
        if (result == null) {
            return create(doWriteLock);
        }

        return new ReadResult<T>(doWriteLock, result);
    }

    private final boolean doWriteLock;
    private final T result;

    private ReadResult(boolean doWriteLock) {
        this.doWriteLock = doWriteLock;
        this.result = null;
    }

    private ReadResult(boolean doWriteLock, T result) {
        this.doWriteLock = doWriteLock;
        this.result = result;
    }

    public final boolean isDoWriteLock() {
        return doWriteLock;
    }

    public final T getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ReadResult [doWriteLock=" + doWriteLock + ", result=" + result + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (doWriteLock ? 1231 : 1237);
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ReadResult other = (ReadResult) obj;
        if (doWriteLock != other.doWriteLock) return false;
        if (result == null) {
            if (other.result != null) return false;
        } else if (!result.equals(other.result)) return false;
        return true;
    }
}
