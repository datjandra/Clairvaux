package org.clairvaux.numenta.prediction;

import java.util.List;

public interface PredictionPipelineListener {
	void onDataRefreshed(String archiveUrl);
	void onDataCached(List<String> fileNames);
	void onStartTraining();
	void onContinueTraining(int iteration);
	void onStopTraining();
	void onStartPrediction();
	void onStopPrediction();
}
