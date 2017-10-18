/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.security;

public class RuntimeAuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 7655381218623647649L;

    public RuntimeAuthorizationException() {
        super();
    }

    public RuntimeAuthorizationException(IPerson person, String activity, String target) {
        super(
                "Person ["
                        + person.getUserName()
                        + "] does not have permission "
                        + activity
                        + " on "
                        + target);
    }

    public RuntimeAuthorizationException(String userName, String activity, String target) {
        super("Person [" + userName + "] does not have permission " + activity + " on " + target);
    }
}
