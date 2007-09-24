/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
