/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import org.jasig.portal.spring.PortalApplicationContextFacade;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Check that a particular named Spring bean is defined.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class SpringBeanCheck 
    implements ICheck {

    private final String beanName;
    
    private final String requiredBeanTypeClassName;

    private CheckResult nullBeanNameResult = CheckResult.createFailure("This check is fatally misconfigured -- it hasn't been told the name of the Spring bean for which it should be checking.", 
            "Fix the check configuration (probably a List named 'checks' declared in applicationContext.xml).");
    
    
    public SpringBeanCheck(String beanName, String requiredBeanTypeClassName) {
        this.beanName = beanName;
        this.requiredBeanTypeClassName = requiredBeanTypeClassName;
    }
    
    public SpringBeanCheck(String beanName) {
        this.beanName = beanName;
        this.requiredBeanTypeClassName = null;
    }
    
    
    public CheckResult doCheck() {
        
        if (this.beanName == null)
            return this.nullBeanNameResult;
        
        
        // either verify that the bean exists (and is of the desired type if specified)
        // or translate the resulting RuntimeException into an explanatory 
        // CheckResult.
        try {
            if (this.requiredBeanTypeClassName == null) {
                PortalApplicationContextFacade.getPortalApplicationContext().getBean(this.beanName);
                return CheckResult.createSuccess("Bean with name [" + this.beanName + "] was present.");
            }
                
            
            PortalApplicationContextFacade.getPortalApplicationContext().getBean(this.beanName, Class.forName(this.requiredBeanTypeClassName));
            return CheckResult.createSuccess("Bean with name [" + this.beanName + "] was present and of class [" + requiredBeanTypeClassName + "]");
            
            
            
        } catch (NoSuchBeanDefinitionException nsbde) {
            String remediationAdvice;
            if (this.requiredBeanTypeClassName == null) {
                remediationAdvice = "Declare a singleton bean of name [" + this.beanName + "] in a Spring bean definition file mapped in /properties/beanRef.xml.";
            } else {
                remediationAdvice = "Declare a singleton bean of name [" + this.beanName + "] and of type [" + this.requiredBeanTypeClassName + "] in a Spring bean definition file mapped in /properties/beanRefFactory.xml.";
            }
            return CheckResult.createFailure("There is no bean named [" + this.beanName + "] in uPortal's Spring bean definition file(s) (e.g., applicationContext.xml).",
                    remediationAdvice);
        } catch (BeanNotOfRequiredTypeException bnfe) {
            return CheckResult.createFailure("The bean named [" + this.beanName + "] defined in uPotal's Spring bean definitions (likely, applicationContext.xml), is not of type [" + this.requiredBeanTypeClassName + "]",
                    "Fix the declaration of bean [" + this.beanName + "] to be of the required type.");
            
        } catch (BeansException be) {
            return CheckResult.createFailure("There was an error instantiating the bean [" + this.beanName + "] required for configuration of configuration checking.", 
                    "The required XML files for Spring bean configuration in uPortal may not be present or may not conform to the required DTD or they may have definitions that a syntactically valid but do not correspond to actual bean constructors and methods.");
        } catch (ClassNotFoundException cnfe) {
            return CheckResult.createFailure("We can't check for the presence of the bean [" + this.beanName + "] because the desired class [" + this.requiredBeanTypeClassName + "] itself could not be found.",
                    "Either fix the configuration of this check so that it's checking for the right class name, or place the required class on the classpath.");
        }
        
    }

    public String getDescription() {
        if (this.beanName == null) 
            return "Fatally misconfigured check doesn't do anything because it hasn't been given a bean name to check.";
        if (this.requiredBeanTypeClassName == null)
            return "Checks for the presence of the Spring bean named [" + this.beanName + "].";
        return "Checks for the presence of the Spring bean named [" + this.beanName + "] and that it is an instance of class [" + this.requiredBeanTypeClassName + "]";
    }

    
    
}
