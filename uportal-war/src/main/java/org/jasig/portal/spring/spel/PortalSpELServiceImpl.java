package org.jasig.portal.spring.spel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.WebRequest;

/**
 * PortalSpELServiceImpl provides the default implementation of 
 * IPortalSpELService.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class PortalSpELServiceImpl implements IPortalSpELService {
    
    /**
     * Expression regex designed to match SpEL expressions contained in 
     * a ${ } block
     */
    protected final static Pattern expressionRegex = Pattern.compile("\\$\\{([^\\}]*)\\}");
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.spring.spel.IPortalSpELService#parseString(java.lang.String, org.springframework.web.context.request.WebRequest)
     */
    public String parseString(String string, WebRequest request){
        
        // evaluate the supplied string against our expression regex
        final Matcher m = expressionRegex.matcher(string);
        
        // Attempt to find the first match against the expression regex.  
        // If the supplied string has no matches, just return the supplied 
        // string without getting any SpEL resources.
        if (!m.find()) {
            return string;
        }

        // get a SpEL parser and context for the supplied request
        final ExpressionParser parser = getParser(request);
        final EvaluationContext context = getEvaluationContext(request);

        // iterate through the list of matches, replacing each match in the 
        // string with the SpEL-evaluated value
        do {
            
            // find the current match
            final String match = m.group();
            
            // parse the expression block
            final String expressionString = m.group(1);
            final Expression expression = parser.parseExpression(expressionString);
            final String value = expression.getValue(context, String.class);
            
            // replace the current matched group in the string with the 
            // parsed expression
            string = string.replace(match, value);
            
        } while (m.find());
        
        return string;
    }

    /**
     * Return a SpEL evaluation context for the supplied web request.
     * 
     * @param request
     * @return
     */
    protected EvaluationContext getEvaluationContext(WebRequest request) {
        final SpELEnvironmentRoot root = new SpELEnvironmentRoot(request);
        return new StandardEvaluationContext(root);
    }

    /**
     * Return a SpEL expression parser for the supplied web request.
     * 
     * @param request
     * @return
     */
    protected ExpressionParser getParser(WebRequest request) {
        return new SpelExpressionParser();
    }
    
    /**
     * Limited-use POJO representing the root of a SpEL environment.  At the
     * current moment, we're only using the request object in the evaluation
     * context, but we'd like to be able to add additional objects in the 
     * future.
     */
    protected class SpELEnvironmentRoot {
        
        private final WebRequest request;
        
        /**
         * Create a new SpEL environment root for use in a SpEL evaluation
         * context.
         * 
         * @param request  web request
         */
        protected SpELEnvironmentRoot(WebRequest request) {
            this.request = request;
        }

        /**
         * Get the request associated with this environment root.
         * 
         * @return
         */
        public WebRequest getRequest() {
            return request;
        }

    }

}
