package org.clairvaux.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.clairvaux.jobs.PredictionPipelineListener;
import org.clairvaux.utils.StringUtils;
import org.cyberneko.html.HTMLConfiguration;

public class RealtimeDataFetcher {

	private static final String REALTIME_DATA_PAGE = "http://www.acleddata.com/data/realtime-data-%d/";
	private static final String REALTIME_DATA_REGEX = "http://www.acleddata.com/wp-content/uploads/%d/%s/(.*)_csv.zip";

	public static void fetch(String parentDir, PredictionPipelineListener pipelineListener, String excludeFile)
			throws IOException {
		String archiveUrl = checkUpdates();
		if (archiveUrl != null && !fileCached(parentDir, archiveUrl)) {
			if (pipelineListener != null) {
				pipelineListener.onDataRefreshed(archiveUrl);
			}
			List<String> fileNames = unpack(parentDir, archiveUrl, excludeFile);
			if (pipelineListener != null) {
				pipelineListener.onDataCached(fileNames);
			}
		}
	}

	private static List<String> unpack(String parentDir, String archiveUrl, String excludeFile) throws IOException {
		List<String> fileNames = new ArrayList<String>();
		byte[] buffer = new byte[2048];
		URL url = new URL(archiveUrl);
		ZipInputStream zis = new ZipInputStream(url.openStream());
		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {
			String fileName = ze.getName();
			if (fileName.contains(excludeFile)) {
				continue;
			}

			File localFile = new File(parentDir + File.separator + fileName);
			fileNames.add(localFile.getName());
			FileOutputStream fos = new FileOutputStream(localFile);

			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();
			ze = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
		return fileNames;
	}

	private static boolean fileCached(String parentDir, String archiveUrl) {
		boolean cached = false;
		File dir = new File(parentDir);
		if (dir.isDirectory()) {
			String[] fileNames = dir.list();
			for (String fileName : fileNames) {
				String normalizedName = fileName.replaceAll("\\s+", "-");
				if (archiveUrl.contains(normalizedName)) {
					cached = true;
					break;
				}
			}
		}
		return cached;
	}

	private static String checkUpdates() throws XNIException, IOException {
		Calendar calendar = Calendar.getInstance();
		Integer year = calendar.get(Calendar.YEAR);
		Integer month = calendar.get(Calendar.MONTH) + 1;
		String monthStr = StringUtils.padString(month.toString(), '0', 2);

		String dataRegex = String.format(REALTIME_DATA_REGEX, year,
				monthStr);
		Pattern dataPattern = Pattern.compile(dataRegex);
		String realtimeDataPage = String.format(REALTIME_DATA_PAGE, year);

		URL url = new URL(realtimeDataPage);
		URLConnection connection = url.openConnection();
		HTMLConfiguration config = new HTMLConfiguration();
		LinkFilter linkFilter = new LinkFilter();
		config.setDocumentHandler(linkFilter);
		config.parse(new XMLInputSource(null, realtimeDataPage, url.toString(), connection.getInputStream(), "UTF-8"));

		Set<String> links = linkFilter.getLinks();
		String archiveUrl = null;
		for (String link : links) {
			Matcher matcher = dataPattern.matcher(link);
			if (matcher.matches()) {
				archiveUrl = link;
				break;
			}
		}
		return archiveUrl;
	}
}
