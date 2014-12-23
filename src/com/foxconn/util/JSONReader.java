package com.foxconn.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONReader {
	public static Object readJSONinFile(File file) throws IOException, ParseException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String s = null;
			StringBuffer sb = new StringBuffer();
			while((s = br.readLine()) != null) {
				sb.append(s);
			}
			return new JSONParser().parse(sb.toString());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static Object readJSONinFile(File file, String charsetName) throws IOException, ParseException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
			String s = null;
			StringBuffer sb = new StringBuffer();
			while((s = br.readLine()) != null) {
				sb.append(s);
			}
			return new JSONParser().parse(sb.toString());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
	}
}
