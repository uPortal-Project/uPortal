/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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


package  org.jasig.portal.services;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.utils.ResourceLoader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

/**
 * ExternalServices starts up all the runtime services for the uPortal.
 * These services can be customized at an installation location by editing the
 * services.xml file under the properties directory.  For example, the
 * connection pooling is a service that can be provided by different vendor
 * implementations.
 * Services are bound into the uPortal /services JNDI branch if <jndiname/> element
 * is specified in the service description.
 * 
 * @author Sridhar Venkatesh <svenkatesh@interactivebusiness.com>
 * @version $Revision$
 */
public class ExternalServices {
    
    private static final Log log = LogFactory.getLog(ExternalServices.class);
    
  private ServiceHandler svcHandler;
  private Context servicesContext;

  public ExternalServices(Context servicesContext) {
    this.servicesContext=servicesContext;
    svcHandler = new ServiceHandler();
  }

    public static void startServices(Context servicesContext)
      throws PortalException
    {

    InputStream svcDescriptor = null;

    try
    {
        svcDescriptor = ResourceLoader
            .getResourceAsStream( ExternalServices.class,
                                  "/properties/services.xml" );
    }
    catch (Exception ex)
    {
        throw new PortalException ("Failed to load services.xml. External " +
                                   "portal services will not be started", ex);
    }

    CarResources cRes = CarResources.getInstance();

    if ( svcDescriptor != null ||
         cRes.hasDescriptors() )
    {
      ExternalServices svcMgr = new ExternalServices(servicesContext);

      if ( cRes.hasDescriptors() )
      {
          try {
              cRes.getServices( (ContentHandler) svcMgr.svcHandler );
          } catch (Exception ex) {
              throw new PortalException ("Failed to start external portal " +
                                         "services in CAR descriptors.", ex);
          }
      }
      if ( svcDescriptor != null )
      {
          try {
              SAXParser parser = svcMgr.createParser();
              parser.parse(svcDescriptor, svcMgr.svcHandler);
          } catch (Exception ex) {
              throw new PortalException ("Failed to start external portal " +
                                         "services defined in services.xml.",
                                         ex);
          } finally {
            try{
                svcDescriptor.close(); //do not need to check for null.
           } catch(IOException exception) {
                log.error( "ExternalServices:startServices()::could not close InputStream "+ exception);   
            }
         }
      }
    }
  }

