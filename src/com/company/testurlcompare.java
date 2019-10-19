package com.company;

public class testurlcompare {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url ="https://www.weibo.com/hk?category=Ãdª«";
		String compareurl="https://www.weibo.com/*";
		if (compareurl.substring(compareurl.length()-1).equals("*")) {
			if (url.substring(0, compareurl.length()-1).equals(compareurl.substring(0,compareurl.length()-1))) {
				System.out.println("yyyy");
			}
		}

	}

}
