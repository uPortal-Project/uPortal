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

package org.jasig.portal.layout.dlm;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.dom4j.Element;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.security.IPerson;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
@Table(name = "UP_DLM_EVALUATOR")
@SequenceGenerator(
        name="UP_DLM_EVALUATOR_GEN",
        sequenceName="UP_DLM_EVALUATOR_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_DLM_EVALUATOR_GEN",
        pkColumnValue="UP_DLM_EVALUATOR",
        allocationSize=1
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="EVALUATOR_TYPE")
public abstract class Evaluator {
    
    public static final String RCS_ID = "@(#) $Header$";
    
    public static final String NAMESPACE_URI = "http://org.jasig.portal.layout.dlm.config";

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_DLM_EVALUATOR_GEN")
    @Column(name = "EVALUATOR_ID")
    private final long evaluatorId;
        
    @SuppressWarnings("unused")
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    public Evaluator() {
        evaluatorId = -1L;
        entityVersion = -1;
    }

    public abstract boolean isApplicable( IPerson person );
    
    /**
     * Serializes this {@link Evaluator} into the same XML format supported 
     * by dlm.xml.  <b>NOTE:</b>  this method will only yield usable XML if 
     * invoked on an instance of {@link FragmentDefinition};  all other 
     * subclasses will return only XML fragments.
     * 
     * @param parent The XML structure (starting with &lt;dlm:fragment&gt;) so far
     */
    public abstract void toElement(Element parent);
    
    public abstract Class<? extends EvaluatorFactory> getFactoryClass();
    
    /**
     * Provides a one-line, human-readable description of the users who are 
     * members of the fragment audience based on this {@link Evaluator}.
     * 
     * @return A short description of what this {@link Evaluator} does
     */
    public abstract String getSummary();

}
