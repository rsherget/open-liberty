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
package com.ibm.websphere.logging.hpel.reader;

/**
 * A filter to select log records based on fields available from the {@link RepositoryLogRecord}.
 * 
 * @ibm-api
 */
public interface LogRecordFilter {
	/**
	 * Checks if record should be accepted into the list.
	 * 
	 * @param record log record to check
	 * @return <code>true</code> if record should be included in the list;
	 * 			<code>false</code> otherwise.
	 */
	boolean accept(RepositoryLogRecord record);
}
