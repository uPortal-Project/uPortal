/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

/**
 * Checks that JAXP is present in the JVM. This allows us to give a
 * useful error back to the deployer if we find JAXP missing. Instead of getting many
 * class not found errors the deployer will see a message instructing how to fix
 * the problem.
 * 
 * JaxpCheck fulfills two purposes.  First, it is an executable class which will
 * make an educated guess at whether the JAXP jars are present (by checking for
 * a particular class found only in the JAXP distribution) and if it guesses we're
 * missing those jars will report this error to System.err and return a nonzero
 * return code.  A nonzero return value from a main method stop an Ant build
 * when the class was run with stopOnFailure declared to true, as it is by default
 * in our build.xml.
 * 
 * Second, JaxpCheck implements the ICheck interface and so can be used via the 
 * CheckRunner API in this package.  This allows us to run this check as part of the
 * context initialization sanity checking.  An instance of this check is configured in
 * 
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class JaxpCheck 
    implements ICheck {
    
    /**
     * The name of a class found only in the JAXP distribution.
     */
    private static final String A_JAXP_CLASS = "javax.xml.xpath.XPathConstants";
    
    /**
     * ICheck implementation to which we delegate as the way we
     * implement ICheck.
     */
    private ICheck checkDelegate;

    public static void main(String[] args) {
        try {
            Class c = JaxpCheck.class.getClassLoader().loadClass(A_JAXP_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            System.err.println("The missing class is provided as part of JAXP.\n" +
                "Check that you have the JAXP jars installed in your JDK.\n" +
                "For more information see lib/jaxp/README.txt.");
            System.exit(1);
        }
        System.exit(0);
    }
    
    public JaxpCheck() {
        super();
        
        /*
         * We implement the ICheck interface by delegation to an instance of
         * ClassPresenceCheck, which we configure here.
         */
        
        ClassPresenceCheck check = new ClassPresenceCheck(A_JAXP_CLASS);
        check.setDescription("Check for the presence of the class " 
                + A_JAXP_CLASS
                + ", which will only be present when JAXP 1.3 or better is installed.");
        
        CheckResult failureResult = 
            CheckResult.createFailure(
                    "The class " + A_JAXP_CLASS + " was not present.  This class is part of the JAXP 1.3 distribution.",
                    "Install the JAXP 1.3 jars into your JDK and into your servlet container as described in /lib/jaxp/README.txt");
        check.setFailureResult(failureResult);
        
        CheckResult successResult = 
            CheckResult.createSuccess(
                    "The class " + A_JAXP_CLASS + " was present.  " 
                    + "This class is part of the JAXP 1.3 distribution and its presence " 
                    + "suggests that JAXP 1.3 is properly installed.");
        check.setSuccessResult(successResult);
        
        this.checkDelegate = check;
        
    }

    public CheckResult doCheck() {
        // we implement this interface method by delegation
        return this.checkDelegate.doCheck();
    }

    public String getDescription() {
        // we implement this interface method by delegation
        return this.checkDelegate.getDescription();
    }
    

}