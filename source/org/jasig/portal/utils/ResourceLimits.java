/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal.utils;

/**
 * Basic resource limits
 * Limits include : hard upper/lower limits, desired size,
 * activeLimit, update time and prune factor.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 */
public class ResourceLimits
{
    protected int maxSize=-1;
    protected int minSize=0;
    protected int optimalSize= 1;
    
    protected int maxActiveSize = -1;

    protected int updateTime = 10000; // 10 seconds
    protected float pruneFactor= 0.1F; // 10 percent
    
    public ResourceLimits(){}
    
    public int getMaxSize() {
	return maxSize;
    }
    public int getMinSize() {
	return minSize;
    }
    public int getOptimalSize() {
	return optimalSize;
    }
    public float getPruneFactor() {
	return pruneFactor;
    }
    public int getUpdateTime() {
	return updateTime;
    }
    public void setUpdateTime(int updateTime) {
	this.updateTime = updateTime;
    }
    public void setPruneFactor(float pruneFactor) {
	this.pruneFactor = pruneFactor;
    }
    public void setOptimalSize(int optimalSize) {
	this.optimalSize = optimalSize;
    }
    public void setMinSize(int minSize) {
	this.minSize = minSize;
    }
    public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
    }
    public int getMaxActiveSize() {
	return maxActiveSize;
    }
    public void setMaxActiveSize(int maxActiveSize) {
	this.maxActiveSize = maxActiveSize;
    }
}
