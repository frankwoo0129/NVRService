package com.foxconn.nvr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CameraExecutor {
	
	private static String ffmpeg = "/usr/local/bin/ffmpeg";
	public static void setCommand(String command) {
		ffmpeg = command;
	}
	
	public static String DEFAULT_TIME_STRING = "01:00:00";
	public static int millis = 500;
	public static SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private int startNumber = 1;
	private final String stream;
	private final String jpegPath;
	private final String videoPath;
	private Process p = null;
	
	public CameraExecutor(String stream, String jpeg, String video) {
		if (stream == null)
			throw new NullPointerException("stream is NULL");
		if (jpeg == null)
			throw new NullPointerException("jpegpath is NULL");
		if (video == null)
			throw new NullPointerException("videopath is NULL");
		
		this.stream = stream;
		this.jpegPath = jpeg;
		this.videoPath = video;
	}

	public void exec() throws IOException {
		startNumber = 1;
		String time = DEFAULT_TIME_STRING;
		
		if (!new File(this.jpegPath).exists())
			throw new FileNotFoundException("jpeg dir is NOT exist");
		if (!new File(this.videoPath).exists())
			throw new FileNotFoundException("video dir is NOT exist");

		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		if (cal.get(Calendar.SECOND) > 10 || cal.get(Calendar.MINUTE) > 0) {
			long init = cal.getTimeInMillis();
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			startNumber = (int) (init - cal.getTimeInMillis()) / millis + 1;

			cal.add(Calendar.HOUR_OF_DAY, 1);
			long sub = cal.getTimeInMillis() - init;
			time = String.format("00:%02d:%02d", sub / 60000,
					(sub % 60000) / 1000);
		}

		StringBuffer sb = new StringBuffer();
		sb.append(ffmpeg);
		sb.append(" -rtsp_transport tcp");
		sb.append(" -i ").append(this.stream);
		sb.append(" -vf fps=fps=1000/").append(millis);
		sb.append(" -start_number ").append(startNumber);
		sb.append(" -t ").append(time);
		sb.append(" -f image2 ");
		sb.append(this.jpegPath).append("/out%d.jpg");
		sb.append(" -vcodec copy");
		sb.append(" -an");
		sb.append(" -t ").append(time);
		sb.append(" -f mp4 ");
		sb.append(this.videoPath).append("/").append(sdFormat.format(date)).append(".mp4");

		BufferedReader br = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			if (exitValue() != 0)
				;// TODO
			p = runtime.exec(sb.toString(), null);
			InputStream in = p.getErrorStream();
			br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			boolean isStart = false;
			while ((line = br.readLine()) != null) {
				if (isStart) {
					// if (line.startsWith("frame=")) {
					// String ss = line.replaceFirst(".*?(\\d+).*", "$1");
					// picNumber = Integer.valueOf(ss) + startNumber - 1;
					// }
				} else if (line.startsWith("Press")) {
					isStart = true;
				}
			}
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
			br = null;
			shutdown();
		}
	}
	
	public void shutdown() {
		if (p != null)
			p.destroy();
		p = null;
	}
	
	public int exitValue() {
		if (p != null) {
			try {
				return p.exitValue();
			} catch (Exception e) {
				return -1;
			}
		} else {
			return 0;
		}
	}
}
