package uni_ko.bpm.Data_Management.XESHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;

public class XES_Writer {

    public void create_header(String path, Data_Set data_set) throws XMLStreamException, FactoryConfigurationError, IOException {
        OutputStream outputStream = new FileOutputStream(new File(path));
        XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(outputStream, "utf-8"));

        out.writeStartDocument("utf-8", "1.0");

        out.writeStartElement("log");
        out.writeAttribute("xes.version", "1.0");
        out.writeAttribute("xmlns", "http://code.deckfour.org/xes");
        out.writeAttribute("xes.creator", "PPM_Plugin");

        out.writeStartElement("extension");
        out.writeAttribute("name", "Concept");
        out.writeAttribute("prefix", "concept");
        out.writeAttribute("uri", "http://code.deckfour.org/xes/concept.xesext");
        out.writeEndElement();

        out.writeStartElement("extension");
        out.writeAttribute("name", "Time");
        out.writeAttribute("prefix", "time");
        out.writeAttribute("uri", "http://code.deckfour.org/xes/time.xesext");
        out.writeEndElement();

        out.writeStartElement("extension");
        out.writeAttribute("name", "Identity");
        out.writeAttribute("prefix", "identity");
        out.writeAttribute("uri", "http://www.xes-standard.org/identity.xesext");
        out.writeEndElement();

        out.writeStartElement("global");
        out.writeAttribute("scope", "trace");
        k_v_pair("string", "concept:name", "name", out);
        out.writeEndElement();

        out.writeStartElement("global");
        out.writeAttribute("scope", "event");
        
        for(Entry<String, Pair<Class<?>, Object>> entry : data_set.getUniqueXESTags().entrySet()) {
        	if(entry.getKey().equals("time:timestamp")) {
        		 k_v_pair("date", "time:timestamp", ((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")).format(new Date(System.currentTimeMillis())).toString()), out);
        	}else if(entry.getKey().contains(":")) {
        		String[] split = entry.getKey().split(":");
        		k_v_pair(entry.getValue().getClass().getSimpleName(), entry.getKey(), split[1], out);
        	}else {
        		String sn = entry.getValue().getClass().getSimpleName();
        		k_v_pair(sn, entry.getKey(), sn, out);
        	}
        }
        out.writeEndElement();
        k_v_pair("string", "creator", "PPM_Plugin", out);

        for (Flow flow : data_set.data_set) {
            out.writeStartElement("trace");
            for (Data_Point d_p : flow.get_flow()) {
                out.writeStartElement("event");
                for(Entry<String, Pair<Class<?>, Object>> entry : d_p.getDataValues().entrySet()) {
                	String name = entry.getValue().getKey().getSimpleName().toLowerCase();
                	String key = entry.getKey();
                	
                	String value = (entry.getValue().getValue() == null) ? "" : entry.getValue().getValue().toString();
                	k_v_pair(name, key, value, out);
                }
                out.writeEndElement();
            }
            out.writeEndElement();
        }
        out.writeEndElement();
        out.writeEndDocument();

        out.close();
        outputStream.close();
    }

    private void k_v_pair(String name, String key, Object value, XMLStreamWriter out) throws XMLStreamException {
        out.writeStartElement(name);
        out.writeAttribute("key", key);
        String value_str = (value != null) ? value.toString() : "null";
        out.writeAttribute("value", value_str);
        out.writeEndElement();
    }
}
