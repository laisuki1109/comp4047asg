package com.company;

import java.io.Serializable;

public class UrlObject implements Serializable{

	String url ;
	String title ;
	int numberAppear ;
	
	public UrlObject(String url,String title,int appear) {
		this.url=url;
		this.title=title;
		this.numberAppear =appear;
	}
	 
	
	@Override
	public String toString() {
		return "This url link is "+url+" , title : "+title+" , "+numberAppear ;
	}
	@Override
	public int hashCode() {
		return this.url.hashCode()  ;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UrlObject) {
			UrlObject p = (UrlObject) obj;
			return this.url.equals(p.url) ;
		} else {
			return false;
		}

	}

}
