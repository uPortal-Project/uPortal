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
        Matcher m = expressionRegex.matcher(string);
        
        // Attempt to find the first match against the expression regex.  
        // If the supplied string has no matches, just return the supplied 
        // string without getting any SpEL resources.
        if (!m.find()) {
            return string;
        }

        // get a SpEL parser and context for the supplied request
        ExpressionParser parser = getParser(request);
        EvaluationContext context = getEvaluationContext(request);

        // iterate through the list of matches, replacing each match in the 
        // string with the SpEL-evaluated value
        do {
            
            // parse the current matched group to get the contents of the 
            // ${ } block
            String match = m.group();
            String expressionString = match.substring(2, match.length()-1);
            
            // parse the expression block
            Expression expression = parser.parseExpression(expressionString);
            String value = expression.getValue(context, String.class);
            
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
        return new StandardEvaluationContext(request);
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

}
