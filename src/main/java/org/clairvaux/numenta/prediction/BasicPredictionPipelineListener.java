package org.clairvaux.numenta.prediction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicPredictionPipelineListener implements PredictionPipelineListener {

	private ConflictPredictionEngine predictionEngine;
	private AggregateSubscriber subscriber;
	private String status;
	
	private final static Logger LOGGER = Logger.getLogger(BasicPredictionPipelineListener.class.getName());
	
	@Override
	public void onDataRefreshed(String archiveUrl) {}

	@Override
	public void onDataCached(List<String> fileNames) {
		status = "{\"status\":\"DATA_CACHED\"}";
		predictionEngine = new ConflictPredictionEngine("", null);
		predictionEngine.setListener(this);
		try {
			predictionEngine.startTraining();
		} catch (IOException e) {
			status = "{\"status\":\"DATA_CACHED\"}";
		}
	}

	@Override
	public void onStartTraining() {
		status = "{\"status\":\"START_TRAINING\"}";
	}

	@Override
	public void onContinueTraining(int iteration) {
		status = String.format("{\"status\":\"ITERATION_%d\"}", iteration);
	}

	@Override
	public void onStopTraining() {
		status = "{\"status\":\"STOP_TRAINING\"}";
		subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{
	    	"EVENT_DATE",	
	    	"INTERACTION"
	    });
		predictionEngine.startPrediction(subscriber);
	}

	@Override
	public void onStartPrediction() {
		status = "{\"status\":\"START_PREDICTION\"}";
	}

	@Override
	public void onStopPrediction() {
		status = "{\"status\":\"STOP_PREDICTION\"}";
		String outputFile = "src/main/webapp/graph.json";
		try {
			subscriber.dumpJsonGraph(new FileWriter(outputFile));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public String getStatus() {
		return status;
	}
}
