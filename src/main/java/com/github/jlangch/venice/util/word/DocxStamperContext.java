package com.github.jlangch.venice.util.word;

import java.util.HashMap;
import java.util.Map;


public class DocxStamperContext {
	
	public DocxStamperContext(final Map<String,Object> properties) {
		this.properties.putAll(properties);
	}
	
	    
    public Map<String,Object> getProperties() {
    	return properties;
    }

     
	private final Map<String,Object> properties = new HashMap<>();

}
