package com.teskalabs.bsmtt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import com.teskalabs.bsmtt.cell.CellData;

import java.util.List;

/**
 * A class that holds all helper methods to get information about the phone and process them.
 * @author Stepan Hruska, Premysl Cerny
 */
public class BSMTTelemetryHelper {

	// Getting information from variables ----------------------------------------------------------

	/**
	 * Returns information about RNCid.
	 * @param cid int
	 * @return int
	 */
	public static int getRNCid(int cid){
		return cid >> 16;
	}

	/**
	 * Returns information about UMTScid.
	 * @param cid int
	 * @return int
	 */
	public static int getUMTScid(int cid){
		return cid & 0xffff;
	}

	/**
	 * Returns information about LteCid.
	 * @param eci int
	 * @return int
	 */
	public static int getLteCid(int eci){
		return eci & 0xff;
	}

	/**
	 * Returns information about ENodeB.
	 * @param eci int
	 * @return int
	 */
	public static int getLteENodeB(int eci){
		return eci >> 8;
	}

	/**
	 * Transforms string rem to pct.
	 * @param inStr String
	 * @return String
	 */
	public static String str_rem_pct(String inStr){
		String res = inStr;
		if (inStr.indexOf("%") > 0){
			res = inStr.substring(0, inStr.indexOf("%"));
		}
		return res;
	}

	/**
	 * Returns the call state as String.
	 * @param callstate int
	 * @return String
	 */
	public static String getCallState(int callstate){
		String CallState;
		switch (callstate) {
			case TelephonyManager.CALL_STATE_IDLE: CallState="IDLE"; break;
			case TelephonyManager.CALL_STATE_OFFHOOK: CallState="OFFHOOK"; break;
			case TelephonyManager.CALL_STATE_RINGING: CallState="RINGING"; break;
			default: CallState = "UNK"+ Integer.toString(callstate); break;
		}
		return CallState;
	}

	/**
	 * Returns the data state as String.
	 * @param tm TelephonyManager
	 * @return String
	 */
	public static String getDataState(TelephonyManager tm){
		String dconn = "Unknown";
		switch (tm.getDataState()) {
			case TelephonyManager.DATA_CONNECTED:
				dconn="Connected";
				break;
			case TelephonyManager.DATA_CONNECTING:
				dconn="Connecting";
				break;
			case TelephonyManager.DATA_DISCONNECTED:
				dconn="Disconnected";
				break;
			case TelephonyManager.DATA_SUSPENDED:
				dconn="Suspended";
				break;
			default:
				break;
		}
		return dconn;
	}

	/**
	 * Returns the network type as String.
	 * @param dataNetworkType int
	 * @return String
	 */
	public static String getNetworkType(int dataNetworkType){
		String dataNetStr;
		switch(dataNetworkType){
			case TelephonyManager.NETWORK_TYPE_GPRS: dataNetStr="GPRS"; break;
			case TelephonyManager.NETWORK_TYPE_EDGE: dataNetStr="EDGE"; break;
			case TelephonyManager.NETWORK_TYPE_UMTS: dataNetStr="UMTS"; break;
			case TelephonyManager.NETWORK_TYPE_UNKNOWN: dataNetStr="UNK"; break;
			case TelephonyManager.NETWORK_TYPE_CDMA: dataNetStr="CDMA"; break;
			case TelephonyManager.NETWORK_TYPE_EVDO_0: dataNetStr="EVDO0"; break;
			case TelephonyManager.NETWORK_TYPE_EVDO_A: dataNetStr="EVDOA"; break;
			case TelephonyManager.NETWORK_TYPE_1xRTT: dataNetStr="1xRTT"; break;
			case TelephonyManager.NETWORK_TYPE_HSDPA: dataNetStr="HSDPA"; break;
			case TelephonyManager.NETWORK_TYPE_HSUPA: dataNetStr="HUSPA"; break;
			case TelephonyManager.NETWORK_TYPE_HSPA: dataNetStr="HSPA"; break;
			case TelephonyManager.NETWORK_TYPE_IDEN: dataNetStr="IDEN"; break;
			case TelephonyManager.NETWORK_TYPE_EVDO_B: dataNetStr="EVDOB"; break;
			case TelephonyManager.NETWORK_TYPE_EHRPD: dataNetStr="EHRPD"; break;
			case TelephonyManager.NETWORK_TYPE_LTE: dataNetStr="LTE"; break;
			case TelephonyManager.NETWORK_TYPE_HSPAP: dataNetStr="HSPAP"; break;
			default: dataNetStr = "Unk" + Integer.toString(dataNetworkType);
		}
		return dataNetStr;
	}

	/**
	 * Returns the pre-formatted phone type as String.
	 * @param telephonyManager TelephonyManager
	 * @return String
	 */
	public static String getPhoneTypeStr(TelephonyManager telephonyManager) {
		int phone_type = telephonyManager.getPhoneType();
		String PhoneTypeStr;

		switch(phone_type) {
			case TelephonyManager.PHONE_TYPE_GSM:
				PhoneTypeStr = "GSM";
				break;
			case TelephonyManager.PHONE_TYPE_CDMA:
				PhoneTypeStr = "CDMA";
				break;
			case TelephonyManager.PHONE_TYPE_NONE:
				PhoneTypeStr = "NONE";
				break;
			case TelephonyManager.PHONE_TYPE_SIP:
				PhoneTypeStr = "SIP";
				break;
			default:
				PhoneTypeStr = "UNK " + Integer.toString(phone_type);
		}

		return PhoneTypeStr;
	}

