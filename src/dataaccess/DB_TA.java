package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import trustauthority.DT;
import trustauthority.Performance;
import trustauthority.Qoswt;
import trustauthority.Transfertrust;
import trustauthority.WSClient_db;
import trustauthority.Wservice_db;


/*
 * Deals with all database access code of trust management module
 * 
 * */
public class DB_TA {
	
	/*
	 * Obtain database connection
	 */
	private static Connection getDBConnection() throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver");
		Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/TAdata?"
		             + "user=myuser&password=sqlroot");
		return connect;
		
	}
	
	/*
	 * Insert a new service provider or client into valid users table 
	 */
	public boolean insertnewuser(String name, String authkey, String role) throws Exception
	{
		System.out.println("In register user");
		Connection connect = null;
		try {
			connect = getDBConnection();
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					   "insert into TAdata.ta_auth(name,authkey,role) values(?,?,?)");
		   preparedStatement1.setString(1, name);
		   preparedStatement1.setString(2, authkey);
		   preparedStatement1.setString(3, role);
		   preparedStatement1.executeUpdate();
		
		   return true;
			
		 }catch (Exception e) {
		    	throw e;
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }

	}
	
	
	/*
	 * Check if a given user and key matches that of a valid user
	 */
	public boolean checkvaliduser(String name, String authkey, String role)
	{
		System.out.println("In check user");
		Connection connect = null;
	    boolean valid = false;
		try {
			connect = getDBConnection();
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					   "select * from  TAdata.ta_auth where name =? and role = ?");
		   preparedStatement1.setString(1, name);
		   preparedStatement1.setString(2, role);
		   ResultSet rs = preparedStatement1.executeQuery();

		   while(rs.next())
		   {
			   if(rs.getString("authkey").equals(authkey))
				   valid =  true;
			  
		   }
		
			
			if(connect!=null)
	    		connect.close();
		 } catch (Exception e) {

		 } 
			
		return valid;
	}
	
	/*
	 * Store weights of each qos of a client-service interaction
	 * Update if it exists
	 */
	public void storeQweights(int cid, int sid, Qoswt wt[]) throws Exception
	{
		System.out.println("In register weights");
		Connection connect = null;
		try {
			connect = getDBConnection();

			//start transaction
			connect.setAutoCommit(false); 
			//delete if it exists
			PreparedStatement preparedStatement2 = connect.prepareStatement(
					"delete from TAdata.clientwt where cid=? and sid=?");
			preparedStatement2.setInt(1, cid);
			preparedStatement2.setInt(2, sid);
			preparedStatement2.executeUpdate();

			
			//store weights in database
			for(int i=0;i<wt.length;i++)
			{
				if(wt[i]==null)
					continue;
				PreparedStatement preparedStatement= connect.prepareStatement(
						"insert into TAdata.clientwt(cid,sid,qos,wt) values (?,?,?,?)");
				preparedStatement.setInt(1, cid);
				preparedStatement.setInt(2, sid);
				preparedStatement.setString(3, wt[i].getQos());
				preparedStatement.setInt(4, wt[i].getVal());
				preparedStatement.executeUpdate();
			}

			//end transaction
			connect.commit();
		      
		    } catch (Exception e) {
		    	connect.rollback();
		    	throw e;
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }

	}
	
	/*
	 * Get the weights of each qos in a client-service interaction 
	 */
	public Map<String, Integer> getQweights(int cid, int sid) throws Exception
	{
		System.out.println("In get weights");
		Connection connect = null;
		try {
			connect = getDBConnection();
			Map<String , Integer> wtmap= new HashMap<String, Integer>();
		   PreparedStatement preparedStatement1 = connect.prepareStatement(
				   "select * from  TAdata.clientwt where cid =? and sid = ?");
		   preparedStatement1.setInt(1, cid);
		   preparedStatement1.setInt(2, sid);
		   ResultSet rs = preparedStatement1.executeQuery();
			    
		    while(rs.next())
		    {
		    	wtmap.put(rs.getString("qos"),rs.getInt("wt") );

		    }
			return wtmap;			    
			
			
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }

	}
	
	/*
	 * Store the advertised Qos of a particular service 
	 * Exception if already exists
	 */
	
	public void storeQadv(int sid,Map<String,Double> advq) throws Exception
	{
		Connection connect = null;
		try {
				connect = getDBConnection();
			   
			   for(String key:advq.keySet())
			   {
				    PreparedStatement preparedStatement = connect.prepareStatement(
				    		"insert into  TAdata.advqos(sid,qos,value) values (?,?,?)");
				    preparedStatement.setInt(1, sid);
				    preparedStatement.setString(2, key);
				    preparedStatement.setDouble(3, advq.get(key));
				    System.out.println("saving " + key + advq.get(key));
				    try{
				    	 preparedStatement.executeUpdate(); 	
				    }
			       catch(SQLException es)
			       {
			    	   if(es.getMessage().contains("Duplicate entry"))
			    		   modifyQadv(sid, key, advq.get(key));
			    	   else
			    		   throw es;
			    		   
			       }

			   }
				System.out.println("In register advertised qos");

 
		      
		    } catch (Exception e) {
		    	throw e;
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }

	}
	/*
	 * Modify advertised Qos of a service if it exists
	 */

	public void modifyQadv(int sid,String qos,double val) throws Exception
	{
		Connection connect = null;
		try {
				connect = getDBConnection();
			   
		        PreparedStatement preparedStatement = connect.prepareStatement(
			    		"update  TAdata.advqos set value=? where sid=? and qos=?");
			    preparedStatement.setInt(2, sid);
			    preparedStatement.setString(3, qos);
			    preparedStatement.setDouble(1,val);
		        preparedStatement.executeUpdate();

		        System.out.println("In modify advertised qos");

 
		      
		    } catch (Exception e) {
		    	throw e;
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }

	}
		
	/*
	 * Returns advertised Qos of a service if it exists
	 * Otherwise return empty map
	 */
	public Map<String,Double> getQadv(int sid) throws Exception
	{
		Connection connect = null;
		try {
				connect = getDBConnection();
			   
				Map<String , Double> advqmap= new HashMap<String, Double>();
				PreparedStatement preparedStatement2 = connect.prepareStatement(
						"select * from  TAdata.advqos where sid =?");
				preparedStatement2.setInt(1, sid);
				ResultSet rs2 = preparedStatement2.executeQuery();
				    
			    while(rs2.next())
			    {
			    	advqmap.put(rs2.getString("qos"),rs2.getDouble("value") );

			    }

			    System.out.println("In get advertised qos");
			    return advqmap;
 
		      
		    } catch (Exception e) {
		    	throw e;
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }
		
	}
		
	/*
	 * check if service data exists and return service id otherwise create new
	 */
	public int storeWservice(String orgname, String wsdlurl, String qosurl,String category, String endpoint) throws Exception
	{
		Connection connect = null;
		boolean exist = false;
		int sid=0;
		try {
			
			connect = getDBConnection();
			   
		   	PreparedStatement preparedStatement1 = connect.prepareStatement(
		   			"select * from  TAdata.wservice where wsdl = ?");
		    preparedStatement1.setString(1,wsdlurl);
		    ResultSet rs = preparedStatement1.executeQuery();
		    
		    if(rs.next())
		    {
		    	exist = true;
		    	sid = rs.getInt("sid");
		    	System.out.println("Existing service");
		    }
		    
		    if(!exist)
		    {
		    	PreparedStatement preparedStatement = connect.prepareStatement(
		    			"insert into  TAdata.wservice (orgname, wsdl,qosurl,category,endpoint) values (?,?,?,?,?)",
		    			Statement.RETURN_GENERATED_KEYS);
		    	preparedStatement.setString(1, orgname);
			    preparedStatement.setString(2, wsdlurl);
			    preparedStatement.setString(3, qosurl);
			    preparedStatement.setString(4, category);
			    preparedStatement.setString(5, endpoint);
		        preparedStatement.executeUpdate();

		        ResultSet keyrs = preparedStatement.getGeneratedKeys();
		        if(keyrs.next())
		        	sid = keyrs.getInt(1);
		        
		    }
		    System.out.println("In store wservice");
			return sid;
			
	    } catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
	}
	
	
	/*
	 * return service details of a given sid if it exists
	 * Otherwise return null
	 */
	public Wservice_db getWservDetails(int sid) throws Exception
	{
		Connection connect = null;
		
		try {
			Wservice_db ws = null;
			connect = getDBConnection();
			   
		   	PreparedStatement preparedStatement1 = connect.prepareStatement(
		   			"select * from  TAdata.wservice where sid = ?");
		    preparedStatement1.setInt(1,sid);
		    ResultSet rs = preparedStatement1.executeQuery();
		    
		    if(rs.next())
		    {
		    	if(ws == null)
		    		ws = new Wservice_db();
		    	ws.setServid(sid);
		    	ws.setOrgname(rs.getString("orgname"));
		    	ws.setWsdlurl(rs.getString("wsdl"));
		    	ws.setQosurl(rs.getString("qosurl"));
		    	ws.setEndpoint(rs.getString("endpoint"));
		    	
		    }
		    else
		    {
		    	System.out.println("Invalid service id");
		    }
		    
		   
		    System.out.println("In get wservice");
			return ws;
			
	    } catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
	}
	
	
	
	
	/*
	 * store client details and return client id
	 * Store in valid users table
	 * If exists return client id
	 */

	public int storeWSclient(String name, String email) throws Exception
	{
		Connection connect = null;
		boolean exist = false;
		int cid=0;
		try {
			
			connect = getDBConnection();
			   
		   	PreparedStatement preparedStatement1 = connect.prepareStatement(
		   			"select * from  TAdata.wclient where email = ?");
		    preparedStatement1.setString(1,email);
		    ResultSet rs = preparedStatement1.executeQuery();
		    
		    if(rs.next())
		    {
		    	exist = true;
		    	cid = rs.getInt("cid");
		    	System.out.println("Existing client");
		    }
		    
		    if(!exist)
		    {
		    	PreparedStatement preparedStatement = connect.prepareStatement(
		    			"insert into  TAdata.wclient (cname, email) values (?,?)",
		    			Statement.RETURN_GENERATED_KEYS);
		    	preparedStatement.setString(1, name);
			    preparedStatement.setString(2, email);
			    preparedStatement.executeUpdate();

		        ResultSet keyrs = preparedStatement.getGeneratedKeys();
		        if(keyrs.next())
		        	cid = keyrs.getInt(1);
		        
		    }
		    System.out.println("In store wsclient");
			return cid;
			
	    } catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
	}
	
	
	/*
	 * return ws client details of a given cid
	 * If it does not exist return null
	 */
	public WSClient_db getWSclientDetails(int cid) throws Exception
	{
		Connection connect = null;
		
		try {
			WSClient_db wc = null;
			connect = getDBConnection();
			   
		   	PreparedStatement preparedStatement1 = connect.prepareStatement(
		   			"select * from  TAdata.wclient where cid = ?");
		    preparedStatement1.setInt(1,cid);
		    ResultSet rs = preparedStatement1.executeQuery();
		    
		    if(rs.next())
		    {
		    	if(wc==null)
		    		wc = new WSClient_db();
		    	wc.setCid(cid);
		    	wc.setEmail(rs.getString("email"));
		    	wc.setName(rs.getString("cname"));
		    	
		    }
		    else
		    {
		    	System.out.println("Invalid client id");
		    }
		    
		   
		    System.out.println("In get ws client");
			return wc;
			
	    } catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
	}
	
		
	
	/*
	 * initialize direct trust between a given client and service
	 * Do nothing if already exists
	 */
	public void initializeDT(int cid, int sid) throws Exception
	{
		Connection connect = null;
		try {
			connect = getDBConnection();

			//check if it exist
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select sid from  TAdata.dirtrust where sid =? and cid = ?");
		    preparedStatement1.setInt(1, sid);
		    preparedStatement1.setInt(2, cid);
		    ResultSet rs = preparedStatement1.executeQuery();
	
		    //create a new entry if not exist
			if(!rs.next())
			{
				PreparedStatement preparedStatement = connect.prepareStatement(
				"insert into  TAdata.dirtrust(cid,sid,value,freq)"+
				" values (?,?,?,?)");
			    preparedStatement.setInt(1, cid);
			    preparedStatement.setInt(2, sid);
			    preparedStatement.setDouble(3, 0.5);
			    preparedStatement.setInt(4, 0);
		        preparedStatement.executeUpdate();
			}

			rs.close();
			System.out.println("In initialize dt");
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
		
	}
	
	/*
	 * update direct trust given multiplication factor
	 */
	public void updateDT(int cid, int sid, double factor) throws Exception
	{
		Connection connect = null;
		try {
			//if it does not exist create new
			initializeDT(cid, sid);
			
			connect = getDBConnection();

			//start transaction
			connect.setAutoCommit(false); 

			PreparedStatement preparedStatement2 = connect.prepareStatement(
					"update  TAdata.dirtrust set value = value * ?, ts = ?, freq = freq + 1 "
			+"where sid=? and cid=? and value * ? <= 1");
			preparedStatement2.setDouble(1, factor);
			preparedStatement2.setTimestamp(2, new Timestamp(new Date().getTime()));
			preparedStatement2.setInt(3,sid);
			preparedStatement2.setInt(4,cid);
			preparedStatement2.setDouble(5, factor);	
			preparedStatement2.executeUpdate();
	        PreparedStatement preparedStatement3 = connect.prepareStatement(
					"update  TAdata.dirtrust set value = 1, ts = ?, freq = freq + 1 "
			+"where sid=? and cid=? and value * ? > 1");
			preparedStatement3.setTimestamp(1, new Timestamp(new Date().getTime()));
			preparedStatement3.setInt(2,sid);
			preparedStatement3.setInt(3,cid);
			preparedStatement3.setDouble(4, factor);
			preparedStatement3.executeUpdate();
	        connect.commit();
	        System.out.println("In update dt");
	                	
			
			
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
	}
		
	/*
	 * Return direct trust of a client in a service if it exists
	 * Otherwise return null
	 */
	public DT getDirtrust(int sid, int cid) throws Exception
	{
		
		Connection connect = null;
		DT present = null;
		try {
			connect = getDBConnection();
			
			
			PreparedStatement preparedStatement1 = connect.prepareStatement("select * from  TAdata.dirtrust where sid =? and cid = ?");
		    preparedStatement1.setInt(1, sid);
		    preparedStatement1.setInt(2, cid);
		    ResultSet rs = preparedStatement1.executeQuery();
		    
		    while(rs.next())
		    {
		    	present = new DT();
		    	present.setSid( rs.getInt("sid"));
		    	present.setCid(rs.getInt("cid"));
		    	present.setValue(rs.getDouble("value"));
		    	present.setFreq(rs.getInt("freq"));
		    	present.setTs(rs.getTimestamp("ts"));
		    	
		    }
		    System.out.println("In get dt");
		    if(present == null)
		    {
		    	initializeDT(cid, sid);
		    	return getDirtrust(sid, cid);
		    }
		    
		    return present;
	
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
		
	}
		
	/*
	 * initialize referral trust of one client in another
	 * Do nothing if exists
	 */
	public void initializeRT(int cid1, int cid2) throws Exception
	{
		Connection connect = null;
		try {
			connect = getDBConnection();

			//check if it exist
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select cid1 from  TAdata.reftrust where cid1 =? and cid2 = ?");
		    preparedStatement1.setInt(1, cid1);
		    preparedStatement1.setInt(2, cid2);
		    ResultSet rs = preparedStatement1.executeQuery();
	
		    //create a new entry if not exist
			if(!rs.next())
			{
				PreparedStatement preparedStatement = connect.prepareStatement(
				"insert into  TAdata.reftrust(cid1,cid2,value)"+
				" values (?,?,?)");
			    preparedStatement.setInt(1, cid1);
			    preparedStatement.setInt(2, cid2);
			    preparedStatement.setDouble(3, 0.5);
			    preparedStatement.executeUpdate();
			}

			rs.close();
			
			
	        System.out.println("In initialize rt");
			
			
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
	}		
		
		
	/*
	 * update referral trust given multiplication factor
	 * if does not exist create new
	 */
	public void updateRT(int cid1, int cid2, double factor) throws Exception
	{
		Connection connect = null;
		try {
			connect = getDBConnection();

			//check if it exist
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select cid1 from  TAdata.reftrust where cid1 =? and cid2 = ?");
		    preparedStatement1.setInt(1, cid1);
		    preparedStatement1.setInt(2, cid2);
		    ResultSet rs = preparedStatement1.executeQuery();
	
		    //create a new entry if not exist
			if(!rs.next())
			{
				PreparedStatement preparedStatement = connect.prepareStatement(
				"insert into  TAdata.reftrust(cid1,cid2,value)"+
				" values (?,?,?)");
			    preparedStatement.setInt(1, cid1);
			    preparedStatement.setInt(2, cid2);
			    preparedStatement.setDouble(3, 0.5);
			    preparedStatement.executeUpdate();
			}

			rs.close();
			
			//start transaction
			connect.setAutoCommit(false); 

			PreparedStatement preparedStatement2 = connect.prepareStatement(
					"update  TAdata.reftrust set value = value * ? "
			+"where cid1=? and cid2=? and value * ? <= 1");
			preparedStatement2.setDouble(1, factor);
			preparedStatement2.setInt(2,cid1);
			preparedStatement2.setInt(3,cid2);
			preparedStatement2.setDouble(4, factor);	
	        preparedStatement2.executeUpdate();	

	        PreparedStatement preparedStatement3 = connect.prepareStatement(
					"update  TAdata.reftrust set value = 1 "
			+"where cid1=? and cid2=? and value * ? > 1");
			preparedStatement3.setInt(1,cid1);
			preparedStatement3.setInt(2,cid2);
			preparedStatement3.setDouble(3, factor);	
	        preparedStatement3.executeUpdate();	

	        connect.commit();
	        System.out.println("In update rt");
			
			
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
	}		
		
		
		
	/*
	 * Get ref trust of client in another client if it exists
	 * create a new one it it does not exist
	 */
	public double getRt(int cid1, int cid2) throws Exception
	{
		Connection connect = null;
		double rt = 0.5;
		try {
			connect = getDBConnection();

			//check if it exist
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select value from  TAdata.reftrust where cid1 =? and cid2 = ?");
		    preparedStatement1.setInt(1, cid1);
		    preparedStatement1.setInt(2, cid2);
		    ResultSet rs = preparedStatement1.executeQuery();
	
		   if(rs.next())
			{
				rt = rs.getDouble("value");
			}
		   else
		   {
			   //create if it does not
			   initializeRT(cid1, cid2);
		   }

			rs.close();
			System.out.println("In get rt");
			return rt;
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
			
		
	}
		
	
	/*
	 * Get client id of clients who interacted with a particular service
	 * Return null if no one interacted
	 */
	public List<Integer> getClientwhointeracted(int sid) throws Exception
	{
		Connection connect = null;
		try {
			connect = getDBConnection();
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select cid from  TAdata.dirtrust where sid =?");
		    preparedStatement1.setInt(1, sid);
		    ResultSet rs = preparedStatement1.executeQuery();
			
		    List<Integer> lt = null;
		    
		    while(rs.next())
		    {
		    	if(lt==null)
		    		lt = new ArrayList<Integer>();
		    	lt.add(rs.getInt("cid"));
		    }
		    
			return lt;
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }
	}
	
	


	
	
	
	
	
	/*********************************************************************************/
	
	//return list of services in a category
		public Wservice_db[] findServices(String category) throws Exception
		{
			Connection connect = null;
			List<Wservice_db> wsl = new ArrayList<Wservice_db>();
			
			try {
				
				connect = getDBConnection();
				   
			   	PreparedStatement preparedStatement1 = connect.prepareStatement(
			   			"select * from  TAdata.wservice where category like ?");
			   	if(category==null)
			   		category = new String("");
		   		preparedStatement1.setString(1,"%"+category+"%");
			    ResultSet rs = preparedStatement1.executeQuery();
			    boolean exist=false;

			    
			    while(rs.next())
			    {
			    	exist = true;
			    	Wservice_db ws = new Wservice_db();
			    	ws.setCategory(rs.getString("category"));
			    	ws.setOrgname(rs.getString("orgname"));
			    	ws.setQosurl(rs.getString("qosurl"));
			    	ws.setWsdlurl(rs.getString("wsdl"));
			    	ws.setServid(rs.getInt("sid"));
			    	wsl.add(ws);
				    System.out.println("found");			    	
			    	
			    }
			    if(!exist)
			    {
			    	throw new Exception("No services found in that category");
			    }
				return (wsl.toArray(new Wservice_db[wsl.size()]));
				
		    } catch (Exception e) {
		    	throw e;
		    } finally {
			    System.out.println("In find wservice");	
		    	if(connect!=null)
		    		connect.close();
		    }	
		
		}
	
	
	public List<Transfertrust> gettruststat(int cid, int sid) throws Exception
	{
		Connection connect = null;
		initilize2RT(cid);
		List<Transfertrust> lt = new ArrayList<Transfertrust>();
		Transfertrust tref;
		try {
			connect = getDBConnection();
		   	PreparedStatement preparedStatement1 = connect.prepareStatement(
		   			"select reftrust.value, reftrust.cid2, dirtrust.value, dirtrust.ts from  TAdata.dirtrust, TAdata.reftrust "
		   			+ "where reftrust.cid2 = dirtrust.cid and reftrust.cid1 = ? and dirtrust.sid= ? and reftrust.cid1 != reftrust.cid2");
		    preparedStatement1.setInt(1, cid);
		    preparedStatement1.setInt(2, sid);
		    ResultSet rs = preparedStatement1.executeQuery();
		    
		    while(rs.next())
		    {
		    	tref = new Transfertrust();
		    	tref.setCid1(cid);
		    	tref.setSid(sid);
		    	
		    	tref.setCid2(rs.getInt(2));
		    	tref.setReftrust(rs.getDouble(1));
		    	tref.setDirtrust(rs.getDouble(3));
		    	tref.setTs(rs.getTimestamp(4));
		    	
		    	lt.add(tref);
		    	
		    }
		    System.out.println("in return stat");
		    return lt;
			
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	
			
	}
	/*
	 * find all clients in system and initilaize reftrust to 0.5 for all of them
	 */
	public void initilize2RT(int cid) throws Exception
	{
		Connection connect = null;
		try {
			connect = getDBConnection();
		   	Statement stmt = connect.createStatement();
		   	PreparedStatement prepstmt = connect.prepareStatement("insert into TAdata.reftrust values (?,?,?),(?,?,?)");
		    ResultSet rs = stmt.executeQuery("select * from  TAdata.wclient where cid !=" + cid);
		    while(rs.next())
		    {
		    	prepstmt.setInt(1, cid);
		    	prepstmt.setInt(2, rs.getInt("cid"));
		    	prepstmt.setDouble(3, 0.5);
		    	prepstmt.setInt(4, rs.getInt("cid"));
		    	prepstmt.setInt(5, cid);
		    	prepstmt.setDouble(6, 0.5);
				    	
		    	try {
					prepstmt.executeUpdate();
				} catch (SQLException se) {
					se.printStackTrace();
					if(se.getMessage().contains("Duplicate entry"))
			    		   ;
			    	   else
			    		   throw se;
					
				}
		    }
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }	 	
		  
	    System.out.println("in initialize rt");
		    
		
		
	}
	
	/*
	 * 
	 */
	public void recordlog(Date dt, int sid,int up, int err, int excp, double rt,int cid ) throws SQLException
	{
		Connection connect = null;
		try {
				connect = getDBConnection();
			   
			   
			    PreparedStatement preparedStatement = 
			    	connect.prepareStatement("insert into  TAdata.invokelog" +
			    			"(ts,sid,opname,up,error,excep,responsetime,cid) values (?,?,?,?,?,?,?,?)");
			    preparedStatement.setTimestamp(1, new Timestamp(dt.getTime()));
			    preparedStatement.setInt(2, sid);
			    preparedStatement.setString(3, "op");
		    	preparedStatement.setInt(4, up);
		    	preparedStatement.setInt(5, err);
		    	preparedStatement.setDouble(6, excp);
		    	preparedStatement.setDouble(7, rt);
		    	preparedStatement.setInt(8, cid);
		        preparedStatement.executeUpdate();
			  

 
		      
		    } catch (Exception e) {
		    	e.printStackTrace();
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }
		System.out.println("In record details");
	}

	public int findsid(String endpoint) throws Exception {
		Connection connect = null;
		int sid=-1;
		try {
			connect = getDBConnection();
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select sid from  TAdata.wservice where endpoint like ?");
		    preparedStatement1.setString(1, endpoint);
		    ResultSet rs = preparedStatement1.executeQuery();
				    
		    while(rs.next())
		    {
		    	sid = rs.getInt("sid");
		    }
		    
			return sid;
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }
	}

	public Performance preparedigest(Date ts, int sid, int cid) throws Exception {
		// create a digest from the last 10 interactions
		Connection connect = null;
		int ct=0,upct=0,err=0,excp=0;
		double avgrt=0, maxrt=0, minrt=0;
		try {
			
			connect = getDBConnection();
			//get count of interactions
		
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select count(*),min(ts) from  (select * from TAdata.invokelog where sid=? and cid=? and ts<=? limit 10) as T");
			preparedStatement1.setInt(1,sid);
			preparedStatement1.setInt(2, cid);
			preparedStatement1.setTimestamp(3, new Timestamp(ts.getTime()));
			Timestamp t =new Timestamp(ts.getTime());
			ResultSet rs = preparedStatement1.executeQuery();
			if(rs.next())
			{
				ct = rs.getInt(1);
				t=rs.getTimestamp(2);
			}
			
			if(ct==0)
				return null;
			//get availability
			PreparedStatement preparedStatement2 = connect.prepareStatement(
					"select count(*) from  TAdata.invokelog where sid=? and cid=? and ts<=? and ts>=? and up=1");
			preparedStatement2.setInt(1,sid);
			preparedStatement2.setInt(2, cid);
			preparedStatement2.setTimestamp(3, new Timestamp(ts.getTime()));
			preparedStatement2.setTimestamp(4, t);
			ResultSet rs2 = preparedStatement2.executeQuery();
			if(rs2.next())
			{
				upct = rs2.getInt(1);
			}
			
			//get error percentage
			PreparedStatement preparedStatement3 = connect.prepareStatement(
					"select count(*) from  TAdata.invokelog where sid=? and cid=? and ts<=? and ts>=? and err=1");
			preparedStatement3.setInt(1,sid);
			preparedStatement3.setInt(2, cid);
			preparedStatement3.setTimestamp(3, new Timestamp(ts.getTime()));
			preparedStatement3.setTimestamp(4, t);
			ResultSet rs3 = preparedStatement2.executeQuery();
			if(rs3.next())
			{
				err = rs3.getInt(1);
			}
			//get exception percentage

			PreparedStatement preparedStatement4 = connect.prepareStatement(
					"select count(*) from  TAdata.invokelog where sid=? and cid=? and ts<=? and ts>=? and excep=1");
			preparedStatement4.setInt(1,sid);
			preparedStatement4.setInt(2, cid);
			preparedStatement4.setTimestamp(3, new Timestamp(ts.getTime()));
			preparedStatement4.setTimestamp(4, t);
			ResultSet rs4 = preparedStatement2.executeQuery();
			if(rs4.next())
			{
				excp = rs4.getInt(1);
			}
			//get max, min and avg of response time
			
			PreparedStatement preparedStatement5 = connect.prepareStatement(
					"select AVG(responsetime),MAX(responsetime), MIN(responsetime) from  TAdata.invokelog where sid=? and cid=? and ts<=? and ts>=? and up=1");
			preparedStatement5.setInt(1,sid);
			preparedStatement5.setInt(2, cid);
			preparedStatement5.setTimestamp(3, new Timestamp(ts.getTime()));
			preparedStatement5.setTimestamp(4, t);
			ResultSet rs5 = preparedStatement5.executeQuery();
			if(rs5.next())
			{
				avgrt = rs5.getDouble(1);
				maxrt = rs5.getDouble(2);
				minrt = rs5.getDouble(3);
			}
			
			//Create a performance object with required values
			Performance pf = new Performance();
			pf.setAvailability((double)upct/ct);
			pf.setTransct(ct);
			pf.setAvgRT(avgrt);
			pf.setMaxRT(maxrt);
			pf.setMinRT(minrt);
			pf.setErrrate((double)err/ct);
			pf.setExcprate((double)excp/ct);
			
			return pf;
			
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }
	}

	public void recorddigest(Date dt, int sid,int cid, Performance pf ) throws SQLException
	{
		Connection connect = null;
		try {
				connect = getDBConnection();
			   
			   
			    PreparedStatement preparedStatement = 
			    	connect.prepareStatement("insert into  TAdata.performdigest" +
			    			"(ts,sid,cid,avail,errrate,excprate,avgrt,minrt,maxrt) values (?,?,?,?,?,?,?,?,?)");
			    preparedStatement.setTimestamp(1, new Timestamp(dt.getTime()));
			    preparedStatement.setInt(2, sid);
			    preparedStatement.setInt(3, cid);
		    	preparedStatement.setDouble(4, pf.getAvailability());
		    	preparedStatement.setDouble(5, pf.getErrrate());
		    	preparedStatement.setDouble(6, pf.getExcprate());
		    	preparedStatement.setDouble(7, pf.getAvgRT());
		    	preparedStatement.setDouble(8, pf.getMinRT());
		    	preparedStatement.setDouble(9, pf.getMaxRT());
		        preparedStatement.executeUpdate();
			  

 
		      
		    } catch (Exception e) {
		    	e.printStackTrace();
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }
		System.out.println("In record digest");
	}

	public Performance getPerformance(Date ts, int sid) throws Exception {
		// create a digest from the last 20 interactions
		Connection connect = null;
		int ct=0,upct=0,err=0,excp=0;
		double avgrt=0, maxrt=0, minrt=0;
		try {
			
			connect = getDBConnection();
			//get count of interactions
		
			PreparedStatement preparedStatement1 = connect.prepareStatement(
					"select count(*),min(ts) from  (select * from TAdata.invokelog where sid=? and ts<=? limit 20) as T");
			preparedStatement1.setInt(1,sid);
			preparedStatement1.setTimestamp(2, new Timestamp(ts.getTime()));
			Timestamp t =new Timestamp(ts.getTime());
			ResultSet rs = preparedStatement1.executeQuery();
			if(rs.next())
			{
				ct = rs.getInt(1);
				t=rs.getTimestamp(2);
			}
			
			if(ct==0)
				return null;
			//get availability
			PreparedStatement preparedStatement2 = connect.prepareStatement(
					"select count(*) from  TAdata.invokelog where sid=? and ts<=? and ts>=? and up=1");
			preparedStatement2.setInt(1,sid);
			preparedStatement2.setTimestamp(2, new Timestamp(ts.getTime()));
			preparedStatement2.setTimestamp(3, t);
			ResultSet rs2 = preparedStatement2.executeQuery();
			if(rs2.next())
			{
				upct = rs2.getInt(1);
			}
			
			//get error percentage
			PreparedStatement preparedStatement3 = connect.prepareStatement(
					"select count(*) from  TAdata.invokelog where sid=? and ts<=? and ts>=? and error=1");
			preparedStatement3.setInt(1,sid);
			preparedStatement3.setTimestamp(2, new Timestamp(ts.getTime()));
			preparedStatement3.setTimestamp(3, t);
			ResultSet rs3 = preparedStatement3.executeQuery();
			if(rs3.next())
			{
				err = rs3.getInt(1);
			}
			//get exception percentage

			PreparedStatement preparedStatement4 = connect.prepareStatement(
					"select count(*) from  TAdata.invokelog where sid=? and ts<=? and ts>=? and excep=1");
			preparedStatement4.setInt(1,sid);
			preparedStatement4.setTimestamp(2, new Timestamp(ts.getTime()));
			preparedStatement4.setTimestamp(3, t);
			ResultSet rs4 = preparedStatement4.executeQuery();
			if(rs4.next())
			{
				excp = rs4.getInt(1);
			}
			//get max, min and avg of response time
			
			PreparedStatement preparedStatement5 = connect.prepareStatement(
					"select AVG(responsetime),MAX(responsetime), MIN(responsetime) from  TAdata.invokelog where sid=? and ts<=? and ts>=? and up=1");
			preparedStatement5.setInt(1,sid);
			preparedStatement5.setTimestamp(2, new Timestamp(ts.getTime()));
			preparedStatement5.setTimestamp(3, t);
			ResultSet rs5 = preparedStatement5.executeQuery();
			if(rs5.next())
			{
				avgrt = rs5.getDouble(1);
				maxrt = rs5.getDouble(2);
				minrt = rs5.getDouble(3);
			}
			System.out.println("err" + err + "excp" + excp);
			//Create a performance object with required values
			Performance pf = new Performance();
			pf.setAvailability((double)upct/ct);
			pf.setTransct(ct);
			pf.setAvgRT(avgrt);
			pf.setMaxRT(maxrt);
			pf.setMinRT(minrt);
			pf.setErrrate((double)err/ct);
			pf.setExcprate((double)excp/ct);
			
			return pf;
			
		} catch (Exception e) {
	    	throw e;
	    } finally {
	    	if(connect!=null)
	    		connect.close();
	    }
	}

	public void dtrecord(int cid, int sid, double dt) throws Exception
	{
		Connection connect = null;
		try {
				connect = getDBConnection();
			   
			   
			    PreparedStatement preparedStatement = connect.prepareStatement("insert into  TAdata.dtrecord(cid, sid, dt) values (?,?,?)");
			    preparedStatement.setInt(1, cid);
			    preparedStatement.setInt(2, sid);
			    preparedStatement.setDouble(3, dt);
		        preparedStatement.executeUpdate();
			  

 
		      
		    } catch (Exception e) {
		    	throw e;
		    } finally {
		    	if(connect!=null)
		    		connect.close();
		    }
		System.out.println("In record details");
	}
	
	
}
