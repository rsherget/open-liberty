/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
package com.ibm.ws.ejbcontainer.remote.ejb3session.sl.ann.ejb;

import static javax.ejb.TransactionAttributeType.MANDATORY;
import static javax.ejb.TransactionAttributeType.NEVER;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;
import static javax.ejb.TransactionAttributeType.SUPPORTS;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

/**
 * Bean implementation class for Enterprise Bean: BasicCMTStatelessRemote
 **/
@Stateless
public class OneIntOnImplStatelessRemoteBean implements AnnotatedCMTStatelessRemote {
    /** New Line character for this system. **/
    public static final String NL = System.getProperty("line.separator", "\n");
    private static final String CLASS_NAME = OneIntOnImplStatelessRemoteBean.class.getName();
    private final static Logger svLogger = Logger.getLogger(CLASS_NAME);

    @Resource
    private SessionContext ivContext;

    @Override
    public void tx_ADefault() {
        svLogger.info("Method tx_ADefault called successfully");
    }

    @Override
    @TransactionAttribute(REQUIRED)
    public void tx_ARequired() {
        svLogger.info("Method tx_ARequired called successfully");
    }

    @Override
    @TransactionAttribute(NOT_SUPPORTED)
    public void tx_ANotSupported() {
        svLogger.info("Method tx_ANotSupported called successfully");
    }

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void tx_ARequiresNew() {
        svLogger.info("Method tx_ARequiresNew called successfully");
    }

    @Override
    @TransactionAttribute(SUPPORTS)
    public void tx_ASupports() {
        svLogger.info("Method tx_ASupports called successfully");
    }

    @Override
    @TransactionAttribute(NEVER)
    public void tx_ANever() {
        svLogger.info("Method tx_ANever called successfully");
    }

    @Override
    @TransactionAttribute(MANDATORY)
    public void tx_AMandatory() {
        svLogger.info("Method tx_AMandatory called successfully");
    }

    public OneIntOnImplStatelessRemoteBean() {}
}