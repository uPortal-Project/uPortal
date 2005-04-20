package org.jasig.portal.layout.dlm;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
   Loads the class specified in className and verifies that it conforms to
   the LayoutDecorator interface. If a null class name is passed in this
   class returns null. If an error occurs an exception is thrown indicating
   the problems that occurred.
 */
class DecoratorLoader
{
    public static final String RCS_ID = "@(#) $Header$";

    static LayoutDecorator load( String className )
    throws Exception
    {
        Class theClass = null;
        try
        {
            theClass = Class.forName( className );
        }
        catch( ClassNotFoundException cnfe )
        {
            throw new Exception( "java.lang.ClassNotFoundException occurred" +
                                 " while loading class '" + 
                                 className + "' (or one of its " +
                                 "dependent classes)." );
        }
        catch( ExceptionInInitializerError eiie )
        {
            StringWriter s = new StringWriter();
            PrintWriter p = new PrintWriter( s );
            eiie.printStackTrace( p );
            p.flush();
            
            throw new Exception( "java.lang.ExceptionInInitializerError " +
                                 "occurred while " +
                                 "loading class '" + 
                                 className + "' (or one of its " +
                                 "dependent classes). \nThis indicates"   +
                                 "that an exception " +
                                 "occurred during evaluation of a static" +
                                 " initializer or the initializer for a " +
                                 "static variable. The stack trace is as" +
                                 " follows:\n----------\n" + s.toString() +
                                 "\n----------" );
        }
        catch( LinkageError le )
        {
            throw new Exception( "java.lang.LinkageError occurred while " +
                                 "loading class '" + 
                                 className + "'. \nThis typically means " +
                                 "that a dependent class has changed " +
                                 "incompatibly after compiling this class. " );
        }

        Object theInstance = null;

        try
        {
            theInstance = theClass.newInstance();
        }
        catch( IllegalAccessException iae ) 
        {
            throw new Exception( "java.lang.IllegalAccessException occurred " +
                                 "while loading class '" + 
                                 className + "' (or one of its " +
                                 "dependent classes). \nVerify that this " +
                                 "is a public class " +
                                 "and that it contains a public, zero " +
                                 "argument constructor." );
        }
        catch( InstantiationException ie ) 
        {
            throw new Exception( "java.lang.InstantiationException occurred " +
                                 "while loading class '" + 
                                 className + "' (or one of its " +
                                 "dependent classes). \nVerify that the " +
                                 "specified class is a " +
                                 "class and not an interface or abstract " +
                                 "class." );
        }
        try
        {
            return (LayoutDecorator) theInstance;
        }
        catch( ClassCastException cce ) 
        {
            throw new Exception( "java.lang.ClassCastException occurred " +
                                 "while loading class '" + 
                                 className + "'. \nVerify that the " +
                                 "class implements the " +
                                 "LayoutDecorator interface." );
        }
    }
}
