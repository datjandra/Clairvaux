package org.clairvaux.utils;

import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
	public static Map<String, Map<String, Object>> setupMap(
			Map<String, Map<String, Object>> map, int n, int w, double min,
			double max, double radius, double resolution, Boolean periodic,
			Boolean clip, Boolean forced, String fieldName, String fieldType,
			String encoderType) {

		if (map == null) {
			map = new HashMap<String, Map<String, Object>>();
		}
		Map<String, Object> inner = null;
		if ((inner = map.get(fieldName)) == null) {
			map.put(fieldName, inner = new HashMap<String, Object>());
		}

		inner.put("n", n);
		inner.put("w", w);
		inner.put("minVal", min);
		inner.put("maxVal", max);
		inner.put("radius", radius);
		inner.put("resolution", resolution);

		if (periodic != null)
			inner.put("periodic", periodic);
		if (clip != null)
			inner.put("clipInput", clip);
		if (forced != null)
			inner.put("forced", forced);
		if (fieldName != null)
			inner.put("fieldName", fieldName);
		if (fieldType != null)
			inner.put("fieldType", fieldType);
		if (encoderType != null)
			inner.put("encoderType", encoderType);
		return map;
	}
}
