package org.clairvaux.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.clairvaux.utils.FileUtils;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {

	private final static int MAX_FILE_SIZE = 5000 * 1024;
	private final static int MAX_MEM_SIZE = 5 * 1024;
	private final static Logger LOGGER = Logger.getLogger(UploadServlet.class.getName());
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String csvFile = null;
		try {
			csvFile = handleUpload(request);
		} catch (FileUploadException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		if (csvFile != null) {
			String modelLink = FileUtils.constructUrl(request, "model", csvFile);
			request.setAttribute("model", modelLink);
		}
		
		String jsp = "/upload.jsp";
		RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher(jsp);
		requestDispatcher.forward(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String jsp = "/upload.jsp";
		RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher(jsp);
		requestDispatcher.forward(request, response);
	}
	
	private String handleUpload(HttpServletRequest request) throws FileUploadException, IOException {
		String csvFile = null;
		if (ServletFileUpload.isMultipartContent(request)) {
			ServletContext servletContext = getServletConfig().getServletContext();
			String root = servletContext.getRealPath("/");
            File path = new File(root, "/data/uploads");
            if (!path.exists()) {
            	path.mkdirs();
            }
            
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(MAX_MEM_SIZE);
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(MAX_FILE_SIZE);
            
            List<FileItem> items = upload.parseRequest(request);
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                String itemName = item.getName().replaceAll("\\s+", "-");
                
                if (!item.isFormField()) {
                	InputStream inputStream = item.getInputStream();
                	OutputStream outputStream = 
                            new FileOutputStream(new File(path, itemName));
                	
                	int read = 0;
            		byte[] bytes = new byte[2048];
            		while ((read = inputStream.read(bytes)) != -1) {
            			outputStream.write(bytes, 0, read);
            		}
                    inputStream.close();
                    outputStream.close();
                    
                    csvFile = itemName;
                    break;
                }
            }
		}
		LOGGER.log(Level.INFO, "Uploaded " + csvFile);
		return csvFile;
	}
}
