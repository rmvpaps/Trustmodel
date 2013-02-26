package trustauthority;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import monitor.WSLAParser;

import dataaccess.DB_TA;


public class TAmodule{
	
	
	DB_TA db = new DB_TA();
	static Date startup = new java.util.Date(0);
	
	

	/*
	 * To be called to register a valid service provider
	 */
	public boolean registerServiceprovider(String orgname, String authkey) throws Myexception
	{
		String errormsg = "";
		if(orgname == null || authkey == null)
		{
			errormsg = "All fields are required";
		}
		else
		{
			try
			{
				db.insertnewuser(orgname, authkey, "SP");
				return true;
			}
			catch (Exception e) {
				errormsg = e.getMessage();
			}
		}
		
		throw new Myexception(errormsg);				
	}

	/*
	 * To be called to register a valid service client entity
	 */
	public int registerServiceClient(String name, String email, String authkey) throws Myexception
	{
		
		String errormsg = "";
		
		if(email==null || name == null || authkey == null)
		{
			errormsg = "All fields are required";
		}
		else
		{
			int cid = -111;
			try
			{
				cid = db.storeWSclient(name, email);
				db.insertnewuser(String.valueOf(cid), authkey, "SC");
				return cid;
			}
			catch(Exception e)
			{
				errormsg = e.getMessage();
			}
			
		}
		throw new Myexception(errormsg);
		
	}
	
	
	/*
	 * To be called through web service to register a particular service with TA. Store advertised QoS also 
	 * and return new WSDL link to be used
	 */
	public Wservice_db registerWebService(String orgname, String authkey, String wsdl, String qosurl, String category, String endpoint) throws Myexception
	{
		int sid = -1111;
		String errormsg = "";
		
		if(db.checkvaliduser(orgname,authkey,"SP"))
		{
			if(orgname!=null && endpoint != null && qosurl!=null)
			{
				try
				{
					Map<String,Double> advq = parseQos(qosurl);
					sid = db.storeWservice(orgname, wsdl, qosurl, category,endpoint);
					db.storeQadv(sid, advq);
					
					/* Create a web service proxy at ~/services/sid and return the new WSDL
					 */
					String newurl = "http://localhost:8080/TrustModel/services/1/"+sid;
					
					Wservice_db nws = new Wservice_db();
					nws.setCategory(category);
					nws.setOrgname(orgname);
					nws.setQosurl(qosurl);
					nws.setServid(sid);
					nws.setWsdlurl(newurl);
					return nws;
				}
				catch(Exception e)
				{
					errormsg = e.getMessage();
				}
			
			}
			else
			{
				errormsg = "All fields are required";
			}
		}
		else
		{
			errormsg = "Authentication failed";
		}
		throw new Myexception(errormsg);		
	}
	
	/*
	 * To be called through web service to register a particular service with TA. Store advertised QoS also 
	 * and return new WSDL link to be used
	 */
	public Wservice_db registerWebService2(String orgname, String authkey, String wsdl, String category, Performance_sp pf,String endpoint) throws Myexception
	{
		int sid = -1111;
		String errormsg = "";
		
		if(db.checkvaliduser(orgname,authkey,"SP"))
		{
			if(orgname!=null && endpoint != null && pf!=null)
			{
				try
				{
					Map<String,Double> advq = pf.convert();
					sid = db.storeWservice(orgname, wsdl, "not specified", category, endpoint);
					db.storeQadv(sid, advq);
					
					/* Create a web service proxy at ~/services/sid and return the new WSDL
					 */
					String newurl = "http://localhost:8080/TrustModel/services/1/"+sid;
					
					Wservice_db nws = new Wservice_db();
					nws.setCategory(category);
					nws.setOrgname(orgname);
					nws.setQosurl("not specified");
					nws.setServid(sid);
					nws.setWsdlurl(wsdl);
					nws.setEndpoint(newurl);
					return nws;
				}
				catch(Exception e)
				{
					errormsg = e.getMessage();
					e.printStackTrace();
				}
			
			}
			else
			{
				errormsg = "All fields are required";
			}
		}
		else
		{
			errormsg = "Authentication failed";
		}
		throw new Myexception(errormsg);		
	}
	
	/*
	 * To be called from web service to start monitoring 
	 * interactions between that service and client
	 */
	
