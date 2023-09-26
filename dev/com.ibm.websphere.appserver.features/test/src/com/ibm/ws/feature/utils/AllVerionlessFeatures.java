package com.ibm.ws.feature.utils;

import java.util.ArrayList;

public class AllVerionlessFeatures {
	
	private ArrayList<VersionlessFeatureDefinition> versionlessFeatures;
	
	public ArrayList<VersionlessFeatureDefinition> getVersionlessFeatures(){
		return this.versionlessFeatures;
	}
	
	public void gatherAllFeatures() {
		versionlessFeatures = new ArrayList<VersionlessFeatureDefinition>();
		
		ArrayList<String[]> persistence = new ArrayList<String[]>();
		
		persistence.add(new String[] {"jpa-2.1", "jakartaPlatform-7.0", "com.ibm.websphere.appserver.jpa-2.1"});
		persistence.add(new String[] {"jpa-2.2", "jakartaPlatform-8.0", "com.ibm.websphere.appserver.jpa-2.2"});
		persistence.add(new String[] {"persistence-3.0", "jakartaPlatform-9.1", "io.openliberty.persistence-3.0"});
		persistence.add(new String[] {"persistence-3.1", "jakartaPlatform-10.0", "io.openliberty.persistence-3.1"});
		
		versionlessFeatures.add(new VersionlessFeatureDefinition("persistence", "Jakarta Persistence", persistence));
		
		ArrayList<String[]> servlet = new ArrayList<String[]>();
		
		servlet.add(new String[] {"servlet-3.1", "jakartaPlatform-7.0", "com.ibm.websphere.appserver.servlet-3.1"});
		servlet.add(new String[] {"servlet-4.0", "jakartaPlatform-8.0", "com.ibm.websphere.appserver.servlet-4.0"});
		servlet.add(new String[] {"servlet-5.0", "jakartaPlatform-9.1", "com.ibm.websphere.appserver.servlet-5.0"});
		servlet.add(new String[] {"servlet-6.0", "jakartaPlatform-10.0", "com.ibm.websphere.appserver.servlet-6.0"});
		
		versionlessFeatures.add(new VersionlessFeatureDefinition("servlet", "Jakarta Servlet", servlet));
		
	}

}
