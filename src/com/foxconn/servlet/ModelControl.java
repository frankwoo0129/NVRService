package com.foxconn.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

import com.foxconn.nvr.ModelConfigureLoader;

public class ModelControl extends HttpServlet {
	
	public final static String MODELMAP = "com.foxconn.nvr.model";
	
	private final static String MODELPARAMETER = "cameramodel";
	
	private static Logger logger = Logger.getLogger(ModelControl.class);
	private static Map<String, String> modelConfig = null;
	private static long lastModified = 0L;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadModelFile();
		logger.info("ModelServlet init");
	}
	
	private void loadModelFile() throws ServletException {
		if (this.getServletContext().getInitParameter(MODELPARAMETER) == null) {
			logger.error("NO '" + MODELPARAMETER + "' parameter");
			throw new ServletException("ModelServlet setting error");
		} else {
			File file = new File(this.getServletContext().getRealPath("/WEB-INF"), this.getServletContext().getInitParameter(MODELPARAMETER));
			try {
				if (lastModified == file.lastModified()) {
					logger.debug("ModelFile is NOT changed");
				} else {
					lastModified = file.lastModified();
					modelConfig = ModelConfigureLoader.loaderByFile(file);
					this.getServletContext().setAttribute(MODELMAP, modelConfig);
					logger.debug("Load ModelFile Successfully");
				}
			} catch (Exception e) {
				logger.error("Load ModelFile Error", e);
				throw new ServletException("ModelServlet setting error");
			}
		}
	}
	
	@Override
	public void destroy() {
		if (modelConfig != null)
			modelConfig.clear();
		modelConfig = null;
		this.getServletContext().removeAttribute(MODELMAP);
		logger.info("ModelServlet destory");
		super.destroy();
	}
	
	/**
	 * Parameter:
	 * None.
	 * 
	 * Return:
	 * code: 200
	 * list:
	 * 
	 * Example:
	 * {"code":200, "list":[
	 * {"model":"XXXXX", "rtsp":"YYYYY"},
	 * {"model":"AAAAA", "rtsp":"BBBBB"}
	 * ]}
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject ret = new JSONObject();
		JSONArray list = new JSONArray();
		try {
			Set<Entry<String, String>> set = modelConfig.entrySet();
			Iterator<Entry<String, String>> iter = set.iterator();
			while (iter.hasNext()) {
				JSONObject obj = new JSONObject();
				Entry<String, String> entry = iter.next();
				obj.put("model", entry.getKey());
				obj.put("rtsp", entry.getValue());
				list.add(obj);
			}
			response.setStatus(HttpServletResponse.SC_OK);
			ret.put("list", list);
			ret.put("code", HttpServletResponse.SC_OK);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error(e.getMessage(), e);
		}
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.println(ret);
		out.close();
	}
	
	/**
	 * Given model and address, and return the rtsp link. user and passwd are option.
	 * @param model
	 * @param user this can be NULL
	 * @param password this can be NULL
	 * @param address
	 * @return
	 * @throws NullPointerException if model or address is NULL.
	 * @throws NullPointerException if the model is NOT exist.
	 */
	@Deprecated
	public static String getStream(String model, String user, String password, String address) {
		if (model == null) {
			throw new NullPointerException("model is NULL");
		} else if (address == null) {
			throw new NullPointerException("address is NULL");
		} else {
			String format = modelConfig.get(model);
			if (format == null) {
				throw new NullPointerException("No this model: " + model);
			} else if (user != null && password != null) {
				return String.format(format, user, password, address);
			} else {
				return String.format(format.replace("%s:%s@%s", "%s"), address);
			}
		}
	}

}
