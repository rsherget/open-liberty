/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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
package io.openliberty.restfulWS30.fat.webXml;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import jakarta.servlet.annotation.WebServlet;

import org.junit.Test;

import componenttest.app.FATServlet;

@SuppressWarnings("serial")
public abstract class AbstractTestServlet extends FATServlet {

    @Test
    public void testCanInvokeResourceClassWithMethodAnnotationsInheritedFromInterface() throws Exception {
        URI uri = URI.create("http://localhost:" + System.getProperty("bvt.prop.HTTP_default") + getContextRoot() + "/pathFromWebXml/foo");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        assertEquals(200, conn.getResponseCode());
        assertEquals("foo", readEntity(conn.getInputStream()));
    }

    @Test
    public void testCanInvokeResourceClassWithMethodAnnotationsOverridingInterface() throws Exception {
        URI uri = URI.create("http://localhost:" + System.getProperty("bvt.prop.HTTP_default") + getContextRoot() + "/pathFromWebXml/bar");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        assertEquals(200, conn.getResponseCode());
        assertEquals("<html><head><title>Bar</title></head><body>Bar</body></html>", readEntity(conn.getInputStream()));
    }

    private String readEntity(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        byte[] b = new byte[256];
        int i = is.read(b);
        while (i > 0) {
            sb.append(new String(b, 0, i));
            i = is.read(b);
        }
        return sb.toString().trim();
    }

    /**
     * Returns context root for the client to use. Must start with slash.
     */
    protected abstract String getContextRoot();
}
