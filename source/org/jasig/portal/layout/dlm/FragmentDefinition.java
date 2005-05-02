/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FragmentDefinition
    implements Evaluator
{
    private static final Log LOG = LogFactory.getLog(FragmentDefinition.class);

    String name = null;
    String ownerID = null;
    int userID = -1;
    String defaultLayoutOwnerID = null;
    Element configDOM = null;
    double precedence = 0.0; // precedence of fragment
    int index = 0; // index of definition within config file
    boolean noAudienceIncluded = false;
    Evaluator[] evaluators = null;
    UserView view = null;
    List roles = null;

    /**
     * This constructor is passed a dlm:fragment element from which this 
     * FragmentDefinition instance gathers its configuration information.
     * 
     * @param e An Element representing a single <dlm:fragment> in dlm.xml.
     * @throws Exception
     */
    public FragmentDefinition ( Element e )
    throws Exception
    {
        final boolean REQUIRED = true;
        final boolean NOT_REQUIRED = false;

        this.configDOM = e;
        NamedNodeMap atts = e.getAttributes();
        
        this.name = loadAttribute( "name", atts, REQUIRED );
        this.ownerID = loadAttribute( "ownerID", atts, REQUIRED );
        this.defaultLayoutOwnerID = loadAttribute( "defaultLayoutOwnerID", 
                                                   atts, NOT_REQUIRED );

        String precedence = loadAttribute( "precedence", atts, REQUIRED );
        try 
        {
            this.precedence = Double.valueOf( precedence ).doubleValue();
        }
        catch( NumberFormatException nfe ) 
        {
            throw new Exception( "Invalid format for precedence attribute " +
                                 "of <fragment> in\n'" + getXML( e ) );
        }
        loadOwnerRoles( e.getElementsByTagName( "dlm:role" ));
        loadAudienceEvaluators( e.getElementsByTagName( "dlm:audience" ) );
    }

    /**
     * Captures the values of any included dlm:role elements so that the owner 
     * can later be granted those roles during fragment activation.
     * 
     * @param nodes
     */
    private void loadOwnerRoles(NodeList nodes)
    {
        roles = new ArrayList();
        
        if ( nodes != null && nodes.getLength() != 0 )
        {
            /*
             * This looks really screwy but is actually necessary. For element
             * nodes you can't simply call the getNodeValue() method on its
             * superclass Node. That simply returns a null value. The role
             * element has textual content only consisting of a single 
             * access group ID. So these element nodes will have a single 
             * text node child node and its getNodeValue() must be called to
             * acquire the group ID. To play it safe I'll grab all child nodes 
             * and for concatenate the value of all child text nodes.
             */
            for( int i = 0; i<nodes.getLength(); i++)
            {
                Element roleElement = (Element) nodes.item(i);
                NodeList childNodes = roleElement.getChildNodes();
                StringBuffer groupId = new StringBuffer();
                
                for( int j=0; j<childNodes.getLength(); j++)
                {
                    Node node = childNodes.item(j);
                    if ( node.getNodeType() == Node.TEXT_NODE )
                        groupId.append(node.getNodeValue());
                }
                 
                roles.add(groupId.toString());
            }
        }
    }

    private void loadAudienceEvaluators( NodeList nodes )
    throws Exception
    {
        final String evaluatorFactoryAtt = "evaluatorFactory";

        if ( nodes == null || nodes.getLength() == 0 )
        {
            noAudienceIncluded = true;
            return;
        }
        
        for ( int i=0; i<nodes.getLength(); i++ )
        {
            Node audience = nodes.item(i);
            NamedNodeMap atts = audience.getAttributes();
            Node att = atts.getNamedItem( evaluatorFactoryAtt );
            if ( att == null || att.getNodeValue().equals("") )
                throw new Exception( "Required attibute '" + 
                                     evaluatorFactoryAtt + "' " +
                                     "is missing or empty on 'audience' " +
                                     " element in\n'" + getXML( audience ) + 
                                     "'" );
            String className = att.getNodeValue();
            EvaluatorFactory factory = loadEvaluatorFactory( className,
                                                             audience );
            addEvaluator( factory, audience );
        }
    }

    private void addEvaluator( EvaluatorFactory factory, Node audience )
    throws Exception
    {
        Evaluator evaluator = factory.getEvaluator( audience );

        if ( evaluator == null )
            throw new Exception( "Evaluator factory '" + 
                                 factory.getClass().getName() + 
                                 "' failed to " +
                                 "return an evaluator for 'audience' element" +
                                 " in\n'" +
                                 getXML( audience ) + 
                                 "'" );
        if ( evaluators == null )
            evaluators = new Evaluator[] { evaluator };
        else
        {
            Evaluator[] newArr = new Evaluator[ evaluators.length + 1 ];
            System.arraycopy( evaluators, 0, newArr, 0, evaluators.length );
            newArr[evaluators.length] = evaluator;
            evaluators = newArr;
        }
    }
    private EvaluatorFactory loadEvaluatorFactory( String factoryClassName,
                                                   Node audience )
    throws Exception
    {
        Class theClass = null;
        try
        {
            theClass = Class.forName( factoryClassName );
        }
        catch( ClassNotFoundException cnfe )
        {
            throw new Exception( "java.lang.ClassNotFoundException occurred" +
                                 " while loading evaluator factory class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + getXML( audience ) + 
                                 "'" );
        }
        catch( ExceptionInInitializerError eiie )
        {
            StringWriter s = new StringWriter();
            PrintWriter p = new PrintWriter( s );
            eiie.printStackTrace( p );
            p.flush();
            
            throw new Exception( "java.lang.ExceptionInInitializerError " +
                                 "occurred while " +
                                 "loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + getXML( audience ) + 
                                 "'. \nThis indicates that an exception " +
                                 "occurred during evaluation of a static" +
                                 " initializer or the initializer for a " +
                                 "static variable. The stack trace is as" +
                                 " follows:\n----------\n" + s.toString() +
                                 "\n----------" );
        }
        catch( LinkageError le )
        {
            throw new Exception( "java.lang.LinkageError occurred while " +
                                 "loading evaluator factory Class '" + 
                                 factoryClassName + "' for " +
                                 "'audience' element in\n'" +
                                 getXML( audience ) + 
                                 "'. \nThis typically means that a " +
                                 "dependent class has changed " +
                                 "incompatibly after compiling the " +
                                 "factory class." );
        }

        Object theInstance = null;

        try
        {
            theInstance = theClass.newInstance();
        }
        catch( IllegalAccessException iae ) 
        {
            throw new Exception( "java.lang.IllegalAccessException occurred " +
                                 "while loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + getXML( audience ) + 
                                 "' \nVerify that this is a public class " +
                                 "and that it contains a public, zero " +
                                 "argument constructor." );
        }
        catch( InstantiationException ie ) 
        {
            throw new Exception( "java.lang.InstantiationException occurred " +
                                 "while loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + getXML( audience ) + 
                                 "' \nVerify that the specified class is a " +
                                 "class and not an interface or abstract " +
                                 "class." );
        }
        try
        {
            return (EvaluatorFactory) theInstance;
        }
        catch( ClassCastException cce ) 
        {
            throw new Exception( "java.lang.ClassCastException occurred " +
                                 "while loading evaluator factory Class '" + 
                                 factoryClassName + "' (or one of its " +
                                 "dependent classes) for 'audience' element " +
                                 "in\n'" + getXML( audience ) + 
                                 "'. \nVerify that the class implements the " +
                                 "EvaluatorFactory interface." );
        }
    }

    public boolean isApplicable( IPerson p )
    {
        boolean isApplicable = false;
        if (LOG.isInfoEnabled())
            LOG.info(">>>> calling " + name + ".isApplicable( "
                    + p.getAttribute("username") + " )");
        if ( userID == -1 ||
             view == null ||
             evaluators == null )
        {
            isApplicable = false;
        }
        else
        {
            for ( int i=0; i<evaluators.length; i++ )
                if ( evaluators[i].isApplicable( p ) )
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
                                  boolean required ) 
    throws Exception
    { 
        Node att = atts.getNamedItem( name );
        if ( required && 
             ( att == null  ||
               att.getNodeValue().equals( "" ) ) )
            throw new Exception( "Missing or empty attribute '" + name +
                                 "' required by <fragment> in\n'" + 
                                 getXML( this.configDOM ) + "'" );
        if ( att == null )
            return null;
        return att.getNodeValue();
    }

    private String getXML( Node node )
    {
        try
        {
            DOMSource ds = new DOMSource( node );
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty( OutputKeys.METHOD, "xml" );
            t.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            t.setOutputProperty( OutputKeys.INDENT, "yes" );
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult( sw );
            t.transform( ds, sr );
            String xml = sw.toString();

            if ( xml.endsWith( "\r\n" ) )
                return xml.substring( 0, xml.length() -2 );
            return xml;
        }
        catch( Exception e )
        {
            // can't generate xml so return anything we can
            return "* " + node.getNodeValue();
        }
    }
}
