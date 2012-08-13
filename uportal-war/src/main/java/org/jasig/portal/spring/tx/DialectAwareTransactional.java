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
package org.jasig.portal.spring.tx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.dialect.Dialect;
import org.springframework.transaction.annotation.Transactional;

/**
 * Added to methods annotated with {@link Transactional} to determine if a transaction is actually
 * needed based on the dialect
 * 
 * @author Eric Dalquist
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DialectAwareTransactional {
    /**
     * The dialect(s) to check against to determine if a TX should be created or not
     */
    Class<? extends Dialect>[] value() default {};
    
    /**
     * If true then a {@link Dialect} match against {@link #value()} results in no TX. If false
     * then the lack of a {@link Dialect} match against {@link #value()} results in no TX.
     */
    boolean exclude() default true;
}
