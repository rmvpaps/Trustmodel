package monitor;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @brief WSLA parser, gets qos information from wsla file
 * @author Lilei
 */
public class WSLAParser {

	/**the wsla file path*/
	private String wslaFilePath;
	
	/**
	 * Get the first child element by name
	 * @param father
	 * @param name
	 * @return
	 */
	protected Element getFirstChildByName(Element father,String name){
		if(father==null||name==null)
			return null;
		NodeList children = father.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) 
		{
			Node node = children.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				Element ele = (Element) node;
				if(ele.getTagName().equals(name))
					return ele;
			}
		}
		return null;
	}
	
	public WSLAParser(String filePath){
		wslaFilePath = filePath;
	}
	
	/**
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @brief parse wsla file 
	 * 
	 * The structure of wsla (web service level agreements) file is like:
	 * 	
	 * 	1	xml definitions...
	 * 	2	parties					(we can ignore it)
	 * 	3	ServiceDefinition		(irrelevant for the wsc)
	 * 		...
	 * 	n	ServiceDefinition		(irrelevant for the wsc)
	 * 	n+1	Obligations				(that's it~,we get qos from this node and its children)
	 * 
	 * Obligation node has very succinct structure, an example is listed below:
	 * 
	 * 	Obligations
	 * 		1		ServiceLevelObjective	Response Time qos of the first service 
	 * 		2		ServiceLevelObjective	Throughput qos of the first service
	 * 				...	
	 * 		2n-1	ServiceLevelObjective	Response Time qos of the nth service
	 * 		2n		ServiceLevelObjective	Throughput qos of the nth service
	 */
	public void parse() throws ParserConfigurationException, SAXException, IOException{
		
		/**first initialize the dom parser...*/
		File qos = new File(wslaFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(qos);
		doc.getDocumentElement().normalize();
		
		/**now we get the root element of qos informations, that is, the element with
		 * name "Obligations"
		 **/
		Element obligations = getFirstChildByName(doc.getDocumentElement(), "Obligations");
		NodeList qosList = obligations.getChildNodes();
		
		
		//Qos parameters and values
		Map<String, String> advq = new HashMap<String, String>();
		
		for(int i=0;i<qosList.getLength();i++)
		{
			Node node = qosList.item(i);
			System.out.println(node.getNodeName());
			if (node.getNodeType() != Node.ELEMENT_NODE) 
				continue;
			else
			{
				System.out.println("going in");
				Element temp = (Element)node ;
				if(temp.getNodeName().contains("ServiceLevelObjective"))
				{
					System.out.println("found");
					Element expression = getFirstChildByName(temp, "Expression");
					Element predicate =  getFirstChildByName(expression, "Predicate");
					String q = getFirstChildByName(predicate,"SLAParameter").getTextContent();
					String val = getFirstChildByName(predicate,"Value").getTextContent();
				
					advq.put(q, val);
					
				}
			}
		}
		
		for(String q:advq.keySet())
		{
			System.out.println(q + "=" + advq.get(q));
		}
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException
	{
		WSLAParser ws = new WSLAParser("/home/rini/projworkspace/TrustManagementService/WebContent/sla.xml");
		ws.parse();
	}

	
}
