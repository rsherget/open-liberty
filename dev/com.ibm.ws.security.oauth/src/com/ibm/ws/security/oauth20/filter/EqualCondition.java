/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
package com.ibm.ws.security.oauth20.filter;

public class EqualCondition extends SimpleCondition {

    public EqualCondition(String key, IValue value) {
        super(key, value);
    }

    public boolean checkCondition(IValue test) throws FilterException {
        return getValue().equals(test);
    }

    public String getOperand() {
        return "==";
    }
}
