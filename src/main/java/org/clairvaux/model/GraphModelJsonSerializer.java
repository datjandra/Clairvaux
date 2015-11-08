package org.clairvaux.model;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GraphModelJsonSerializer extends JsonSerializer<GraphModel> {

	@Override
	public void serialize(GraphModel graphModel, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();		
		writeAggregateMatrix(graphModel, jgen);				
		writeConfusionMatrix(graphModel, jgen);		
		jgen.writeEndObject();
	}
	
	private void writeAggregateMatrix(GraphModel graphModel, JsonGenerator jgen) 
			throws IOException, JsonProcessingException {
		jgen.writeObjectFieldStart("aggregate");
		jgen.writeArrayFieldStart("nodes");
		List<String> nodes = graphModel.getNodes();
		for (String node : nodes) {
			jgen.writeStartObject();
			jgen.writeStringField("name", node);
			jgen.writeNumberField("group", 1);
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		
		jgen.writeArrayFieldStart("links");
		Set<Triple<Integer,Integer,Integer>> links = graphModel.getLinks();
		for (Triple<Integer,Integer,Integer> link : links) {
			Integer source = link.getFirst();
			Integer target = link.getSecond();
			jgen.writeStartObject();
			jgen.writeNumberField("source", source);
			jgen.writeNumberField("target", target);
			jgen.writeNumberField("value", link.getThird());
			jgen.writeNumberField("probability", graphModel.lookupFinalProb(source, target));			
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		jgen.writeEndObject();
	}
	
	private void writeConfusionMatrix(GraphModel graphModel, JsonGenerator jgen) 
			throws IOException, JsonProcessingException {		
		jgen.writeObjectFieldStart("quality");
		jgen.writeNumberField("correct", graphModel.getCorrect());
		jgen.writeNumberField("predicted", graphModel.getPredicted());
		jgen.writeNumberField("total", graphModel.getTotal());
		jgen.writeNumberField("accuracy", graphModel.getAccuracy());		
		
		jgen.writeArrayFieldStart("nodes");
		List<Object> classList = graphModel.getClassList();
		for (Object className : classList) {
			jgen.writeStartObject();
			jgen.writeStringField("name", className.toString());
			jgen.writeNumberField("group", 1);
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		
		jgen.writeArrayFieldStart("links");
		for (Object predictedClass : classList) {
			for (Object actualClass : classList) {
				Integer count = graphModel.lookupPredictionCount(predictedClass, actualClass);				
				Integer predicted = classList.indexOf(predictedClass);
				Integer actual = classList.indexOf(actualClass);
				if (count != null) {
					jgen.writeStartObject();
					jgen.writeNumberField("source", predicted);
					jgen.writeNumberField("target", actual);
					jgen.writeNumberField("value", count);				
					jgen.writeEndObject();	
				}							
			}									
		}
		jgen.writeEndArray();
		jgen.writeEndObject();		
	}
}
