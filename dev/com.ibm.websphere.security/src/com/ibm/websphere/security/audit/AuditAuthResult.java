/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
package com.ibm.websphere.security.audit;

import com.ibm.websphere.ras.annotation.Trivial;

/**
 * AuthenticationResult enumeration of response codes.
 * 
 * A separate class because the compiler conflicts between ant and
 * eclipse are very annoying.
 */
@Trivial
public enum AuditAuthResult {
    UNKNOWN, SUCCESS, FAILURE, SEND_401, REDIRECT, TAI_CHALLENGE, CONTINUE, REDIRECT_TO_PROVIDER, RETURN, OAUTH_CHALLENGE
}