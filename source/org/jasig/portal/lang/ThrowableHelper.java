/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * The <code>ThrowableHelper</code> class defines a set of utility
 * methods for handling common error management operations in a
 * fashion which takes advantage of the J2SDK 1.4 constructs while
 * maintaining J2SDK 1.3 compatibility.
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version "$Revision$"
 **/
public final class ThrowableHelper
{
    /** Statically configured internationalization token. */
    private static final String I18N_TOKEN = Resources.getString(
        ThrowableHelper.class,
        "i18n_token"
        );

    /** Default throwable handler. */
    private static final ThrowableHandler DEFAULT_HANDLER = getDefaultHandler(
        Resources.getString(
            ThrowableHelper.class,
            "default_throwable_handler_class"
            )
        );

    /**
     * Returns a constructed instance of the default handler. The
     * class name passed to the method must be an implementation of
     * the <code>ThrowableHandler</code> interface and have an
     * accessible default constructor.
     *
     * @param className name of the throwable handler class
     * 
     * @return instance of the specified handler class, or the default
     * error handler
     **/
    public static final ThrowableHandler getDefaultHandler(
        String className
        )
    {
        ThrowableHandler handler = new ThrowableHandler()
            {
                /**
                 * Handles the condition specified in the
                 * parameters. The handler can use the client class to
                 * resolve the property name of the error message, and
                 * generate a localized message from the optional
                 * objects.
                 *
                 * @param client client calling the handle method
                 *
                 * @param property property name associated with error
                 * message
                 * 
                 * @param objects objects associated with the error
                 * message, or <code>null</code>
                 *
                 * @param cause throwable condition which caused the
                 * error, or <code>null</code>
                 *
                 * @throws NullPointerException if client or property
                 * is <code>null</code>
                 **/
                public void handle(
                    Class client,
                    String property,
                    String[] objects, 
                    Throwable cause
                    )
                {
                    if( null != property )
                    {
                        String errorMessage = Resources.getString(
                            client,
                            property,
                            objects
                            );

                        System.err.println( 
                            errorMessage 
                            );
                    }
                
                    if( null != cause )
                    {
                        cause.printStackTrace();
                    }
                }
            };            

        try
        {
            if( null != className && !"".equals( className ) )
            {    
                handler = (ThrowableHandler)Class.forName(
                    className
                    ).newInstance();
            }
        }
        catch( ClassNotFoundException x )
        {
            ThrowableHelper.handle(
                ThrowableHelper.class,
                "error.class_not_found",
                new String[]{ className },
                x,
                handler
                );
        }
        catch( IllegalAccessException x )
        {   
            ThrowableHelper.handle(
                ThrowableHelper.class,
                "error.illegal_access",
                new String[]{ className },
                x,
                handler
                );
        }
        catch( InstantiationException x )
        {
            ThrowableHelper.handle(
                ThrowableHelper.class,
                "error.instantiation",
                new String[]{ className },
                x,
                handler
                );
        }
        catch( ExceptionInInitializerError x )
        {
            ThrowableHelper.handle(
                ThrowableHelper.class,
                "error.initialization",
                new String[]{ className },
                x,
                handler
                );
        }

        return handler;
    }

    /**
     * Returns an internationalized error message which can be
     * reconstructed into a localized error message with the
     * <code>getLocalizedMessage</code> method call.
     *
     * @param client class of the client making this call
     *
     * @param error error property name of the error condition
     *
     * @return internationalized error message
     **/
    public static final String getInternationalizedMessage(
        Class client,
        String error
        )
    {
        return getInternationalizedMessage(
            client,
            error,
            null
            );
    }

    /**
     * Returns an internationalized error message which can be
     * reconstructed into a localized error message with the
     * <code>getLocalizedMessage</code> method call.
     *
     * @param client class of the client making this call
     *
     * @param error error property name of the error condition
     *
     * @param objects string objects which should be stored for the
     * message
     *
     * @return internationalized error message
     *
     * @throws NullPointerException if client, or error parameters are
     * null
     **/
    public static final String getInternationalizedMessage(
        Class client,
        String error,
        String[] objects
        )
    {
        if( null == client )
        {
            throw new NullPointerException(
                ThrowableHelper.getInternationalizedMessage(
                    ThrowableHelper.class,
                    "getInternationalizedMessage.client_argument_is_null"
                    )
                );
        }

        if( null == error )
        {
            throw new NullPointerException(
                ThrowableHelper.getInternationalizedMessage(
                    ThrowableHelper.class,
                    "getInternationalizedMessage.error_argument_is_null"
                    )
                );
        }

        StringBuffer buffer = new StringBuffer().
            append( I18N_TOKEN ).
            append( client.getName() ).
            append( I18N_TOKEN ).
            append( error );

        if( null != objects )
        {
            for( int i = 0; i < objects.length; i++ )
            {
                buffer.append( I18N_TOKEN ).append( objects[i] );
            }
        }

        return buffer.toString();
    }

