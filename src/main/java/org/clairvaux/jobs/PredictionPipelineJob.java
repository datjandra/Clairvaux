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
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class PredictionPipelineJob implements Job {

	private final static String SEPARATOR = File.separator;
	private final static Logger LOGGER = Logger
			.getLogger(PredictionPipelineJob.class.getName());
	private final static Long MAX_AGE = 3 * 2592000000L; // 3 months
	private final static Integer CLAIRVAUX_TRAINING_CYCLES = 10;

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		LOGGER.log(Level.INFO, "begin job");
		JobDataMap map = ctx.getJobDetail().getJobDataMap();
		String root = String.format("%s%sdata", map.getString("path"),
				SEPARATOR);
		purgeFiles(root);

		File modelDir = new File(root, "models");
		File[] modelFiles = FileUtils.filesByLastModified(modelDir);
		Set<String> modelNames = new HashSet<String>();
		for (File file : modelFiles) {
			modelNames.add(FileUtils.stripExtension(file.getName()));
		}

		int trainingCycles = 1;
		boolean learn = false;
		File workingFile = null;
		File uploadDir = new File(root, "uploads");
		File[] uploadFiles = FileUtils.filesByLastModified(uploadDir);
		for (File file : uploadFiles) {
			String fileName = file.getName();
			if (!fileName.endsWith(".csv")) {
				continue;
			}

			String baseName = FileUtils.stripExtension(file.getName());
			if (modelNames.contains(baseName)) {
				workingFile = file;
				break;
			} else {
				try {
					trainingCycles = Integer.parseInt(System.getProperty(
							"CLAIRVAUX_TRAINING_CYCLES",
							CLAIRVAUX_TRAINING_CYCLES.toString()));
				} catch (NumberFormatException e) {
					trainingCycles = CLAIRVAUX_TRAINING_CYCLES;
				}
				learn = true;
				workingFile = file;
				break;
			}
		}

		if (workingFile != null) {
			AggregateSubscriber subscriber = new AggregateSubscriber(
					"EVENT_TYPE", new String[] { "EVENT_DATE", "INTERACTION",
							"LOCATION" });
			try {
				ConflictPredictionEngine.NETWORK.startCompute(
						workingFile.getAbsolutePath(), null, subscriber,
						trainingCycles, learn);
				String baseName = FileUtils.stripExtension(workingFile
						.getName());
				File graphFile = new File(modelDir, String.format("%s.json",
						baseName));
				subscriber.dumpJsonGraph(new FileWriter(graphFile));
				LOGGER.log(Level.INFO, "Prediction accuracy: "
						+ subscriber.getConfusionMatrix().getAccuracy());
				LOGGER.log(Level.INFO,
						"Created model file " + graphFile.getAbsolutePath());
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			workingFile.delete();
		}
		LOGGER.log(Level.INFO, "end job");
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
