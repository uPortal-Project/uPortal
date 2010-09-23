package org.jasig.portal.spring.spel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;

/**
 * PortalSpELServiceImpl provides the default implementation of 
 * IPortalSpELService.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Service
public class PortalSpELServiceImpl implements IPortalSpELService {
    
    /**
     * Expression regex designed to match SpEL expressions contained in 
     * a ${ } block
     */
    public final static Pattern EXPRESSION_REGEX = Pattern.compile("\\$\\{([^\\}]*)\\}");
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ExpressionParser expressionParser = new SpelExpressionParser();
    private Ehcache expressionCache;
    
    private IPortalRequestUtils portalRequestUtils;
    private IUserInstanceManager userInstanceManager;

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }


    public void setExpressionParser(ExpressionParser expressionParser) {
        Validate.notNull(expressionParser);
        this.expressionParser = expressionParser;
    }

    @Autowired
    public void setExpressionCache(@Qualifier("SpELExpressionCache") Ehcache expressionCache) {
        this.expressionCache = expressionCache;
    }

    @Override
    public Expression parseExpression(String expressionString) throws ParseException {
        if (this.expressionCache == null) {
            return this.expressionParser.parseExpression(expressionString);
        }
        
        Element element = this.expressionCache.get(expressionString);
        if (element != null) {
            return (Expression)element.getObjectValue();
        }
        
        final Expression expression = this.expressionParser.parseExpression(expressionString);
        element = new Element(expressionString, expression);
        this.expressionCache.put(element);
        
        return expression;
    }

    @Override
    public String parseString(String string, WebRequest request){
        
        // evaluate the supplied string against our expression regex
        final Matcher m = EXPRESSION_REGEX.matcher(string);
        
        // Attempt to find the first match against the expression regex.  
        // If the supplied string has no matches, just return the supplied 
        // string without getting any SpEL resources.
        if (!m.find()) {
            return string;
        }

        // iterate through the list of matches, replacing each match in the 
        // string with the SpEL-evaluated value
        do {
            
            // find the current match
            final String match = m.group();
            
            // parse the expression block
            final String expressionString = m.group(1);

            //Evaluate the expression
            final String value = this.getValue(expressionString, request, String.class);
            
            // replace the current matched group in the string with the 
            // parsed expression
            string = string.replace(match, value);
            
        } while (m.find());
        
        return string;
    }
    
    @Override
    public <T> T getValue(String expressionString, WebRequest request, Class<T> desiredResultType) {
        final Expression expression = this.parseExpression(expressionString);
        return this.getValue(expression, request, desiredResultType);
    }
    
    @Override
    public <T> T getValue(Expression expression, WebRequest request, Class<T> desiredResultType) {
        final EvaluationContext evaluationContext = this.getEvaluationContext(request);
        return expression.getValue(evaluationContext, desiredResultType);
    }

    @Override
    public Object getValue(String expressionString, WebRequest request) {
        final Expression expression = this.parseExpression(expressionString);
        return this.getValue(expression, request);
    }

    @Override
    public Object getValue(Expression expression, WebRequest request) {
        final EvaluationContext evaluationContext = this.getEvaluationContext(request);
        return expression.getValue(evaluationContext);
    }

    /**
     * Return a SpEL evaluation context for the supplied web request.
     * 
     * @param request
     * @return
     */
    protected EvaluationContext getEvaluationContext(WebRequest request) {
        final HttpServletRequest httpRequest = this.portalRequestUtils.getOriginalPortalRequest(request);
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(httpRequest);
        final IPerson person = userInstance.getPerson();
        
        final SpELEnvironmentRoot root = new SpELEnvironmentRoot(request, person);
        return new StandardEvaluationContext(root);
    }

    /**
     * Limited-use POJO representing the root of a SpEL environment.  At the
     * current moment, we're only using the request object in the evaluation
     * context, but we'd like to be able to add additional objects in the 
     * future.
     */
    @SuppressWarnings("unused")
    private static class SpELEnvironmentRoot {
        
        private final WebRequest request;
        private final IPerson person;
        
        /**
         * Create a new SpEL environment root for use in a SpEL evaluation
         * context.
         * 
         * @param request  web request
         */
        private SpELEnvironmentRoot(WebRequest request, IPerson person) {
            this.request = request;
            this.person = person;
        }

        /**
         * Get the request associated with this environment root.
         */
        public WebRequest getRequest() {
            return request;
        }

        /**
         * The person associated with this environment root
         */
        public IPerson getPerson() {
            return this.person;
        }
    }

}
