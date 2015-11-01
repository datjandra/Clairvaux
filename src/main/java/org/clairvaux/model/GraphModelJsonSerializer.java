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
			jgen.writeStartObject();
			jgen.writeNumberField("source", link.getFirst());
			jgen.writeNumberField("target", link.getSecond());
			jgen.writeNumberField("value", link.getThird());
			jgen.writeNumberField("probability", graphModel.lookupMostRecentProb(link.getFirst(), link.getSecond()));
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		jgen.writeEndObject();
	}

}
