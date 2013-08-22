package com.javath.stock.settrade;

public enum Streaming4 {
	MarketSummary {
		@Override
        public String toString() {
            return "S4MarketSummary";
        }
	},
	InstrumentTicker{
		@Override
        public String toString() {
            return "S4InstrumentTicker";
        }
	},
	InstrumentInfo
	{
		@Override
        public String toString() {
            return "S4InstrumentInfo";
        }
	},
	MarketTicker
	{
		@Override
        public String toString() {
            return "S4MarketTicker";
        }
	};
	
	public static Streaming4 getService(String service) {
		if (service.equals("S4MarketSummary"))
			return MarketSummary;
		else if (service.equals("S4InstrumentTicker"))
			return InstrumentTicker;
		else if (service.equals("S4InstrumentInfo"))
			return InstrumentInfo;
		else if (service.equals("S4MarketTicker"))
			return MarketTicker;
		else
			return null;
	}
}
