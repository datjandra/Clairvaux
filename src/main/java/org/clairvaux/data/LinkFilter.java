package org.clairvaux.data;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.cyberneko.html.filters.DefaultFilter;

class LinkFilter extends DefaultFilter {

	private Set<String> orderedLinks = new LinkedHashSet<String>();

	@Override
	public void startElement(QName element, XMLAttributes attrs, Augmentations augs) {
		if ("A".equals(element.rawname)) {
			String href = attrs.getValue("HREF");
			String hrefLowerCase = attrs.getValue("href");
			if (href != null) {
				orderedLinks.add(href.toLowerCase());
			} else if (hrefLowerCase != null) {
				orderedLinks.add(hrefLowerCase);
			}
		}
	}

	public Set<String> getLinks() {
		return orderedLinks;
	}
}