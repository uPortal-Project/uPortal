/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.EntityTypes;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IEntityNameFinderFactory;

/**
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class EntityNameFinderService
{
  private static EntityNameFinderService m_instance;
  private Map nameFinders = null;
  private static boolean initialized = false;
  /**
   *
   */
private EntityNameFinderService ()
{
    super();
    initialize();
}
/**
 * @return org.jasig.portal.groups.IEntityNameFinder
 */
public IEntityNameFinder getNameFinder(Class type) throws GroupsException
{
        IEntityNameFinder finder = (IEntityNameFinder) (getNameFinders().get(type));
        if ( finder == null )
            { throw new GroupsException("Name finder for " + type.getName() + " could not be located."); }
        return finder;
}
/**
 * @return java.util.Map
 */
private Map getNameFinders()
{
    if ( nameFinders == null )
        { nameFinders = new HashMap(10); }
    return nameFinders;
}
/**
  * Gets all the entity types and tries to instantiate and cache a finder for each
  * one.  There needn't be a finder for every entity type, so if there's no entry
  * in the portal.properties, we just log the fact and continue.
 */
private synchronized void initialize()
{
    Iterator types = EntityTypes.singleton().getAllEntityTypes();
    String factoryName = null;

    while ( types.hasNext() )
    {
        Class type = (Class) types.next();
        if ( type != Object.class )
        {
            String factoryKey = "org.jasig.portal.services.EntityNameFinderService.NameFinderFactory.implementation_"+type.getName();
            try
            {
                factoryName = PropertiesManager.getProperty(factoryKey);
            }
            catch (Exception runtime)
            {
                String dMsg = "EntityNameFinderService.initialize(): " +
                    "could not find property for " + type.getName() + " factory.";
                LogService.log( LogService.DEBUG, dMsg );
            }
            if ( factoryName != null )
            {
                try
                {
                    IEntityNameFinderFactory factory =
                        (IEntityNameFinderFactory) Class.forName(factoryName).newInstance();
                    getNameFinders().put( type, factory.newFinder() );
                }
                catch (Exception e)
                {
                    String eMsg = "EntityNameFinderService.initialize(): " +
                        "Could not instantiate finder for " + type.getName() + ": ";
                    LogService.log( LogService.ERROR, eMsg + e.getMessage() );
                }
            }
        }
    }
    setInitialized(true);
}
  /**
   * @return EntityNameFinderService
   */
  public final static synchronized EntityNameFinderService instance()
  {
    if ( m_instance == null )
      { m_instance = new EntityNameFinderService(); }
    return m_instance;
  }
/**
 * @return boolean
 */
private static boolean isInitialized() {
        return initialized;
}
/**
* @param newInitialized boolean
 */
static void setInitialized(boolean newInitialized) {
    initialized = newInitialized;
}
/**
 */
public static void start()
{
    if (! isInitialized() )
        { instance(); }
}
}
