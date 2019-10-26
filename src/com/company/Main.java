package com.company;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

class MyParserCallback extends HTMLEditorKit.ParserCallback {
	public String content = new String();
	public List<String> urls = new ArrayList<String>();
	public boolean isStyle = false;
	public String title = new String();
	public boolean isTitle = false;

	// Override features of the parent's class
	@Override
	public void handleText(char[] data, int pos) {
		if (!isStyle && !isTitle) {
			content += new String(data) + " ";
		} else if (isTitle) {
			title += new String(data) + " ";
		}
	}

	// Override handleStartTag() of the parent's class
	@Override
	public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrSet, int pos) {
		isStyle = false;
		isTitle = false;
		// when calling this function is will save the url as create new column "urls"
		// in callback
		// then the developer can call callback.url to get all url
		if (tag.toString().equals("a")) {
			Enumeration e = attrSet.getAttributeNames();
			while (e.hasMoreElements()) {
				Object aname = e.nextElement();
				if (aname.toString().equals("href")) {
					String u = (String) attrSet.getAttribute(aname);
					if (urls.size() < 1024 && !urls.contains(u))
						urls.add(u);
				}
			}
		}
		// to check the starttag if start tag is style or script do not print anything
		else if (tag == HTML.Tag.STYLE) {
			isStyle = true;
		} else if (tag == HTML.Tag.SCRIPT) {
			isStyle = true;
		} else if (tag.toString().equals("title")) {
			isTitle = true;
		}
	}

}

public class Main {
	// get the html content without tags
	// the function call Parse package function

	static ArrayList<String> ignore_words = new ArrayList<String>();
	static ArrayList<String> blacklist_words = new ArrayList<String>();
	static ArrayList<String> blacklist_urls = new ArrayList<String>();
	static ArrayList<String> keywords = new ArrayList<String>();
	static ArrayList<String> urlPool = new ArrayList<String>();
	static ArrayList<String> processedUrlPool = new ArrayList<String>();
	static int x;
	static int y;
	static HashMap<String, Set<UrlObject>> keywordUrlMap = new HashMap<String, Set<UrlObject>>();

	public static String loadPlainText(String urlString) {
		try {
			MyParserCallback callback = new MyParserCallback();
			ParserDelegator parser = new ParserDelegator();

			URL url = new URL(urlString);
			InputStreamReader reader = new InputStreamReader(url.openStream());
			parser.parse(reader, callback, true); // call MyParserCallback to process the URL stream

			return callback.content;
		} catch (Exception e) {
			return null;
		}
	}

	// The function to call Parser.CallBackHandle start tags
	public static List<String> getURLs(String srcPage) {
		try {
			URL url = new URL(srcPage);
			InputStreamReader reader = new InputStreamReader(url.openStream());

			ParserDelegator parser = new ParserDelegator();
			MyParserCallback callback = new MyParserCallback();
			parser.parse(reader, callback, true);
			// parse the result to callback
			for (int i = 0; i < callback.urls.size(); i++) {
				String str = callback.urls.get(i);
				if (!isAbsURL(str)) {

					callback.urls.set(i, toAbsURL(str, url).toString());

				}
			}
			return callback.urls;
		} catch (Exception e) {
			return null;
		}
	}

	// The function to separate string output
	public static void getUniqueWords(String text, int fileIndex) throws IOException {
		if (text != null) {
			String[] words = text.split("[\\d\\W]+");
			// get the url link and store in UrlObject
			URL url = new URL(urlPool.get(0));
			InputStreamReader reader = new InputStreamReader(url.openStream());
			ParserDelegator parser = new ParserDelegator();
			MyParserCallback callback = new MyParserCallback();
			parser.parse(reader, callback, true);
			// default as appear only 1

			for (String w : words) {
				w = w.toLowerCase();
				if (!ignore_words.contains(w) && !blacklist_words.contains(w)) {
					if (!keywordUrlMap.containsKey(w)) {
						// if not appear before, add new urlObject
						Set<UrlObject> setA = new HashSet<UrlObject>();
						UrlObject newUrl = new UrlObject(urlPool.get(0), callback.title, 1);
						setA.add(newUrl);
						keywordUrlMap.put(w, setA);


					} else if (keywordUrlMap.containsKey(w)) {
						Set<UrlObject> setB = keywordUrlMap.get(w);
						UrlObject newUrl = new UrlObject(urlPool.get(0), callback.title, 1);
						UrlObject existinSet = isPresentinSet(setB, newUrl);

						if (existinSet != null) {
							existinSet.numberAppear++;
							setB.remove(existinSet);
							setB.add(existinSet);
							keywordUrlMap.put(w, setB);
						} else {
							setB.add(newUrl);
							keywordUrlMap.put(w, setB);
						}

					}

				}



			}
		}

	}

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

