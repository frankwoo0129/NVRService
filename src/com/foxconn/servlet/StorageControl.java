package com.foxconn.servlet;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.foxconn.nvr.StorageConfigure;
import com.foxconn.nvr.StorageConfigureLoader;

public class StorageControl extends HttpServlet {
	
	public static final String STORAGEMAP = "com.foxconn.nvr.storage";
	public static final long DAY_OF_MILLISECOND = 86400000L;
	public static final long HALF_DAY_OF_MILLISECOND = 43200000L;
	public static final int DAY_OF_DEFAULT = 2;
	public static final int PERCENTAGE_OF_DEFAULT = 90;
	
	private static final String STORAGEPARAMETER = "storage";
	
	private static Logger logger = Logger.getLogger(StorageControl.class);
	private static Map<String, StorageConfigure> mapStorageConfig = null;
	private static long lastModified = 0L;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadStorageFile();
		logger.info("StorageServlet init");
	}
	
	private void loadStorageFile() throws ServletException {
		logger.debug("loadStorageFile");
		if (this.getServletContext().getInitParameter(STORAGEPARAMETER) == null) {
			logger.error("No '" + STORAGEPARAMETER + "' parameter");
			throw new ServletException("StorageServlet setting error");
		} else {
			File file = new File(this.getServletContext().getRealPath("/WEB-INF"), this.getServletContext().getInitParameter(STORAGEPARAMETER));
			try {
				if (lastModified == file.lastModified()) {
					logger.debug("StorageFile is NOT changed");
				} else {
					lastModified = file.lastModified();
					mapStorageConfig = StorageConfigureLoader.loaderByFile(file);
					this.getServletContext().setAttribute(STORAGEMAP, mapStorageConfig);
					logger.debug("Load StorageFile Successfully");
				}	
			} catch (Exception e) {
				logger.error("Load StorageFile Error", e);
				throw new ServletException("StorageServlet setting error");
			}
		}
	}
	
	public void destroy() {
		if (mapStorageConfig != null)
			mapStorageConfig.clear();
		mapStorageConfig = null;
		this.getServletContext().removeAttribute(STORAGEMAP);
		logger.info("Storage Servlet destory");
		super.destroy();
	}

}
