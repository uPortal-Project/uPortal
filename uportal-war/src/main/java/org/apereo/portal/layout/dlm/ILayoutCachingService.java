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
package org.apereo.portal.layout.dlm;

import org.apereo.portal.IUserProfile;
import org.apereo.portal.security.IPerson;

/**
 * Base API for layout caching service. The implementation should handle appropriately expiring
 * layouts when the user logs out
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ILayoutCachingService {

  public void cacheLayout(IPerson owner, IUserProfile profile, DistributedUserLayout layout);

  public void removeCachedLayout(IPerson owner, IUserProfile profile);

  public DistributedUserLayout getCachedLayout(IPerson owner, IUserProfile profile);
}
