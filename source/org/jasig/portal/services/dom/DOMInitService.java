/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
