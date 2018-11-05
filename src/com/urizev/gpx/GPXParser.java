/*
 * GPXParser.java
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

package com.urizev.gpx;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.urizev.gpx.beans.GPX;
import com.urizev.gpx.beans.Route;
import com.urizev.gpx.beans.Track;
import com.urizev.gpx.beans.Waypoint;
import com.urizev.gpx.extensions.IExtensionParser;
import com.urizev.gpx.types.FixType;

/**
 * <p>
 * This class defines methods for parsing and writing gpx files.
 * </p>
 * <br>
 * Usage for parsing a gpx file into a {@link GPX} object:<br>
 * <code>
 * GPXParser p = new GPXParser();<br>
 * FileInputStream in = new FileInputStream("inFile.gpx");<br>
 * GPX gpx = p.parseGPX(in);<br>
 * </code> <br>
 * Usage for writing a {@link GPX} object to a file:<br>
 * <code>
 * GPXParser p = new GPXParser();<br>
 * FileOutputStream out = new FileOutputStream("outFile.gpx");<br>
 * p.writeGPX(gpx, out);<br>
 * out.close();<br>
 * </code>
 */
public class GPXParser {

	private final ArrayList<IExtensionParser> extensionParsers = new ArrayList<IExtensionParser>();

	/**
	 * Adds a new extension parser to be used when parsing a gpx steam
	 * 
	 * @param parser
	 *            an instance of a {@link IExtensionParser} implementation
	 */
	public void addExtensionParser(IExtensionParser parser) {
		this.extensionParsers.add(parser);
	}

