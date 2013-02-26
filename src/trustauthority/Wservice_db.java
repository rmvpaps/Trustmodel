package trustauthority;

public class Wservice_db {

	int servid;
	String orgname;
	String wsdlurl;
	String qosurl;
	String category;
	String endpoint;
	
	
	
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getServid() {
		return servid;
	}
	public void setServid(int servid) {
		this.servid = servid;
	}
	public String getOrgname() {
		return orgname;
	}
	public void setOrgname(String orgname) {
		this.orgname = orgname;
	}
	public String getWsdlurl() {
		return wsdlurl;
	}
	public void setWsdlurl(String wsdlurl) {
		this.wsdlurl = wsdlurl;
	}
	public String getQosurl() {
		return qosurl;
	}
	public void setQosurl(String qosurl) {
		this.qosurl = qosurl;
	}
	
	
	
}
