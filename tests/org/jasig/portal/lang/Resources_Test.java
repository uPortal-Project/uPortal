/* Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. Redistributions of any form whatsoever must retain the following
*    acknowledgment:
*    "This product includes software developed by the JA-SIG Collaborative
*    (http://www.jasig.org/)."
*
* THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
* EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
* NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
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
