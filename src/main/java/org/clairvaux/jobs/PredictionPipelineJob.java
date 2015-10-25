package org.clairvaux.jobs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clairvaux.numenta.prediction.AggregateSubscriber;
import org.clairvaux.numenta.prediction.ConflictPredictionEngine;
import org.clairvaux.numenta.prediction.PredictionPipelineListener;
import org.clairvaux.utils.FileUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class PredictionPipelineJob implements Job, PredictionPipelineListener {

	private String root;
	private String workingFile;
	private ConflictPredictionEngine predictionEngine;
	private AggregateSubscriber subscriber;
	
	private final static String SEPARATOR = File.separator;
	private final static Logger LOGGER = Logger.getLogger(PredictionPipelineJob.class.getName());
	private final static Long MAX_AGE = 3 * 2592000000L; // 3 months
	private final static Integer TRAINING_CYCLES = 100;
	
	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		LOGGER.log(Level.INFO, "execute()");
		resetAll();
		purgeOldFiles();
		JobDataMap map = ctx.getJobDetail().getJobDataMap();
		root = String.format("%s%sdata", map.getString("path"), SEPARATOR);
		
		Set<String> modelBaseNames = new HashSet<String>();
		File modelDir = new File(root, "models");
		File[] modelFiles = modelDir.listFiles();
		if (modelFiles == null) {
			return;
		}
		
		for (File file : modelFiles) {
			modelBaseNames.add(FileUtils.stripExtension(file.getName()));
		}
		
		File uploadDir = new File(root, "uploads");
		File csvFile = null;
		File[] uploadFiles = uploadDir.listFiles();
		if (uploadFiles == null) {
			return;
		}
		
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
		
		if (csvFile != null) {
			predictionEngine = 
					new ConflictPredictionEngine(csvFile.getAbsolutePath(), TRAINING_CYCLES);
			predictionEngine.setListener(this);
			try {
				predictionEngine.startTraining();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	@Override
	public void onDataRefreshed(String archiveUrl) {
		LOGGER.log(Level.INFO, "onDataRefreshed");
	}

	@Override
	public void onDataCached(List<String> fileNames) {
		LOGGER.log(Level.INFO, "onDataCached");
	}

	@Override
	public void onStartTraining() {
		LOGGER.log(Level.INFO, "onStartTraining()");
	}

	@Override
	public void onContinueTraining(int iteration) {
		LOGGER.log(Level.INFO, "training iterations: " + iteration);
	}

	@Override
	public void onStopTraining() {
		LOGGER.log(Level.INFO, "onStopTraining()");
		if (workingFile == null || workingFile.isEmpty()) {
			return;
		}
		
		subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{
		    	"EVENT_DATE",	
		    	"INTERACTION"
		    });
		predictionEngine.startPrediction(subscriber);
	}

	@Override
	public void onStartPrediction() {
		LOGGER.log(Level.INFO, "onStartPrediction()");
	}

	@Override
	public void onStopPrediction() {
		LOGGER.log(Level.INFO, "onStopPrediction()");
		if (workingFile == null || workingFile.isEmpty()) {
			return;
		}
		
		if (subscriber == null) {
			return;
		}
		
		File modelDir = new File(root, "models");
		File outputFile = new File(modelDir, String.format("%s.json", workingFile));
		try {
			Writer writer = new FileWriter(outputFile);
			subscriber.dumpJsonGraph(writer);
			writer.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private void resetAll() {
		workingFile = null;
		predictionEngine = null;
		subscriber = null;
	}
	
	private void purgeOldFiles() {
		Date now = new Date();
		File modelDir = new File(root, "models");
		File[] modelFiles = modelDir.listFiles();
		if (modelFiles != null) {
			for (File file : modelFiles) {
				if (FileUtils.purgeable(file, now, MAX_AGE)) {
					file.delete();
				}
			}
		}
		
		File uploadDir = new File(root, "uploads");
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
