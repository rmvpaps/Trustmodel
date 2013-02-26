package trustauthority;

public class Qoswt_sp {

	int availability;
	int minRT;
	int maxRT;
	int avgRT;
	int errrate;
	int excprate;
	public int getAvailability() {
		return availability;
	}
	public void setAvailability(int availability) {
		this.availability = availability;
	}
	public int getMinRT() {
		return minRT;
	}
	public void setMinRT(int minRT) {
		this.minRT = minRT;
	}
	public int getMaxRT() {
		return maxRT;
	}
	public void setMaxRT(int maxRT) {
		this.maxRT = maxRT;
	}
	public int getAvgRT() {
		return avgRT;
	}
	public void setAvgRT(int avgRT) {
		this.avgRT = avgRT;
	}
	public int getErrrate() {
		return errrate;
	}
	public void setErrrate(int errrate) {
		this.errrate = errrate;
	}
	public int getExcprate() {
		return excprate;
	}
	public void setExcprate(int excprate) {
		this.excprate = excprate;
	}
	
	Qoswt[] convert()
	{
		Qoswt[] wt = new Qoswt[6];
		
		wt[0] = new Qoswt("Availability", this.getAvailability());
		wt[1] = new Qoswt("Errorrate",this.getErrrate());
		wt[2] = new Qoswt("Exceptionrate",this.getExcprate());
		wt[3] = new Qoswt("MinResponsetime", this.getMinRT());
		wt[4] = new Qoswt("MaxResponsetime", this.getMaxRT());
		wt[5] = new Qoswt("AvgResponsetime", this.getAvgRT());
		
		
		return wt;
	}
}
