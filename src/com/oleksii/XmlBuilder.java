package com.oleksii;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlBuilder {
	
	String filePath = "C:\\temp\\";
	
	public static void stringToDom(String xmlSource, String filePath, String fileName) throws IOException {
		File file = new File(filePath + fileName);
		file.getParentFile().mkdirs();
				
	    FileWriter fw = new FileWriter(file);
	    fw.write(xmlSource);
	    fw.close();
	}

	public String createSmil(String dateStr) {
		
		StringBuilder xmlBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		   xmlBuilder.append("\n");
		   
		   xmlBuilder.append("<smil title=\"" + dateStr + ".smil" + "\">");
			   xmlBuilder.append("\n\t");
			   xmlBuilder.append("<body>");			   		
				   xmlBuilder.append("\n\t\t");
				   xmlBuilder.append("<switch>");
				   xmlBuilder.append("\n\t\t\t");
				   		xmlBuilder.append(addHDVideoModule(dateStr));
				   		
				   		xmlBuilder.append("<video height=\"480\" src=\"" + dateStr + ".mp4" + "\" width=\"854\">");
						   xmlBuilder.append("\n\t\t\t\t");
							   		xmlBuilder.append("<param name=\"videoBitrate\" value=\"300000\" valuetype=\"data\">" + "</param>");			   		
								    xmlBuilder.append("\n\t\t\t\t");
								    xmlBuilder.append("<param name=\"audioBitrate\" value=\"112000\" valuetype=\"data\">" + "</param>");				   		
						   xmlBuilder.append("\n\t\t\t");
						   xmlBuilder.append("</video>");
				   xmlBuilder.append("\n\t\t");
				   xmlBuilder.append("</switch>");		   
				   xmlBuilder.append("\n\t");
			   xmlBuilder.append("</body>");
			   xmlBuilder.append("\n");
		   xmlBuilder.append("</smil>"); 
		   
		   return xmlBuilder.toString();
	}
	
	
	public String addHDVideoModule(String dateStr) {
		StringBuilder xmlBuilder = new StringBuilder("");
		
		return xmlBuilder.toString();
	}
	
   
}