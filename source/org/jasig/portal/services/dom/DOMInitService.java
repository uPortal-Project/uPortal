/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
