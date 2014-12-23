package com.foxconn.nvr;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.foxconn.util.JSONReader;

public class CameraConfigureLoader {
	public static Map<String, CameraConfigure> loadByFile(File file) throws IOException {
		HashMap<String, CameraConfigure> map = new HashMap<String, CameraConfigure>();
		JSONArray list;
		try {
			list = (JSONArray) JSONReader.readJSONinFile(file, "UTF-8");
		} catch (ParseException e) {
			throw new IOException("JSON Parse Error");
		} catch (ClassCastException e) {
			throw new IOException("ClassCastException, it's NOT JSONArray.");
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
			
		try {
			for (Object obj : list) {
				JSONObject json = (JSONObject) obj;
				String address = (String) json.get("address");
				String title = (String) json.get("title");
				String model = (String) json.get("model");
				String user = (json.get("user") == null) ? null : (String) json.get("user");
				String password = (json.get("passwd") == null) ? null : (String) json.get("passwd");
				CameraConfigure config = new CameraConfigure(address, title, model);
				config.setUser(user);
				config.setPassword(password);
				map.put(address, config);
			}
		} catch (Exception e) {
			throw new IOException("Configure Error", e);
		}
		return map;
	}
}
