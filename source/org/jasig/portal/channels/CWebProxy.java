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

package org.jasig.portal.channels;

/**
 * THIS CLASS IS NOW DEPRECATED - Use 
 * org.jasig.portal.channels.webproxy.CWebProxy.
 * <p>A channel which transforms and interacts with dynamic XML or HTML.
 *    See http://www.mun.ca/cc/portal/cw/ for full documentation.
 *    This version introduces experimental features, which may or may
 *    not survive to the next release.  Default values are backwards
 *    compatible with uPortal version 2.0.1.  Only defaults have been
 *    fully tested.</p>
 *
 * <p>Static Channel Parameters:</p>
 * <ol>
 *  <li>"cw_xml" - a URI for the source XML document
 *  <li>"cw_ssl" - a URI for the corresponding .ssl (stylesheet list) file
 *  <li>"cw_xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  <li>"cw_xsl" - a URI for the stylesheet to use
 *                  <i>If <code>cw_xsl</code> is supplied, <code>cw_ssl</code>
 *                  and <code>cw_xslTitle</code> will be ignored.</i>
 *  <li>"cw_passThrough" - indicates how RunTimeData is to be passed through.
 *                  <i>If <code>cw_passThrough</code> is supplied, and not set
 *                  to "all" or "application", additional RunTimeData
 *                  parameters not starting with "cw_" will be passed as
 *                  request parameters to the XML URI.  If
 *                  <code>cw_passThrough</code> is set to "marked", this will
 *                  happen only if there is also a RunTimeData parameter of
 *                  <code>cw_inChannelLink</code>.  "application" is intended
 *                  to keep application-specific links in the channel, while
 *                  "all" should keep all links in the channel.  This
 *                  distinction is handled entirely in the stylesheets.</i>
 *  <li>"cw_tidy" - output from <code>xmlUri</code> will be passed though Jtidy
 *  <li>"cw_info" - a URI to be called for the <code>info</code> event.
 *  <li>"cw_help" - a URI to be called for the <code>help</code> event.
 *  <li>"cw_edit" - a URI to be called for the <code>edit</code> event.
 * <li>"cw_cacheDefaultTimeout" - Default timeout in seconds.
 *  <li>"cw_cacheDefaultScope" - Default cache scope.  <i>May be
 *                  <code>system</code> (one copy for all users), or
 *                  <code>user</code> (one copy per user), or
 *                  <code>instance</code> (cache for this channel instance
 *                  only).</i>
 *  <li>"cw_cacheDefaultMode" - Default caching mode.
 *                  <i>May be <code>none</code> (normally don't cache),
 *                  <code>http</code> (follow http caching directives), or
 *                  <code>all</code> (cache everything).  Http is not
 *                  currently implemented.</i>
 *  <li>"cw_cacheTimeout" - override default for this request only.
 *                  <i>Primarily intended as a runtime parameter, but can
 *                  user statically to override the first instance.</i>
 *  <li>"cw_cacheScope" - override default for this request only.
 *                  <i>Primarily intended as a runtime parameter, but can
 *                  user statically to override the first instance.</i>
 *  <li>"cw_cacheMode" - override default for this request only.
 *                  <i>Primarily intended as a runtime parameter, but can
 *                  user statically to override the first instance.</i>
 *  <li>"cw_person" - IPerson attributes to pass.
 *                  <i>A comma-separated list of IPerson attributes to
 *                  pass to the back end application.</i>
 *  <li>"upc_localConnContext" - The class name of the LocalConnectionContext 
 *                  implementation.
 *                  <i>Use when local data needs to be sent with the
 *                  request for the URL.</i>
 * </ol>
 * <p>Runtime Channel Parameters:</p>
 *    The static parameters above can be updated by equivalent Runtime
 *    parameters.  Caching parameters can also be changed temporarily.
 *    Cache scope and mode can only be made more restrictive, not less.
 *    The following parameter is runtime-only.
 * </p>
 * <ol>
 *  <li>"cw_reset" - an instruction to return to reset internal variables.
 *                 The value <code>return</code> resets <code>cw_xml</code>
 *                 to its last value before changed by button events.  The
 *                 value "reset" returns all variables to the static data
 *                 values.  Runtime data parameter only.
 *  <li>"cw_download" - use download worker for this link or form 
 *                 <i>any link or form that contains this parameter will be 
 *                 handled by the download worker, if the pass-through mode 
 *                 is set to rewrite the link or form.  This allows downloads
 *                 from the proxied site to be delivered via the portal, 
 *                 primarily useful if the download requires verification 
 *                 of a session referenced by a proxied cookie</i>
 *  
 * </ol>
 * <p>This channel can be used for all XML formats with appropriate stylesheets.
 *    All static data parameters as well as additional runtime data parameters
 *    passed to this channel via HttpRequest will in turn be passed on to the
 *    XSLT stylesheet as stylesheet parameters.  They can be read in the
 *    stylesheet as follows:
 *    <code>&lt;xsl:param
 *    name="yourParamName"&gt;aDefaultValue&lt;/xsl:param&gt;</code>
 * </p>
 * @author Andrew Draskoy, andrew@mun.ca
 * @author Sarah Arnott, sarnott@mun.ca
 * @version $Revision$
 * @deprecated Use org.jasig.portal.channels.webproxy.CWebProxy
 */
public class CWebProxy extends org.jasig.portal.channels.webproxy.CWebProxy
{
}
