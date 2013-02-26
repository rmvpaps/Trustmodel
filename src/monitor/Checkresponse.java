package monitor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import dataaccess.DB_TA;
import trustauthority.Performance;
import trustauthority.TAmodule;

public class Checkresponse {
	
	DB_TA db = new DB_TA();	
	TAmodule ta = new TAmodule();
	
	public void check(final int sid, final int cid, final String response, final long rt,final int up, final int err,final int excp, final String binding) {
	    new Thread(new Runnable() {
	        public void run(){
	            record(sid,cid,response,rt,up,err,excp,binding);
	        }
	    }).start();
	}

	protected void record(int sid, int cid, String response, long rt,int up, int err,int excp, String binding) {


		if(binding=="soap")
		{
			System.out.println("Analyse soap response");
			try {
				if(up>0)
				{
				MessageFactory mf = MessageFactory.newInstance();
				SOAPMessage message = mf.createMessage();
			    SOAPPart soapPart = message.getSOAPPart();
			    SOAPEnvelope envelope = soapPart.getEnvelope();
			    SOAPBody body = envelope.getBody();
			    
			    //Populate the Message.  In here, I populate the message from a response string
			    InputStream is1 = new ByteArrayInputStream(response.getBytes());
			    StreamSource preppedMsgSrc = new StreamSource(is1);
			    soapPart.setContent(preppedMsgSrc);
			    message.saveChanges();
				
			    if (message.getSOAPBody().hasFault()) {
			         System.out.println(message.getSOAPBody().getFault());
			         excp++;
			     } else {
			         System.out.println("ok");
			     }
			}
			} catch (SOAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		recordqos(new Date(), sid, up, err, excp, rt, cid);
		System.out.println("woke up");
		
	}

	public String getendpoint(int sid) throws Exception
	{
		String endpoint=null;
		endpoint = db.getWservDetails(sid).getEndpoint();
		return endpoint;
		
	}
	
	public void recordqos(Date ts, int sid,int up, int err, int excp, double rt,int cid)
	{
		
		try {
			
			db.recordlog(ts, sid, up, err, excp, rt, cid);
			Performance pf =  db.preparedigest(ts,sid,cid);
			if(pf!=null)
			{
				db.recorddigest(ts, sid, cid, pf);		
				ta.provideRating(cid, sid, pf.converttomonq());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		
	}
	
	
}
