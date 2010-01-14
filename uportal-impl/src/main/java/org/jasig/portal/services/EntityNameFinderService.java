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

package org.jasig.portal.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IEntityNameFinderFactory;
import org.jasig.portal.properties.PropertiesManager;

/**
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class EntityNameFinderService
{
    
    private static final Log log = LogFactory.getLog(EntityNameFinderService.class);
    
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
                log.debug( dMsg );
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
                    log.error( eMsg, e);
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
