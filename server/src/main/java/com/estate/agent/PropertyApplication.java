package com.estate.agent;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


@ApplicationPath("/api")
public class PropertyApplication extends Application {

	Set<Object> singletons = new HashSet<Object>();

	PropertyApplication() {
		singletons.add(Api.class);
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}