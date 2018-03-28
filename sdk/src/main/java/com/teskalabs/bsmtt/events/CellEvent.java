package com.teskalabs.bsmtt.events;

import com.teskalabs.bsmtt.cell.CellData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An event that reacts to phone cell changes.
 * @author Premysl Cerny
 */
public class CellEvent extends JsonEvent {
	// Information related to the cell
	private CellData m_cellData; // added by Premysl

	/**
	 * A basic constructor.
	 */
	public CellEvent() {
		super(BSMTTEvents.CELL_EVENT);
		m_cellData = new CellData();
	}

	/**
	 * Returns current cell data.
	 * @return CellData
	 */
	public CellData getCellData() {
		return m_cellData;
	}

	/**
	 * Reacts to changes of the phone cell of the event.
	 * @param cellData CellData
	 */
	public void changeCell(CellData cellData) {
		// Check
		if (m_cellData.getEnb() == cellData.getEnb() &&
				m_cellData.getPsc() == cellData.getPsc() &&
				m_cellData.getRnc() == cellData.getRnc() &&
				m_cellData.getASU() == cellData.getASU() &&
				m_cellData.getBSID() == cellData.getBSID() &&
				m_cellData.getBSILat() == cellData.getBSILat() &&
				m_cellData.getBSILon() == cellData.getBSILon() &&
				m_cellData.getCi() == cellData.getCi() &&
				m_cellData.getCid() == cellData.getCid() &&
				m_cellData.getDbm() == cellData.getDbm() &&
				m_cellData.getLac() == cellData.getLac() &&
				m_cellData.getNetID() == cellData.getNetID() &&
				m_cellData.getPci() == cellData.getPci() &&
				m_cellData.getSysID() == cellData.getSysID() &&
				m_cellData.getTac() == cellData.getTac() &&
				m_cellData.getTimAdv() == cellData.getTimAdv())
			return;
		// Save
		try {
			m_cellData = cellData.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// Saving to the JSON
		JSONObject data = getEventData();
		try {
			// From the phone response
			if (m_cellData != null) {
				if (m_cellData.getASU() != Integer.MIN_VALUE)
					data.put("ASU", m_cellData.getASU());
				if (m_cellData.getBSID() != Integer.MIN_VALUE)
					data.put("BSID", m_cellData.getBSID());
				if (m_cellData.getBSILat() != Integer.MIN_VALUE)
					data.put("BSILat", m_cellData.getBSILat());
				if (m_cellData.getBSILon() != Integer.MIN_VALUE)
					data.put("BSILon", m_cellData.getBSILon());
				if (m_cellData.getCi() != Integer.MIN_VALUE)
					data.put("ci", m_cellData.getCi());
				if (m_cellData.getCid() != Integer.MIN_VALUE)
					data.put("cid", m_cellData.getCid());
				if (m_cellData.getDbm() != Integer.MIN_VALUE)
					data.put("dbm", m_cellData.getDbm());
				if (m_cellData.getEnb() != Integer.MIN_VALUE)
					data.put("enb", m_cellData.getEnb());
				if (m_cellData.getLac() != Integer.MIN_VALUE)
					data.put("lac", m_cellData.getLac());
				if (m_cellData.getNetID() != Integer.MIN_VALUE)
					data.put("NetID", m_cellData.getNetID());
				if (m_cellData.getPci() != Integer.MIN_VALUE)
					data.put("pci", m_cellData.getPci());
				if (m_cellData.getPsc() != Integer.MIN_VALUE)
					data.put("psc", m_cellData.getPsc());
				if (m_cellData.getRnc() != Integer.MIN_VALUE)
					data.put("rnc", m_cellData.getRnc());
				if (m_cellData.getSysID() != Integer.MIN_VALUE)
					data.put("SysID", m_cellData.getSysID());
				if (m_cellData.getTac() != Integer.MIN_VALUE)
					data.put("tac", m_cellData.getTac());
				if (m_cellData.getTimAdv() != Integer.MIN_VALUE)
					data.put("TimAdv", m_cellData.getTimAdv());
				saveEventData(data); // save
				dataReceived(); // notify
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
