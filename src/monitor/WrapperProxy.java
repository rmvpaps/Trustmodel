package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


/**
 * Servlet implementation class WrapperProxy
 * Will intercept all requests to registered services
 */
public class WrapperProxy extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WrapperProxy() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("In get");

		System.out.println("In get" + request.getRequestURL());
		System.out.println(request.getParameter("product_id"));
		System.out.println(request.getParameter("url"));
		
		//PrintWriter out = response.getWriter();
		//out.println("OK");

	
		//get actual host
			Checkresponse chk = new Checkresponse();		
			int up=1,err=0,excp=0;
			int sid = Integer.parseInt(request.getParameter("product_id"));
			String url = request.getParameter("url");
			int cid=Integer.parseInt(request.getParameter("cid"));

			
			//find actual service from service id
			String host=null;
			String scheme = "http";
			try {
				host = chk.getendpoint(sid);
				
				if(host.contains("https"))
				{
					scheme = "https";
					host = host.substring(8);
				}
				else
					host = host.substring(7);
				System.out.println("host="+host);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//connect to actual host
			int port=80;
			HttpClient client = new DefaultHttpClient();
			URIBuilder builder = new URIBuilder();
			builder.setScheme(scheme).setHost(host).setPath("/"+url);

			Enumeration<String> hdr =request.getParameterNames();
			while(hdr.hasMoreElements())
			{
				String hd = hdr.nextElement();
				if(hd.equalsIgnoreCase("url")) continue;
				if(hd.equalsIgnoreCase("product_id")) continue;
				if(hd.equalsIgnoreCase("cid")) continue;
			    builder.setParameter(hd,request.getParameter(hd) );  
				System.out.println(hd + request.getParameter(hd));
			}
			
			URI uri = null;
			try {
				 uri = builder.build();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	 
			
			

			
			System.out.println(uri.toASCIIString());
			HttpGet post = new HttpGet(uri);
			
			//call the actual service and measure
	    	String cbuff = "";
	    	PrintWriter p = response.getWriter();  
			HttpResponse htp = null;
	    	long rt=-1;
	    	try
	    	{
	    	
	    	long start = System.currentTimeMillis();
	    	htp = client.execute(post);
	    	long end = System.currentTimeMillis();
	    	rt = end - start;
	    	if(htp.getStatusLine().getStatusCode()>=500)
	    		excp++;
	    	}    	
	    	catch (ClientProtocolException cpe) {
				err++;
			}
	    	catch(IOException ioe)
	    	{
	    		p.write(ioe.getMessage());
	    		p.close();
	    		up=0;
	    	}
	    	System.out.println(rt);
	 	
	    	if(up>0)
	    	{
	    	
			//collect response
	    	BufferedReader rd =new BufferedReader(new InputStreamReader(htp.getEntity().getContent()));


	    	try{
	    		String line=null;
				while((line = rd.readLine())!=null)
				{
					cbuff = cbuff + line + "\n";
					//System.out.println(new String(line));
				}
				p.write(cbuff);
				p.close();
	    	}
			catch(Exception e) { e.printStackTrace(); }
			//return response
			//process response asynchronously
	    	}
	    	else
	    	{
	    		p.write("down");
	    	}
			
			
			
			//"http://www.deeptraining.com/webservices/weather.asmx?WSDL";//http://wsf.cdyne.com/WeatherWS/Weather.asmx";//http://www.webservicex.net/globalweather.asmx";//http://localhost:9050/RMVServices/services/Sortutility1";
			
	 		
			/*Enumeration<String> hdr = request.getHeaderNames();
			while(hdr.hasMoreElements())
			{
				String hd = hdr.nextElement();
				if(hd.equalsIgnoreCase("Content-length")) continue;
				post.addHeader(hd,request.getHeader(hd));
				System.out.println(hd + request.getHeader(hd));
			}*/
	    	
			chk.check(sid,cid,cbuff,rt,up,err,excp,"http");
			
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("In post" + request.getRequestURL());
		System.out.println(request.getParameter("product_id"));
		System.out.println(request.getParameter("url"));
		
		String binding = "http";
		
		//get actual host
		Checkresponse chk = new Checkresponse();		
		int up=1,err=0,excp=0;
		int sid = Integer.parseInt(request.getParameter("product_id"));
		String url = request.getParameter("url");
		int cid=Integer.parseInt(request.getParameter("cid"));

		
		//find actual service from service id
		String host=null;
		try {
			host = chk.getendpoint(sid);
			if(url!=null)
				host = host + "/" + url;
			System.out.println("host="+host);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//connect to actual host
		int port=80;
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(host);
		
		//check if input message is SOAP
		String soapactn = request.getHeader("soapaction");
		if(soapactn!=null)
		{
			binding = "soap";
			String soapmsg = "";
			try{
				java.io.BufferedReader reader = request.getReader();
				String line=null;
				while((line = reader.readLine()) != null) {soapmsg=soapmsg + line; 		}
				System.out.println(soapmsg);
				reader.close();
				}
				catch(Exception e) { System.out.println(e); }
			
			//prepare request to actual service
			post.setHeader("soapaction", soapactn);
			
			StringEntity str = new StringEntity(soapmsg);
	    	str.setContentType(request.getContentType());
	    	post.setEntity(str);
			
		}
		else
		{
			Enumeration<String> hdr =request.getParameterNames();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			while(hdr.hasMoreElements())
			{
				String hd = hdr.nextElement();
				if(hd.equalsIgnoreCase("url")) continue;
				if(hd.equalsIgnoreCase("product_id")) continue;
				if(hd.equalsIgnoreCase("cid")) continue;
			      
				nameValuePairs.add(new BasicNameValuePair(hd,request.getParameter(hd)));
				System.out.println(hd + request.getParameter(hd));
			}

			 post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		}
		
		//call the actual service and measure
    	String cbuff = "";
    	PrintWriter p = response.getWriter();  
		HttpResponse htp = null;
    	long rt=100000,start = 0,end;
    	try
    	{
    	start = System.currentTimeMillis();
    	htp = client.execute(post);
    	end = System.currentTimeMillis();
    	rt = end - start;
    	if(htp.getStatusLine().getStatusCode()>=500)
    		excp++;
    	}    	
    	catch (ClientProtocolException cpe) {
			end = System.currentTimeMillis();
	    	rt = end - start;
	    	err++;
		}
    	catch(IOException ioe)
    	{
    		p.write(ioe.getMessage());
    		p.close();
    		up=0;
    	}
    	System.out.println(rt);
 	
    	if(up>0)
    	{
    	
		//collect response
    	BufferedReader rd =new BufferedReader(new InputStreamReader(htp.getEntity().getContent()));


    	try{
    		String line=null;
			while((line = rd.readLine())!=null)
			{
				cbuff = cbuff + line;
				System.out.println(new String(line));
			}
			p.write(cbuff);
			p.close();
    	}
		catch(Exception e) { e.printStackTrace(); }
		//return response
		//process response asynchronously
    	}
		
		
		
		
		//"http://www.deeptraining.com/webservices/weather.asmx?WSDL";//http://wsf.cdyne.com/WeatherWS/Weather.asmx";//http://www.webservicex.net/globalweather.asmx";//http://localhost:9050/RMVServices/services/Sortutility1";
		
 		
		/*Enumeration<String> hdr = request.getHeaderNames();
		while(hdr.hasMoreElements())
		{
			String hd = hdr.nextElement();
			if(hd.equalsIgnoreCase("Content-length")) continue;
			post.addHeader(hd,request.getHeader(hd));
			System.out.println(hd + request.getHeader(hd));
		}*/
    	
		chk.check(sid,cid,cbuff,rt,up,err,excp,binding);
		
	}
    	
	
}
