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

package org.jasig.portal;

/**
 * @author gilbert
 *
 * 
 */
public class Errors {
	public static final ErrorID noResourceFile = new ErrorID("deploy","NoResource","Missing Resource file");
	public static final ErrorID noPropertyFile = new ErrorID("deploy","NoPropertyFile","Missing Property file");
	public static final ErrorID noProperty     = new ErrorID("config","NoProperty","Missing Property file");
	public static final ErrorID propInit       = new ErrorID("config","PropertyInit","Error initializing from Properties");
	public static final ErrorID startServices  = new ErrorID("config","StartServicesFail","Failed to start Portal services");
	public static final ErrorID connectDB      = new ErrorID("config","NoDatabaseConn","Unable to connect to database");
	public static final ErrorID execDB         = new ErrorID("database","SQLError","Database request failed");
	public static final ErrorID noUserInfo     = new ErrorID("database","NoUserInfo","No portal data for userid");           
	public static final ErrorID noTemplateInfo = new ErrorID("database","NoTemplateInfo","No portal data for template");           
	public static final ErrorID bug            = new ErrorID("programming","Bug","Programming error");
	public static final ErrorID badarg         = new ErrorID("programming","badarg","Invalid argument in function call");
	public static final ErrorID badstate       = new ErrorID("programming","badstate","Object is closed, not initialized, or in an incorrect state for request");
	public static final ErrorID missingdata    = new ErrorID("programming","missingdata","Data missing from an internal collection or object");
	public static final ErrorID inconsistency  = new ErrorID("programming","inconsistent","Data is internally inconsistent or malformed");
	public static final ErrorID badrequest     = new ErrorID("programming","badrequest","Program internally generated an invalid request");
	public static final ErrorID newInstance    = new ErrorID("config","newInstance","Problem loading class or creating instance");
	public static final ErrorID browserGone    = new ErrorID("user","disconnect","Browser disconnected from Servlet");
	public static final ErrorID persistout     = new ErrorID("persistance","writing","Unable to save information to disk/database");
	public static final ErrorID persistin      = new ErrorID("persistance","reading","Unable to read information from disk/database");
	public static final ErrorID remote         = new ErrorID("remote","remote","Error in Remote Server");
	public static final ErrorID XMLparse	   = new ErrorID("XML","parse","Error parsing XML");
	public static final ErrorID legacy         = new ErrorID("legacy","legacy","PortalException has no ErrorID");
	
	
}
