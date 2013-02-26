package trustauthority;

import java.sql.Timestamp;

public class Transfertrust {
	
	int cid1;
	int cid2;
	double reftrust;
	int sid;
	double dirtrust;
	Timestamp ts;
	
	
	public int getCid1() {
		return cid1;
	}
	public void setCid1(int cid1) {
		this.cid1 = cid1;
	}
	public int getCid2() {
		return cid2;
	}
	public void setCid2(int cid2) {
		this.cid2 = cid2;
	}
	public double getReftrust() {
		return reftrust;
	}
	public void setReftrust(double reftrust) {
		this.reftrust = reftrust;
	}
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public double getDirtrust() {
		return dirtrust;
	}
	public void setDirtrust(double dirtrust) {
		this.dirtrust = dirtrust;
	}
	public Timestamp getTs() {
		return ts;
	}
	public void setTs(Timestamp ts) {
		this.ts = ts;
	}
	

}
