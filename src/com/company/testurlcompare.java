package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class testurlcompare {
	public static String getRedirectUrl(String url) {
		URL urlTmp = null;
		String redUrl = null;
		HttpURLConnection connection = null;

		try {
			urlTmp = new URL(url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		try {
			connection = (HttpURLConnection) urlTmp.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			connection.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		}

		redUrl = connection.getURL().toString();
		connection.disconnect();

		return redUrl;
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		// TODO Auto-generated method stub
		String url ="http://comp.hkbu.edu.hk/v1/";
		//System.out.println(getRedirectUrl(url));
		URL link = new URL(url);
		System.out.println(link.getAuthority()+" "+link.getPath()+ " "+link.getFile());
		int pathLen = link.getPath().length();
		String str="/v1/?lang=tc";
		System.out.println(str.substring(0,pathLen).equals(link.getPath()));
		str =str.substring(pathLen);
		System.out.println(str);
		


	}

}
