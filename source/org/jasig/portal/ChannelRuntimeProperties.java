/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Used to store Runtime channel properties.
 * Current version gathers the following information
 * <ul>
 *  <li> willRender  - a boolean flag signaling if the channel will render at all in the current state. This flag will be checked after the ChannelRuntimeData has been passed to a channel, but prior to the renderXML() call.</li>
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
     * Setter method for willRender property.
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
