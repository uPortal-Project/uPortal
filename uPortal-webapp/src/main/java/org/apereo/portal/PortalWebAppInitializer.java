package org.apereo.portal;

import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This class does the following: 1. creates the root application context using the specified config
 * locations 2. initializes the context loader
 *
 * <p>This replaces the the following, which were previously defined in web.xml file.
 *
 * <pre>{@code
 * <context-param>
 *     <param-name>contextConfigLocation</param-name>
 *     <param-value>classpath:/properties/contexts/*.xml,classpath:/properties/contextOverrides/*.xml</param-value>
 * </context-param>
 *
 * <!--
 *  | Loads/Unloads the Spring WebApplicationContext
 *  +-->
 * <listener>
 *     <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
 * </listener>
 *
 * }</pre>
 *
 * This new approach allows us to dynamically update the servlet context programatically with
 * Spring, which was needed in order support Spring Session handling as a feature that could be
 * enabled/disabled with configuration.
 */
public class PortalWebAppInitializer extends AbstractContextLoaderInitializer {

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setConfigLocation(
                "classpath:/properties/contexts/*.xml,classpath:/properties/contextOverrides/*.xml");
        return context;
    }
}
