/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.tools;

import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelType;
import org.jasig.portal.IChannelRegistryStore;

/**
 * <p>A tool for registering a new channel type with uPortal.
 * Channel types are defined by a java class name and an associated
 * channel publishing document.  Once added with this tool, a channel
 * type will be available to channel publishers for the production
 * of new channel definitions based on the channel type.</p>
 * <p>Usage: RegisterChannelType <class> <name> <description> <CPD URI></p>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RegisterChannelType
{
    protected static IChannelRegistryStore chanRegStore = null;

    public static void main(String[] args)
    {
        // Enforce that exactly 4 arguments are given: class, name, description, and URI
        // and that no arguments are empty
        if (args.length == 4)
        {
            try
            {
                msg(
                    register(
                        args[0].trim(),
                        args[1].trim(),
                        args[2].trim(),
                        args[3].trim()));
            }
            catch (IllegalArgumentException e)
            {
                msg(e.getMessage());
                printHelp();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            printHelp();
        }
    }

    /**
     * Utility method for checking validity of type parameters.
     * @param name
     * @param value
     * @throws IllegalArgumentException
     */
    private static void checkValidity(String name, String value)
    throws IllegalArgumentException
    {
        if (value == null || value.length() == 0)
            throw new IllegalArgumentException("Parameter '" + name + 
            "' must be specified when publishing a channel type."); 
    }
    
    /**
     * Registers a new channel type for the given parameters or updates an
     * existing type with the specified parameters if an existing type is found
     * having the specified uri.
     */
    public static String register(
    String clazz,
    String name,
    String description,
    String uri) throws Exception
  {
      clazz = clazz.trim();
      name = name.trim();
      description = description.trim();
      uri = uri.trim();
      
      checkValidity("<class>", clazz);
      checkValidity("<name>", name);
      checkValidity("<description>", description);
      checkValidity("<CPD URI>", uri);
      
      boolean isNew = false;
      chanRegStore = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
      ChannelType[] types = chanRegStore.getChannelTypes();
      ChannelType chanType = null;
        
      for(int i=0; i<types.length && chanType == null; i++)
      {
          if (types[i].getCpdUri().equals(uri))
              chanType = types[i];
      }
      if (chanType == null) // new one being defined
      {
          isNew = true;
          chanType = chanRegStore.newChannelType();
      }
            
      chanType.setJavaClass(clazz);
      chanType.setName(name);
      chanType.setDescription(description);
      chanType.setCpdUri(uri);
      chanRegStore.saveChannelType(chanType);
      return "The \"" + name + "\" channel type has been "
      + (isNew ? "added" : "updated") + " successfully.";
  }

  private static void msg (String msg) {
    System.out.println(msg);
  }

  private static void printHelp () {
    msg("Usage: RegisterChannelType <class> <name> <description> <CPD URI>");
    msg("  <class> = Fully qualified channel java class. Ex: org.jasig.portal.channels.CImage");
    msg("  <name> = Name of channel type.  Ex: Image channel");
    msg("  <description> = Description of channel type.  Ex: Displays image with optional title and subtitle");
    msg("  <CPD URI> = URL or classpath-relative path to channel publishing document.  Ex: /org/jasig/portal/channels/CImage/CImage.cpd");
  }
}