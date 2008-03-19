/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.jndi;

import java.util.Enumeration;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

import org.jasig.portal.PortalException;
import org.jasig.portal.services.ExternalServices;
import org.jasig.portal.spring.web.context.support.HttpSessionDestroyedEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jndi.JndiAccessor;
import org.springframework.jndi.JndiCallback;
import org.springframework.jndi.JndiTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class JndiManagerImpl extends JndiAccessor implements IJndiManager, ApplicationListener, InitializingBean {

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        this.initializePortalContext();
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof HttpSessionDestroyedEvent) {
            final HttpSession session = ((HttpSessionDestroyedEvent)event).getSession();
            this.destroySessionContext(session);
        }
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.jndi.IJndiManager#initializeSessionContext(javax.servlet.http.HttpSession, java.lang.String, java.lang.String, org.w3c.dom.Document)
     */
    public void initializeSessionContext(HttpSession session, String userId, String layoutId, Document userLayout) {
        final String sessionId = session.getId();
        
        
        final JndiTemplate jndiTemplate = this.getJndiTemplate();

        // bind userId to /sessions context
        try {
            final Context sessionsContext = (Context) jndiTemplate.lookup("/sessions", Context.class);
            
            try {
                sessionsContext.bind(sessionId, userId);
            }
            catch (NameAlreadyBoundException nabe) {
                sessionsContext.rebind(sessionId, userId);
            }
        }
        catch (NamingException ne) {
            this.logger.warn("Unable to obtain /sessions context, no session data will be available in the context for sessionId='" + sessionId + "', userId='" + userId + "', and layoutId='" + layoutId + "'", ne);
        }

        final Context usersContext;
        try {
            // get /users context
            usersContext = (Context) jndiTemplate.lookup("/users", Context.class);
        }
        catch (NamingException ne) {
            final PortalException portalException = new PortalException("Could not find /users context", ne);
            this.logger.error(portalException.getMessage(), ne);
            throw portalException;
        }

        // get or create /users/[userId] context
        Context userIdContext = null;
        Context sessionsContext = null;
        Context layoutsContext = null;
        try {
            userIdContext = (Context) usersContext.lookup(userId);

            // lookup layouts and sessions contexts
            try {
                layoutsContext = (Context) userIdContext.lookup("layouts");
            }
            catch (NamingException ne) {
                this.logger.warn("The '/users/" + userId + "/layouts' Context did not exist, even though the '/users/" + userId + "' Context did. It will be created.");
                layoutsContext = userIdContext.createSubcontext("layouts");
            }

            try {
                sessionsContext = (Context) userIdContext.lookup("sessions");
            }
            catch (NamingException ne) {
                this.logger.error("The Context '/users/" + userId + "/sessions' did not exist, even though the '/users/" + userId + "' Context did. It will be created.");
                sessionsContext = userIdContext.createSubcontext("sessions");
            }

        }
        catch (NamingException ne) {
            // new user
            try {
                userIdContext = usersContext.createSubcontext(userId);
                
                // create layouts and sessions context
                layoutsContext = userIdContext.createSubcontext("layouts");
                sessionsContext = userIdContext.createSubcontext("sessions");
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Created and initialized Contexts for a userId='" + userId + "'");
                }
            }
            catch (NamingException ne2) {
                final PortalException portalException = new PortalException("exception encountered while trying to create  '/users/" + userId + "' and layouts/sessions Contexts", ne2);
                this.logger.error(portalException.getMessage(), ne2);
                throw portalException;
            }
        }

        // bind sessions/[sessionId] context
        final Context sessionIdContext;
        try {
            sessionIdContext = sessionsContext.createSubcontext(sessionId);
        }
        catch (NameAlreadyBoundException nabe) {
            final PortalException portalException = new PortalException("A session context is already bound at '/users/" + userId + "/sessions/" + sessionId + "'", nabe);
            this.logger.error(portalException.getMessage(), nabe);
            throw portalException;
        }
        catch (NamingException ne) {
            final PortalException portalException = new PortalException("Excpetion encountered while trying to create Context '/users/" + userId + "/sessions/" + sessionId + "'", ne);
            this.logger.error(portalException.getMessage(), ne);
            throw portalException;
        }

        // bind layoutId
        try {
            sessionIdContext.bind("layoutId", layoutId);
        }
        catch (NamingException ne) {
            final PortalException portalException = new PortalException("Excpetion encountered while trying to bind '" + layoutId + "' to '/users/" + userId + "/sessions/" + sessionId + "/layoutId'", ne);
            this.logger.error(portalException.getMessage(), ne);
            throw portalException;
        }

        // make sure channel-obj context exists
        try {
            sessionIdContext.createSubcontext("channel-obj");
        }
        catch (NameAlreadyBoundException nabe) {
            // ignore
        }
        catch (NamingException ne) {
            this.logger.warn("Excpetion encountered while create Context '" + layoutId + "' to '/users/" + userId + "/sessions/" + sessionId + "/channel-obj', this will be ignored.", ne);
        }

        // check if the layout id binding already exists
        try {
            //Check if layouts/[layoutId]/ alread exists
            layoutsContext.lookup(layoutId);
            
            // assume layouts/[layoutId]/ has already been populated

            // bind layouts/[layoutId]/sessions/[sessionId]
            final Context layoutSessionsContext;
            try {
                layoutSessionsContext = (Context) userIdContext.lookup("layouts/" + layoutId + "/sessions");
            }
            catch (NamingException ne) {
                final PortalException portalException = new PortalException("Exception occured while looking up Context '/users/" + userId + "/layouts/" + layoutId + "/sessions/' even though Context '/users/" + userId + "/layouts' already existed.", ne);
                this.logger.error(portalException.getMessage(), ne);
                throw portalException;
            }
            
            try {
                layoutSessionsContext.createSubcontext(sessionId);

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Created Context '/users/" + userId + "/layouts/" + layoutId + "/sessions/" + sessionId + "'");
                }
            }
            catch (NamingException ne) {
                final PortalException portalException = new PortalException("Exception occured while creating Context '/users/" + userId + "/layouts/" + layoutId + "/sessions/" + sessionId + "'", ne);
                this.logger.error(portalException.getMessage(), ne);
                throw portalException;
            }
        }
        catch (NamingException ne) {
            final Context layoutIdContext;
            try {
                // given layout id has not been registered yet
                layoutIdContext = layoutsContext.createSubcontext(layoutId);
    
                // bind layouts/[layoutId]/sessions/[sessionId] context
                final Context layoutSessionsContext = layoutIdContext.createSubcontext("sessions");
                layoutSessionsContext.createSubcontext(sessionId);
    
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Created Context '/users/" + userId + "/layouts/" + layoutId + "'");
                }
            }
            catch (NamingException ne2) {
                final PortalException portalException = new PortalException("Exception occured while creating the '/users/" + userId + "/layouts/" + layoutId + "' Context.", ne2);
                this.logger.error(portalException.getMessage(), ne2);
                throw portalException;
            }
            
            try {
                final Context channel_idsContext = layoutIdContext.createSubcontext("channel-ids");
                
                
                // Get the list of channels in the user's layout
                final NodeList channelNodes = userLayout.getElementsByTagName("channel");
                // Parse through the channels and populate the JNDI
                for (int channelNodeIndex = 0; channelNodeIndex < channelNodes.getLength(); channelNodeIndex++) {
                    // Attempt to get the fname and instance ID from the channel
                    final Node channelNode = channelNodes.item(channelNodeIndex);
                    final NamedNodeMap channelAttributes = channelNode.getAttributes();
                    
                    final Node fname = channelAttributes.getNamedItem("fname");
                    final Node instanceId = channelAttributes.getNamedItem("ID");
                    if (fname != null && instanceId != null) {
                        //System.out.println("fname found -> " + fname);
                        // Create a new composite name from the fname
                        final CompositeName cname = new CompositeName(fname.getNodeValue());
                        
                        // Get a list of the name components
                        final Enumeration<String> subContextNameEnum = cname.getAll();
                        // Get the root of the context
                        Context nextContext = channel_idsContext;
                        // Add all of the subcontexts in the fname
                        while (subContextNameEnum.hasMoreElements()) {
                            final String subContextName = subContextNameEnum.nextElement();
                            if (subContextNameEnum.hasMoreElements()) {
                                // Bind a new sub context if the current name component is not the leaf
                                nextContext = nextContext.createSubcontext(subContextName);
                            }
                            else {
                                nextContext.rebind(subContextName, instanceId.getNodeValue());
                                
                                if (this.logger.isDebugEnabled()) {
                                    this.logger.debug("Bound channel id '" + instanceId.getNodeValue() + "' to '" + nextContext.getNameInNamespace() + "/" + subContextName + "'");
                                }
                            }
                        }
                    }
                }
            }
            catch (NamingException ne2) {
                final PortalException portalException = new PortalException("Exception occured while creating or populating the '/users/" + userId + "/layouts/" + layoutId + "/channel-ids' Context.", ne2);
                this.logger.error(portalException.getMessage(), ne2);
                throw portalException;
            }
        }
        
        this.logger.info("JNDI Context configured for sessionId='" + sessionId + "', userId='" + userId + "', and layoutId='" + layoutId + "'");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.jndi.IJndiManager#destroySessionContext(javax.servlet.http.HttpSession)
     */
    public void destroySessionContext(HttpSession session) {
        final JndiTemplate jndiTemplate = this.getJndiTemplate();
        
        final String sessionId = session.getId();


        final Context usersContext;
        try {
            // get /users context
            usersContext = (Context) jndiTemplate.lookup("/users", Context.class);
        }
        catch (NamingException ne) {
            final PortalException portalException = new PortalException("Could not find /users context", ne);
            this.logger.error(portalException.getMessage(), ne);
            throw portalException;
        }
        
        //No context, nothing to do
        if (usersContext == null) {
            this.logger.warn("No JNDI Context removed for sessionId='" + sessionId + "'");
            return;
        }

        // obtain /sessions context
        final Context topSessionsContext;
        try {
            topSessionsContext = (Context) jndiTemplate.lookup("/sessions", Context.class);
        }
        catch (NamingException ne) {
            this.logger.warn("Could not get /sessions context. No JNDI context will be removed for sessionId='" + sessionId + "'", ne);
            return;
        }

        final String userId;
        // obtain userId by looking at /sessions bindings
        try {
            userId = (String) topSessionsContext.lookup(sessionId);
        }
        catch (NamingException ne) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Session '" + sessionId + "' is not registered under /sessions context, returning immediatly.", ne);
            }

            return;
        }
        
        if (userId == null) {
            // could do a /users/[userId]/sessions/* traversal here instead
            this.logger.warn("Unable to determine userId for a session " + sessionId + " ... giving up on JNDI cleanup.");
            return;
        }

        // unbind userId binding in /sessions
        try {
            topSessionsContext.unbind(sessionId);
        }
        catch (NamingException ne) {
            this.logger.warn("Problems unbinding '/sessions/" + sessionId + "', continuing with cleanup.", ne);
        }

        final Context userIdContext;
        try {
            userIdContext = (Context) usersContext.lookup(userId);
        }
        catch (NamingException ne) {
            this.logger.warn("Context '/users/" + userId + "' doesn't exist. Ending JNDI Cleanup here.", ne);
            return;
        }

        final Context sessionsContext;
        try {
            sessionsContext = (Context) userIdContext.lookup("sessions");
        }
        catch (NamingException ne) {
            this.logger.warn("Context '/users/" + userId + "/sessions' doesn't exist. Ending JNDI Cleanup here.", ne);
            return;
        }

        final Context sessionIdContext;
        try {
            sessionIdContext = (Context) sessionsContext.lookup(sessionId);
        }
        catch (NamingException ne) {
            this.logger.warn("Context '/users/" + userId + "/sessions/" + sessionId + "' doesn't exist. Ending JNDI Cleanup here.", ne);
            return;
        }

        // determine layoutId
        String layoutId = null;
        try {
            layoutId = (String) sessionIdContext.lookup("layoutId");
        }
        catch (NamingException ne) {
            this.logger.warn("'/users/" + userId + "/sessions/" + sessionId + "/layoutId' is not bound.", ne);
        }

        // destroy sessionIdContext
        try {
            sessionsContext.unbind(sessionId);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Destroyed Context '/users/" + userId + "/sessions/" + sessionId + "'");
            }
        }
        catch (NamingException ne) {
            this.logger.warn("Exception occurred while trying to destroy context  '/users/" + userId + "/sessions/" + sessionId + "', ignoring and continuing with cleanup.", ne);
        }

        // see if this was the only session
        try {
            final NamingEnumeration<NameClassPair> userSessionsList = userIdContext.list("sessions");
            if (!userSessionsList.hasMore()) {
                // destroy userIdContext alltogether
                usersContext.unbind(userId);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Destroyed Context '/users/" + userId + "' since the last remaining session has been unbound.");
                }
            }
            else {
                // remove sessionId from the layouts/[layoutId]/sessions
                try {
                    final Context layoutsContext = (Context) userIdContext.lookup("layouts");
                    
                    try {
                        final Context layoutIdContext = (Context) layoutsContext.lookup(layoutId);
                        
                        try {
                            final Context layoutSessionsContext = (Context) layoutIdContext.lookup("sessions");
                            
                            // unbind sessionId
                            layoutSessionsContext.unbind(sessionId);
                            if (this.logger.isDebugEnabled()) {
                                this.logger.debug("Destroyed Context '/users/" + userId + "/layouts/" + layoutId + "/sessions/" + sessionId + "'");
                            }

                            // see if the lsessionsContext is empty
                            final NamingEnumeration<NameClassPair> layoutSessionsList = layoutIdContext.list("sessions");
                            if (!layoutSessionsList.hasMore()) {
                                // destroy the layoutId context
                                try {
                                    layoutsContext.unbind(layoutId);
                                    if (this.logger.isDebugEnabled()) {
                                        this.logger.debug("Destroyed Context '/users/" + userId + "/layouts/" + layoutId + "' since the last session using it has been unbound.");
                                    }

                                }
                                catch (NamingException ne) {
                                    this.logger.warn("Exception while destroying '/users/" + userId + "/layouts/" + layoutId + "', ignoring and continuing with cleanup.", ne);
                                }
                            }
                        }
                        catch (NamingException ne) {
                            this.logger.warn("Exception while looking up  '/users/" + userId + "/layouts/" + layoutId + "/sesions', ignoring and continuing with cleanup.", ne);
                        }
                    }
                    catch (NamingException ne) {
                        this.logger.warn("Exception while looking up  /users/" + userId + "/layouts/', ignoring and continuing with cleanup.", ne);
                    }
                }
                catch (NamingException ne) {
                    this.logger.warn("Exception while looking up  /users/" + userId + "/layouts', ignoring and continuing with cleanup.", ne);
                }
            }
        }
        catch (NamingException ne) {
            this.logger.warn("Error listing  '/users/" + userId + "/sessions/', ignoring and continuing with cleanup.", ne);
        }
        
        this.logger.info("JNDI Context removed for sessionId='" + sessionId + "', userId='" + userId + "', and layoutId='" + layoutId + "'");
    }
    
    /**
     * Sets up the base sub contexts in the portal JNDI context.
     */
    protected void initializePortalContext() throws NamingException {
        final JndiTemplate jndiTemplate = this.getJndiTemplate();

        // Create a subcontext for portal-wide services, initialize services 
        // Start any portal services configured in services.xml
        final Context servicesContext = (Context)jndiTemplate.execute(new CreateSubContextCallback("services"));
        ExternalServices.startServices(servicesContext);

        // Create a subcontext for user specific bindings
        jndiTemplate.execute(new CreateSubContextCallback("users"));

        // Create a subcontext for session listings
        jndiTemplate.execute(new CreateSubContextCallback("sessions"));

        this.logger.info("Initialized portal JNDI context");
    }
    
    /**
     * Creates a sub-context with the specified name.
     */
    private static class CreateSubContextCallback implements JndiCallback {
        private final String subContextName;
        
        public CreateSubContextCallback(String subContextName) {
            this.subContextName = subContextName;
        }
        
        /* (non-Javadoc)
         * @see org.springframework.jndi.JndiCallback#doInContext(javax.naming.Context)
         */
        public Object doInContext(Context ctx) throws NamingException {
            final Context subcontext = ctx.createSubcontext(this.subContextName);
            return subcontext;
        }
    }
}
