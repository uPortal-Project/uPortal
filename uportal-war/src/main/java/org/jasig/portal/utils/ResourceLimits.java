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

package  org.jasig.portal.utils;

/**
 * Basic resource limits
 * Limits include : hard upper/lower limits, desired size,
 * activeLimit, update time and prune factor.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
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
