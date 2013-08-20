package com.javath.stock.settrade;

public enum Streaming4 {
	S4MarketSummary,
	S4InstrumentTicker,
	S4InstrumentInfo,
	S4MarketTicker;
	
	public static Streaming4 getService(String service) {
		if (service.equals("S4MarketSummary"))
			return S4MarketSummary;
		else if (service.equals("S4InstrumentTicker"))
			return S4InstrumentTicker;
		else if (service.equals("S4InstrumentInfo"))
			return S4InstrumentInfo;
		else if (service.equals("S4MarketTicker"))
			return S4MarketTicker;
		else
			return null;
	}
}
