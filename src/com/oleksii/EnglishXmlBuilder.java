package com.oleksii;

import com.oleksii.XmlBuilder;

public class EnglishXmlBuilder extends XmlBuilder {

	@Override
	public String addHDVideoModule(String dateStr) {
		StringBuilder xmlBuilder = new StringBuilder("");		
		   
		   xmlBuilder.append("<video height=\"720\" src=\"" + dateStr + "HD.mp4" + "\" width=\"1280\">");
		   xmlBuilder.append("\n\t\t\t\t");
			   		xmlBuilder.append("<param name=\"videoBitrate\" value=\"1000000\" valuetype=\"data\">" + "</param>");			   		
				    xmlBuilder.append("\n\t\t\t\t");
				    xmlBuilder.append("<param name=\"audioBitrate\" value=\"112000\" valuetype=\"data\">" + "</param>");				   		
		   xmlBuilder.append("\n\t\t\t");
		   xmlBuilder.append("</video>");		   
		   xmlBuilder.append("\n\t\t\t");
		   
		   return xmlBuilder.toString();
	}

}