/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.threading;

/**
 * A thread pool without a maximum number of possible worker threads
 *
 * @author <a href="mailto:mvi@immagic.com>Mike Ivanov</a>
 * @version $Revision$
 */


public class UnboundedThreadPool extends BoundedThreadPool {

       /**
	 * UnoundedThreadPool Construcutor
	 *
	 * @param minThreads the initial number of worker threads to place in the pool
	 * @param threadPriority the priority these worker threads should have
	 */
	public UnboundedThreadPool(int minThreads, int threadPriority) {
                super(minThreads,minThreads*2,threadPriority);
	}


}
