package trustauthority;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Performance {
	
	Date ts;
	int transct;
	double Availability;
	double minRT;
	double maxRT;
	double avgRT;
	double errrate;
	double excprate;
	public Date getTs() {
		return ts;
	}
	public void setTs(Date ts) {
		this.ts = ts;
	}
	public int getTransct() {
		return transct;
	}
	public void setTransct(int transct) {
		this.transct = transct;
	}
	public double getAvailability() {
		return Availability;
	}
	public void setAvailability(double availability) {
		Availability = availability;
	}
	public double getMinRT() {
		return minRT;
	}
	public void setMinRT(double minRT) {
		this.minRT = minRT;
	}
	public double getMaxRT() {
		return maxRT;
	}
	public void setMaxRT(double maxRT) {
		this.maxRT = maxRT;
	}
	public double getAvgRT() {
		return avgRT;
	}
	public void setAvgRT(double avgRT) {
		this.avgRT = avgRT;
	}
	public double getErrrate() {
		return errrate;
	}
	public void setErrrate(double errrate) {
		this.errrate = errrate;
	}
	public double getExcprate() {
		return excprate;
	}
	public void setExcprate(double excprate) {
		this.excprate = excprate;
	}
	
	public Map<String,Double> converttomonq()
	{
		Map<String, Double> monq = new HashMap<String, Double>();
		monq.put("Availability", getAvailability());
		monq.put("Errorrate",getErrrate());
		monq.put("Exceptionrate", getExcprate());
		monq.put("MinResponsetime", getMinRT());
		monq.put("MaxResponsetime", getMaxRT());
		monq.put("AvgResponsetime", getAvgRT());
		
		return monq;
	}
	
	

}
