/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.lang;

import junit.framework.TestCase;

/**
 * The <code>TypeConverter_Test</code> class tests <code>TypeConverter</code> class.
 *
 *
 * @version $Revision$
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 **/
public class TypeConverter_Test
    extends TestCase
{
    /** Class version identifier. */
    public static final String RCS_ID = "@(#) $Header$";

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
        return new junit.framework.TestSuite( TypeConverter_Test.class );
    }

    /**
     * Setup for each test method.
     **/
    public void setUp() {}

    /**
     * Tear down for each test method.
     **/
    public void tearDown() {}

    public TypeConverter_Test( String name )
    {
        super( name );
    }

    public void test_toString_boolean()
    {
        assertEquals( TypeConverter.TRUE, "true" );
        assertEquals( TypeConverter.FALSE, "false" );

        assertEquals( TypeConverter.toString( true ), TypeConverter.TRUE );
        assertEquals( TypeConverter.toString( false ), TypeConverter.FALSE );

        Object o1 = null;
        Object o2 = null;

        o1 = (Object)TypeConverter.toString( true );
        o2 = (Object)TypeConverter.toString( true );
        if( o1 != o2 )
        {
            fail( "Expected canonical 'true' value." );
        }

        o1 = (Object)TypeConverter.toString( false );
        o2 = (Object)TypeConverter.toString( false );
        if( o1 != o2 )
        {
            fail( "Expected canonical 'false' value." );
        }
    }

    public void test_toBoolean()
    {
        assertTrue( TypeConverter.toBoolean( "true" ) );
        assertTrue( !TypeConverter.toBoolean( "false" ) );
        assertTrue( TypeConverter.toBoolean( "True" ) );
        assertTrue( !TypeConverter.toBoolean( "False" ) );
        assertTrue( TypeConverter.toBoolean( "TRUE" ) );
        assertTrue( !TypeConverter.toBoolean( "FALSE" ) );

        String value = null;

        try
        {
            value = "false ";
            TypeConverter.toBoolean( value );
            fail( "Expected " + value + " to throw IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            value = "true ";
            TypeConverter.toBoolean( value );
            fail( "Expected " + value + " to throw IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            value = null;
            TypeConverter.toBoolean( value );
            fail( "Expected " + value + " to throw IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }
    }

    public void test_toString_char()
    {
        assertEquals( 1, TypeConverter.toString( 'a' ).length() );
    }

    public void test_toChar_String()
    {
        try
        {
            TypeConverter.toChar( (String)null );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toChar( "" );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }
    }

    public void test_toString_int()
    {
        assertEquals(
            Integer.toString( Integer.MAX_VALUE ),
            TypeConverter.toString( Integer.MAX_VALUE )
            );

        assertEquals(
            Integer.toString( Integer.MIN_VALUE ),
            TypeConverter.toString( Integer.MIN_VALUE )
            );

        assertEquals(
            Integer.toString( -1 ),
            TypeConverter.toString( -1 )
            );

        assertEquals(
            Integer.toString( 0 ),
            TypeConverter.toString( 0 )
            );

        assertEquals(
            Integer.toString( 1 ),
            TypeConverter.toString( 1 )
            );
    }

    public void test_toInt_String()
    {
        assertEquals(
            Integer.MAX_VALUE,
            TypeConverter.toInt( Integer.toString( Integer.MAX_VALUE ) )
            );

        assertEquals(
            Integer.MIN_VALUE,
            TypeConverter.toInt( Integer.toString( Integer.MIN_VALUE ) )
            );

        assertEquals(
            -1,
            TypeConverter.toInt( Integer.toString( -1 ) )
            );

        assertEquals(
            0,
            TypeConverter.toInt( Integer.toString( 0 ) )
            );

        assertEquals(
            1,
            TypeConverter.toInt( Integer.toString( 1 ) )
            );

        try
        {
            TypeConverter.toInt( (String)null );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toInt( "not an integer" );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toInt( Long.toString( Long.MAX_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toInt( Float.toString( Float.MAX_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toInt( Double.toString( Double.MAX_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }
    }


    public void test_toString_long()
    {
        assertEquals(
            Long.toString( Long.MAX_VALUE ),
            TypeConverter.toString( Long.MAX_VALUE )
            );

        assertEquals(
            Long.toString( Long.MIN_VALUE ),
            TypeConverter.toString( Long.MIN_VALUE )
            );

        assertEquals(
            Long.toString( -1 ),
            TypeConverter.toString( -1 )
            );

        assertEquals(
            Long.toString( 0 ),
            TypeConverter.toString( 0 )
            );

        assertEquals(
            Long.toString( 1 ),
            TypeConverter.toString( 1 )
            );
    }

    public void test_toLong_String()
    {
        assertEquals(
            Long.MAX_VALUE,
            TypeConverter.toLong( Long.toString( Long.MAX_VALUE ) )
            );

        assertEquals(
            Long.MIN_VALUE,
            TypeConverter.toLong( Long.toString( Long.MIN_VALUE ) )
            );

        assertEquals(
            -1,
            TypeConverter.toLong( Long.toString( -1 ) )
            );

        assertEquals(
            0,
            TypeConverter.toLong( Long.toString( 0 ) )
            );

        assertEquals(
            1,
            TypeConverter.toLong( Long.toString( 1 ) )
            );

        try
        {
            TypeConverter.toLong( (String)null );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toLong( "not a long" );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toLong( Double.toString( Double.MAX_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toLong( Float.toString( Float.MAX_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toLong( Float.toString( Float.MIN_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toLong( Double.toString( Double.MAX_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toLong( Double.toString( Double.MIN_VALUE ) );
            fail( "Expected null to throw an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }
    }

    public void test_toString_float()
    {
        assertEquals(
            Float.toString( Float.MAX_VALUE ),
            TypeConverter.toString( Float.MAX_VALUE )
            );

        assertEquals(
            Float.toString( Float.MIN_VALUE ),
            TypeConverter.toString( Float.MIN_VALUE )
            );

        assertEquals(
            Float.toString( -1.0F ),
            TypeConverter.toString( -1.0F )
            );

        assertEquals(
            Float.toString( 0.0F ),
            TypeConverter.toString( 0.0F )
            );

        assertEquals(
            Float.toString( 1.0F ),
            TypeConverter.toString( 1.0F )
            );

        assertEquals(
            "Infinity",
            TypeConverter.toString( Float.POSITIVE_INFINITY )
            );

        assertEquals(
            "-Infinity",
            TypeConverter.toString( Float.NEGATIVE_INFINITY )
            );
    }

    public void test_toFloat_String()
    {
        float ERROR = 0.0F;

        assertEquals(
            Float.MAX_VALUE,
            TypeConverter.toFloat( Float.toString( Float.MAX_VALUE ) ),
            ERROR
            );

        assertEquals(
            Float.MIN_VALUE,
            TypeConverter.toFloat( Float.toString( Float.MIN_VALUE ) ),
            ERROR
            );

        assertEquals(
            -1.0F,
            TypeConverter.toFloat( Float.toString( -1.0F ) ),
            ERROR
            );

        assertEquals(
            0.0F,
            TypeConverter.toFloat( Float.toString( 0.0F ) ),
            ERROR
            );

        assertEquals(
            1.0F,
            TypeConverter.toFloat( Float.toString( 1.0F ) ),
            ERROR
            );

        try
        {
            TypeConverter.toFloat( (String)null );
            fail( "Expected an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toFloat( "not a float" );
            fail( "Expected an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        assertEquals(
            Float.POSITIVE_INFINITY,
            TypeConverter.toFloat( Double.toString( Double.MAX_VALUE ) ),
            ERROR
            );

        assertEquals(
            Float.POSITIVE_INFINITY,
            TypeConverter.toFloat( Double.toString( Double.POSITIVE_INFINITY ) ),
            ERROR
            );

        assertEquals(
            0F,
            TypeConverter.toFloat( Double.toString( Double.MIN_VALUE ) ),
            ERROR
            );

        assertEquals(
            Float.NEGATIVE_INFINITY,
            TypeConverter.toFloat( Double.toString( Double.NEGATIVE_INFINITY ) ),
            ERROR
            );
    }

    public void test_toString_double()
    {
        assertEquals(
            Double.toString( Double.MAX_VALUE ),
            TypeConverter.toString( Double.MAX_VALUE )
            );

        assertEquals(
            Double.toString( Double.MIN_VALUE ),
            TypeConverter.toString( Double.MIN_VALUE )
            );

        assertEquals(
            Double.toString( -1.0D ),
            TypeConverter.toString( -1.0D )
            );

        assertEquals(
            Double.toString( 0.0D ),
            TypeConverter.toString( 0.0D )
            );

        assertEquals(
            Double.toString( 1.0D ),
            TypeConverter.toString( 1.0D )
            );

        assertEquals(
            "Infinity",
            TypeConverter.toString( Double.POSITIVE_INFINITY )
            );

        assertEquals(
            "-Infinity",
            TypeConverter.toString( Double.NEGATIVE_INFINITY )
            );
    }

    public void test_toDouble_String()
    {
        float ERROR = 0.0F;

        assertEquals(
            Double.MAX_VALUE,
            TypeConverter.toDouble( Double.toString( Double.MAX_VALUE ) ),
            ERROR
            );

        assertEquals(
            Double.MIN_VALUE,
            TypeConverter.toDouble( Double.toString( Double.MIN_VALUE ) ),
            ERROR
            );

        assertEquals(
            -1.0D,
            TypeConverter.toDouble( Double.toString( -1.0D ) ),
            ERROR
            );

        assertEquals(
            0.0D,
            TypeConverter.toDouble( Double.toString( 0.0D ) ),
            ERROR
            );

        assertEquals(
            1.0D,
            TypeConverter.toDouble( Double.toString( 1.0D ) ),
            ERROR
            );

        try
        {
            TypeConverter.toDouble( (String)null );
            fail( "Expected an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }

        try
        {
            TypeConverter.toDouble( "not a float" );
            fail( "Expected an IllegalArgumentException." );
        }
        catch( IllegalArgumentException x )
        {
            ;// correct
        }


        assertEquals(
            Double.POSITIVE_INFINITY,
            TypeConverter.toDouble( Double.toString( Double.POSITIVE_INFINITY ) ),
            ERROR
            );

        assertEquals(
            Double.MIN_VALUE,
            TypeConverter.toDouble( Double.toString( Double.MIN_VALUE ) ),
            ERROR
            );

        assertEquals(
            Double.NEGATIVE_INFINITY,
            TypeConverter.toDouble( Double.toString( Double.NEGATIVE_INFINITY ) ),
            ERROR
            );
    }
}




