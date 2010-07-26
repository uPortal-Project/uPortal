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
            (Object[])objects
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
