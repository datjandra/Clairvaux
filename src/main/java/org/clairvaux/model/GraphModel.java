package org.clairvaux.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphModel {

	private List<String> nodes = null;
	private Set<Triple<Integer,Integer,Integer>> links = null;
	private TwoKeyHashMap<Integer,Integer,Double> finalProbDist = null;
	private TwoKeyHashMap<Object,Object,Integer> predictionMatrix = null;
	private Double accuracy;
	private Integer correct;
	private Integer predicted;
	private Integer total;
		
	public void addOccurence(String first, String second, Integer count) {
		if (nodes == null) {
			nodes = new ArrayList<String>();
		}
		
		if (!nodes.contains(first)) {
			nodes.add(first);
		}
		
		if (!nodes.contains(second)) {
			nodes.add(second);
		}
		
		if (links == null) {
			links = new HashSet<Triple<Integer,Integer,Integer>>();
		}
		
		Integer firstIndex = nodes.indexOf(first);
		Integer secondIndex = nodes.indexOf(second);
		links.add(new Triple<Integer,Integer,Integer>(firstIndex, secondIndex, count));
	}
	
	public void addProb(String first, String second, Double prob) {
		if (finalProbDist == null) {
			finalProbDist = new TwoKeyHashMap<Integer,Integer,Double>();
		}
		Integer firstIndex = nodes.indexOf(first);
		Integer secondIndex = nodes.indexOf(second);
		finalProbDist.put(firstIndex, secondIndex, prob);
	}		
	
	public List<String> getNodes() {
		return nodes;
	}
	
	public Set<Triple<Integer,Integer,Integer>> getLinks() {
		return links;
	}
	
	public Double lookupFinalProb(Integer first, Integer second) {
		return finalProbDist.get(first, second);
	}
	
	public void setPredictionMatrix(TwoKeyHashMap<Object,Object,Integer> predictionMatrix) {
		this.predictionMatrix = predictionMatrix;
	}
	
	public Integer lookupPredictionCount(Object predicted, Object actual) {
		return predictionMatrix.get(predicted, actual);
	}
	
	public List<Object> getClassList() {
		Set<Object> classes = new HashSet<Object>();
		Set<Pair<Object,Object>> classPairs = predictionMatrix.keySet();
		for (Pair<Object,Object> pair : classPairs) {
			classes.add(pair.getFirst());
			classes.add(pair.getSecond());
		}
		return Collections.unmodifiableList(new ArrayList<Object>(classes));
	}

	public Double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	public Integer getCorrect() {
		return correct;
	}

	public void setCorrect(Integer correct) {
		this.correct = correct;
	}

	public Integer getPredicted() {
		return predicted;
	}

	public void setPredicted(Integer predicted) {
		this.predicted = predicted;
	}
	
	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}	
}
