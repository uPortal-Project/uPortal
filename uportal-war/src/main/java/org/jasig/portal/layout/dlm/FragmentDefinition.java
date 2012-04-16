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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.layout.dlm.providers.EvaluatorGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FragmentDefinition extends EvaluatorGroup
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
    private final String name;

    @Column(name = "OWNER_ID")
    private String ownerID = null;
    
    @Column(name = "PRECEDENCE")
    private double precedence = 0.0; // precedence of fragment

    @Column(name = "DESCRIPTION")
    private String description;

    /* These variables are bound to a uP userId later in the life cycle, not managed by hibernate */
    @Transient
    private int index = 0; // index of definition within config file
    @Transient
    String defaultLayoutOwnerID = null;

    /**
     * No-arg constructor required by JPA/Hibernate.
     */
    @SuppressWarnings("unused")
    private FragmentDefinition () {
        this.name = null;
    }
    
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
        NamedNodeMap atts = e.getAttributes();
        this.name = loadAttribute( "name", atts, true, e );
        
        loadFromEelement(e);
    }
    
    public void loadFromEelement(Element e) {
        final boolean REQUIRED = true;
        final boolean NOT_REQUIRED = false;

        NamedNodeMap atts = e.getAttributes();
        
        this.ownerID = loadAttribute( "ownerID", atts, REQUIRED, e );
        this.defaultLayoutOwnerID = loadAttribute( "defaultLayoutOwnerID", 
                                                   atts, NOT_REQUIRED, e );
        this.description = loadAttribute("description", atts, NOT_REQUIRED, e);

        String precedence = loadAttribute( "precedence", atts, REQUIRED, e );
        try 
        {
            this.precedence = Double.valueOf( precedence ).doubleValue();
        }
        catch( NumberFormatException nfe ) 
        {
            throw new RuntimeException( "Invalid format for precedence attribute " +
                                 "of <fragment> in\n'" + XmlUtilitiesImpl.toString(e), nfe );
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
    
    public String getDescription() {
        return description;
    }

    public static String getDefaultLayoutOwnerId()
    {
        return cDefaultLayoutOwnerId;
    }
    
    public int getEvaluatorCount() {
        return this.evaluators == null ? 0 : this.evaluators.size();
    }
    
    public List<Evaluator> getEvaluators() {
        return this.evaluators;
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
                                     " element in\n'" + XmlUtilitiesImpl.toString(audience) + 
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
                                 XmlUtilitiesImpl.toString(audience) + 
                                 "'" );
        if (evaluators == null) {
            evaluators = new LinkedList<Evaluator>(); 
        }
        evaluators.add(evaluator);
    }
    
    private EvaluatorFactory loadEvaluatorFactory( String factoryClassName,
                                                   Node audience )
    {
        Class<?> theClass = null;
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
                                 "in\n'" + XmlUtilitiesImpl.toString(audience) + 
                                 "'" );
        }
        catch( ExceptionInInitializerError eiie )
        {
            throw new RuntimeException( "java.lang.ExceptionInInitializerError " +
                                 "occurred while " +
                                 "loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + XmlUtilitiesImpl.toString(audience) + 
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
                                 XmlUtilitiesImpl.toString(audience) + 
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
                                 "in\n'" + XmlUtilitiesImpl.toString(audience) + 
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
                                 "in\n'" + XmlUtilitiesImpl.toString(audience) + 
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
                                 "in\n'" + XmlUtilitiesImpl.toString(audience) + 
                                 "'. \nVerify that the class implements the " +
                                 "EvaluatorFactory interface.", cce );
        }
    }

    @Override
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
                                 XmlUtilitiesImpl.toString(e) + "'" );
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
        rslt.addAttribute("description", this.getDescription());

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

    @Override
    public String getSummary() {
        // This method is for audience evaluators...
        throw new UnsupportedOperationException();
    }

}