    /**
     * Returns the localized error message associated with the
     * specified internationalized error message constructed with the
     * <code>getInternationalizedMessage</code> method. The default
     * locale is used.
     *
     * @param i18nMessage internationalized error message
     *
     * @return localized error message, or <code>null</code>
     **/
    public static final String getLocalizedMessage(
        String i18nMessage
        )
    {
        return getLocalizedMessage(
            i18nMessage,
            Locale.getDefault()
            );
    }
    
    /**
     * Returns the localized string associated with the argument.
     * 
     * @param throwable object whose message is to be localized
     * 
     * @return localized message associated with <code>Throwable</code>
     **/
    public static final String getLocalizedMessage(
        Throwable throwable
        )
    {
        return getLocalizedMessage(
            throwable,
            Locale.getDefault()
            );
    }
    
    /**
     * Returns the localized string associated with the argument
     * in the specified locale, if possible. If the locale is not
     * supported the default locale message will be returned.
     * 
     * @param throwable object whose message is to be localized
     * 
     * @param locale locale to localized the message
     * 
     * @return localized message associated with <code>Throwable</code>
     **/
    public static final String getLocalizedMessage(
        Throwable throwable,
        Locale locale
        )
    {
        return getLocalizedMessage(
            throwable.getMessage(),
            locale
            );
    }
    
    /**
     * Returns the localized error message associated with the
     * specified internationalized error message constructed with the
     * <code>getInternationalizedMessage</code> method. The locale of
     * for the translation is picked up from the current session.
     *
     * @param i18nMessage internationalized error message
     *
     * @param locale locale to translate the message
     *
     * @return localized error message, or <code>null</code>
     **/
    public static final String getLocalizedMessage(
        String i18nMessage,
        Locale locale
        )
    {
        String l10nMessage = null;

        if( null != i18nMessage )
        {
            try
            {
                // if the i18nMessage starts with a I18N_TOKEN, the
                // tokenizer's nextToken returns the next token
                // instead of null, so add some data to make sure this
                // skip does not happen automatically
                StringTokenizer tokenizer = new StringTokenizer(
                    "junk" + i18nMessage,
                    I18N_TOKEN
                    );
                
                // now skip the "junk" token
                tokenizer.nextToken();
                
                String className = tokenizer.nextToken();
                String errorName = tokenizer.nextToken();

                List strings = new ArrayList();

                while( tokenizer.hasMoreTokens() )
                {
                    strings.add( tokenizer.nextToken() );
                }

                String[] objects = new String[ strings.size() ];
                for( int i = 0; i < objects.length; i++ )
                {
                    objects[i] = (String)strings.get( i );
                }

                ResourceBundle bundle = ResourceBundle.getBundle( 
                    className, 
                    locale 
                    );
                     
                String message = bundle.getString(
                    errorName
                    );

                l10nMessage = MessageFormat.format( message, (Object[])objects );
            }
            catch( NoSuchElementException x )
            {
                // Not a well-formed internationalized string.
                l10nMessage = i18nMessage;
            }
        }

        return l10nMessage;
    }

    /**
     * Initizlizes the chained cause of the throwable if possible
     * using the J2SDK 1.4 constructs. In a J2SDK pre-1.4 environment,
     * this method does nothing.
     *
     * @param throwable throwable whose cause should be set
     *
     * @param cause throwable which caused the throwable condition
     **/
    public static final void initCause(
        Throwable throwable,
        Throwable cause
        )
    {
        try
        {
            Method initCause = throwable.getClass().getMethod(
                "initCause",
                new Class[]{ Throwable.class }
                );

            initCause.invoke( throwable, new Object[]{ cause } );
        }
        catch( NoSuchMethodException x )
        {
            ThrowableHelper.handle( x );
        }
        catch( InvocationTargetException x )
        {
            ThrowableHelper.handle( x );
        }
        catch( IllegalAccessException x )
        {
            ThrowableHelper.handle( x );
        }
        catch( IllegalStateException x )
        {
            ThrowableHelper.handle( x );
        }
    }

