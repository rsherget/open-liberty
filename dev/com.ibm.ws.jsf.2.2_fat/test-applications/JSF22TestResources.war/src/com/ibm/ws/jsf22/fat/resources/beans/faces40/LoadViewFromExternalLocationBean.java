/*******************************************************************************
 * Copyright (c) 2015, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.jsf22.fat.resources.beans.faces40;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * The test bean for loading views from external locations, in this case, using a class-path.
 */
@Named
@SessionScoped
public class LoadViewFromExternalLocationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public LoadViewFromExternalLocationBean() {
    }

    /**
     * Map a given viewId (class-path) from an external location to a URL.
     *
     * Resources packaged into the class-path must be accessed using the getResource() method
     * of the ClassLoader obtained by calling the getContextClassLoader() method of the current Thread
     *
     * @param viewId the viewId (class-path) to the resource file in the external location
     * @return a URL object
     */
    public URL resolveUrl(String viewId) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(viewId);
        return url;
    }

    /**
     * Map a resource file from an external location using a class-path to a URL
     * by calling the ResourceResolver.resolveUrl method.
     *
     * @param resourceName Name of the resource in the external location
     * @return a String containing the URI path used in the Facelet view
     * @throws URISyntaxException
     */
    public String loadViewFromClasspath(String resourceName) throws URISyntaxException {
        // Construct the viewId of the resource file and map it to a URL
        URL url = this.resolveUrl("/META-INF/resources/templates/" + resourceName);
        return url.toURI().toString();
    }

}
