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

import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
public class FragmentDefinition extends Evaluator
{
    
    public static final String NAMESPACE_URI = "http://org.jasig.portal.layout.dlm.config";
    
    public static final Namespace NAMESPACE = DocumentHelper.createNamespace("dlm", NAMESPACE_URI);
    
    /**
     * User account whose layout should be copied to create a layout for new
     * fragments. 
     */
    private static final String cDefaultLayoutOwnerId = "fragmentTemplate";

    private static final Log LOG = LogFactory.getLog(FragmentDefinition.class);

    @Column(name = "FRAGMENT_NAME")
    private String name = null;

    @Column(name = "OWNER_ID")
    private String ownerID = null;
    
    @Column(name = "PRECEDENCE")
    private double precedence = 0.0; // precedence of fragment
    
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @IndexColumn(name = "EVAL_INDEX")
    @JoinTable(name = "UP_DLM_EVALUATOR_PAREN", joinColumns = @JoinColumn(name = "PAREN_EVAL_ID"), inverseJoinColumns = @JoinColumn(name = "CHILD_EVAL_ID"))
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private List<Evaluator> evaluators = null;

    /* These variables are bound to a uP userId later in the life cycle, not managed by hibernate */
    @Transient
    private int index = 0; // index of definition within config file
    @Transient
    String defaultLayoutOwnerID = null;

    /**
     * No-arg constructor required by JPA/Hibernate.
     */
    public FragmentDefinition () {}
    
    /*
     * For unit testing...
     */
    protected FragmentDefinition(String name) {
        this.name = name;
    }
    
    /**
     * This constructor is passed a dlm:fragment element from which this 
     * FragmentDefinition instance gathers its configuration information.
     * 
     * @param e An Element representing a single <dlm:fragment> in dlm.xml.
     * @throws Exception
     */
    public FragmentDefinition ( Element e ) {
        loadFromEelement(e);
    }
    