    /**
     * Creates the specified <code>Throwable</code> object with the
     * internationalized error code and arguments. If the cause object
     * is not null, the specified object's <code>initCause</code>
     * method will be invoked reflectively to enable the standard
     * chaining constructs. The specified error code is an
     * internationalized error property value which must be defined in
     * a resource bundle associated with the client, specificifically
     * the code will perform a <code>ResourceBundle.getBundle( <name
     * of client> )</code> to resolve the resource.
     *
     * Invoking this method is equivalent to creating the
     * <code>Throwable</code> with an internationalized message and
     * then invoking <code>ThrowableHelper.initCause</code>, e.g.,:
     *
     * <code><pre>
     * Throwable someCause = ...
     * 
     * String i18nMessage = ThrowableHelper.getInternationalizedMessage(
     *     MyApplication.class,
     *     "myapplication.some_error_code",
     *     new String[]{ "32", "1024" }
     *     );
     *
     * IllegalArgumentException iae = new IllegalArgumentException(
     *     i18nMessage
     *     );
     *
     * ThrowableHelper.initCause(
     *     iae,
     *     someCause
     *     );
     * </pre></code>
     *
     * @param throwableClass throwable object to be constructed
     *
     * @param client class file of the client
     *
     * @param propertyName internationalized error code property name
     * defined in the client's resouce bundle
     *
     * @param args arguments for the localized message
     *
     * @param cause cause of the created exception, or
     * <code>null</code>
     *
     * @return throwable object of the specified type
     **/
    public static final Throwable create(
        Class throwableClass,
        Class client,
        String propertyName,
        String [] args,
        Throwable cause
        )
    {
        Throwable throwable = null;

        String i18nMessage = getInternationalizedMessage(
            client,
            propertyName,
            args
            );

        throwable = createThrowable(
            throwableClass,
            new Class[]{ String.class },
            new Object[]{ i18nMessage }
            );

        if( null != cause )
        {
            ThrowableHelper.initCause(
                throwable,
                cause
                );
        }
        
        return throwable.fillInStackTrace();
    }

    /**
     * Creates a <code>Throwable</code> object of the specified type
     * using a constructor matching the specified arguments. The
     * constructed object is returned. It is considered a static
     * programming error if the requested object cannot be constructed
     * with the specified parameters, so checking for
     * <code>null</code> in the return is inappropriate; instead,
     * validate that the specified <code>Throwable</code> class can be
     * constructed in the specified manner by, for example, building
     * unit test code which performs the same construction..
     * 
     * @param throwableClass class to construct
     *
     * @param clsArguments constructor argument class types
     *
     * @param objArguments constructor argument object values
     *
     * @return <code>Throwable</code> object of the specified type, or
     * <code>null</code>
     **/
    private static final Throwable createThrowable(
        Class throwableClass,
        Class[] clsArguments,
        Object[] objArguments
        )
    {
        Throwable throwable = null;

        try
        {
            Constructor ctor = throwableClass.getConstructor(
                clsArguments
                );

            throwable = (Throwable) ctor.newInstance(
                objArguments
                );
        }
        catch( NoSuchMethodException x )
        {
            ThrowableHelper.handle( x );
        }
        catch( IllegalAccessException x )
        {
            ThrowableHelper.handle( x );
        }
        catch( InstantiationException x )
        {
            ThrowableHelper.handle( x );
        }
        catch( InvocationTargetException x )
        {
            ThrowableHelper.handle( x );
        }

        return throwable;
    }
    
    /**
     * Handles the throwable condition specified in the parameter. The
     * default handler is used to process this handler.
     *  
     * @param cause throwable condition which should be handled
     **/
    public static final void handle(
        Throwable cause
        )
    {
        ThrowableHelper.handle(
            null,
            null,
            null,
            cause,
            DEFAULT_HANDLER
            );
    }

    /**
     * Handles the throwable condition specified in the
     * parameters. The default handler is used to process this
     * handler.
     *  
     * @param client client performing the handling
     * 
     * @param cause throwable condition which should be handled
     * 
     * @param message associated with handling the error
     * 
     * @param objects associated with the error message
     **/
    public static final void handle(
        Class client,
        String message,
        String[] objects,
        Throwable cause
        )
    {
        ThrowableHelper.handle(
            client,
            message,
            objects,
            cause,
            DEFAULT_HANDLER
            );
    }

    /**
     * Handles the throwable condition specified in the parameters. If
     * the specified handler is <code>null</code> the default handler
     * is used; otherwise, the specified handler is used to process
     * this request.
     *  
     * @param client client performing the handling
     * 
     * @param cause throwable condition which should be handled
     * 
     * @param message associated with handling the error
     * 
     * @param objects associated with the error message
     * 
     * @param handler which should be executed instead of the default
     * handler
     **/
    public static final void handle(
        Class client,
        String message,
        String[] objects,
        Throwable cause,
        ThrowableHandler handler
        )
    {
        ThrowableHandler errorHandler = handler;
        
        if( null == errorHandler )
        {
            errorHandler = DEFAULT_HANDLER;
        }

        errorHandler.handle(
            client,
            message,
            objects,
            cause
            );
    }

    /**
     * Private constructor.
     **/
    private ThrowableHelper(){}
}
