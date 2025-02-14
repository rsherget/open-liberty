/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package javax.servlet.sip.ar.spi;

import javax.servlet.sip.ar.SipApplicationRouter;

/**
 * The @SipServlet annotation allows for the SipServlet metadata to 
 * be declared without having to create the deployment descriptor. 
 */

public abstract class SipApplicationRouterProvider {
	
	/**
	 * Default Ctor
	 */
	public SipApplicationRouterProvider(){}
	
	/**
	 * Retrieve an instance of the application router created by this provider 
	 * @return - application router instance
	 */
	public abstract SipApplicationRouter getSipApplicationRouter();
	
}