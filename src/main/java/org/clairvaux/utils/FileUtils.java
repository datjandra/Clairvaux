package org.clairvaux.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

public class FileUtils {

	private FileUtils() {}
	
	public static boolean purgeable(File file, Date date, Long maxAge) {
		Long age = date.getTime() - file.lastModified();
        return (age > maxAge);
    }
	
	public static String stripExtension(String fileName) {
        int dotPosition = fileName.lastIndexOf('.');
        if (dotPosition == -1) {
            return fileName;
        }
        return fileName.substring(0, dotPosition);
    }
	
	public static String extractBaseName(String fileName) {
		return stripExtension(fileName.substring(fileName.lastIndexOf('/')+1));
	}
	
	public static List<String> getFileNames(String dirName) {
		List<String> fileNames = new ArrayList<String>();
		File fileDir = new File(dirName);
		File[] files = fileDir.listFiles();
		if (files != null) {
			for (File file : files) {
				fileNames.add(file.getName());
			}
		}
		return fileNames;
	}
	
	public static String constructUrl(HttpServletRequest request, String paramName, String fileName) 
			throws UnsupportedEncodingException {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();
		String baseName = extractBaseName(fileName);
		String modelLink = 
				String.format("%s://%s:%d%s/view?%s=%s", scheme, serverName, 
						serverPort, contextPath, paramName, URLEncoder.encode(baseName, "UTF-8"));
		return modelLink;
	}
	
	public static String extractFromUrl(String link, String extension) throws IOException {
		String extractedFile = null;
		URL url = new URL(link);
		byte[] buffer = new byte[4096];
		ZipInputStream zis = new ZipInputStream(url.openStream());
		try {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				if (fileName.endsWith(extension)) {
					extractedFile = fileName;
					FileOutputStream fos = new FileOutputStream(fileName);             
			        int len;
			        while ((len = zis.read(buffer)) > 0) {
			        	fos.write(buffer, 0, len);
			        }
			        fos.close();   
			        ze = zis.getNextEntry();
					break;
				}
			}
			zis.closeEntry();
		} finally {
			try {
				zis.close();
			} catch (Exception e) {}
		}
		return extractedFile;
	}
	
	public static File[] filesByLastModified(String dirName) {
		File dir = new File(dirName);
		return filesByLastModified(dir);
	}
	
	public static File[] filesByLastModified(File dir) {
		File[] files = dir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
		    public int compare(File first, File second) {
		        return -Long.compare(first.lastModified(), second.lastModified());
		    }
		});
		return files;
	}
}
