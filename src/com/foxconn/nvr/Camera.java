package com.foxconn.nvr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

public class Camera {
	
	private final CameraConfigure config;
	private final StorageConfigure storageConfig;
	private final String stream;
	private Timer timer = null;
	private CameraExecutor executor = null;
	private CameraStorage storage = null;

	public Camera(CameraConfigure config, StorageConfigure storageConfig, String stream) {
		this.config = config;
		this.storageConfig = storageConfig;
		this.stream = stream;
	}

	public String getAddress() {
		return this.config.getAddress();
	}
	
	public String getTitle() {
		return this.config.getTitle();
	}
	
	public String getModel() {
		return this.config.getModel();
	}
	
	public String getVideoPath() {
		return Paths.get(this.storageConfig.getVideoPath(), config.getAddress()).toAbsolutePath().toString();
	}

	public String getJPEGPath() {
		return Paths.get(this.storageConfig.getJPEGPath(), config.getAddress()).toAbsolutePath().toString();
	}

	public void start() {
		if (timer != null) {
			// It is still running.
			throw new RuntimeException("It is running, " + config.getAddress());
		} else if (this.storageConfig.getVideoPath() == null) {
			// videoStoragePath is NOT set.
			throw new RuntimeException("videoStoragePath is NULL");
		} else if (this.storageConfig.getJPEGPath() == null) {
			// jpegStoragePath is NOT set.
			throw new RuntimeException("jpegStoragePath is NULL");
		}

		File videodir = Paths.get(this.storageConfig.getVideoPath(), config.getAddress()).toFile();
		File jpegdir = Paths.get(this.storageConfig.getJPEGPath(), config.getAddress()).toFile();
		if (!videodir.exists() && !videodir.mkdirs())
			throw new RuntimeException("video Directory create failed");
		if (!jpegdir.exists() && !jpegdir.mkdirs())
			throw new RuntimeException("jpeg Directory create failed");
		
		executor = new CameraExecutor(
				stream,
				jpegdir.getAbsolutePath(),
				videodir.getAbsolutePath());
		storage = new CameraStorage(this.storageConfig.getStoredDay(),
				videodir.getAbsolutePath());
		
		if (timer == null)
			timer = new Timer();
		TimerTask scheduledTask = new TimerTask(){
			@Override
			public void run() {
				try {
					executor.exec();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		TimerTask storageTask = new TimerTask(){
			@Override
			public void run() {
				try {
					storage.exec();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		
		timer.schedule(scheduledTask, 0L, 100L);
		timer.scheduleAtFixedRate(storageTask, 0L, 3600000L);
	}

	public void stop() {
		if (timer != null)
			timer.cancel();
		if (executor != null)
			executor.shutdown();
		timer = null;
		executor = null;
	}

}