	/*public void bindToService(int sid, int cid, String authkey, Qoswt wt[]) throws Myexception
	{	
		String errmsg = "";
		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				db.storeQweights(cid, sid,wt);
				db.initializeDT(cid, sid);
				return;
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
	}*/
	
	public Wservice_db bindToService(int sid, int cid, String authkey, Qoswt wt[]) throws Myexception
	{	
		String errmsg = "";
		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				Wservice_db ws = db.getWservDetails(sid);
				db.storeQweights(cid, sid,wt);
				db.initializeDT(cid, sid);
				ws.setWsdlurl("http://localhost:8080/TrustManagementService/services/"+cid+"/"+sid);
				return ws;
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
	}
	
	
	
	public Wservice_db bindToService_sp(int sid, int cid, String authkey, Qoswt_sp wt) throws Myexception
	{	
		String errmsg = "";
		Qoswt[] wts = wt.convert();
		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				Wservice_db ws = db.getWservDetails(sid);
				db.storeQweights(cid, sid,wts);
				db.initializeDT(cid, sid);
				ws.setEndpoint("http://localhost:8080/TrustModel/services/"+cid+"/"+sid);
				return ws;
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
	}
	
	/*
	 * To be called from web service to get the trust value of a particular service
	 * uses methods less efficient - multiple database calls
	 */
	public TrustResult getTrust_LS(int sid, int cid, String authkey) throws Myexception
	{
		
		String errmsg = "";
		TrustResult tr = new TrustResult();
		

		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				double wtsum=0,dtsum=0,trust=0.5;
				//perform the trust calculation
				
				//get direct trust
				DT dt1 = db.getDirtrust(sid, cid);
				DT_sp dts = new DT_sp(dt1);
				tr.setDt(dts);
				List<Integer> clist = db.getClientwhointeracted(sid);
				for(int i=0;i<clist.size();i++)
				{
					int cid2 = clist.get(i);
					if(cid2==cid)
						continue;
					DT dt = db.getDirtrust(sid, cid2);
					double rt = db.getRt(cid, cid2);
					double ts = timediff(dt.getTs(),new Date());
					System.out.println("cid1" + cid + "cid2" + cid2 + "dt" + dt.getValue() + "ref" + rt);
					dtsum +=  rt * dt.getValue() /ts ;
					wtsum += rt/ts;
				}
				//if someone has interacted before
				if(clist.size()!=0)
				{
					//if direct experience
					if(dt1!= null)
					{
						double exp1 = expe(dt1.freq);
				    	trust = exp1*dt1.getValue() + (1-exp1)*(dtsum/wtsum);
					}//if no direct experience
					else
						trust = dtsum/wtsum;
					tr.setIndirect_trust(dtsum/wtsum);
				}
				tr.setOverall_trust(trust);
				
				return tr;
				
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
	}
	
	
	
	/*
	 * To be called from web service to get the trust value of a particular service
	 * uses method directly from database
	 */
	
	public TrustResult getTrust(int sid, int cid, String authkey) throws Myexception
	{
		
		String errmsg = "";
		TrustResult tr = new TrustResult();
		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				//perform the trust calculation
				
				List<Transfertrust> lt= db.gettruststat(cid, sid);
				DT dt= db.getDirtrust(sid, cid);
				DT_sp dts = new DT_sp(dt);
				tr.setDt(dts);
				Date curr = new Date();
				double dtsum=0,wtsum=0,trust=0.5;
				for(int i=0; i<lt.size(); i++)
				{
					Transfertrust temp = lt.get(i);
					System.out.println("cid1"+ temp.getCid1() +  "cid2"+temp.getCid2() +  "dt" + temp.getDirtrust() + "ref" + temp.getReftrust());
					dtsum += temp.getDirtrust() * temp.getReftrust() /(timediff(temp.getTs(),curr));
					wtsum += temp.getReftrust() /(timediff(temp.getTs(), curr));
				}
				//if someone has interacted before
				if(wtsum!=0)
				{
					System.out.println("Considering recommendations");
					//if direct experience
					if(dt!= null)
					{
						System.out.println("Considering direct trust");
						double exp1 = expe(dt.freq);
				    	trust = exp1*dt.getValue() + (1-exp1)*(dtsum/wtsum);
					}//if no direct experience
					else 
						trust = dtsum/wtsum;
					
					tr.setIndirect_trust(dtsum/wtsum);
				}
				else if(dt!= null)
				{
					trust = dt.getValue();
				}
				tr.setOverall_trust(trust);
				return tr;
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
	}
	
	
	/*
	 * Called from web service to accept user feedback on a service
	 */
	public void givefeedback(double val, int sid, int cid,String authkey) throws Myexception
	{

		String errmsg = "";
		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				//call to record feedback
				DT dt= db.getDirtrust(sid, cid);
				if(dt!=null || dt.getFreq()!=0)
				{
					if(val<0.3)
						updateDT(sid, cid, false);
					else if(val>.7)
						updateDT(sid, cid, true);					
				}
				else
					errmsg = "No interactions recorded. Bind to service first";
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
	
		
	}
	
