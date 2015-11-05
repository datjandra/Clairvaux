package org.clairvaux.numenta.prediction;

import java.util.Set;

import org.clairvaux.model.Pair;
import org.clairvaux.model.TwoKeyHashMap;

public class ConfusionMatrix {
	
	private Object lastPredicted;
	private Integer total = 0;
	private final TwoKeyHashMap<Object,Object,Integer> predictionMatrix;
	
	public ConfusionMatrix() {
		predictionMatrix = new TwoKeyHashMap<Object,Object,Integer>();
	}
	
	public void update(Object actual, Object predicted) {
		if (lastPredicted != null) {
			if (!predictionMatrix.containsKey(lastPredicted.toString(), actual.toString())) {
				predictionMatrix.put(lastPredicted.toString(), actual.toString(), 1);
			} else {
				Integer count = predictionMatrix.get(lastPredicted.toString(), actual.toString());
				predictionMatrix.put(lastPredicted.toString(), actual.toString(), count+1);
			}
		}
		lastPredicted = predicted;
	}
	
	public Integer getCorrect() {
		Integer correct = 0;
		Set<Pair<Object,Object>> keyPairs = predictionMatrix.keySet();
		for (Pair<Object,Object> pair : keyPairs) {
			if (pair.getFirst().equals(pair.getSecond())) {
				correct += predictionMatrix.get(pair);
			}
		}
		return correct;
	}
	
	public void incrementTotal() {
		total++;
	}
	
	public Integer getTotal() {
		return total;
	}
	
	public Integer getPredicted() {
		Integer predicted = 0;
		for (Integer count : predictionMatrix.values()) {
			predicted += count;
		}
		return predicted;
	}
	
	public Double getAccuracy() {
		Double accuracy = null;
		Integer predicted = getPredicted();
		if (predicted > 0) {
			accuracy = getCorrect().doubleValue() / predicted.doubleValue();
		}
		return accuracy;
	}
}
