package com.teskalabs.bsmtt.events;

/**
 * An event that reacts to certain dimension changes.
 * @author Premysl Cerny
 */
public class BasicEvent extends JsonEvent {
	/**
	 * A basic constructor.
	 */
	public BasicEvent() {
		super(BSMTTEvents.BASIC_EVENT);
	}

	/**
	 * Readiness depends on changed dimensions.
	 * @return boolean
	 */
	@Override
	public boolean isReady() {
		if (ismDimensionsChanged()) {
			setmDimensionsChanged(false);
			forceReady();
		}
		return super.isReady();
	}
}
