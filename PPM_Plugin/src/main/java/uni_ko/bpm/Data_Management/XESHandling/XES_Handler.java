package uni_ko.bpm.Data_Management.XESHandling;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;

public class XES_Handler{
	private String process_Definition_Id = null;
	public static Data_Set read_XES(String path, List<DataSetCalculation> dsc) {
		return XES_Handler.read_XES(new File(path),null, dsc);
	}
	public static Data_Set read_XES(File inputFile, String process_Definition_Id, List<DataSetCalculation> dsc) {
		try {
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	        SAXParser saxParser;
			saxParser = factory.newSAXParser();
			
	        XES_Parser xes_parser = new XES_Parser(dsc);
	        saxParser.getParser();
	        
			saxParser.parse(inputFile, xes_parser);
	
			Data_Set result = xes_parser.get_Data_Set();
			if(process_Definition_Id != null) {
				result.modify_process_definition_Id(process_Definition_Id);
			}else if(result.get_process_definition_Id() == null) {
				result.modify_process_definition_Id(inputFile.getName());
			}
	        return result;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void write_XES(String path, Data_Set data_set) {
		try {
			XES_Writer xes_writer = new XES_Writer();
			xes_writer.create_header(path, data_set);
		} catch (XMLStreamException
				| FactoryConfigurationError | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
