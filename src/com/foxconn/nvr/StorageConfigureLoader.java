package com.foxconn.nvr;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.foxconn.util.JSONReader;

public class StorageConfigureLoader {
	@SuppressWarnings("rawtypes")
	public static Map<String, StorageConfigure> loaderByFile(File file) throws IOException {
		HashMap<String, StorageConfigure> map = new HashMap<String, StorageConfigure>();
		try {
			JSONObject obj = (JSONObject) JSONReader.readJSONinFile(file);
			String videoPath = (String) obj.get("videopath");
			String jpegPath = (String) obj.get("jpegpath");
			int storedDay = ((Long) obj.get("day")).intValue();
			JSONArray group = (JSONArray) obj.get("storagegroup");
			for (Object outur : group) {
				JSONObject outurObj = (JSONObject) outur;
				JSONObject list = (JSONObject) outurObj.get("list");
				String path = (String) outurObj.get("videopath");
				Iterator iter = list.keySet().iterator();
				while (iter.hasNext()) {
					String address = (String) iter.next();
					JSONObject innerObj = (JSONObject) list.get(address);
					StorageConfigure config;
					if (innerObj.get("day") != null) {
						int day = ((Long) innerObj.get("day")).intValue();
						config = new StorageConfigure(address, day);
					} else {
						config = new StorageConfigure(address, storedDay);
					}
					config.setVideoPath(videoPath + path);
					config.setJPEGPath(jpegPath);
					map.put(address, config);
				}
			}
		} catch (ParseException e) {
			throw new IOException("JSON Parse Error");
		} catch (ClassCastException e) {
			throw new IOException("ClassCastException.");
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		return map;
	}
}