		redUrl = "http://" + connection.getURL().getAuthority().toString() + connection.getURL().getFile().toString();
		connection.disconnect();

		return redUrl;
	}

	public static UrlObject isPresentinSet(Set<UrlObject> setA, UrlObject link) {
		if (setA.contains(link)) {
			for (UrlObject obj : setA) {
				if (obj.equals(link))
					return obj;
			}
		}

		return null;
	}

	// check it is an absoulte url
	public static boolean isAbsURL(String str) {
		return str.matches("^[a-z0-9]+://.+");
	}

	// convert to absoulte url
	public static URL toAbsURL(String str, URL ref) throws MalformedURLException {
		URL url = null;

		String prefix = "http://" + ref.getHost() + ref.getPath();

		if (ref.getPort() > -1) {
			prefix += ":" + ref.getPort();
		}
		if (!str.startsWith("/")) {
			// System.out.println(ref.getPath());
			if (ref.getPath().length() > ref.getFile().length()) {
				int len = ref.getPath().length() - ref.getFile().length();
				int pathLen = ref.getPath().length();
				// System.out.println(str.substring(0,pathLen));
				if (str.length() >= pathLen) {
					if (str.substring(0, pathLen).equals(ref.getPath())) {
						str = str.substring(pathLen);
					}
				}
				String tmp = ref.getPath().substring(0, len) + "/";
				prefix += tmp.replace("//", "/");
			} else {

				int pathLen = ref.getPath().length();
				// System.out.println(str.substring(0,pathLen));
				if (str.length() >= pathLen) {
					if (str.substring(0, pathLen).equals(ref.getPath())) {
						str = str.substring(pathLen);
					}
				}
			}

		} else {

			int pathLen = ref.getPath().length();
			if (str.length() >= pathLen) {
				if (str.substring(0, pathLen).equals(ref.getPath())) {
					str = str.substring(pathLen);
				}
			}

		}
		url = new URL(prefix + str);

		return url;
	}

	public static void readfile() {
		try {
			File ignore_word_file = new File("src/ignore_of_words.txt");
			File blacklist_word_file = new File("src/blacklist_of_words.txt");
			File blacklist_url_file = new File("src/blacklist_of_urls.txt");
			Scanner igsc = new Scanner(ignore_word_file);
			Scanner bwsc = new Scanner(blacklist_word_file);
			Scanner busc = new Scanner(blacklist_url_file);
			while (igsc.hasNextLine()) {
				ignore_words.add(igsc.nextLine());
			}
			while (bwsc.hasNextLine()) {
				blacklist_words.add(bwsc.nextLine());
			}
			while (busc.hasNextLine()) {
				blacklist_urls.add(busc.nextLine());
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static boolean isValidUrl(String url) {
		boolean isValid = true;
		if (!urlPool.contains(url)) {
			if (urlPool.size() < x) {
				// not in processed url pool
				for (int i = 0; i < processedUrlPool.size(); i++) {
					if (processedUrlPool.get(i).equals(url)) {
						isValid = false;
						// System.out.println(url+" The url is processed already");
						break;
					}
				}
				if (isValid) {
					// check black list
					for (int i = 0; i < blacklist_urls.size(); i++) {
						// check the blacklist url with "*"
						if (blacklist_urls.get(i).substring(blacklist_urls.get(i).length() - 1).equals("*")) {
							if ((blacklist_urls.get(i).length() - 1) <= url.length()) {
								if (url.substring(0, blacklist_urls.get(i).length() - 1).equals(
										blacklist_urls.get(i).substring(0, blacklist_urls.get(i).length() - 1))) {
									isValid = false;
									// System.out.println(url+" url is in the blacklist");
									break;
								}
							}
						} else if (url.equals(blacklist_urls.get(i))) {
							isValid = false;
							// System.out.println(url+" url is in the blacklist");
							break;
						}
					}
				}
			} else {
				isValid = false;
				// System.out.println("url pool is full");
			}
		} else {
			isValid = false;
			// System.out.println(url+" urllink is already in url pool");
		}
		return isValid;
	}

	public static boolean isValidProcessedUrl(String url) {
		boolean isValid = true;

		for (int i = 0; i < processedUrlPool.size(); i++) {
			if (processedUrlPool.get(i).equals(url)) {
				isValid = false;
				// System.out.println(url+" The url is processed already");
				break;
			}
		}

		return isValid;
	}

	public static void main(String[] args) throws Exception {

		// main logic
		// get the ignore words, blacklist words,url into array list

		try {
			readfile();
		} catch (Exception e) {
			System.out.println(e);
		}
		// user input
		Scanner in = new Scanner(System.in);
		System.out.println("Enter x value :");
		x = in.nextInt();

		System.out.println("Enter y value :");
		y = in.nextInt();

		System.out.println("Enter the url :");
		String url = in.next();
		if (isValidUrl(getRedirectUrl(url))) {
			urlPool.add(url);
		}
		
		if (urlPool.size() > 0) {
			int i = 1;

			while (processedUrlPool.size() < y) {
				System.out.println(i + " ******************************************");
				System.out.println(urlPool);
				// System.out.println(loadPlainText(url));

				String reUrl = getRedirectUrl(urlPool.get(0));
				System.out.println(reUrl);
				// check the redirect link is not in processedUrlPool
				if (isValidProcessedUrl(reUrl)) {
					getUniqueWords((loadPlainText(reUrl)), 1); // insert the key value pair in keywordUrlPool hashmap
					List<String> urlList = getURLs(reUrl);
					//System.out.println(urlList);
					processedUrlPool.add(reUrl); // insert the url into processed url pool
					urlPool.remove(0); // delete in url pool
					if (urlList != null) {
						for (String temp : urlList) {
							if (isValidUrl(temp)) {
								urlPool.add(temp);//add link into url pool
							}
						}
					}
					i++;
				} else {
					urlPool.remove(0);
				}
			}
		} else {
			System.out.println("Invalid link");
		}
		// serialize the hashmap
		try
        {
               FileOutputStream fos =
                  new FileOutputStream("hashmap.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(keywordUrlMap);
               oos.close();
               fos.close();
               System.out.println("Serialized HashMap data is saved in hashmap.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }

		// de-serial the hash map
//		 try
//	      {
//			 HashMap<String, Set<UrlObject>> keywordUrlMap2 = new HashMap<String, Set<UrlObject>>();
//	         FileInputStream fis = new FileInputStream("hashmap.ser");
//	         ObjectInputStream ois = new ObjectInputStream(fis);
//	         keywordUrlMap2 = (HashMap) ois.readObject();
//	         ois.close();
//	         fis.close();
//	         for (String i : keywordUrlMap2.keySet()) {
//					System.out.println("key: " + i + " value: " + keywordUrlMap.get(i));
//				}
//	      }catch(IOException ioe)
//	      {
//	         ioe.printStackTrace();
//	         return;
//	      }catch(ClassNotFoundException c)
//	      {
//	         System.out.println("Class not found");
//	         c.printStackTrace();
//	         return;
//	      }

		System.out.println(urlPool);
		System.out.println(processedUrlPool);
		
//		for (String i : keywordUrlMap.keySet()) {
//			System.out.println("key: " + i + " value: " + keywordUrlMap.get(i));
//		}

	}

}
