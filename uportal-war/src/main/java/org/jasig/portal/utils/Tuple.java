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

package org.jasig.portal.utils;

import java.io.Serializable;

/**
 * Simple object that contains two values who's references are immutable once initialized.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Tuple<A, B> implements Serializable {
    private static final long serialVersionUID = 1L;

    public final A first;
    public final B second;
    private final boolean immutable;
    private final int hash; 
    
    public static <A1, B1> Tuple<A1, B1> of(A1 a1, B1 b1) {
        return new Tuple<A1, B1>(a1, b1);
    }

    public Tuple(A first, B second) {
        this(first, second, false);
    }

    public Tuple(A first, B second, boolean immutable) {
        this.first = first;
        this.second = second;
        this.immutable = immutable;
        
        if (this.immutable) {
            hash = internalHashCode();
        }
        else {
            hash = 0;
        }
    }
    
    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tuple other = (Tuple) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        }
        else if (!first.equals(other.first))
            return false;
        if (second == null) {
            if (other.second != null)
                return false;
        }
        else if (!second.equals(other.second))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (this.immutable) {
            return this.hash;
        }
        
        return internalHashCode();
    }
    
    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return "Tuple [first=" + first + ", second=" + second + "]";
    }
}
