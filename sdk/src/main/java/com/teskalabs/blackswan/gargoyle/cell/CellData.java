package com.teskalabs.blackswan.gargoyle.cell;

/**
 * Holds necessary information about the phone cell.
 * @author Premysl Cerny
 */
public class CellData implements Cloneable {
	private int cid;
	private int lac;
	private int psc;
	private int BSID;
	private int BSILat;
	private int BSILon;
	private int SysID;
	private int NetID;
	private int tac;
	private int pci;
	private int ci;
	private int ASU;
	private int dbm;
	private int rnc;
	private int enb;
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
		cid = Integer.MIN_VALUE;
		lac = Integer.MIN_VALUE;
		psc = Integer.MIN_VALUE;
		BSID = Integer.MIN_VALUE;
		BSILat = Integer.MIN_VALUE;
		BSILon = Integer.MIN_VALUE;
		SysID = Integer.MIN_VALUE;
		NetID = Integer.MIN_VALUE;
		tac = Integer.MIN_VALUE;
		pci = Integer.MIN_VALUE;
		ci = Integer.MIN_VALUE;
		ASU = Integer.MIN_VALUE;
		dbm = Integer.MIN_VALUE;
		rnc = Integer.MIN_VALUE;
		enb = Integer.MIN_VALUE;
		TimAdv = Integer.MIN_VALUE;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getLac() {
		return lac;
	}

	public void setLac(int lac) {
		this.lac = lac;
	}

	public int getPsc() {
		return psc;
	}

	public void setPsc(int psc) {
		this.psc = psc;
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
		return dbm;
	}

	public void setDbm(int dbm) {
		this.dbm = dbm;
	}

	public int getRnc() {
		return rnc;
	}

	public void setRnc(int rnc) {
		this.rnc = rnc;
	}

	public int getEnb() {
		return enb;
	}

	public void setEnb(int enb) {
		this.enb = enb;
	}

	public int getTimAdv() {
		return TimAdv;
	}

	public void setTimAdv(int timAdv) {
		TimAdv = timAdv;
	}
}
