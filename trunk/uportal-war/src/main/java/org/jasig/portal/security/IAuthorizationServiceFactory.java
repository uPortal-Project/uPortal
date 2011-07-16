/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security;

 import org.jasig.portal.AuthorizationException;
 
/**
 * <p>A context-specific factory class interface that should be implemented
 * by factory classes defined for each context provider. The provider's
 * constructor should not be public to discourage it's instantiation through
 * means other than the corresponding factory. This formalism should be
 * followed for consistency even when the factory performs no additional
 * value-add than instantiating the appropriate context class.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

public interface IAuthorizationServiceFactory
{
  public IAuthorizationService getAuthorization() throws AuthorizationException;
}
