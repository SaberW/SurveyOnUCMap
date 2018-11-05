package cn.creable.surveyOnUCMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;

public class PathAnalysisHandler extends DefaultHandler {
	
	String text;
	
	public Coordinate[] points;

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.endsWith("routelatlon"))
		{
			String[] list=text.split(";");
			points=new Coordinate[list.length];
			for (int i=0;i<list.length;++i)
			{
				String[] list2=list[i].split(",");
				points[i]=new Coordinate(Double.parseDouble(list2[0]),Double.parseDouble(list2[1]));
				
			}
			text=null;
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (text!=null)
		{
			String currentValue = new String(ch, start, length);
			text+=currentValue;
		}
		super.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.endsWith("routelatlon"))
		{
			text="";
		}
		super.startElement(uri, localName, qName, attributes);
	}

}
