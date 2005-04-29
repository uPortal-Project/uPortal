/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Used to store Runtime channel properties.
 * 
 * Warning: ChannelRuntimeProperties are currently useless and have no effect.
 * 
 * Current version gathers the following information
 * <ul>
 *  <li> willRender  - a boolean flag signaling if the channel will render at all in the current state. 
 * This flag could be checked after the ChannelRuntimeData has been passed to a channel, but prior to the renderXML() call.
 *   However, no framework code currently checks the willRender property of ChannelRuntimeData.</li>
 * </ul>
 * @version $Revision$
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 */
public class ChannelRuntimeProperties
{
  private boolean bool_willRender;

    /**
     * Default constructor.
     * willRender property is initialized to <code>true</code>.
     *
     */
    public ChannelRuntimeProperties() {
      // set the default values here
      bool_willRender = true;
    }

    /**
     * Set whether the channel will render if asked to do so (if renderXML() is called).
     *
     * Warning: no known code currently accesses this property of ChannelRuntimeProperties
     * and so currently setting this property will have no effect.
     *
     * @param value a <code>boolean</code> value
     */
    public void setWillRender (boolean value) {
        bool_willRender = value;
    }


    /**
     * Getter method for willRender property.
     *
     * @return a <code>boolean</code> value
     */
    public boolean willRender() {
        return bool_willRender;
    }
}
