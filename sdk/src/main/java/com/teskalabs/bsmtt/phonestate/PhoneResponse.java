package com.teskalabs.bsmtt.phonestate;

import android.telephony.CellInfo;
import android.telephony.CellLocation;
import java.util.List;

/**
 * A data class that holds information about the current phone state.
 * @author Premysl Cerny
 */
public class PhoneResponse implements Cloneable {
	private int data_state;
	private int data_networkType;
	private int sig_ASU;
	private int sig_dbm;
	private int DataActivity_dir;
	private int callState;
	private boolean mFirstDataState;
	private boolean mFirstCallState;
	private long mRX;
	private long mTX;
	private String inNum;

	// just for checking
	private android.telephony.CellLocation cellLocation;
	private List<CellInfo> cellInfo;

	@Override
	public PhoneResponse clone() throws CloneNotSupportedException {
		return (PhoneResponse)super.clone();
	}

	public PhoneResponse() {
		data_state = Integer.MIN_VALUE;
		data_networkType = Integer.MIN_VALUE;
		sig_ASU = Integer.MIN_VALUE;
		sig_dbm = Integer.MIN_VALUE;
		DataActivity_dir = Integer.MIN_VALUE;
		callState = Integer.MIN_VALUE;
		mFirstCallState = true;
		mFirstDataState = true;
		mRX = Long.MIN_VALUE;
		mTX = Long.MIN_VALUE;
		inNum = "";
	}

	public int getData_state() {
		return data_state;
	}

	public void setData_state(int data_state) {
		this.data_state = data_state;
	}

	public int getData_networkType() {
		return data_networkType;
	}

	public void setData_networkType(int data_networkType) {
		this.data_networkType = data_networkType;
	}

	public boolean ismFirstDataState() {
		return mFirstDataState;
	}

	public void setmFirstDataState(boolean mFirstDataState) {
		this.mFirstDataState = mFirstDataState;
	}

	public boolean ismFirstCallState() {
		return mFirstCallState;
	}

	public void setmFirstCallState(boolean mFirstCallState) {
		this.mFirstCallState = mFirstCallState;
	}

	public long getmRX() {
		return mRX;
	}

	public void setmRX(long mRX) {
		this.mRX = mRX;
	}

	public long getmTX() {
		return mTX;
	}

	public void setmTX(long mTX) {
		this.mTX = mTX;
	}

	public int getSig_ASU() {
		return sig_ASU;
	}

	public void setSig_ASU(int sig_ASU) {
		this.sig_ASU = sig_ASU;
	}

	public int getSig_dbm() {
		return sig_dbm;
	}

	public void setSig_dbm(int sig_dbm) {
		this.sig_dbm = sig_dbm;
	}

	public int getDataActivity_dir() {
		return DataActivity_dir;
	}

	public void setDataActivity_dir(int dataActivity_dir) {
		DataActivity_dir = dataActivity_dir;
	}

	public int getCallState() {
		return callState;
	}

	public void setCallState(int callState) {
		this.callState = callState;
	}

	public String getInNum() {
		return inNum;
	}

	public void setInNum(String inNum) {
		this.inNum = inNum;
	}

	public CellLocation getCellLocation() {
		return cellLocation;
	}

	public void setCellLocation(CellLocation cellLocation) {
		this.cellLocation = cellLocation;
	}

	public List<CellInfo> getCellInfo() {
		return cellInfo;
	}

	public void setCellInfo(List<CellInfo> cellInfo) {
		this.cellInfo = cellInfo;
	}
}
