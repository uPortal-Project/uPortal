/////////////////////////////////////////////////////////////////////////////
//
//                Copyright (c) Campus Pipeline, Inc. 2001
//                          All Rights Reserved
//
//  THIS WORK IS AN UNPUBLISHED WORK AND CONTAINS CONFIDENTIAL, PROPRIETARY,
//  AND TRADE SECRET INFORMATION OF CAMPUS PIPELINE, INC.  ACCESS TO THIS
//  WORK IS RESTRICTED TO CAMPUS PIPELINE, INC. EMPLOYEES AND TO ENTITIES
//  OTHER THAN CAMPUS PIPELINE, INC. WHO ARE PARTIES TO CURRENT LICENSE AND
//  CONFIDENTIALITY AGREEMENTS WITH CAMPUS PIPELINE, INC. OR ITS AUTHORIZED
//  DISTRIBUTORS.  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED,
//  COPIED, DISTRIBUTED, REPRODUCED, REVISED, MODIFIED, TRANSLATED,
//  ABRIDGED, CONDENSED, EXPANDED, COLLECTED, COMPILED, LINKED, RECAST,
//  TRANSFORMED, ADAPTED, OR REVERSE ENGINEERED WITHOUT THE PRIOR WRITTEN
//  CONSENT OF CAMPUS PIPELINE, INC.  ANY USE OR EXPLOITATION OF THIS WORK
//  WITHOUT EXPRESS AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL
//  AND CIVIL LIABILITY.
//
/////////////////////////////////////////////////////////////////////////////


package org.jasig.portal.lang;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Properties;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

/**
 * The <code>TypeConverter_Test</code> class tests <code>TypeConverter</code> class.
 *
 *
 * @version $Revision$
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 **/
public class Resources_Test
    extends TestCase
{
    /**
     * Run all the test cases defined in the class.
     *
     * @param args not used
     **/
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( suite() );
    }

    /**
     * Build a test suite using reflection.
     *
     * @return test suite for the class
     **/
    public static junit.framework.TestSuite suite()
    {
        return new junit.framework.TestSuite( Resources_Test.class );
    }

    /**
     * Setup for each test method.
     **/
    public void setUp() {}

    /**
     * Tear down for each test method.
     **/
    public void tearDown() {}

    public Resources_Test( String name )
    {
        super( name );
    }
    
    public void test()
        throws Exception
    {
        Object[][] tests = new Object[][]
        {
            {null,                  NullPointerException.class},
            {"",                    MissingResourceException.class},
            {"someUnknownProperty", MissingResourceException.class},
            {"testGetString",       null }
        };
        
        tryEach( tests );
    }
    
    private void tryEach( Object[][] objects )
        throws Exception
    {
        for( int i = 0; i < objects.length; i++ )
        {
            String name = (String)objects[i][0];
            Class throwable = (Class)objects[i][1];
            
            tryValue( name, throwable  );
            tryValue( name, null, throwable  );
            tryValue( name, new String[]{ "some value" }, throwable );
        }
    }
    
    private void tryValue(
        String name,
        String[] objects,
        Class throwable
        )
    {
        try
        {
            stringsEqual( name, objects );
        }
        catch( Throwable t )
        {
            TestCase.assertEquals(
                (null!=throwable?throwable.getName():"No throwable object" ), 
                t.getClass().getName()
                );
        }
    }

    private void tryValue(
        String name,
        Class throwable
        )
    {
        try
        {
            stringsEqual( name );
        }
        catch( ComparisonFailure x )
        {
            throw x;
        }
        catch( Throwable t )
        {
            String expected = "Should not have thrown an exception.";
            
            if( null != throwable )
                expected = throwable.getName();
            
            String got = t.getClass().getName();
            
            TestCase.assertEquals( 
                expected, 
                got
                );
        }
    }
    
    private String getString( 
        String name, 
        String[] objects 
        )
    {
        return Resources.getString(
            Resources_Test.class,
            name,
            objects
            );
    }
    
    private String getString( String name )
    {
        return Resources.getString(
           Resources_Test.class,
           name
           );
    }
    
    private String loadString( String name )
        throws Exception
    {
        Properties properties = new Properties();
        
        properties.load(
            getClass().getResourceAsStream( "Resources_Test.properties" )
            );
        
        String value = properties.getProperty( name );
        
        if( null == value )
            throw new MissingResourceException( "Undefined property.", Resources_Test.class.getName(), name );
        
        return value;
    }
    
    private String loadString(
        String name,
        String[] objects
        )
        throws Exception
    {
        return MessageFormat.format(
            loadString( name ),
            objects
            );
    }
    
    private void stringsEqual( String name )
        throws Exception
    {
        TestCase.assertEquals(
            loadString( name ),
            getString( name )
            );
    }
    
    private void stringsEqual(
        String name,
        String[] objects
        )
        throws Exception
    {
        TestCase.assertEquals(
            loadString( name, objects ),
            getString( name, objects )
            );
    }
}
