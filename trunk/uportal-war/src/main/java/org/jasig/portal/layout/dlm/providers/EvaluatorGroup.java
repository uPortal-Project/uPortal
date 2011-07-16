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

package org.jasig.portal.layout.dlm.providers;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;
import org.jasig.portal.layout.dlm.Evaluator;

/**
 * An {@link Evaluator} that contains a group of other evaluators
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class EvaluatorGroup extends Evaluator {
    @OneToMany(targetEntity=Evaluator.class, cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
    @IndexColumn(name = "EVAL_INDEX")
    @JoinTable(name = "UP_DLM_EVALUATOR_PAREN", joinColumns = @JoinColumn(name = "PAREN_EVAL_ID"), inverseJoinColumns = @JoinColumn(name = "CHILD_EVAL_ID"))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    protected List<Evaluator> evaluators = new LinkedList<Evaluator>();

    public void addEvaluator(Evaluator e) {
        this.evaluators.add(e);
    }
}