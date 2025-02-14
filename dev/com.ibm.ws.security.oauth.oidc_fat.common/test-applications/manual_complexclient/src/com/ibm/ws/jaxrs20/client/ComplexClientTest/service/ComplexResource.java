/*******************************************************************************
 * Copyright (c) 2014, 2020 IBM Corporation and others.
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
package com.ibm.ws.jaxrs20.client.ComplexClientTest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import com.ibm.websphere.security.WSSecurityException;
import com.ibm.websphere.security.auth.WSSubject;

/**
 * basic resource to test jaxrs20 client API
 */
@Path("ComplexResource")
public class ComplexResource {
	@POST
	@Path("postman/{param}")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("application/x-www-form-urlencoded")
	public String postman(@PathParam("param") String param) {
		String subjectInfo = getSubjectInfo();
		return "EPOSTMAN:" + param + subjectInfo;
	}

	@GET
	@Path("echo1/{param}")
	public String echo1(@PathParam("param") String param) {
		String subjectInfo = getSubjectInfo();
		return "ECHO1:" + param + subjectInfo;
	}

	@GET
	@Path("echo2/{param}")
	public String echo2(@PathParam("param") String param) {
		String subjectInfo = getSubjectInfo();
		return "ECHO2:" + param + subjectInfo;
	}

	@GET
	@Produces("application/json")
	@Path("string")
	public List<String> getCollection() {
		String subjectInfo = getSubjectInfo();
		List<String> list = new ArrayList<String>();
		list.add("string1");
		list.add("");
		list.add("string3");
		list.add(subjectInfo);
		return list;
	}

	@GET
	@Path("three")
	public String returnThree() {
		throw new WebApplicationException(333);
	}

	@GET
	@Path("four")
	public String returnFour() {
		throw new WebApplicationException(444);
	}

	@GET
	@Path("five")
	public String returnFive() {
		throw new WebApplicationException(555);
	}

	@GET
	@Path("person")
	@Produces({ "application/xml", "application/json" })
	public Person getEmptyBook() {
		Person person = new Person();
		person.setFirst("first1");
		person.setLast("last1");
		return person;
	}

	public static class Person {
		String first;
		String last;

		public String getFirst() {
			return first;
		}

		public void setFirst(String first) {
			this.first = first;
		}

		public String getLast() {
			return last;
		}

		public void setLast(String last) {
			this.last = last;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Person))
				return false;
			Person other = (Person) o;
			return this.first.equals(other.first)
					&& this.last.equals(other.last);
		}
	}

	@TRACE
	@Path("trace")
	public String trace() {
		return "trace";
	}

	@GET
	@Path("/SelectVariantTestResponse")
	public Response selectVariantTestResponse(@Context Request req) {
		List<Variant> list = Variant.encodings("CP1250", "UTF-8")
				.languages(Locale.ENGLISH)
				.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build();
		Variant selectedVariant = req.selectVariant(list);
		if (null == selectedVariant)
			return Response.notAcceptable(list).build();
		return Response.ok("entity").build();
	}

	String getSubjectInfo() {
		Subject runAsSubject = null;
		try {
			runAsSubject = WSSubject.getRunAsSubject();
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String info = runAsSubject == null ? " not found" : runAsSubject
				.toString();
		return "\n\nRunAsSubject:" + info;
	}
}
