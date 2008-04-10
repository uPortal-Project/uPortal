/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.lang;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * The <code>ThrowableHelper_Test</code> class tests
 * <code>ThrowableHelper</code> class.
 *
 * @version $Revision$
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 **/
public class ThrowableHelper_Test
    extends TestCase
{
    /** Class version identifier. */
    public static final String RCS_ID = "@(#) $Header$";

    /**
     * Setup for each test method.
     **/
    public void setUp() {}

    /**
     * Tear down for each test method.
     **/
    public void tearDown() {}

    public ThrowableHelper_Test( String name )
    {
        super( name );
    }
    
    public void testGetDefaultHandler()
    {
        ThrowableHandler defaultHandler =
            ThrowableHelper.getDefaultHandler(
                null
                );
        
        String defaultHandlerClass = 
            defaultHandler.getClass().getName();
        
        String handlerClass = "";
        
        verifyEquals(
            defaultHandlerClass,
            ""
            );
        
        verifyEquals(
            defaultHandlerClass,
            "someUnknownClass"
            );
        
        // Trigger a InstantiationException
        verifyEquals(
            defaultHandlerClass,
            "java.lang.reflect.Constructor"
            );
        
        // Trigger an IllegalAccessException
        verifyEquals(
            defaultHandlerClass,
            "java.lang.reflect.ReflectAccess"
            );

        // Trigger an ExceptionInInitializerError...
    }
    
    private void verifyEquals(
        String correctClassName,
        String className
        )
    {
        ThrowableHandler handler = ThrowableHelper.getDefaultHandler(
            className
            );

        TestCase.assertEquals(
            correctClassName,
            handler.getClass().getName()
            );
    }
    
    public void testGetInternationalizedMessage()
    {
        try
        {
            ThrowableHelper.getInternationalizedMessage(
                null,
                null,
                null
                );
            
            TestCase.fail( "Expected a NullPointerException" );
        }
        catch( NullPointerException x )
        {
            ;//correct
        }
        
        try
        {
            ThrowableHelper.getInternationalizedMessage(
                ThrowableHelper_Test.class,
                null,
                null
                );
            
            TestCase.fail( "Expected a NullPointerException" );
        }
        catch( NullPointerException x )
        {
            ;//correct
        }
        
        String i18nMessage = ThrowableHelper.getInternationalizedMessage(
            ThrowableHelper_Test.class,
            "some.error.message",
            null
            );
        
        i18nMessage = ThrowableHelper.getInternationalizedMessage(
            ThrowableHelper_Test.class,
            "some.error.message",
            new String[]{ null }
            );
        
        i18nMessage = ThrowableHelper.getInternationalizedMessage(
            ThrowableHelper_Test.class,
            "some.error.message",
            new String[]{ "one", "two" }
            );
    }
    
    public void testGetLocalizedMessage()
    {
        Throwable throwable = new Exception();
        
        String l10nMessage = ThrowableHelper.getLocalizedMessage(
            throwable
            );
        
        l10nMessage = ThrowableHelper.getLocalizedMessage(
            throwable,
            java.util.Locale.getDefault()
            );
        
        String i18nMessage = ThrowableHelper.getInternationalizedMessage(
            ThrowableHelper_Test.class,
            "some.error",
            new String[]{ "one", "two" }
            );
        
        l10nMessage = ThrowableHelper.getLocalizedMessage(
            i18nMessage,
            Locale.getDefault()
            );
        
        l10nMessage = ThrowableHelper.getLocalizedMessage(
            "not_a_well_formed_i18n_Message",
            Locale.getDefault()
            );
    }
    
    public void testInitCause()
    {
        try
        {
            ThrowableHelper.initCause(
                null,
                null
                );
            
            TestCase.fail( "Expected NullPointerException" );
        }
        catch( NullPointerException x )
        {
            ;//correct
        }

        ThrowableHelper.initCause(
            new Throwable(),
            new Exception()
            );
    }
    
    public void testCreate()
    {
        ThrowableHelper.create(
            IllegalAccessException.class,
            ThrowableHelper_Test.class,
            "some.property",
            new String[]{"one", "two" },
            new Error( "my random cause " )
            );
    }
    
    public void testHandle()
    {
        ThrowableHelper.handle(
            new Throwable( "something" )
            );
        
        ThrowableHelper.handle(
            ThrowableHelper_Test.class,
            "some.error",
            new String[]{ "one", "two" },
            new Error( "my cause" )
            );
    }
}

/**
 * The <code>ExceptionInInitializerHandler</code> class tests
 * <code>ThrowableHelper</code> class.
 *
 * @version $Revision$
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 **/
class ExceptionInInitializerHandler
    implements ThrowableHandler
{
    /*
      static
      {
      String somethingNull = null;
     
      // Trigger a NullPointerException during static initialization.
      somethingNull.length();
      }
    */

    /**
     * Handles the condition specified in the parameters. The handler
     * can use the client class to resolve the property name of the
     * error message, and generate a localized message from the
     * optional objects.
     *
     * @param client client calling the handle method
     *
     * @param name property name associated with error message
     * 
     * @param objects objects associated with the error message, or
     * <code>null</code>
     *
     * @param cause throwable condition which caused the error, or
     * <code>null</code>
     *
     * @throws NullPointerException if client or property is
     * <code>null</code>
     **/
    public void handle(
        Class client,
        String name,
        String[] objects,
        Throwable cause
        )
    {
        ;//do nothing
    }
}

/**
 * The <code>IllegalAccessHandler</code> class tests
 * <code>ThrowableHelper</code> class.
 *
 * @version $Revision$
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 **/
class IllegalAccessHandler
    implements ThrowableHandler
{
    protected IllegalAccessHandler( String something )
    {
    }
    
    /**
     * Handles the condition specified in the parameters. The handler
     * can use the client class to resolve the property name of the
     * error message, and generate a localized message from the
     * optional objects.
     *
     * @param client client calling the handle method
     *
     * @param name property name associated with error message
     * 
     * @param objects objects associated with the error message, or
     * <code>null</code>
     *
     * @param cause throwable condition which caused the error, or
     * <code>null</code>
     *
     * @throws NullPointerException if client or property is
     * <code>null</code>
     **/
    public void handle(
        Class client,
        String name,
        String[] objects,
        Throwable cause
        )
    {
        ;//do nothing
    }
}

/**
 * The <code>InstantiationExceptionHandler</code> class tests
 * <code>ThrowableHelper</code> class.
 *
 * @version $Revision$
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 **/
class InstantiationExceptionHandler
    implements ThrowableHandler
{
    /**
     * Hide the constructor to trigger the instantiation exception.
     **/
    private InstantiationExceptionHandler()
    {
    }
    
    /**
     * Handles the condition specified in the parameters. The handler
     * can use the client class to resolve the property name of the
     * error message, and generate a localized message from the
     * optional objects.
     *
     * @param client client calling the handle method
     *
     * @param name property name associated with error message
     * 
     * @param objects objects associated with the error message, or
     * <code>null</code>
     *
     * @param cause throwable condition which caused the error, or
     * <code>null</code>
     *
     * @throws NullPointerException if client or property is
     * <code>null</code>
     **/
    public void handle(
        Class client,
        String name,
        String[] objects,
        Throwable cause
        )
    {
        ;//do nothing
    }
}
