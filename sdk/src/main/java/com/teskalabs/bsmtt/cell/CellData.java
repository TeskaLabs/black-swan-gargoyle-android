package com.teskalabs.bsmtt.cell;

/**
 * Holds necessary information about the phone cell.
 * @author Premysl Cerny
 */
public class CellData implements Cloneable {
	private int CID;
	private int LAC;
	private int PSC;
	private int BSID;
	private int BSILat;
	private int BSILon;
	private int SysID;
	private int NetID;
	private int tac;
	private int pci;
	private int ci;
	private int ASU;
	private int Dbm;
	private int RNC;
	private int eNodeB;
	private int TimAdv;

	/**
	 * Clones the object so two references may work with separate data.
	 * @return CellData
	 * @throws CloneNotSupportedException If not supported.
	 */
	@Override
	public CellData clone() throws CloneNotSupportedException {
		return (CellData)super.clone();
	}

	/**
	 * Initial data.
	 */
	public CellData() {
		CID = Integer.MIN_VALUE;
		LAC = Integer.MIN_VALUE;
		PSC = Integer.MIN_VALUE;
		BSID = Integer.MIN_VALUE;
		BSILat = Integer.MIN_VALUE;
		BSILon = Integer.MIN_VALUE;
		SysID = Integer.MIN_VALUE;
		NetID = Integer.MIN_VALUE;
		tac = Integer.MIN_VALUE;
		pci = Integer.MIN_VALUE;
		ci = Integer.MIN_VALUE;
		ASU = Integer.MIN_VALUE;
		Dbm = Integer.MIN_VALUE;
		RNC = Integer.MIN_VALUE;
		eNodeB = Integer.MIN_VALUE;
		TimAdv = Integer.MIN_VALUE;
	}

	public int getCID() {
		return CID;
	}

	public void setCID(int CID) {
		this.CID = CID;
	}

	public int getLAC() {
		return LAC;
	}

	public void setLAC(int LAC) {
		this.LAC = LAC;
	}

	public int getPSC() {
		return PSC;
	}

	public void setPSC(int PSC) {
		this.PSC = PSC;
	}

	public int getBSID() {
		return BSID;
	}

	public void setBSID(int BSID) {
		this.BSID = BSID;
	}

	public int getBSILat() {
		return BSILat;
	}

	public void setBSILat(int BSILat) {
		this.BSILat = BSILat;
	}

	public int getBSILon() {
		return BSILon;
	}

	public void setBSILon(int BSILon) {
		this.BSILon = BSILon;
	}

	public int getSysID() {
		return SysID;
	}

	public void setSysID(int sysID) {
		SysID = sysID;
	}

	public int getNetID() {
		return NetID;
	}

	public void setNetID(int netID) {
		NetID = netID;
	}

	public int getTac() {
		return tac;
	}

	public void setTac(int tac) {
		this.tac = tac;
	}

	public int getPci() {
		return pci;
	}

	public void setPci(int pci) {
		this.pci = pci;
	}

	public int getCi() {
		return ci;
	}

	public void setCi(int ci) {
		this.ci = ci;
	}

	public int getASU() {
		return ASU;
	}

	public void setASU(int ASU) {
		this.ASU = ASU;
	}

	public int getDbm() {
		return Dbm;
	}

	public void setDbm(int dbm) {
		Dbm = dbm;
	}

	public int getRNC() {
		return RNC;
	}

	public void setRNC(int RNC) {
		this.RNC = RNC;
	}

	public int geteNodeB() {
		return eNodeB;
	}

	public void seteNodeB(int eNodeB) {
		this.eNodeB = eNodeB;
	}

	public int getTimAdv() {
		return TimAdv;
	}

	public void setTimAdv(int timAdv) {
		TimAdv = timAdv;
	}
}