	/**
	 * Parses a stream containing GPX data
	 * 
	 * @param in
	 *            the input stream
	 * @return {@link GPX} object containing parsed data, or null if no gpx data
	 *         was found in the seream
	 * @throws Exception
	 */
	public GPX parseGPX(InputStream in) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(in);
		Node firstChild = doc.getFirstChild();
		if (firstChild != null
				&& GPXConstants.GPX_NODE.equals(firstChild.getNodeName())) {
			GPX gpx = new GPX();
			NamedNodeMap attrs = firstChild.getAttributes();
			for (int idx = 0; idx < attrs.getLength(); idx++) {
				Node attr = attrs.item(idx);
				if (GPXConstants.VERSION_ATTR.equals(attr.getNodeName())) {
					gpx.setVersion(attr.getNodeValue());
				} else if (GPXConstants.CREATOR_ATTR.equals(attr.getNodeName())) {
					gpx.setCreator(attr.getNodeValue());
				}
			}
			NodeList nodes = firstChild.getChildNodes();
			for (int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				if (GPXConstants.WPT_NODE.equals(currentNode.getNodeName())) {
					Waypoint w = this.parseWaypoint(currentNode);
					if (w != null) {
						gpx.addWaypoint(w);
					}
				} else if (GPXConstants.TRK_NODE.equals(currentNode
						.getNodeName())) {
					Track trk = this.parseTrack(currentNode);
					if (trk != null) {
						gpx.addTrack(trk);
					}
				} else if (GPXConstants.EXTENSIONS_NODE.equals(currentNode
						.getNodeName())) {
					for (IExtensionParser parser : this.extensionParsers) {
						Object data = parser.parseGPXExtension(currentNode);
						gpx.addExtensionData(parser.getId(), data);
					}
				} else if (GPXConstants.RTE_NODE.equals(currentNode
						.getNodeName())) {
					Route rte = this.parseRoute(currentNode);
					if (rte != null) {
						gpx.addRoute(rte);
					}
				}
			}
			// TODO: parse route node
			return gpx;
		} else {
		}
		return null;
	}

	/**
	 * Removes an extension parser previously added
	 * 
	 * @param parser
	 *            an instance of a {@link IExtensionParser} implementation
	 */
	public void removeExtensionParser(IExtensionParser parser) {
		this.extensionParsers.remove(parser);
	}

	public void writeGPX(GPX gpx, OutputStream out)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.newDocument();
		Node gpxNode = doc.createElement(GPXConstants.GPX_NODE);
		this.addBasicGPXInfoToNode(gpx, gpxNode, doc);
		if (gpx.getWaypoints() != null) {
			for (Waypoint wp : gpx.getWaypoints()) {
				this.addWaypointToGPXNode(wp, gpxNode, doc);
			}
			for (Track track : gpx.getTracks()) {
				this.addTrackToGPXNode(track, gpxNode, doc);
			}
			for (Route route : gpx.getRoutes()) {
				this.addRouteToGPXNode(route, gpxNode, doc);
			}
		}

		doc.appendChild(gpxNode);

		// Use a Transformer for output
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);
	}

	private void addBasicGPXInfoToNode(GPX gpx, Node gpxNode, Document doc) {
		NamedNodeMap attrs = gpxNode.getAttributes();
		if (gpx.getVersion() != null) {
			Node verNode = doc.createAttribute(GPXConstants.VERSION_ATTR);
			verNode.setNodeValue(gpx.getVersion());
			attrs.setNamedItem(verNode);
		}
		if (gpx.getCreator() != null) {
			Node creatorNode = doc.createAttribute(GPXConstants.CREATOR_ATTR);
			creatorNode.setNodeValue(gpx.getCreator());
			attrs.setNamedItem(creatorNode);
		}

		if (gpx.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			for (IExtensionParser parser : this.extensionParsers) {
				parser.writeGPXExtensionData(node, gpx, doc);
			}
			gpxNode.appendChild(node);
		}
	}

	private void addGenericWaypointToGPXNode(String tagName, Waypoint wpt,
			Node gpxNode, Document doc) {
		Node wptNode = doc.createElement(tagName);
		NamedNodeMap attrs = wptNode.getAttributes();
		if (wpt.getLatitude() != null) {
			Node latNode = doc.createAttribute(GPXConstants.LAT_ATTR);
			latNode.setNodeValue(wpt.getLatitude().toString());
			attrs.setNamedItem(latNode);
		}
		if (wpt.getLongitude() != null) {
			Node longNode = doc.createAttribute(GPXConstants.LON_ATTR);
			longNode.setNodeValue(wpt.getLongitude().toString());
			attrs.setNamedItem(longNode);
		}
		if (wpt.getElevation() != null) {
			Node node = doc.createElement(GPXConstants.ELE_NODE);
			node.appendChild(doc.createTextNode(wpt.getElevation().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getTime() != null) {
			Node node = doc.createElement(GPXConstants.TIME_NODE);
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'kk:mm:ss'Z'");
			node.appendChild(doc.createTextNode(sdf.format(wpt.getTime())));
			wptNode.appendChild(node);
		}
		if (wpt.getMagneticDeclination() != null) {
			Node node = doc.createElement(GPXConstants.MAGVAR_NODE);
			node.appendChild(doc.createTextNode(wpt.getMagneticDeclination()
					.toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getGeoidHeight() != null) {
			Node node = doc.createElement(GPXConstants.GEOIDHEIGHT_NODE);
			node.appendChild(doc
					.createTextNode(wpt.getGeoidHeight().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getName() != null) {
			Node node = doc.createElement(GPXConstants.NAME_NODE);
			node.appendChild(doc.createTextNode(wpt.getName()));
			wptNode.appendChild(node);
		}
		if (wpt.getComment() != null) {
			Node node = doc.createElement(GPXConstants.CMT_NODE);
			node.appendChild(doc.createTextNode(wpt.getComment()));
			wptNode.appendChild(node);
		}
		if (wpt.getDescription() != null) {
			Node node = doc.createElement(GPXConstants.DESC_NODE);
			node.appendChild(doc.createTextNode(wpt.getDescription()));
			wptNode.appendChild(node);
		}
		if (wpt.getSrc() != null) {
			Node node = doc.createElement(GPXConstants.SRC_NODE);
			node.appendChild(doc.createTextNode(wpt.getSrc()));
			wptNode.appendChild(node);
		}
		// TODO: write link node
		if (wpt.getSym() != null) {
			Node node = doc.createElement(GPXConstants.SYM_NODE);
			node.appendChild(doc.createTextNode(wpt.getSym()));
			wptNode.appendChild(node);
		}
		if (wpt.getType() != null) {
			Node node = doc.createElement(GPXConstants.TYPE_NODE);
			node.appendChild(doc.createTextNode(wpt.getType()));
			wptNode.appendChild(node);
		}
		if (wpt.getFix() != null) {
			Node node = doc.createElement(GPXConstants.FIX_NODE);
			node.appendChild(doc.createTextNode(wpt.getFix().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getSat() != null) {
			Node node = doc.createElement(GPXConstants.SAT_NODE);
			node.appendChild(doc.createTextNode(wpt.getSat().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getHdop() != null) {
			Node node = doc.createElement(GPXConstants.HDOP_NODE);
			node.appendChild(doc.createTextNode(wpt.getHdop().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getVdop() != null) {
			Node node = doc.createElement(GPXConstants.VDOP_NODE);
			node.appendChild(doc.createTextNode(wpt.getVdop().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getPdop() != null) {
			Node node = doc.createElement(GPXConstants.PDOP_NODE);
			node.appendChild(doc.createTextNode(wpt.getPdop().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getAgeOfGPSData() != null) {
			Node node = doc.createElement(GPXConstants.AGEOFGPSDATA_NODE);
			node.appendChild(doc.createTextNode(wpt.getAgeOfGPSData()
					.toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getDgpsid() != null) {
			Node node = doc.createElement(GPXConstants.DGPSID_NODE);
			node.appendChild(doc.createTextNode(wpt.getDgpsid().toString()));
			wptNode.appendChild(node);
		}
		if (wpt.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			Iterator<IExtensionParser> it = this.extensionParsers.iterator();
			while (it.hasNext()) {
				it.next().writeWaypointExtensionData(node, wpt, doc);
			}
			wptNode.appendChild(node);
		}
		gpxNode.appendChild(wptNode);
	}

	private void addRouteToGPXNode(Route rte, Node gpxNode, Document doc) {
		Node trkNode = doc.createElement(GPXConstants.RTE_NODE);

		if (rte.getName() != null) {
			Node node = doc.createElement(GPXConstants.NAME_NODE);
			node.appendChild(doc.createTextNode(rte.getName()));
			trkNode.appendChild(node);
		}
		if (rte.getComment() != null) {
			Node node = doc.createElement(GPXConstants.CMT_NODE);
			node.appendChild(doc.createTextNode(rte.getComment()));
			trkNode.appendChild(node);
		}
		if (rte.getDescription() != null) {
			Node node = doc.createElement(GPXConstants.DESC_NODE);
			node.appendChild(doc.createTextNode(rte.getDescription()));
			trkNode.appendChild(node);
		}
		if (rte.getSrc() != null) {
			Node node = doc.createElement(GPXConstants.SRC_NODE);
			node.appendChild(doc.createTextNode(rte.getSrc()));
			trkNode.appendChild(node);
		}
		// TODO: write link
		if (rte.getNumber() != null) {
			Node node = doc.createElement(GPXConstants.NUMBER_NODE);
			node.appendChild(doc.createTextNode(rte.getNumber().toString()));
			trkNode.appendChild(node);
		}
		if (rte.getType() != null) {
			Node node = doc.createElement(GPXConstants.TYPE_NODE);
			node.appendChild(doc.createTextNode(rte.getType()));
			trkNode.appendChild(node);
		}
		if (rte.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			Iterator<IExtensionParser> it = this.extensionParsers.iterator();
			while (it.hasNext()) {
				it.next().writeRouteExtensionData(node, rte, doc);
			}
			trkNode.appendChild(node);
		}
		if (rte.getRoutePoints() != null) {
			Iterator<Waypoint> it = rte.getRoutePoints().iterator();
			while (it.hasNext()) {
				this.addGenericWaypointToGPXNode(GPXConstants.RTEPT_NODE,
						it.next(), trkNode, doc);
			}
		}
		gpxNode.appendChild(trkNode);
	}

	private void addTrackToGPXNode(Track trk, Node gpxNode, Document doc) {
		Node trkNode = doc.createElement(GPXConstants.TRK_NODE);

		if (trk.getName() != null) {
			Node node = doc.createElement(GPXConstants.NAME_NODE);
			node.appendChild(doc.createTextNode(trk.getName()));
			trkNode.appendChild(node);
		}
		if (trk.getComment() != null) {
			Node node = doc.createElement(GPXConstants.CMT_NODE);
			node.appendChild(doc.createTextNode(trk.getComment()));
			trkNode.appendChild(node);
		}
		if (trk.getDescription() != null) {
			Node node = doc.createElement(GPXConstants.DESC_NODE);
			node.appendChild(doc.createTextNode(trk.getDescription()));
			trkNode.appendChild(node);
		}
		if (trk.getSrc() != null) {
			Node node = doc.createElement(GPXConstants.SRC_NODE);
			node.appendChild(doc.createTextNode(trk.getSrc()));
			trkNode.appendChild(node);
		}
		// TODO: write link
		if (trk.getNumber() != null) {
			Node node = doc.createElement(GPXConstants.NUMBER_NODE);
			node.appendChild(doc.createTextNode(trk.getNumber().toString()));
			trkNode.appendChild(node);
		}
		if (trk.getType() != null) {
			Node node = doc.createElement(GPXConstants.TYPE_NODE);
			node.appendChild(doc.createTextNode(trk.getType()));
			trkNode.appendChild(node);
		}
		if (trk.getExtensionsParsed() > 0) {
			Node node = doc.createElement(GPXConstants.EXTENSIONS_NODE);
			Iterator<IExtensionParser> it = this.extensionParsers.iterator();
			while (it.hasNext()) {
				it.next().writeTrackExtensionData(node, trk, doc);
			}
			trkNode.appendChild(node);
		}
		if (trk.getTrackPoints() != null) {
			Node trksegNode = doc.createElement(GPXConstants.TRKSEG_NODE);
			Iterator<Waypoint> it = trk.getTrackPoints().iterator();
			while (it.hasNext()) {
				this.addGenericWaypointToGPXNode(GPXConstants.TRKPT_NODE,
						it.next(), trksegNode, doc);
			}
			trkNode.appendChild(trksegNode);
		}
		gpxNode.appendChild(trkNode);
	}

	private void addWaypointToGPXNode(Waypoint wpt, Node gpxNode, Document doc) {
		this.addGenericWaypointToGPXNode(GPXConstants.WPT_NODE, wpt, gpxNode,
				doc);
	}

	private Date getNodeValueAsDate(Node node) throws DOMException,
			ParseException {
		// 2012-02-25T09:28:45Z
		Date val = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
		val = sdf.parse(node.getFirstChild().getNodeValue());

		return val;
	}

	private Double getNodeValueAsDouble(Node node) {
		return Double.parseDouble(node.getFirstChild().getNodeValue());
	}

	private FixType getNodeValueAsFixType(Node node) {
		return FixType.returnType(node.getFirstChild().getNodeValue());
	}

	private Integer getNodeValueAsInteger(Node node) {
		return Integer.parseInt(node.getFirstChild().getNodeValue());
	}

	private String getNodeValueAsString(Node node) {
		if (node == null) {
			return null;
		}

		Node child = node.getFirstChild();
		if (child == null) {
			return null;
		}
		return child.getNodeValue();
	}

	private Route parseRoute(Node node) throws Exception {
		if (node == null) {
			return null;
		}
		Route rte = new Route();
		NodeList nodes = node.getChildNodes();
		if (nodes != null) {
			for (int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				if (GPXConstants.NAME_NODE.equals(currentNode.getNodeName())) {
					rte.setName(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.CMT_NODE.equals(currentNode
						.getNodeName())) {
					rte.setComment(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.DESC_NODE.equals(currentNode
						.getNodeName())) {
					rte.setDescription(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.SRC_NODE.equals(currentNode
						.getNodeName())) {
					rte.setSrc(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.LINK_NODE.equals(currentNode
						.getNodeName())) {
					// TODO: parse link
					// rte.setLink(getNodeValueAsLink(currentNode));
				} else if (GPXConstants.NUMBER_NODE.equals(currentNode
						.getNodeName())) {
					rte.setNumber(this.getNodeValueAsInteger(currentNode));
				} else if (GPXConstants.TYPE_NODE.equals(currentNode
						.getNodeName())) {
					rte.setType(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.RTEPT_NODE.equals(currentNode
						.getNodeName())) {
					Waypoint wp = this.parseWaypoint(currentNode);
					if (wp != null) {
						rte.addRoutePoint(wp);
					}
				} else if (GPXConstants.EXTENSIONS_NODE.equals(currentNode
						.getNodeName())) {
					Iterator<IExtensionParser> it = this.extensionParsers
							.iterator();
					while (it.hasNext()) {
						while (it.hasNext()) {
							IExtensionParser parser = it.next();
							Object data = parser
									.parseRouteExtension(currentNode);
							rte.addExtensionData(parser.getId(), data);
						}
					}
				}
			}
		}

		return rte;
	}

	private Track parseTrack(Node node) throws Exception {
		if (node == null) {
			return null;
		}
		Track trk = new Track();
		NodeList nodes = node.getChildNodes();
		if (nodes != null) {
			for (int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				if (GPXConstants.NAME_NODE.equals(currentNode.getNodeName())) {
					trk.setName(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.CMT_NODE.equals(currentNode
						.getNodeName())) {
					trk.setComment(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.DESC_NODE.equals(currentNode
						.getNodeName())) {
					trk.setDescription(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.SRC_NODE.equals(currentNode
						.getNodeName())) {
					trk.setSrc(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.LINK_NODE.equals(currentNode
						.getNodeName())) {
					// TODO: parse link
					// trk.setLink(getNodeValueAsLink(currentNode));
				} else if (GPXConstants.NUMBER_NODE.equals(currentNode
						.getNodeName())) {
					trk.setNumber(this.getNodeValueAsInteger(currentNode));
				} else if (GPXConstants.TYPE_NODE.equals(currentNode
						.getNodeName())) {
					trk.setType(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.TRKSEG_NODE.equals(currentNode
						.getNodeName())) {
					ArrayList<Waypoint> segment = this.parseTrackSeg(currentNode);
					trk.addTrackPoints(segment);
					trk.addTrackSegment(segment);
				} else if (GPXConstants.EXTENSIONS_NODE.equals(currentNode
						.getNodeName())) {
					Iterator<IExtensionParser> it = this.extensionParsers
							.iterator();
					while (it.hasNext()) {
						while (it.hasNext()) {
							IExtensionParser parser = it.next();
							Object data = parser
									.parseTrackExtension(currentNode);
							trk.addExtensionData(parser.getId(), data);
						}
					}
				}
			}
		}

		return trk;
	}

	private ArrayList<Waypoint> parseTrackSeg(Node node) throws Exception {
		if (node == null) {
			return null;
		}
		ArrayList<Waypoint> trkpts = new ArrayList<Waypoint>();

		NodeList nodes = node.getChildNodes();
		if (nodes != null) {
			for (int idx = 0; idx < nodes.getLength(); idx++) {
				Node currentNode = nodes.item(idx);
				if (GPXConstants.TRKPT_NODE.equals(currentNode.getNodeName())) {
					Waypoint wp = this.parseWaypoint(currentNode);
					if (wp != null) {
						trkpts.add(wp);
					}
				} else if (GPXConstants.EXTENSIONS_NODE.equals(currentNode
						.getNodeName())) {
					/*
					 * Iterator<IExtensionParser> it =
					 * extensionParsers.iterator(); while(it.hasNext()) {
					 * IExtensionParser parser = it.next(); Object data =
					 * parser.parseWaypointExtension(currentNode);
					 * //.addExtensionData(parser.getId(), data); }
					 */
				}
			}
		}
		return trkpts;
	}

	/**
	 * Parses a wpt node into a Waypoint object
	 * 
	 * @param node
	 * @return Waypoint object with info from the received node
	 * @throws Exception
	 */
	private Waypoint parseWaypoint(Node node) throws Exception {
		if (node == null) {
			return null;
		}
		Waypoint w = new Waypoint(null, 0, 0);
		NamedNodeMap attrs = node.getAttributes();
		// check for lat attribute
		Node latNode = attrs.getNamedItem(GPXConstants.LAT_ATTR);
		if (latNode != null) {
			Double latVal = null;
			latVal = Double.parseDouble(latNode.getNodeValue());
			w.setLatitude(latVal);
		} else {
			throw new Exception("no lat value in waypoint data.");
		}
		// check for lon attribute
		Node lonNode = attrs.getNamedItem(GPXConstants.LON_ATTR);
		if (lonNode != null) {
			Double lonVal = Double.parseDouble(lonNode.getNodeValue());
			w.setLongitude(lonVal);
		} else {
			throw new Exception("no lon value in waypoint data.");
		}

		NodeList childNodes = node.getChildNodes();
		if (childNodes != null) {
			for (int idx = 0; idx < childNodes.getLength(); idx++) {
				Node currentNode = childNodes.item(idx);
				if (GPXConstants.ELE_NODE.equals(currentNode.getNodeName())) {
					w.setElevation(this.getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.TIME_NODE.equals(currentNode
						.getNodeName())) {
					w.setTime(this.getNodeValueAsDate(currentNode));
				} else if (GPXConstants.NAME_NODE.equals(currentNode
						.getNodeName())) {
					w.setName(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.CMT_NODE.equals(currentNode
						.getNodeName())) {
					w.setComment(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.DESC_NODE.equals(currentNode
						.getNodeName())) {
					w.setDescription(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.SRC_NODE.equals(currentNode
						.getNodeName())) {
					w.setSrc(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.MAGVAR_NODE.equals(currentNode
						.getNodeName())) {
					w.setMagneticDeclination(this
							.getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.GEOIDHEIGHT_NODE.equals(currentNode
						.getNodeName())) {
					w.setGeoidHeight(this.getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.LINK_NODE.equals(currentNode
						.getNodeName())) {
					// TODO: parse link
					// w.setGeoidHeight(getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.SYM_NODE.equals(currentNode
						.getNodeName())) {
					w.setSym(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.FIX_NODE.equals(currentNode
						.getNodeName())) {
					w.setFix(this.getNodeValueAsFixType(currentNode));
				} else if (GPXConstants.TYPE_NODE.equals(currentNode
						.getNodeName())) {
					w.setType(this.getNodeValueAsString(currentNode));
				} else if (GPXConstants.SAT_NODE.equals(currentNode
						.getNodeName())) {
					w.setSat(this.getNodeValueAsInteger(currentNode));
				} else if (GPXConstants.HDOP_NODE.equals(currentNode
						.getNodeName())) {
					w.setHdop(this.getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.VDOP_NODE.equals(currentNode
						.getNodeName())) {
					w.setVdop(this.getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.PDOP_NODE.equals(currentNode
						.getNodeName())) {
					w.setPdop(this.getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.AGEOFGPSDATA_NODE.equals(currentNode
						.getNodeName())) {
					w.setAgeOfGPSData(this.getNodeValueAsDouble(currentNode));
				} else if (GPXConstants.DGPSID_NODE.equals(currentNode
						.getNodeName())) {
					w.setDgpsid(this.getNodeValueAsInteger(currentNode));
				} else if (GPXConstants.EXTENSIONS_NODE.equals(currentNode
						.getNodeName())) {
					Iterator<IExtensionParser> it = this.extensionParsers
							.iterator();
					while (it.hasNext()) {
						IExtensionParser parser = it.next();
						Object data = parser
								.parseWaypointExtension(currentNode);
						w.addExtensionData(parser.getId(), data);
					}
				}
			}
		}

		return w;
	}
}