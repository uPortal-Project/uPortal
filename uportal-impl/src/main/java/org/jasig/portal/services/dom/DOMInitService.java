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

package  org.jasig.portal.services.dom;

/**
 *  This service is meant to run at system initialization that will provide
 *  any DOM initialization. For example, with resin, you can configure which
 *  compiler to use. It will use the DOMInitServicaFactory to retrieve the
 *  actual implementation of the init service.
 *
 * @author     Nick Bolton, nbolton@unicon.net
 * @version    $Revision$
 */
public final class DOMInitService {

  private static final DOMInitService m_instance = new DOMInitService();
  private static boolean bInitialized = false;


  protected DOMInitService() {
    initialize();
  }

  public final static DOMInitService instance() {
    return m_instance;
  }

  /**
   *  Executes the actual dom init service.
   */
  private final static void initialize () {
    // don't bother if we are already initialized
    if (bInitialized) {
      return;
    }

    try {
      IDOMInitService service = DOMInitServiceFactory.getService();
      if (service != null) {
        service.initialize();
      }
      bInitialized = true;
    } catch (Exception e) {
      System.err.println("Problem executing DOM initialization");
      e.printStackTrace();
    } catch (Error er) {
      System.err.println("Problem executing DOM initialization");
      er.printStackTrace();
    }
  }
}
