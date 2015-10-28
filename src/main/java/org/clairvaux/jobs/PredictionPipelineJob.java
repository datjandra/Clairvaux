package org.clairvaux.jobs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clairvaux.numenta.prediction.AggregateSubscriber;
import org.clairvaux.numenta.prediction.ConflictPredictionEngine;
import org.clairvaux.utils.FileUtils;
import org.clairvaux.utils.NupicConverter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class PredictionPipelineJob implements Job {

	private final static String SEPARATOR = File.separator;
	private final static Logger LOGGER = Logger.getLogger(PredictionPipelineJob.class.getName());
	private final static Long MAX_AGE = 3 * 2592000000L; // 3 months
	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		LOGGER.log(Level.INFO, "execute()");
		JobDataMap map = ctx.getJobDetail().getJobDataMap();
		String root = String.format("%s%sdata", map.getString("path"), SEPARATOR);
		purgeFiles(root);
		
		Set<String> modelBaseNames = new HashSet<String>();
		File modelDir = new File(root, "models");
		File[] modelFiles = modelDir.listFiles();
		if (modelFiles != null) {	
			for (File file : modelFiles) {
				modelBaseNames.add(FileUtils.stripExtension(file.getName()));
			}
		}
		
		String workingFile = null;
		File uploadDir = new File(root, "uploads");
		LOGGER.log(Level.INFO, "uploads working dir " + uploadDir.getAbsolutePath());
		
		File csvFile = null;
		File[] uploadFiles = uploadDir.listFiles();
		if (uploadFiles != null) {
			for (File file : uploadFiles) {
				String fileName = file.getName();
				String baseName = FileUtils.stripExtension(fileName);
				if (modelBaseNames.contains(baseName)) {
					continue;
				}
			
				if (fileName.endsWith(".csv")) {
					workingFile = baseName;
					csvFile = file;
					break;
				}
			}
		}
		
		if (csvFile != null) {
			try {
				File nupicDir = new File(root, "nupic");
				File nupicFile = new File(nupicDir, String.format("%s.csv", workingFile.replace("_csv", "_nupic")));
				LOGGER.log(Level.INFO, "NUPIC working dir " + nupicDir.getAbsolutePath());
				NupicConverter.outputNupicFormat(csvFile.getAbsolutePath(), nupicFile.getAbsolutePath(), false);				
			
				AggregateSubscriber subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{
				    	"EVENT_DATE",	
				    	"INTERACTION"
				    });
				ConflictPredictionEngine predictionEngine = 
						new ConflictPredictionEngine(nupicFile.getAbsolutePath());
				predictionEngine.startNetwork(subscriber);
				
				LOGGER.log(Level.INFO, "model working dir " + modelDir.getAbsolutePath());
				File graphFile = new File(modelDir, String.format("%s.json", workingFile));
				subscriber.dumpJsonGraph(new FileWriter(graphFile));
			} catch (IOException | InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
	
	private static void purgeFiles(String root) {
		Date now = new Date();
		File modelDir = new File(root, "models");
		if (!modelDir.exists()) {
			modelDir.mkdirs();
		}
		File[] modelFiles = modelDir.listFiles();
		if (modelFiles != null) {
			for (File file : modelFiles) {
				if (FileUtils.purgeable(file, now, MAX_AGE)) {
					file.delete();
				}
			}
		}
		
		File nupicDir = new File(root, "nupic");
		if (!nupicDir.exists()) {
			nupicDir.mkdirs();
		}
		File[] nupicFiles = nupicDir.listFiles();
		if (nupicFiles != null) {
			for (File file : nupicFiles) {
				if (FileUtils.purgeable(file, now, MAX_AGE)) {
					file.delete();
				}
			}
		}
		
		File uploadDir = new File(root, "uploads");
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
		File[] uploadFiles = uploadDir.listFiles();
		if (uploadFiles != null) {
			for (File file : uploadFiles) {
				if (FileUtils.purgeable(file, now, MAX_AGE)) {
					file.delete();
				}
			}
		}
	}
}
