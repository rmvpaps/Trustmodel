package trustauthority;

import java.sql.Timestamp;

public class DT_sp {
	int cid;
	int sid;
	double value;
	int freq;

	public DT_sp()
	{
		super();
	}
	public DT_sp(DT dt1) {
		super();
		if(dt1!=null)
		{
			this.setCid(dt1.getCid());
			this.setSid(dt1.getSid());
			this.setFreq(dt1.getFreq());
			this.setValue(dt1.getValue());			
		}

	}
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}


}
