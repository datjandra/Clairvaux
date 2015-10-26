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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.opencsv.CSVWriter;

import rx.Subscriber;

public class AggregateSubscriber extends Subscriber<Inference> {

	private final String predictedField;
	private final String[] header;
	private final String[] extraFields;
	private final TwoKeyHashMap<String,String,Integer> mutualCounts;
	private final List<String[]> entryList;
	
	private final static Logger LOGGER = Logger.getLogger(AggregateSubscriber.class.getName()); 
	
	public AggregateSubscriber(String predictedField, String[] extraFields) {
		this.mutualCounts = new TwoKeyHashMap<String,String,Integer>();
		this.predictedField = predictedField;
		this.extraFields = extraFields;	
		this.entryList = new ArrayList<String[]>();
		
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
		Object actual = inference.getClassification(predictedField).getActualValue(0);
		Object predicted = inference.getClassification(predictedField).getMostProbableValue(1);
		if (actual == null || predicted == null) {
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
		Set<Entry<Pair<String,String>, Integer>> entries = mutualCounts.entrySet();
		for (Entry<Pair<String,String>, Integer> entry : entries) {
			Pair<String,String> edge = entry.getKey();
			Integer count = entry.getValue();
			graphModel.addOccurence(edge.getFirst(), edge.getSecond(), count);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(GraphModel.class, new GraphModelJsonSerializer());
		mapper.registerModule(module);
		mapper.writerWithDefaultPrettyPrinter().writeValue(writer, graphModel);
	}
}
