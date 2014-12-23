 package com.foxconn.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.foxconn.nvr.Camera;
import com.foxconn.nvr.CameraConfigure;
import com.foxconn.nvr.CameraConfigureLoader;
import com.foxconn.nvr.StorageConfigure;

public class CameraControl extends HttpServlet {
	
	private final static String CONFIGUREPARAMETER = "configfile";
	
	private static boolean isInit = false;
	private static boolean isStart = false;
	private static Logger logger = Logger.getLogger(CameraControl.class);
	private static Object lock = new Object();// Lock. For init(ServletConfig) and start()
	private static Map<String, CameraConfigure> mapCameraConfig = null;// Address -> Configure
	private static Map<String, Camera> cameras = new HashMap<String, Camera>();// Address -> Camera
	private static Map<String, String> titles = new HashMap<String, String>();// Title -> Address. To check title is exist.
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		synchronized(lock) {
			if (isInit)
				return ;
			else
				isInit = true;
			super.init(config);
		}
		
		loadConfigureFile();
		start();
		
		logger.info("CameraServlet init");
	}
	
	private void loadConfigureFile() throws ServletException {
		logger.debug("loadConfigureFile");
		if (this.getServletContext().getInitParameter(CONFIGUREPARAMETER) == null) {
			logger.error("No '"+ CONFIGUREPARAMETER + "' parameter");
			throw new ServletException("CamearaServlet setting error");
		} else {
			File file = new File(this.getServletContext().getRealPath("/WEB-INF"), this.getServletContext().getInitParameter(CONFIGUREPARAMETER));
			try {
				mapCameraConfig = CameraConfigureLoader.loadByFile(file);
				Collection<CameraConfigure> c = mapCameraConfig.values();
				Iterator<CameraConfigure> iter = c.iterator();
				while (iter.hasNext()) {
					addCamera(iter.next());
				}
			} catch (Exception e) {
				logger.error("Load ConfigureFile Error", e);
				throw new ServletException("CamearaServlet setting error");
			}
		}
	}
	
	public void reset() {
		stop();
		cameras.clear();
		titles.clear();
	}
	
	@Override
	public void destroy() {
		reset();
		cameras = null;
		titles = null;
		logger.info("CameraServlet destroy");
		super.destroy();
	}
	
	/**
	 * Start All Cameras
	 * @see Camera#start()
	 */
	private void start() {
		logger.debug("start");
		synchronized(lock) {
			if (isStart) {
				logger.warn("All cameras are starting");
				return;
			} else {
				isStart = true;
			}
		}
		
		Set<Entry<String, Camera>> set = cameras.entrySet();
		Iterator<Entry<String, Camera>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, Camera> entry = iter.next();
			Camera camera = entry.getValue();
			if (camera != null) {
				try {
					camera.start();
				} catch (Exception e) {
					logger.error("start Camera error, address: " + camera.getAddress());
				}
			} else {
				logger.warn("Camera is NULL, address=" + entry.getKey());
			}
		}
	}
	
	/**
	 * Stop All Cameras
	 * @see Camera#stop()
	 */
	private void stop() {
		logger.debug("stop");
		Set<Entry<String, Camera>> set = cameras.entrySet();
		Iterator<Entry<String, Camera>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, Camera> entry = iter.next();
			Camera camera = entry.getValue();
			if (camera != null) {
				camera.stop();
				logger.info("stop Camera. address: " + camera.getAddress() + ", title: " + camera.getTitle());
			} else {
				logger.warn("Camera is NULL, address=" + entry.getKey());
			}
		}
		synchronized(lock) {
			isStart = false;
		}
	}
	
	/**
	 * check JSON if there are parameter needed, "address", "title", "group", "model".
	 * @throws NullPointerException
	 */
	@Deprecated
	public void checkJSON(JSONObject obj) {
		if (obj.get("address") == null || !(obj.get("address") instanceof String)) {
			throw new NullPointerException("address is NULL");
		} else if (obj.get("title") == null || !(obj.get("title") instanceof String)) {
			throw new NullPointerException("title is NULL");
		} else if (obj.get("model") == null || !(obj.get("model") instanceof String)) {
			throw new NullPointerException("model is NULL");
		}
	}
	
	/**
	 * Add a camera. The camera will NOT start.
	 * @throws IllegalArgumentException if the address or title is exists, or model is NOT exists.
	 */
	private boolean addCamera(CameraConfigure config) throws ServletException {
		logger.info("addCamera");
		if (config == null)
			throw new NullPointerException("config is NULL");
		logger.info("address: " + config.getAddress());
		logger.info("title: " + config.getTitle());
		logger.info("model: " + config.getModel());
		logger.debug("user: " + config.getUser());
		logger.debug("passwd: " + config.getPassword());
		if (cameras.get(config.getAddress()) != null) {
			throw new IllegalArgumentException("camera is exist, address: " + config.getAddress());
		} else {
			try {
				@SuppressWarnings("unchecked")
				Map<String, StorageConfigure> mapStorageConfig = (Map<String, StorageConfigure>) this.getServletContext().getAttribute(StorageControl.STORAGEMAP);
				StorageConfigure storageConfigure = mapStorageConfig.get(config.getAddress());
				if (storageConfigure == null) {
					logger.warn("add camera error, address=" + config.getAddress());
					return false;
				} 

				@SuppressWarnings("unchecked")
				Map<String, String> modelConfig = (Map<String, String>) this.getServletContext().getAttribute(ModelControl.MODELMAP);
				String format = modelConfig.get(config.getModel());
				String stream;
				if (format == null) {
					throw new IllegalArgumentException("No this model: " + config.getModel());
				} else if (config.getUser() != null && config.getPassword() != null) {
					stream = String.format(format, config.getUser(), config.getPassword(), config.getAddress());
				} else {
					logger.warn("'user' or 'password' is NULL, address=" + config.getAddress());
					stream = String.format(format.replace("%s:%s@%s", "%s"), config.getAddress());
				}
				
				Camera camera = new Camera(config, storageConfigure, stream);
				cameras.put(config.getAddress(), camera);
				mapCameraConfig.put(config.getAddress(), config);
				titles.put(config.getTitle(), config.getAddress());
				return true;
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (Exception e) {
				logger.error("storage configure file error", e);
				throw new ServletException("Servlet setting error");
			}
		}
	}
	
	
	/**
	 * Remove a camera
	 * @param address IP address
	 * @throws IllegalAccessException Get Camera Error
	 * @see CameraControl#getCamera(String)
	 */
	@SuppressWarnings("unused")
	private void removeCamera(String address) throws IllegalAccessException {
		logger.info("removeCamera");
		logger.info("address: " + address);
		Camera camera = getCamera(address);
		camera.stop();
		cameras.remove(address);
		titles.remove(camera.getTitle());
		mapCameraConfig.remove(address);
	}
	
	
	/**
	 * @param address IP address
	 * @return JSONObject it contains "address", "title", "videopath", "jpegpath"
	 * @throws IllegalAccessException Get Camera Error
	 * @see CameraControl#getCamera(String)
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getCameraStatus(String address) throws IllegalAccessException {
		JSONObject ret = new JSONObject();
		Camera camera = getCamera(address);
		ret.put("address", camera.getAddress());
		ret.put("title", camera.getTitle());
		ret.put("videopath", camera.getVideoPath());
		ret.put("jpegpath", camera.getJPEGPath());
		return ret;
	}
	
	
	/**
	 * @param address IP address
	 * @return Camera Object
	 * @throws IllegalAccessException Get Camera Error
	 * @throws NullPointerException if address is NULL.
	 */
	private Camera getCamera(String address) throws IllegalAccessException {
		Camera camera = cameras.get(address);
		if (camera == null) {
			logger.debug("camera is NULL, address: " + address);
			throw new IllegalAccessException("camera is NULL");
		} else {
			return camera;
		}
	}
	
	/**
	 * Parameter:
	 * address: camera's address you want to get the status.
	 * title: same with address.
	 * 
	 * return:
	 * the return is json.
	 * code: http code, only 200 and 400.
	 * msg: return msg if error.
	 * address:
	 * title:
	 * videopath:
	 * jpegpath:
	 * 
	 * @see CameraControl#getCameraStatus(String, String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject ret = new JSONObject();
		if (request.getPathInfo() == null) {
			try {
				Iterator<Entry<String, CameraConfigure>> iter = mapCameraConfig.entrySet().iterator();
				JSONArray list = new JSONArray();
				while (iter.hasNext()) {
					CameraConfigure config = iter.next().getValue();
					JSONObject obj = new JSONObject();
					obj.put("address", config.getAddress());
					obj.put("title", config.getTitle());
					obj.put("model", config.getModel());
					list.add(obj);
				}
				ret.put("list", list);
				ret.put("code", HttpServletResponse.SC_OK);
			} catch (Exception e) {
				logger.error(e.getClass().getName(), e);
				ret.put("msg", "Server Error");
				ret.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			String address = null;
			try {
				address = request.getPathInfo().split("/")[1];
				JSONObject obj = getCameraStatus(address);
				ret.putAll(obj);
				ret.put("code", HttpServletResponse.SC_OK);
			} catch (Exception e) {
				ret.put("msg", e.getMessage());
				ret.put("address", address);
				ret.put("code", HttpServletResponse.SC_NOT_FOUND);
			}
		}
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(ret);
		out.close();
	}
	
}