	/**
	 * Checks if there is a mobile (data) connection.
	 * @param context Context
	 * @return boolean
	 */
	public static boolean haveMobileConnection(Context context){
		boolean haveMobile = false;
		try {
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();
			for ( NetworkInfo ni : netInfo) {
				if (ni.getTypeName().equalsIgnoreCase("mobile"))
					if (ni.isConnected())
						haveMobile = true;
			}
			return haveMobile;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks if the network connection is available.
	 * @param context Context
	 * @return boolean
	 */
	public static boolean isNetworkAvailable(Context context) {
		try {
			ConnectivityManager connectivityManager
					= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		} catch (NullPointerException e) {
			return false;
		}
	}

	// Loading information -------------------------------------------------------------------------

	/**
	 * Returns the phone's vendor model.
	 * @return String
	 */
	public static String getPhoneVendorModel(){
		String mMoldel = Build.MODEL;
		String mManufacturer = Build.MANUFACTURER;

		if (!mMoldel.startsWith(mManufacturer)){
			mMoldel = mManufacturer + " " + mMoldel;
		}

		return mMoldel;
	}

	// Extracting information ----------------------------------------------------------------------

	/**
	 * Gets the cell location along with other information.
	 * @param telephonyManager TelephonyManager
	 * @return CellData
	 * @author Premysl Cerny
	 */
	public static CellData getCellLocation(CellData currentData, TelephonyManager telephonyManager, String type) {
		try {
			CellLocation location = telephonyManager.getCellLocation();
			if (type.equalsIgnoreCase("GSM")) {
				GsmCellLocation GSMLocation = (GsmCellLocation)location;
				currentData.setCID(GSMLocation.getCid());
				currentData.setLAC(GSMLocation.getLac());
				currentData.setPSC(GSMLocation.getPsc());
			} else if (type.equalsIgnoreCase("CDMA")) {
				CdmaCellLocation CDMALocation = (CdmaCellLocation)location;
				currentData.setBSID(CDMALocation.getBaseStationId());
				currentData.setNetID(CDMALocation.getNetworkId());
				currentData.setSysID(CDMALocation.getSystemId());
				currentData.setBSILat(CDMALocation.getBaseStationLatitude());
				currentData.setBSILon(CDMALocation.getBaseStationLongitude());
			}
			return currentData;
		} catch (SecurityException e) {
			e.printStackTrace();
			return currentData;
		}
	}

	/**
	 * Gets the cell identity data along with some information.
	 * @param currentData CellData
	 * @param telephonyManager TelephonyManager
	 * @return CellData
	 */
	public static CellData getCellSignal(CellData currentData, TelephonyManager telephonyManager) {
		try {
			List<CellInfo> ACInfo = telephonyManager.getAllCellInfo();
			if ((ACInfo != null) && (ACInfo.size() > 0)) {
				CellInfo ci = ACInfo.get(0);
				if (ci instanceof CellInfoLte) {
					CellIdentityLte cid = ((CellInfoLte) ci).getCellIdentity();
					CellSignalStrengthLte cs = ((CellInfoLte) ci).getCellSignalStrength();
					// cell
					currentData.setTac(cid.getTac());
					currentData.setPci(cid.getPci());
					currentData.seteNodeB(BSMTTelemetryHelper.getLteENodeB(cid.getCi()));
					currentData.setCi(getLteCid(cid.getCi()));
					// signal
					currentData.setASU(cs.getAsuLevel());
					currentData.setDbm(cs.getDbm());
					currentData.setTimAdv(cs.getTimingAdvance());
				} else if (ci instanceof CellInfoGsm) {
					CellIdentityGsm cid = ((CellInfoGsm) ci).getCellIdentity();
					CellSignalStrengthGsm cs = ((CellInfoGsm) ci).getCellSignalStrength();
					// cell
					currentData.setLAC(cid.getLac());
					currentData.setCID(cid.getCid());
					// signal
					currentData.setASU(cs.getAsuLevel());
					currentData.setDbm(cs.getDbm());
				} else if (ci instanceof CellInfoCdma) {
					CellIdentityCdma cid = ((CellInfoCdma) ci).getCellIdentity();
					CellSignalStrengthCdma cs = ((CellInfoCdma) ci).getCellSignalStrength();
					// cell
					currentData.setNetID(cid.getNetworkId());
					currentData.setSysID(cid.getSystemId());
					currentData.setBSID(cid.getBasestationId());
					// signal
					currentData.setASU(cs.getAsuLevel());
					currentData.setDbm(cs.getDbm());
				} else if (ci instanceof CellInfoWcdma) {
					CellIdentityWcdma cid = ((CellInfoWcdma) ci).getCellIdentity();
					CellSignalStrengthWcdma cs = ((CellInfoWcdma) ci).getCellSignalStrength();
					// cell
					currentData.setLAC(cid.getLac());
					currentData.setRNC(BSMTTelemetryHelper.getRNCid(cid.getCid()));
					currentData.setCID(BSMTTelemetryHelper.getUMTScid(cid.getCid()));
					currentData.setPSC(cid.getPsc());
					// signal
					currentData.setASU(cs.getAsuLevel());
					currentData.setDbm(cs.getDbm());
				} // else unknown cell
			}
			// return
			return currentData;
		} catch (SecurityException e) {
			e.printStackTrace();
			return currentData;
		}
	}

	// Permissions ---------------------------------------------------------------------------------

	/**
	 * Checks if it is allowed to use the access location.
	 * @return boolean
	 */
	public static boolean isCoarseLocationPermissionGranted(Context c) {
		if (Build.VERSION.SDK_INT >= 23) {
			if (c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Checks if it is allowed to access the phone state.
	 * @return boolean
	 */
	public static  boolean isPhoneStatePermissionGranted(Context c) {
		if (Build.VERSION.SDK_INT >= 23) {
			if (c.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
}
