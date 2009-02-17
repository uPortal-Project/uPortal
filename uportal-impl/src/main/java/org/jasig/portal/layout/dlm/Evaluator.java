/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.dom4j.Element;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.security.IPerson;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
@Table(name = "UP_DLM_EVALUATOR")
@GenericGenerator(
        name = "UP_DLM_EVALUATOR_GEN", 
        strategy = "native", 
        parameters = {
            @Parameter(name = "sequence", value = "UP_DLM_EVALUATOR_SEQ"),
            @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
            @Parameter(name = "column", value = "NEXT_UP_DLM_EVALUATOR_HI")
        }
    )
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Evaluator {
    
    public static final String RCS_ID = "@(#) $Header$";
    
    public static final String NAMESPACE_URI = "http://org.jasig.portal.layout.dlm.config";

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(generator = "UP_DLM_EVALUATOR_GEN")
    private final long evaluatorId;
        
    public Evaluator() {
        evaluatorId = -1L;
    }

    public abstract boolean isApplicable( IPerson person );
    
    /**
     * Serializes this <code>Evaluator</code> into the same XML format supported 
     * by dlm.xml.  <b>NOTE:</b>  this method will only yield usable XML if 
     * invoked on an instance of {@link FragmentDefinition};  all other 
     * subclasses will return only XML fragments.
     * 
     * @param parent The XML structure (starting with &lt;dlm:fragment&gt;) so far
     */
    public abstract void toElement(Element parent);
    
    public abstract Class<? extends EvaluatorFactory> getFactoryClass();

}
