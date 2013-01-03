package org.jasig.portal.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Appends the username of the current user to the name of the current thread
 * 
 * @author Eric Dalquist
 */
@Service("threadNamingRequestFilter")
public class ThreadNamingRequestFilter extends OncePerRequestFilter {
    private final ThreadLocal<String> originalThreadNameLocal = new ThreadLocal<String>(); 
    private IPersonManager personManager;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    /**
     * Update the thread name to use the specified username. Useful for authentication requests
     * where the username changes mid-request
     */
    public void updateCurrentUsername(String newUsername) {
        final String originalThreadName = originalThreadNameLocal.get();
        if (originalThreadName != null && newUsername != null) {
            final Thread currentThread = Thread.currentThread();
            final String threadName = getThreadName(originalThreadName, newUsername);
            currentThread.setName(threadName);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String username = getUserName(request);
        if (username == null) {
            //No user, skip thread naming
            filterChain.doFilter(request, response);
        }
        else {
            final Thread currentThread = Thread.currentThread();
            final String originalThreadName = currentThread.getName();
            try {
                originalThreadNameLocal.set(originalThreadName);
                final String threadName = getThreadName(originalThreadName, username);
                currentThread.setName(threadName);
                filterChain.doFilter(request, response);
            }
            finally {
                currentThread.setName(originalThreadName);
                originalThreadNameLocal.remove();
            }
        }
    }

    protected String getThreadName(String originalThreadName, String newUsername) {
        return originalThreadName + "-" + newUsername;
    }

    protected String getUserName(HttpServletRequest request) {
        final IPerson person = this.personManager.getPerson(request);
        if (person == null) {
            return null;
        }
        
        return person.getUserName();
    }
}
