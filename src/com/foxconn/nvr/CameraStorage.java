package com.foxconn.nvr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class CameraStorage {
	
//	private static long FREE_SPACE_LIMIT = 200000000000L;
	private final int storedDay;
	private final String videoPath;
	
	public CameraStorage(int storedDay, String videoPath) {
		if (videoPath == null)
			throw new NullPointerException("videoPath is NULL");
		this.videoPath = videoPath;
		this.storedDay = storedDay;
	}
	
	public void exec() throws IOException {
		if (storedDay <= 0)
			return ;
		else {
			File dir = new File(videoPath);
			if (!dir.exists())
				throw new FileNotFoundException("'" + videoPath + "' is NOT exists");
			File[] list = dir.listFiles();
			if (list != null && list.length != 0) {
				Calendar cal = Calendar.getInstance();
				for (File f : list) {
					if ((cal.getTimeInMillis() - f.lastModified()) > (86400000L * storedDay) )
						f.delete();
				}
			}
		}
	}
}
