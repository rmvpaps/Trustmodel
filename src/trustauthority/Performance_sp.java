package trustauthority;


import java.util.HashMap;
import java.util.Map;

public class Performance_sp {
	

	double Availability;
	double minRT;
	double maxRT;
	double avgRT;
	double errrate;
	double excprate;
	
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
	Map<String, Double> convert()
	{
		Map<String,Double> advq = new HashMap<String, Double>();
		advq.put("Availability", this.getAvailability());
		advq.put("Errorrate",this.getErrrate());
		advq.put("Exceptionrate", this.getExcprate());
		advq.put("MinResponsetime",this.getMinRT());
		advq.put("MaxResponsetime", this.getMaxRT());
		advq.put("AvgResponsetime", this.getAvgRT());
		
		return advq;
	}
	
	

}