    public void loadFromEelement(Element e) {
        final boolean REQUIRED = true;
        final boolean NOT_REQUIRED = false;

        NamedNodeMap atts = e.getAttributes();
        
        this.name = loadAttribute( "name", atts, REQUIRED, e );
        this.ownerID = loadAttribute( "ownerID", atts, REQUIRED, e );
        this.defaultLayoutOwnerID = loadAttribute( "defaultLayoutOwnerID", 
                                                   atts, NOT_REQUIRED, e );

        String precedence = loadAttribute( "precedence", atts, REQUIRED, e );
        try 
        {
            this.precedence = Double.valueOf( precedence ).doubleValue();
        }
        catch( NumberFormatException nfe ) 
        {
            throw new RuntimeException( "Invalid format for precedence attribute " +
                                 "of <fragment> in\n'" + XML.serializeNode(e), nfe );
        }
        
        // Audience Evaluators.
        // NB:  We're about to re-parse the complete set of evaluators, 
        // so we need to remove any that are already present.
        if (this.evaluators != null) {
            this.evaluators.clear();
        }
        loadAudienceEvaluators( e.getElementsByTagName( "dlm:audience" ) );
        
    }
    
    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return this.ownerID;
    }

    public double getPrecedence() {
        return this.precedence;
    }
    
    public static String getDefaultLayoutOwnerId()
    {
        return cDefaultLayoutOwnerId;
    }
    
    public int getEvaluatorCount() {
        return this.evaluators == null ? 0 : this.evaluators.size();
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public boolean isNoAudienceIncluded() {
        return evaluators == null || evaluators.size() == 0;
    }

    private void loadAudienceEvaluators( NodeList nodes )
    {
        final String evaluatorFactoryAtt = "evaluatorFactory";
        
        for ( int i=0; i<nodes.getLength(); i++ )
        {
            Node audience = nodes.item(i);
            NamedNodeMap atts = audience.getAttributes();
            Node att = atts.getNamedItem( evaluatorFactoryAtt );
            if ( att == null || att.getNodeValue().equals("") )
                throw new RuntimeException( "Required attibute '" + 
                                     evaluatorFactoryAtt + "' " +
                                     "is missing or empty on 'audience' " +
                                     " element in\n'" + XML.serializeNode(audience) + 
                                     "'" );
            String className = att.getNodeValue();
            EvaluatorFactory factory = loadEvaluatorFactory( className,
                                                             audience );
            addEvaluator( factory, audience );
        }
    }

    private void addEvaluator( EvaluatorFactory factory, Node audience )
    {
        Evaluator evaluator = factory.getEvaluator( audience );

        if ( evaluator == null )
            throw new RuntimeException( "Evaluator factory '" + 
                                 factory.getClass().getName() + 
                                 "' failed to " +
                                 "return an evaluator for 'audience' element" +
                                 " in\n'" +
                                 XML.serializeNode(audience) + 
                                 "'" );
        if (evaluators == null) {
            evaluators = new LinkedList<Evaluator>(); 
        }
        evaluators.add(evaluator);
    }
    
    private EvaluatorFactory loadEvaluatorFactory( String factoryClassName,
                                                   Node audience )
    {
        Class theClass = null;
        try
        {
            theClass = Class.forName( factoryClassName );
        }
        catch( ClassNotFoundException cnfe )
        {
            throw new RuntimeException( "java.lang.ClassNotFoundException occurred" +
                                 " while loading evaluator factory class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + XML.serializeNode(audience) + 
                                 "'" );
        }
        catch( ExceptionInInitializerError eiie )
        {
            throw new RuntimeException( "java.lang.ExceptionInInitializerError " +
                                 "occurred while " +
                                 "loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + XML.serializeNode(audience) + 
                                 "'. \nThis indicates that an exception " +
                                 "occurred during evaluation of a static" +
                                 " initializer or the initializer for a " +
                                 "static variable.", eiie );
        }
        catch( LinkageError le )
        {
            throw new RuntimeException( "java.lang.LinkageError occurred while " +
                                 "loading evaluator factory Class '" + 
                                 factoryClassName + "' for " +
                                 "'audience' element in\n'" +
                                 XML.serializeNode(audience) + 
                                 "'. \nThis typically means that a " +
                                 "dependent class has changed " +
                                 "incompatibly after compiling the " +
                                 "factory class.", le );
        }

        Object theInstance = null;

        try
        {
            theInstance = theClass.newInstance();
        }
        catch( IllegalAccessException iae ) 
        {
            throw new RuntimeException( "java.lang.IllegalAccessException occurred " +
                                 "while loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + XML.serializeNode(audience) + 
                                 "' \nVerify that this is a public class " +
                                 "and that it contains a public, zero " +
                                 "argument constructor.", iae );
        }
        catch( InstantiationException ie ) 
        {
            throw new RuntimeException( "java.lang.InstantiationException occurred " +
                                 "while loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + XML.serializeNode(audience) + 
                                 "' \nVerify that the specified class is a " +
                                 "class and not an interface or abstract " +
                                 "class.", ie );
        }
        try
        {
            return (EvaluatorFactory) theInstance;
        }
        catch( ClassCastException cce ) 
        {
            throw new RuntimeException( "java.lang.ClassCastException occurred " +
                                 "while loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + XML.serializeNode(audience) + 
                                 "'. \nVerify that the class implements the " +
                                 "EvaluatorFactory interface.", cce );
        }
    }

    public boolean isApplicable( IPerson p )
    {
        boolean isApplicable = false;
        if (LOG.isInfoEnabled())
            LOG.info(">>>> calling " + name + ".isApplicable( "
                    + p.getAttribute("username") + " )");
        if ( /*view == null ||
             view.getUserId() == -1 || */
             evaluators == null )
        {
            isApplicable = false;
            if (LOG.isDebugEnabled()) {
                LOG.debug("isApplicable()=false due to evaluators collection being null");
            }
        }
        else
        {
            for ( int i=0; i<evaluators.size(); i++ )
                if ( evaluators.get(i).isApplicable( p ) )
                {
                    isApplicable = true;
                    break;
                }
        }
        if (LOG.isInfoEnabled())
            LOG.info("---- " + name
                    + ".isApplicable( "
                    + p.getAttribute( "username" )
                    + " )=" + isApplicable);
        return isApplicable;
    }
    
    private String loadAttribute( String name, 
                                  NamedNodeMap atts, 
                                  boolean required,
                                  Element e) 
    { 
        Node att = atts.getNamedItem( name );
        if ( required && 
             ( att == null  ||
               att.getNodeValue().equals( "" ) ) )
            throw new RuntimeException( "Missing or empty attribute '" + name +
                                 "' required by <fragment> in\n'" + 
                                 XML.serializeNode(e) + "'" );
        if ( att == null )
            return null;
        return att.getNodeValue();
    }
        
    @Override
    public void toElement(org.dom4j.Element parent) {
        
        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        QName q = new QName("fragment", FragmentDefinition.NAMESPACE);
        org.dom4j.Element rslt = DocumentHelper.createElement(q);
        rslt.addAttribute("name", this.getName());
        rslt.addAttribute("ownerID", this.getOwnerId());
        rslt.addAttribute("precedence", Double.toString(this.getPrecedence()));

        // Serialize our children...
        for (Evaluator v : this.evaluators) {
            v.toElement(rslt);
        }
        
        parent.add(rslt);
        
    }
    
    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        String msg = "This method is not necessary for serializing " +
                        "FragmentDefinition instances and should " +
                        "not be invoked.";
        throw new UnsupportedOperationException(msg);
    }

}
