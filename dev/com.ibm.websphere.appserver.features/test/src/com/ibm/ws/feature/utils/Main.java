package com.ibm.ws.feature.utils;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		AllVerionlessFeatures features = new AllVerionlessFeatures();
		VersionlessFeatureCreator creator = new VersionlessFeatureCreator();
		
		features.gatherAllFeatures();
		
		for(VersionlessFeatureDefinition feat : features.getVersionlessFeatures()) {
			try {
				creator.createPublicVersionlessFeature(feat);
				creator.createPrivateFeatures(feat);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
