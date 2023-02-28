/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.context;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * HttpSessionAspect is responsible for instrumenting classes that implement HttpSession
 * with logging data, to determine if the instance of HttpSession is trying to add
 * a class that is not serialzable to the session.  HttpSessionAspect is not needed
 * at this point for any production functionality; it is only used to identify
 * classes that need to be modified to be serializable.
 *
 * This is class is being included in the uPortal-webapp package so that as part
 * of the uPortal gradle build, the needed classes are available as part of the
 * uPortal packaging.  uPortal-start will then pull needed instrumented classes
 * and include them in the appropriate place within the tomcat instance.  This assumes
 * that uPortal-start is being used to manage the installation into the appropriate
 * tomcat server.
 * @author mgillian
 *
 */
@Aspect
public class HttpSessionAspect {
    private static final Logger log = Logger.getLogger(HttpSessionAspect.class.getName());

    @Before("execution(* javax.servlet.http.HttpSession+.setAttribute(..))")
    public void loggingAdvice(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length < 2) {
            log.log(Level.SEVERE, "HttpSession.setAttribute does not have at least 2 parameters");
            return;
        }

        // HttpSession.setAttribute takes two parameters:
        //   key: a string name for the parameter, not important for this use case
        //   value: the object to be stored, which must be serializable
        Object value = args[1];
        if (!(value instanceof Serializable)) {
            log.log(Level.INFO, "value [" + value.getClass().getName() + "] is NOT serializable");
        } else {
            log.log(Level.FINE, "value [" + value.getClass().getName() + "] is serializable");
        }
    }
}
