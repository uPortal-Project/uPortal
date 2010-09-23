/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.utils.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.util.Assert;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Serializable key;
    private final int hashCode;

    public CacheKey(Serializable key) {
        Assert.notNull(key);
        this.key = key;
        this.hashCode = this.key.hashCode();
    }
    
    /**
     * Takes a list of serializable objects and stores them in an {@link ArrayList} to create a single serializable key object
     */
    public CacheKey(Serializable... keyParts) {
        Assert.notEmpty(keyParts);
        this.key = new ArrayList<Serializable>(Arrays.asList(keyParts));
        this.hashCode = this.key.hashCode();
    }
    
    public CacheKey(Collection<? extends Serializable> keyParts) {
        Assert.notEmpty(keyParts);
        this.key = new ArrayList<Serializable>(keyParts);
        this.hashCode = this.key.hashCode();
    }
    
    public Serializable getKey() {
        return this.key;
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CacheKey other = (CacheKey) obj;
        if (this.key == null) {
            if (other.key != null) {
                return false;
            }
        }
        else if (!this.key.equals(other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CacheKey [" + this.key + "]";
    }
}
