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
package org.jasig.portal.portlet.container.cache;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.jasig.portal.utils.TeeOutputStream;

import com.google.common.base.Function;

/**
 * Subclass of {@link TeeOutputStream} that stops writing to the branch
 * once the limit is hit by calling {@link #setBranch(OutputStream)} with
 * {@link NullOutputStream}.
 * 
 * A callback {@link Function} to be executed when the limit is hit can 
 * be provided as well.
 * 
 * @author Nicholas Blair
 */
public class LimitingTeeOutputStream extends TeeOutputStream {
    private final long maximumBytes;
    private final OutputStream branch;
    private final Function<LimitingTeeOutputStream, ?> limitReachedCallback;
    private long byteCount = 0;
    private boolean limitReached = false;

    public LimitingTeeOutputStream(long maximumBytes, OutputStream out, OutputStream branch) {
        this(maximumBytes, out, branch, null);
    }
    
    public LimitingTeeOutputStream(long maximumBytes, OutputStream out, OutputStream branch,
            Function<LimitingTeeOutputStream, ?> limitReachedCallback) {
        super(out, branch);
        this.maximumBytes = maximumBytes;
        this.branch = branch;
        this.limitReachedCallback = limitReachedCallback;
    }
    
    /**
     * Sets the byte count back to 0. If {@link #isLimitReached()} is true
     * it is set back to false and the original branch {@link OutputStream}
     * is set as the current branch again.
     */
    public void resetByteCount() {
        this.byteCount = 0;
        
        if (limitReached) {
            limitReached = false;
            this.setBranch(branch);
        }
    }
    
    /**
     * @return Number of bytes seen so far
     */
    public long getByteCount() {
        return byteCount;
    }

    /**
     * @return true if the limit has been reached
     */
    public boolean isLimitReached() {
        return limitReached;
    }

    @Override
    protected void beforeWrite(int n) throws IOException {
        this.byteCount += n;
        
        if (this.maximumBytes > 0 && !this.limitReached && this.byteCount > this.maximumBytes) {
            //Hit limit, replace tee'd OutputStream with a null OutputStream
            this.limitReached = true;
            this.setBranch(NullOutputStream.NULL_OUTPUT_STREAM);
            
            if (this.limitReachedCallback != null) {
                this.limitReachedCallback.apply(this);
            }
        }
    }
}
