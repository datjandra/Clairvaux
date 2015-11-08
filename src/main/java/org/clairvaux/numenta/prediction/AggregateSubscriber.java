package org.clairvaux.numenta.prediction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clairvaux.model.GraphModel;
import org.clairvaux.model.GraphModelJsonSerializer;
import org.clairvaux.model.Pair;
import org.clairvaux.model.TwoKeyHashMap;
import org.numenta.nupic.algorithms.ClassifierResult;
import org.numenta.nupic.network.Inference;

import rx.Subscriber;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.opencsv.CSVWriter;

public class AggregateSubscriber extends Subscriber<Inference> {

	private final String predictedField;
	private final String[] header;
	private final String[] extraFields;
	private final TwoKeyHashMap<String,String,Integer> mutualCounts;
	private final TwoKeyHashMap<String,String,Double> finalProbDist;
	private final ConfusionMatrix confusionMatrix;
	private final List<String[]> entryList;
	
	private final static Logger LOGGER = Logger.getLogger(AggregateSubscriber.class.getName()); 
	
	public AggregateSubscriber(String predictedField, String[] extraFields) {
		this.mutualCounts = new TwoKeyHashMap<String,String,Integer>();
		this.finalProbDist = new TwoKeyHashMap<String,String,Double>();
		this.predictedField = predictedField;
		this.extraFields = extraFields;	
		this.entryList = new ArrayList<String[]>();
		this.confusionMatrix = new ConfusionMatrix();
		
		header = new String[extraFields.length + 4];
		header[0] = "RECORD_NUM";
		header[1] = String.format("ACTUAL_%s", predictedField);
		header[2] = String.format("PREDICTED_%s", predictedField);
		for (int i=0; i<extraFields.length; i++) {
			header[i+3] = extraFields[i];
		}
		header[header.length-1] = "COUNT";
	}
	
	@Override
	public void onCompleted() {
		LOGGER.log(Level.INFO, "onCompleted()");
	}

	@Override
	public void onError(Throwable e) {
		LOGGER.log(Level.WARNING, e.getMessage(), e);
	}

	@Override
	public void onNext(Inference inference) {
		Integer recordNum = inference.getRecordNum();
		ClassifierResult<Object> classification = inference.getClassification(predictedField);
		Object actual = inference.getClassifierInput().get(predictedField).get("inputValue");
		Object predicted = classification.getMostProbableValue(1);
		confusionMatrix.incrementTotal();
		
		if (predicted == null) {
			return;
		}
		
		String actualValue = actual.toString();
		String predictedValue = predicted.toString();
		if (!mutualCounts.containsKey(actualValue, predictedValue)) {
			mutualCounts.put(actualValue, predictedValue, 1);
		} else {
			Integer count = mutualCounts.get(actualValue, predictedValue);
			mutualCounts.put(actualValue, predictedValue, count + 1);
		}
		confusionMatrix.update(actualValue, predictedValue);
		
		double[] stats = classification.getStats(1);
		Object[] actualValues = classification.getActualValues();
		for (int i=0; i<actualValues.length; i++) {
			finalProbDist.put(actualValue, actualValues[i].toString(), Double.valueOf(stats[i]));
		}
		
		String[] entries = new String[extraFields.length + 3];
		entries[0] = recordNum.toString();
		entries[1] = actual.toString();
		entries[2] = predicted.toString();
		
		for (int i=0; i<extraFields.length; i++) {
			ClassifierResult<Object> classifierResult = inference.getClassification(extraFields[i]);
			entries[i+3] = (classifierResult != null ? classifierResult.getActualValue(0).toString() : "");
		}
		entryList.add(entries);
	}
	
	public void dumpCSV(String csvFile) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
		writer.writeNext(header);
		for (String[] entry : entryList) {
			String[] updatedEntry = new String[entry.length + 1];
			for (int i=0; i<updatedEntry.length-1; i++) {
				updatedEntry[i] = entry[i];
			}
			
			Integer count = mutualCounts.get(entry[1], entry[2]);
			updatedEntry[updatedEntry.length-1] = count != null ? count.toString() : "";
			writer.writeNext(updatedEntry);
		}
		writer.flush();
		writer.close();
	}
	
	public void dumpJsonGraph(Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
		GraphModel graphModel = new GraphModel();
		graphModel.setCorrect(confusionMatrix.getCorrect());
		graphModel.setPredicted(confusionMatrix.getPredicted());
		graphModel.setTotal(confusionMatrix.getTotal());
		graphModel.setAccuracy(confusionMatrix.getAccuracy());
		graphModel.setPredictionMatrix(confusionMatrix.getPredictionMatrix());		
		
		Set<Entry<Pair<String,String>, Integer>> entries = mutualCounts.entrySet();
		for (Entry<Pair<String,String>, Integer> entry : entries) {
			Pair<String,String> edge = entry.getKey();
			String source = edge.getFirst();
			String target = edge.getSecond();
			Integer count = entry.getValue();
			graphModel.addOccurence(source, target, count);
			graphModel.addProb(source, target, finalProbDist.get(source, target));				
		}		
		
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(GraphModel.class, new GraphModelJsonSerializer());
		mapper.registerModule(module);
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(writer, graphModel);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {}
			}
		}
	}
	
	public ConfusionMatrix getConfusionMatrix() {
		return confusionMatrix;
	}
	
	public TwoKeyHashMap<String,String,Integer> getMutualCounts() {
		return mutualCounts;
	}
}
