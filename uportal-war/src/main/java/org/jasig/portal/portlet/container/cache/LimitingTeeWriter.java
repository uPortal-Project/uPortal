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
import java.io.Writer;

import org.apache.commons.io.output.NullWriter;
import org.jasig.portal.utils.TeeWriter;

import com.google.common.base.Function;

/**
 * Subclass of {@link TeeWriter} that stops writing to the branch
 * once the limit is hit by calling {@link #setBranch(Writer)} with
 * {@link NullWriter}.
 * 
 * A callback {@link Function} to be executed when the limit is hit can 
 * be provided as well.
 * 
 * @author Nicholas Blair
 */
public class LimitingTeeWriter extends TeeWriter {
    private final long maximumCharacters;
    private final Writer branch;
    private final Function<LimitingTeeWriter, ?> limitReachedCallback;
    private long characterCount = 0;
    private boolean limitReached = false;

    public LimitingTeeWriter(long maximumCharacters, Writer out, Writer branch) {
        this(maximumCharacters, out, branch, null);
    }
    
    public LimitingTeeWriter(long maximumCharacters, Writer out, Writer branch,
            Function<LimitingTeeWriter, ?> limitReachedCallback) {
        super(out, branch);
        this.maximumCharacters = maximumCharacters;
        this.branch = branch;
        this.limitReachedCallback = limitReachedCallback;
    }
    
    /**
     * Sets the character count back to 0. If {@link #isLimitReached()} is true
     * it is set back to false and the original branch {@link Writer}
     * is set as the current branch again.
     */
    public void resetByteCount() {
        this.characterCount = 0;
        
        if (limitReached) {
            limitReached = false;
            this.setBranch(branch);
        }
    }
    
    /**
     * @return Number of characters seen so far
     */
    public long getCharacterCount() {
        return characterCount;
    }

    /**
     * @return true if the limit has been reached
     */
    public boolean isLimitReached() {
        return limitReached;
    }

    @Override
    protected void beforeWrite(int n) throws IOException {
        this.characterCount += n;
        
        if (this.maximumCharacters > 0 && !this.limitReached && this.characterCount > this.maximumCharacters) {
            //Hit limit, replace tee'd writer with a null writer
            this.limitReached = true;
            this.setBranch(NullWriter.NULL_WRITER);
            
            if (this.limitReachedCallback != null) {
                this.limitReachedCallback.apply(this);
            }
        }
    }
}
