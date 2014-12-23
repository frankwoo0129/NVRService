package com.foxconn.nvr;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.foxconn.util.JSONReader;

public class ModelConfigureLoader {
	
	public static Map<String, String> loaderByFile(File file) throws IOException {
		HashMap<String, String> map = new HashMap<String, String>();
		JSONArray list;
		try {
			list = (JSONArray) JSONReader.readJSONinFile(file);
		} catch (ParseException e) {
			throw new IOException("JSON parser Error, it's NOT JSONArray.");
		} catch (ClassCastException e) {
			throw new IOException("ClassCastException.");
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		try {
			for (Object obj : list) {
				JSONObject json = (JSONObject) obj;
				map.put((String) json.get("model"), (String) json.get("rtsp"));
			}
		} catch (Exception e) {
			throw new IOException("Configure Error", e);
		}
		return map;
	}

}
