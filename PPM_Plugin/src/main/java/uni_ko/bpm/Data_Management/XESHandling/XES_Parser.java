package uni_ko.bpm.Data_Management.XESHandling;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;
import weka.core.UnsupportedClassTypeException;


public class XES_Parser extends DefaultHandler {

	private List<DataSetCalculation> dsc;
	public XES_Parser(List<DataSetCalculation> dsc) {
		this.dsc = dsc;
	}
    private Data_Set d_s = new Data_Set(null, dsc);

    private Flow current_flow = null;
    private Data_Point current_point = null;


    private HashMap<String, Integer> unique_Task_Name = new HashMap<String, Integer>();
    private boolean in_trace = false;
    private Date last_Date;

    private boolean failed = false;
    private boolean has_start_tag = false;
    
    
    
    @Override
    public void startElement(String uri, String localName, String element_Name, Attributes attributes) throws SAXException {
        
    	if (element_Name.equalsIgnoreCase("trace")) {
            this.in_trace = true;
        } else if (this.current_flow == null && in_trace && attributes.getValue("key").equalsIgnoreCase("concept:name")) {
        	this.current_flow = new Flow(attributes.getValue("value"));
        } else if (this.current_point == null && element_Name.equalsIgnoreCase("event")) {
            this.current_point = new Data_Point();
        }else if(element_Name.equalsIgnoreCase("event")) {
        	// ignore because in start to complete cycle
        } else if (in_trace && this.current_point != null) {
            // currently writing to event
        	String name = attributes.getValue("key").toLowerCase();
        	String value = attributes.getValue("value");

        	switch (name) {
			case "lifecycle:transition":
            	if(value.equals("start")) {
            		this.has_start_tag = true;
            	}else if(value.equals("complete")) {
            		this.has_start_tag = false;
            	}
				break;
			default:
				if(value.equals("null")) {
					if(this.d_s.getUniqueXESTags().containsKey(name)) {
						try {
							this.checkSXesT(SupportedXESTypes.lookup(this.d_s.getUniqueXESTags().get(name).getKey()), name, value);
						} catch (UnsupportedClassTypeException e) {
							e.printStackTrace();
						}
					}else {
						this.checkSXesT(SupportedXESTypes.lookup(element_Name), name, value);
					}
				}else {
		        	for(SupportedXESTypes sxt : SupportedXESTypes.values()) {
		        		if(sxt.regEx.matcher(value).matches()) {
		        			this.checkSXesT(sxt, name, value);
		        			break;
		        		}
		        		
		        	}
				}
			}
        }
        this.d_s.set_unique_Tokens(this.unique_Task_Name);

    }

    @Override
    public void endElement(String uri, String localName, String element_Name) throws SAXException {
        if (element_Name.equalsIgnoreCase("trace")) {
            Flow f = this.current_flow;
            this.d_s.add_Flow(f);
            this.current_flow = new Flow(null);
            this.current_flow = null;
            this.last_Date = null;
        } else if (element_Name.equalsIgnoreCase("event")) {
        	if(!this.has_start_tag) {
                Data_Point dp = this.current_point;
                this.current_flow.add_Data_Point(dp, failed);
                this.failed = false;
                this.current_point = new Data_Point();
                this.current_point = null;
        	}
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {

    }

    public Data_Set get_Data_Set() {
        return this.d_s;
    }
    
    private void checkSXesT(SupportedXESTypes sXesT, String name, String value) {
		if(sXesT.type.equals(Date.class)) {
			try {
				this.current_point.putDateValue(name, Date.class, sXesT.getCaster().invoke(null, value));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}else {
			try {
				this.current_point.putDataValue(name, sXesT.type, sXesT.getCaster().invoke(null, value));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
    }


}
