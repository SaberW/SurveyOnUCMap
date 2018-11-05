/*
 * BasicExtensionParser.java
 * 
 * Copyright (c) 2012, AlternativeVision. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.urizev.gpx.extensions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.urizev.gpx.beans.GPX;
import com.urizev.gpx.beans.Route;
import com.urizev.gpx.beans.Track;
import com.urizev.gpx.beans.Waypoint;

public class DummyExtensionParser implements IExtensionParser {

	public String getId() {
		return "Basic Extension Parser";
	}

	public Object parseWaypointExtension(Node node) {
		return "Parsed Waypoint data";
	}

	public Object parseTrackExtension(Node node) {
		return "Parsed Track data";
	}

	public Object parseGPXExtension(Node node) {
		return "Parsed GPX data";
	}
	
	public Object parseRouteExtension(Node node) {
		return "Parsed Route data";
	}

	public void writeGPXExtensionData(Node node, GPX wpt, Document doc) {
		// TODO Auto-generated method stub		
	}

	public void writeWaypointExtensionData(Node node, Waypoint wpt, Document doc) {
		Node sampleNode = doc.createElement("mySampleExtension");
		sampleNode.setNodeValue("mySampleWaypointValue");
		node.appendChild(sampleNode);
	}

	public void writeTrackExtensionData(Node node, Track wpt, Document doc) {
		Node sampleNode = doc.createElement("mySampleExtension");
		sampleNode.setNodeValue("mySampleTrackValue");
		node.appendChild(sampleNode);
		
	}

	public void writeRouteExtensionData(Node node, Route wpt, Document doc) {
		Node sampleNode = doc.createElement("mySampleExtension");
		sampleNode.setNodeValue("mySampleRouteValue");
		node.appendChild(sampleNode);
	}
}
