package org.clairvaux.jobs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clairvaux.data.RealtimeDataFetcher;
import org.clairvaux.numenta.prediction.AggregateSubscriber;
import org.clairvaux.numenta.prediction.ConflictPredictionEngine;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PredictionPipelineJob implements Job, PredictionPipelineListener {

	private String rootPath;
	private ConflictPredictionEngine predictionEngine;
	private AggregateSubscriber subscriber;
	private final static Logger LOGGER = Logger.getLogger(PredictionPipelineJob.class.getName());
	
	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		JobDataMap map = ctx.getJobDetail().getJobDataMap();
		rootPath = map.getString("path") + File.separator + "data";
		try {
			RealtimeDataFetcher.fetch(".", this, "MACOSX");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void onDataRefreshed(String archiveUrl) {
		LOGGER.log(Level.INFO, "Found new archive: " + archiveUrl);
	}

	@Override
	public void onDataCached(List<String> fileNames) {
		LOGGER.log(Level.INFO, String.format("Archived %d files", fileNames.size()));
		File dataDir = new File(rootPath);
		if (dataDir.isDirectory()) {
			File[] csvFiles = dataDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.getName().endsWith(".csv");
				}
			});
			
			Set<File> deleteSet = new HashSet<File>();
			File dataFile = null;
			for (File csvFile : csvFiles) {
				deleteSet.add(csvFile);
				if (dataFile == null) {
					dataFile = csvFile;
				} else if (dataFile.lastModified() < csvFile.lastModified()) {
					dataFile = csvFile;
				}
			}
			deleteSet.remove(dataFile);
			
			for (File file : deleteSet) {
				file.delete();
			}
			
			try {
				predictionEngine = new ConflictPredictionEngine(dataFile.getAbsolutePath(), null);
				predictionEngine.setListener(this);
				predictionEngine.startTraining();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
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
		String outputFile = rootPath + File.separator + "graph.json";
		try {
			subscriber.dumpJson(outputFile);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
