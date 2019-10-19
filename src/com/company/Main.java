package com.company;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

class MyParserCallback extends HTMLEditorKit.ParserCallback {
    public String content = new String();
    public List<String> urls = new ArrayList<String>();
    public Boolean isStyle=false;
    // Override features of the parent's class
    @Override
    public void handleText(char[] data, int pos) {
    	if(!isStyle) {
    		content += new String(data) + " ";
    		}
    }

    // Override handleStartTag() of the parent's class
    @Override
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrSet, int pos)
    {
    	isStyle=false;
    	// when calling this function is will save the url as create new column "urls" in callback
    	//then the developer can call callback.url to get all url
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
        	isStyle=true;
        }
        else if (tag == HTML.Tag.SCRIPT) {
        	isStyle=true;
        } 
    }
    
}

public class Main {
     //      get the html content without tags
    // the function call Parse package function
	
	static ArrayList<String> ignore_words = new ArrayList<String>();
	static ArrayList<String> blacklist_words = new ArrayList<String>();
	static ArrayList<String> blacklist_urls = new ArrayList<String>();
	static ArrayList<String> keywords = new ArrayList<String>();
	static String[] urlPool;
	static UrlObject[] processedUrlPool;
	
	
    public static String loadPlainText(String urlString) throws IOException {
        MyParserCallback callback = new MyParserCallback();
        ParserDelegator parser = new ParserDelegator();

        URL url = new URL(urlString);
        InputStreamReader reader = new InputStreamReader(url.openStream());
        parser.parse(reader, callback, true); // call MyParserCallback to process the URL stream

        return callback.content;
    }
    
    //The function to call Parser.CallBackHandle start tags
    public static List<String> getURLs(String srcPage) throws IOException {
        URL url = new URL(srcPage);
        InputStreamReader reader = new InputStreamReader(url.openStream());

        ParserDelegator parser = new ParserDelegator();
        MyParserCallback callback = new MyParserCallback();
        parser.parse(reader, callback, true);
        //parse the result to callback
        for (int i=0; i<callback.urls.size(); i++) { 
            String str = callback.urls.get(i);
            if (!isAbsURL(str)) {
                callback.urls.set(i, toAbsURL(str, url).toString());
            }
        }
        return callback.urls;
    }

    // The function to separate string output
    public static List<String> getUniqueWords(String text) {
        String[] words = text.split("[\\d\\W]+");
        ArrayList<String> uniqueWords = new ArrayList<String>();

        for (String w : words) {
            w = w.toLowerCase();

            if (!uniqueWords.contains(w))
                uniqueWords.add(w);
        }
        // abc order (joe instant function(?)
        uniqueWords.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        });

        return uniqueWords;
    }
    
    //check it is an absoulte url
    public static boolean isAbsURL(String str) {
        return str.matches("^[a-z0-9]+://.+");
    }

    //convert to absoulte url
    public static URL toAbsURL(String str, URL ref) throws MalformedURLException {
        URL url = null;

        String prefix =  ref.getProtocol() + "://" + ref.getHost()+ ref.getPath();

        if (ref.getPort() > -1)
            prefix += ":" + ref.getPort();

        if (!str.startsWith("/")) {
            int len = ref.getPath().length() - ref.getFile().length();
            String tmp = ref.getPath().substring(0, len) + "/";
            prefix += tmp.replace("//", "/");
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
    		while(igsc.hasNextLine()) {
    			ignore_words.add(igsc.nextLine());
    		}
    		while(bwsc.hasNextLine()) {
    			blacklist_words.add(bwsc.nextLine());
    		}
    		while(busc.hasNextLine()) {
    			blacklist_urls.add(busc.nextLine());
    		}
    		
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    }



    //get all the html content with tag
 /*
    static String loadWebPage(String urlString) {
        byte[] buffer = new byte[1024];
        String content = new String();
        try {
            URL url = new URL(urlString);
            InputStream in = url.openStream();
            int len;
            while((len = in.read(buffer)) != -1)
                content += new String(buffer);
        } catch (IOException e) {
            content = "<h1>Unable to download the page</h1>" + urlString;
        }
        return content;
    }
*/
    public static void main(String[] args) throws IOException {
        // write your code here
    	//get the ignore words, blacklist words,url into array list
    	/*
    	try {
    		readfile();
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	*/
    	Scanner in = new Scanner(System.in);  
    	
    	//define the length of url pool
        System.out.println("Enter x");
        int x = in.nextInt();
        urlPool = new String[x];
        
        // define the length of processed url pool
        System.out.println("Enter y");
        int y = in.nextInt();
        processedUrlPool = new UrlObject[y];
        
        //put the url into urlPool array
        String url = "http://comp.hkbu.edu.hk/v1/";
        urlPool[0]=url;
        
            //System.out.println(loadPlainText(url));
            //System.out.println(getUniqueWords((loadPlainText(url))));
            //System.out.println(getURLs(url));

        System.out.println("hello");

        FileWriter writer = new FileWriter("output.txt"); 
    	for(String str: getUniqueWords(loadPlainText(url))) {
    	  writer.write(str + System.lineSeparator());
    	}
    	writer.close();
    }

}
