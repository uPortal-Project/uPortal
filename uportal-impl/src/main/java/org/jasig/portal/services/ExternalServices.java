/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services;

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
import org.jasig.portal.jndi.IJndiManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jndi.JndiTemplate;
import org.xml.sax.Attributes;

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
 * @deprecated Configure services as Spring beans, this class only remains to support CARs 
 */
@Deprecated
public class ExternalServices implements InitializingBean {

    private static final Log log = LogFactory.getLog(ExternalServices.class);

    private IJndiManager jndiManager;
    private CarResources carResources;
    private ServiceHandler svcHandler;
    private Context servicesContext;
    
    public static void startServices(Context servicesContext) throws PortalException {
        log.warn("Method 'public static void startServices(Context)' is no longer supported. ExternalServices will be initialized by Spring");
    }
    
    public IJndiManager getJndiManager() {
        return this.jndiManager;
    }
    /**
     * @param jndiManager the jndiManager to set
     */
    @Required
    public void setJndiManager(IJndiManager jndiManager) {
        this.jndiManager = jndiManager;
    }

    public CarResources getCarResources() {
        return this.carResources;
    }
    /**
     * @param carResources the carResources to set
     */
    @Required
    public void setCarResources(CarResources carResources) {
        this.carResources = carResources;
    }


    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        final JndiTemplate jndiTemplate = this.jndiManager.getJndiTemplate();
        this.servicesContext = (Context)jndiTemplate.lookup("/services", Context.class);
        this.svcHandler = new ServiceHandler();
        
        
        if (this.carResources.hasDescriptors()) {
            try {
                this.carResources.getServices(this.svcHandler);
            }
            catch (final Exception ex) {
                throw new PortalException("Failed to start external portal " + "services in CAR descriptors.", ex);
            }
        }
    }

    protected SAXParser createParser() throws Exception {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        return factory.newSAXParser();
    }

    /**
     * Controls output of ExternalServices.
     * @param msg a string to output
     */
    protected void outputMessage(String msg) {
        System.out.println("External services: " + msg);
        log.info("External services: " + msg);
    }

    /**
     * Returns the appropriate class for the given class name.  Checks to see
     * whether the class belongs to a primitive data type or a Java Object.
     * @param className - Name of the class. Primitive datatypes must be specified
     *                    as xxx.class or Xxxx.TYPE. (e.g. int.class or Integer.TYPE).
     */
    public Class<?> getClassObject(String className) throws Exception {
        if (className.indexOf("TYPE") != -1 || className.indexOf("class") != -1) {
            if (className.equals("boolean.class") || className.equals("Boolean.TYPE")) {
                return Boolean.TYPE;
            }
            else if (className.equals("byte.class") || className.equals("Byte.TYPE")) {
                return Byte.TYPE;
            }
            else if (className.equals("short.class") || className.equals("Short.TYPE")) {
                return Short.TYPE;
            }
            else if (className.equals("char.class") || className.equals("Char.TYPE")) {
                return java.lang.Character.TYPE;
            }
            else if (className.equals("int.class") || className.equals("Integer.TYPE")) {
                return Integer.TYPE;
            }
            else if (className.equals("long.class") || className.equals("Long.TYPE")) {
                return Long.TYPE;
            }
            else if (className.equals("float.class") || className.equals("Float.TYPE")) {
                return Float.TYPE;
            }
            else if (className.equals("double.class") || className.equals("Double.TYPE")) {
                return Double.TYPE;
            }
        }
        else {
            return carResources.getClassLoader().loadClass(className);
        }
        return null;
    }

    class ServiceItem {
        StringBuffer name = new StringBuffer("");
        StringBuffer javaClass = new StringBuffer("");
        StringBuffer jndiName = new StringBuffer("");
        StringBuffer startMethod = new StringBuffer("");
        StringBuffer methodType = new StringBuffer("");
        List<Argument> argList;

        public void setName(String svcName) {
            this.name.append(svcName);
        }

        public String getName() {
            return this.name.toString();
        }

        public void setJavaClass(String svcClass) {
            this.javaClass.append(svcClass);
        }

        public String getJavaClass() {
            return this.javaClass.toString();
        }

        public void setStartMethod(String methodName) {
            this.startMethod.append(methodName);
        }

        public String getStartMethod() {
            return this.startMethod.toString();
        }

        public void setJndiName(String name) {
            this.jndiName.append(name);
        }

        public String getJndiName() {
            return this.jndiName.toString();
        }

        public void setMethodType(String type) {
            this.methodType.append(type);
        }

        public boolean isStatic() {
            return this.methodType.length() > 0;
        }

        public void addArgument(Argument argItem) {
            if (this.argList == null) {
                this.argList = new ArrayList<Argument>();
            }

            this.argList.add(argItem);
        }

        public Object[] getArguments() throws Exception {
            Object[] args;

            if (this.argList != null) {
                args = new Object[this.argList.size()];
                for (int i = 0; i < this.argList.size(); i++) {
                    final Argument argItem = this.argList.get(i);
                    args[i] = argItem.getValue();
                }
            }
            else {
                args = new Object[0];
            }

            return args;
        }

        public Class<?>[] getArgumentClasses() throws Exception {
            Class<?>[] classNames = null;

            if (this.argList != null) {
                classNames = new Class[this.argList.size()];

                for (int i = 0; i < this.argList.size(); i++) {
                    final Argument argItem = this.argList.get(i);
                    final String className = argItem.getDataType();

                    if (argItem.getArray()) {
                        classNames[i] = Array.newInstance(getClassObject(className), 1).getClass();
                    }
                    else {
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
        ArrayList<String> values = new ArrayList<String>(); // when array=true, there may be more than one value

        public void addValue(String argValue) {
            this.values.add(argValue);
        }

        public String getDataType() {
            return this.datatype;
        }

        public void setDataType(String argDataType) {
            this.datatype = argDataType;
        }

        public boolean getArray() {
            return this.array;
        }

        public void setArray(boolean b) {
            this.array = b;
        }

        public Object getValue() throws Exception {
            Object value = null;
            if (this.array) {
                value = Array.newInstance(getClassObject(this.datatype), this.values.size());
                for (int i = 0; i < this.values.size(); i++) {
                    Array.set(value, i, this.values.get(i));
                }
            }
            else {
                value = this.getValue(this.datatype, this.values.get(0));
            }
            return value;
        }

        private Object getValue(String className, String value) throws Exception {
            if (className.indexOf("TYPE") != -1 || className.indexOf("class") != -1) {
                try {
                    if (className.equals("boolean.class") || className.equals("BOOLEAN.TYPE")) {
                        return new Boolean(value);
                    }
                    else if (className.equals("byte.class") || className.equals("Byte.TYPE")) {
                        return new Byte(value);
                    }
                    else if (className.equals("short.class") || className.equals("Short.TYPE")) {
                        return new Short(value);
                    }
                    else if (className.equals("char.class") || className.equals("Char.TYPE")) {
                        return new java.lang.Character(value.charAt(0));
                    }
                    else if (className.equals("int.class") || className.equals("Integer.TYPE")) {
                        return new Integer(value);
                    }
                    else if (className.equals("long.class") || className.equals("Long.TYPE")) {
                        return new Long(value);
                    }
                    else if (className.equals("float.class") || className.equals("Float.TYPE")) {
                        return new Float(value);
                    }
                    else if (className.equals("double.class") || className.equals("Double.TYPE")) {
                        return new Double(value);
                    }
                }
                catch (final NumberFormatException nfe) {
                    ExternalServices.this.outputMessage("Cannot convert " + value + " to declared datatype class "
                            + className);
                }
            }
            else {
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
        boolean jndiNameporcessing = false;

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            this.elementName = qName;
            if (qName.equals("service")) {
                this.svcItem = new ServiceItem();
            }
            else if (qName.equals("method")) {
                this.svcItem.setMethodType(atts.getValue("type"));
            }
            else if (qName.equals("arguments")) {
                this.argProcessing = true;
            }
            else if (qName.equals("argitem")) {
                this.argItem = new Argument();
            }
            else if (qName.equals("datatype")) {
                final String array = atts.getValue("array");
                this.argItem.setArray(array != null && array.equals("true"));
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) {
            this.elementName = null; // The element has ended.  Null it.

            if (qName.equals("service")) {
                final String javaClass = this.svcItem.getJavaClass();
                Class<?>[] classNames = null;
                Object[] args = null;
                Class<?> svcClass = null;

                try {
                    svcClass = carResources.getClassLoader().loadClass(javaClass);
                    args = this.svcItem.getArguments();
                    classNames = this.svcItem.getArgumentClasses();
                }
                catch (final java.lang.ClassNotFoundException cnfe) {
                    ExternalServices.this.outputMessage("Class not found - " + cnfe.getMessage());
                    return;
                }
                catch (final Exception e) {
                    ExternalServices.this.outputMessage("The service \"" + this.svcItem.getName()
                            + "\" FAILED TO START.");
                    return;
                }

                try {
                    Object obj = null;
                    Object returnObject = null;

                    // check if any method is specified
                    if (this.svcItem.getStartMethod().length() > 0) {
                        final Method startMethod = svcClass.getMethod(this.svcItem.getStartMethod(), classNames);
                        if (Modifier.isStatic(startMethod.getModifiers())) {
                            // no need to instantiate an object
                            returnObject = startMethod.invoke(null, args);
                        }
                        else {
                            // instantiate
                            obj = svcClass.newInstance();
                            returnObject = startMethod.invoke(obj, args);
                        }
                        ExternalServices.this.outputMessage("initialized \"" + this.svcItem.getName() + "\"");
                    }

                    // check if jndi binding needed
                    if (this.svcItem.getJndiName().length() > 0) {
                        if (returnObject != null) {
                            // a non-void method was specified
                            // in the service description, bind
                            // returned object
                            ExternalServices.this.servicesContext.bind(this.svcItem.getJndiName(), returnObject);
                            ExternalServices.this.outputMessage("bound intialization result for service \""
                                    + this.svcItem.getName() + "\"");
                        }
                        else {
                            if (obj == null) {
                                // instantiate
                                obj = svcClass.newInstance();
                            }
                            ExternalServices.this.servicesContext.bind(this.svcItem.getJndiName(), obj);
                            ExternalServices.this.outputMessage("bound class instance for service \""
                                    + this.svcItem.getName() + "\"");
                        }
                    }
                    return;
                }
                catch (final java.lang.NoSuchMethodException nsme) {
                    ExternalServices.this.outputMessage("Method not found - " + nsme.getMessage());
                }
                catch (final java.lang.Exception ex) {
                    ExternalServices.this.outputMessage("General Exception - " + ex.getMessage());
                    ex.printStackTrace();
                }
                ExternalServices.this.outputMessage("The service \"" + this.svcItem.getName() + "\" FAILED TO START.");

            }
            else if (qName.equals("arguments")) {
                this.argProcessing = false;
            }
            else if (qName.equals("argitem")) {
                this.svcItem.addArgument(this.argItem);
                this.argItem = null;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (this.elementName == null) {
                return;
            }

            final String chValue = new String(ch, start, length);

            if (this.elementName.equals("name")) {
                this.svcItem.setName(chValue);
            }
            else if (this.elementName.equals("class")) {
                this.svcItem.setJavaClass(chValue);
            }
            else if (this.elementName.equals("jndi_name")) {
                this.svcItem.setJndiName(chValue);
            }
            else if (this.elementName.equals("method")) {
                this.svcItem.setStartMethod(chValue);
            }
            else if (this.elementName.equals("datatype") && this.argProcessing) {
                this.argItem.setDataType(chValue);
            }
            else if (this.elementName.equals("value")) {
                if (this.argProcessing) {
                    this.argItem.addValue(chValue);
                }
            }
        }
    }
}