	/*
	 * To be called from web service to get performance details of a service based on its interaction with us
	 */
	
	public Performance getCurrentPerformance(int cid, String authkey, int sid) throws Myexception
	{
		String errmsg = "";
		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				//call to get performance details
				Performance p = db.preparedigest(new Date(), sid, cid);					
				if(p==null)
					errmsg="No performace recorded";
				else
					return p;
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
		
	}
	
	/*
	 * to be called from web service to get performance details of a service as a whole
	 */
	
	public Performance getServicePerformance(int sid) throws Myexception
	{
		String errmsg = "";
		try
		{
				//call to get performance details
				Performance p = db.getPerformance(new Date(), sid);			
				if(p==null)
					errmsg="No performace recorded";
				else
					return p;
			
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
		
	}
	
	/*
	 * to be called from web service to return all registered services
	 * 
	 */
	public Wservice_db[] getregisteredservices(int cid,String authkey,String category) throws Myexception
	{
		String errmsg = "";
		try
		{
			if(db.checkvaliduser(String.valueOf(cid), authkey, "SC"))
			{
				
				Wservice_db[] wsarray;
				wsarray = db.findServices(category);				
				if(wsarray==null)
					errmsg="No services registered";
				else
					return wsarray;
			}
			else
			{
				errmsg = "Authentication failed";
			}
		}
		catch(Exception e)
		{
			errmsg = e.getMessage();
		}
		
		throw new Myexception(errmsg);
	}
/*************************************************************************************************************/	
	
	/*
	 * function to accept current rating of a service
	 */
	public void provideRating(int cid, int sid, Map<String,Double> monq) throws Exception
	{
		
		Map<String,Double> advq = db.getQadv(sid);
		Map<String,Integer> wtq = db.getQweights(cid, sid);
		if(wtq.size()==0 && cid==1)
			wtq = getdefaultwt();
		if(advq!=null && wtq!= null)
			updateDT(sid, cid, CheckSatisfied(advq,wtq,monq));	
		try
		{
		db.dtrecord(cid, sid, db.getDirtrust(sid, cid).getValue());
		}
		catch(Exception e)
		{
			
		}
		
	}
	
	private Map<String,Integer> getdefaultwt()
	{
		Map<String,Integer> wtq = new HashMap<String, Integer>();
		wtq.put("Availability",1);
		wtq.put("Errorrate",1);
		wtq.put("Exceptionrate",1);
		wtq.put("MinResponsetime",1);
		wtq.put("MaxResponsetime",1);
		wtq.put("AvgResponsetime",1);
		return wtq;
	}
	
	/*
	 * To update direct trust
	 * then reftrusts
	 */
	private void updateDT(int sid, int cid, boolean satisfied) throws Exception
	{
		DT dt = db.getDirtrust(sid, cid);
		double tdiff = timediff(dt.getTs(),new Date());
		System.out.println("old = "+dt.getValue() +"diff" + tdiff);
		if(satisfied)
			db.updateDT(cid, sid, 1 + 0.1*tdiff);
		else
			db.updateDT(cid, sid, 1 - 0.2*tdiff);
		
		//update reftrust of all clients who have interacted before with this service
		dt = db.getDirtrust(sid, cid);
		System.out.println("new = "+dt.getValue());
		List<Integer> lc = db.getClientwhointeracted(sid);
		for(int i=0; i< lc.size();i++)
		{
			int cid2 = lc.get(i);
			if(cid2==cid)
				continue;
			DT dt2 = db.getDirtrust(sid, cid2);
			double diff =  (dt2.getValue() - dt.getValue())/timediff(dt2.getTs(),dt.getTs());
			if(diff < 0.2)
				db.updateRT(cid, cid2, 1.1);
			else if(diff > 0.5)
				db.updateRT(cid, cid2, 0.8);
				
		}
		
	}
	
	/*
	 * check whether an interaction is satisfactory
	 */			
	private boolean CheckSatisfied(Map<String, Double> advqmap,
			Map<String, Integer> wtmap, Map<String, Double> monq) throws Exception {
		System.out.println("Check satisfied");
		
		//calculate deviation
	    double deviation = 0,sum1=0,sum2=0;
	    
	    for( String key:monq.keySet())
	    {

	    	if(wtmap.containsKey(key) && advqmap.containsKey(key))
	    	{
		    	System.out.println(key);	
		    	
	    		//normalized difference
	    		double diff = advqmap.get(key) - monq.get(key);
		    	if((key=="AvgResponsetime")||(key=="MaxResponsetime")||(key=="MinResponsetime"))
		    	{
		    		diff = -diff/advqmap.get(key);	
		    		if(diff<-1)
		    			diff = -1;
		    		else if(diff > 1)
		    			diff = 1;
		    		
		    	}
		    	if((key=="Exceptionrate")||(key=="Errorrate"))
		    	{
		    		diff = (1-monq.get(key)) -(1-advqmap.get(key));
		    		
		    	}
		    	  	
	    		
	    		sum1+= wtmap.get(key) * diff;
				sum2+= wtmap.get(key);
	    	}
	    }
		if(sum2==0)
			throw new Exception("Bad weights");

		deviation = sum1/sum2;
		if(deviation > 0.001)
		{		System.out.println("not satisfied");
			return false;
		}
		else
			return true;

	}

	/*
	 * To parse a Qos spec and return a map
	 */
	private Map<String, Double> parseQos(String qosurl) throws ParserConfigurationException, SAXException, IOException
	{
		Map<String, Double> advq = new HashMap<String, Double>();
		WSLAParser wsl = new WSLAParser(qosurl);
		wsl.parse();
		/*Hard coded - need to parse qos spec*/
		advq.put("availability", .99);
		advq.put("AvgResponsetime", 0.78);
		advq.put("Errorrate", 0.99);
		
		return advq;
		
	}
	
	

	/*
	 * To be called through web service to find the registered services in a category
	 */	
	public Wservice_db[] findWebService(String email, String authkey, String category ) throws Myexception
	{
		String errormsg = "";
		Wservice_db[] wsarr;
		
		if(db.checkvaliduser(email,authkey,"SC"))
		{
			if(category!= null)
			{
				try
				{
					wsarr = db.findServices(category);
					return wsarr;
				}
				catch(Exception e)
				{
					errormsg = e.getMessage();
				}
			}
			else
			{
				errormsg = "All fields are required";
			}
		}
		else
		{
			errormsg = "Authentication failed";
		}
		throw new Myexception(errormsg);		
	}
	
	/*
	 * To get value corresponding to time difference
	 */
	private double timediff(Date prev, Date curr)
	{
		
		return 1 + (curr.getTime() - prev.getTime())/(curr.getTime() - startup.getTime());
	}
	
	/*
	 * To get value corresponding to frequency
	 */
	private double expe(int freq)
	{
		return 1-(Math.exp(-freq));

	}

	
	/*
	 * To normalize performance values
	 */
/*	
	public static void main(String[] args) throws Exception
	{
		TAmodule ta1 = new TAmodule();
		//ta1.registerServiceprovider("RMVServices", "123456");
		//ta1.registerWebService("RMVServices", "123456", 
		//"http://www.ebi.ac.uk/soaplab/typed/services/nucleic_composition.banana?WSDL", 
		//"http://www.ebi.ac.uk/soaplab/typed/services/nucleic_composition.banana?QOS", "genedatabase");
		
		//ta1.registerServiceClient("m110405cs", "rini_mcs11@nitc.ac.in", "121212");
		/*Qoswt[] wt = new Qoswt[2];
		wt[0] = new Qoswt("Availability",5);
		wt[1] = new Qoswt("responsetime",3);
		ta1.bindToService(6,6,"131313",wt);*/
		
		/*Map<String, Double> monq = new HashMap<String, Double>();
		monq.put("Availability", 0.75);
		ta1.provideRating(6, 6, monq);
		System.out.println(ta1.getTrust(6, 8, "121212"));
	}
*/
}
