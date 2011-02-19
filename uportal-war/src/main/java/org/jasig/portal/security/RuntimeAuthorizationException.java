package org.jasig.portal.security;

public class RuntimeAuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 7655381218623647649L;

    public RuntimeAuthorizationException() {
        super();
    }
    
    public RuntimeAuthorizationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RuntimeAuthorizationException(String message) {
        super(message);
    }

    public RuntimeAuthorizationException(Throwable throwable) {
        super(throwable);
    }
    
    public RuntimeAuthorizationException(IPerson person, String activity, String target) {
        super("Person [" + person.getUserName() + "] does not have permission " + activity + " on " + target);
    }

}