  protected SAXParser createParser() throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    return factory.newSAXParser();
  }

  /**
   * Controls output of ExternalServices.
   * @param msg a string to output
   */
  protected void outputMessage(String msg) {
    System.out.println("External services: " + msg);
    log.info( "External services: " + msg);
  }

  /**
   * Returns the appropriate class for the given class name.  Checks to see
   * whether the class belongs to a primitive data type or a Java Object.
   * @param className - Name of the class. Primitive datatypes must be specified
   *                    as xxx.class or Xxxx.TYPE. (e.g. int.class or Integer.TYPE).
   */
  public static Class getClassObject (String className) throws Exception {
    if ((className.indexOf("TYPE") != -1) || (className.indexOf("class") != -1)) {
      if (className.equals("boolean.class") || className.equals("Boolean.TYPE")) {
        return Boolean.TYPE;
      } else if (className.equals("byte.class") || className.equals("Byte.TYPE")) {
        return Byte.TYPE;
      } else if (className.equals("short.class") || className.equals("Short.TYPE")) {
        return Short.TYPE;
      } else if (className.equals("char.class") || className.equals("Char.TYPE")) {
        return java.lang.Character.TYPE;
      } else if (className.equals("int.class") || className.equals("Integer.TYPE")) {
        return Integer.TYPE;
      } else if (className.equals("long.class") || className.equals("Long.TYPE")) {
        return Long.TYPE;
      } else if (className.equals("float.class") || className.equals("Float.TYPE")) {
        return Float.TYPE;
      } else if (className.equals("double.class") || className.equals("Double.TYPE")) {
        return Double.TYPE;
      }
    } else {
     return CarResources.getInstance().getClassLoader().loadClass(className);
    }
    return null;
  }

  class ServiceItem {
    String name;
    String javaClass;
    String jndiName;
    String startMethod;
    String methodType;
    List argList;

    public void setName (String svcName) { name = svcName; }
    public String getName () { return name; }

    public void setJavaClass (String svcClass) { javaClass = svcClass; }
    public String getJavaClass () { return javaClass; }

    public void setStartMethod (String methodName) { startMethod = methodName; }
    public String getStartMethod () { return startMethod; }

    public void setJndiName (String name) { this.jndiName = name; }
    public String getJndiName () { return jndiName; }

    public void setMethodType(String type) { methodType = type; }

    public boolean isStatic() { return !(methodType == null); }

    public void addArgument (Argument argItem) {
     if (argList == null)
       argList = new ArrayList();

     argList.add(argItem);
    }

    public Object[] getArguments() throws Exception {
     Object[] args;

     if (argList != null) {
       args = new Object[argList.size()];
       for (int i=0; i < argList.size(); i++) {
         Argument argItem = (Argument)argList.get(i);
         args[i] = argItem.getValue();
       }
     }
     else
       args = new Object[0];

     return args;
    }

    public Class[] getArgumentClasses() throws Exception {
     Class[] classNames = null;

     if (argList != null) {
       classNames = new Class[argList.size()];

       for (int i=0; i < argList.size(); i++) {
         Argument argItem = (Argument)argList.get(i);
         String className = argItem.getDataType();

         if (argItem.getArray()) {
           classNames[i] = Array.newInstance(getClassObject(className), 1).getClass();
         } else {
           classNames[i] = getClassObject(className);
         }
       }
     }

     return classNames;
    }
  }

  class Argument {
    String datatype;
    boolean array;
    ArrayList values = new ArrayList(); // when array=true, there may be more than one value

    public void addValue (String argValue) { values.add(argValue); }
    public String getDataType() { return datatype; }
    public void setDataType(String argDataType) { datatype = argDataType; }
    public boolean getArray() { return array; }
    public void setArray(boolean b) {array = b; }

    public Object getValue() throws Exception {
      Object value = null;
      if (array) {
        value = Array.newInstance(getClassObject(datatype), values.size());
        for (int i = 0; i < values.size(); i++) {
          Array.set(value, i, values.get(i));
        }
      } else {
        value = getValue (datatype, (String)values.get(0));
      }
      return value;
    }

    private Object getValue (String className, String value) throws Exception {
      if ((className.indexOf("TYPE") != -1) || (className.indexOf("class") != -1)) {
        try {
          if (className.equals("boolean.class") || className.equals("BOOLEAN.TYPE")) {
            return new Boolean(value);
          } else if (className.equals("byte.class") || className.equals("Byte.TYPE")) {
            return new Byte(value);
          } else if (className.equals("short.class") || className.equals("Short.TYPE")) {
            return new Short(value);
          } else if (className.equals("char.class") || className.equals("Char.TYPE")) {
            return new java.lang.Character(value.charAt(0));
          } else if (className.equals("int.class") || className.equals("Integer.TYPE")) {
            return new Integer(value);
          } else if (className.equals("long.class") || className.equals("Long.TYPE")) {
            return new Long(value);
          } else if (className.equals("float.class") || className.equals("Float.TYPE")) {
            return new Float(value);
          } else if (className.equals("double.class") || className.equals("Double.TYPE")) {
            return new Double(value);
          }
        } catch (NumberFormatException nfe) {
          outputMessage("Cannot convert " + value + " to declared datatype class " + className);
        }
      } else {
        return value;
      }
      return null;
    }
  }

  class ServiceHandler extends org.xml.sax.helpers.DefaultHandler {
    ServiceItem svcItem;
    String elementName;
    Argument argItem;
    boolean argProcessing = false;
    boolean jndiNameporcessing=false;

    public void startElement (String namespaceURI, String localName, String qName, Attributes atts) {
      elementName = qName;
      if (qName.equals("service")) {
        svcItem = new ServiceItem();
      } else if (qName.equals("method")) {
        svcItem.setMethodType(atts.getValue("type"));
      } else if (qName.equals("arguments")) {
        argProcessing = true;
      } else if (qName.equals("argitem")) {
        argItem = new Argument();
      } else if (qName.equals("datatype")) {
        String array = atts.getValue("array");
        argItem.setArray(array != null && array.equals("true"));
      }
    }

    public void endElement (String namespaceURI, String localName, String qName) {
      elementName = null;  // The element has ended.  Null it.

      if (qName.equals("service")) {
        String name = svcItem.getName();
        String javaClass = svcItem.getJavaClass();
        Class[] classNames = null;
        Object[] args = null;
        Class svcClass = null;

        try {
          svcClass   = CarResources.getInstance()
              .getClassLoader().loadClass(javaClass);
          args       = svcItem.getArguments();
          classNames = svcItem.getArgumentClasses();
        } catch (java.lang.ClassNotFoundException cnfe) {
          outputMessage("Class not found - " + cnfe.getMessage());
          return;
        } catch (Exception e) {
          outputMessage("The service \"" + svcItem.getName() + "\" FAILED TO START.");
          return;
        }

        try {
            Object obj=null;
            Object returnObject=null;
            
          // check if any method is specified
          if(svcItem.getStartMethod()!=null) {
              Method startMethod = svcClass.getMethod(svcItem.getStartMethod(), classNames);
              if(Modifier.isStatic(startMethod.getModifiers())) {
                  // no need to instantiate an object
                  returnObject=startMethod.invoke(null,args);
              } else {
                  // instantiate 
                  obj = svcClass.newInstance();
                  returnObject=startMethod.invoke(obj,args);
              }
              outputMessage("initialized \"" + svcItem.getName() + "\"");
          }
 
          // check if jndi binding needed
          if(svcItem.getJndiName()!=null) {
              if(returnObject!=null) {
                  // a non-void method was specified
                  // in the service description, bind 
                  // returned object
                  servicesContext.bind(svcItem.getJndiName(), returnObject);
                  outputMessage("bound intialization result for service \"" + svcItem.getName() + "\"");
              } else {
                  if(obj==null) {
                      // instantiate
                      obj = svcClass.newInstance();
                  }
                  servicesContext.bind(svcItem.getJndiName(), obj);
                  outputMessage("bound class instance for service \"" + svcItem.getName() + "\"");
              }
          }
          return;
        } catch (java.lang.NoSuchMethodException nsme) {
          outputMessage("Method not found - " + nsme.getMessage());
        } catch (java.lang.Exception ex) {
          outputMessage("General Exception - " + ex.getMessage());
          ex.printStackTrace();
        }
        outputMessage("The service \"" + svcItem.getName() + "\" FAILED TO START.");

      } else if (qName.equals("arguments")) {
        argProcessing = false;
      } else if (qName.equals("argitem")) {
        svcItem.addArgument(argItem);
        argItem = null;
      }
    }

    public void characters (char ch[], int start, int length) {
      if (elementName == null)
        return;

      String chValue = new String(ch, start, length);

      if (elementName.equals("name")) {
        svcItem.setName(chValue);
      } else if (elementName.equals("class")) {
        svcItem.setJavaClass(chValue);
      } else if (elementName.equals("jndi_name")) {
        svcItem.setJndiName(chValue);
      } else if (elementName.equals("method")) {
        svcItem.setStartMethod(chValue);
      } else if (elementName.equals("datatype") && argProcessing) {
          argItem.setDataType(chValue);
      } else if (elementName.equals("value")) {
        if (argProcessing) {
          argItem.addValue(chValue);
        }
      }
    }
  }
}




