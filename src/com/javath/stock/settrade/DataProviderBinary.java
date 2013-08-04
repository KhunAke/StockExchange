package com.javath.stock.settrade;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

import com.javath.Object;
import com.javath.ObjectException;

public class DataProviderBinary extends Object {
	
	private boolean updateSequenceId = false;
	private int sequenceId = -1;
	private int optionSequenceId = -1;
	
	// Result from method Read(InputStream)
	private String[] marketTicker;
	private String[] bid_offer;
	// map symbol, date
	private Map<String,String> symbol_date = new HashMap<String,String>();
	//private Set<String> symbols = new HashSet<String>();
	
	private void clearDataResult() {
		marketTicker = null;
		bid_offer = null;
		symbol_date = new HashMap<String,String>();
	}
	
	public int getSequenceId() {
		return sequenceId;
	}
	
	public int getOptionSequenceId() {
		return optionSequenceId;
	}
	
	public String[] getMarketTicker() {
		return marketTicker;
	}
	
	public String[] getBidOffer() {
		return bid_offer;
	}
	
	public String getSymbol() {
		String symbol = "";
		Iterator<String> symbols = this.symbol_date.keySet().iterator();
		while (symbols.hasNext()) {
			symbol += (String) symbols.next() + ",";
		}
		try {
			return symbol.substring(0, symbol.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	public Map<String,String> getSymbolDate() {
		return symbol_date;
	}
	
	public static void main(String[] args) {
		try {
			Properties system = System.getProperties();
			system.setProperty("java.util.logging.config.file", "etc/logging.properties");
			new DataProviderBinary().read(
					new FileInputStream("var\\Streaming4DataProviderBinary.2013-07-11.187d6a6.jsp"));
			//		new FileInputStream("var\\Streaming4DataProviderBinary.jsp"));
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		}
	}
	
	
	public DataProviderBinary read(InputStream input) {
		try {
			// Clear Result
			clearDataResult();
			// Process
			if (input.available() == 0)
				return this;
			int begin = readInteger(input, 2);
			if (begin == 0x81FF) { // 33279 {81FF}
				int size = readInteger(input, 2);
				byte[] buffer = new byte[size];
				if (read(input, buffer) == size) {
					int end = readInteger(input, 2);
					if (end == 0x82FE) { // 33534 {82FE}
						block(buffer);
					} else
						throw new ObjectException("Format is not valid.");
				} else {
					throw new ObjectException("Blocksize mismatch.");
				}
			} else
				throw new ObjectException("Format is not valid.");
		} catch (IOException e) {
			ObjectException ex = new ObjectException("Cannot intitiate a class object.");
			//ex.addSuppressed(e);
			throw ex;
		}
		return this;
	}
	
	private int read(InputStream input, byte[] data) throws IOException {
		return input.read(data);
	}
	
	private int intDecode(byte[] data) {
		boolean sign = false;
		if ((data.length == 4) && ((data[0] & 0x80) != 0x00)) {
			data[0] &= 0x7F;
			sign = true;
		}
		char[] hex = Hex.encodeHex(data);
		int integer = Integer.decode("#" + String.valueOf(hex));
		if (sign) {
			integer |= 0x80000000;
			data[0] |= 0x80;
		}
		return integer; 
	}
	
	private int readInteger(InputStream input, int number) throws IOException {
		byte[] buffer = new byte[number];
		if (read(input, buffer) != number)
			throw new ObjectException("Blocksize mismatch.");
		//int result = intDecode(buffer);
		//String format = "Integer = %1$d {%1$0" + (number * 2) + "X}";
		//logger.finest(message(format, result));
		//return result;
		return intDecode(buffer);
	}
	
	private double doubleDecode(byte[] data) {
		char[] hex = Hex.encodeHex(data);
		long bits = Long.decode("#" + String.valueOf(hex));
		Double.longBitsToDouble(bits);
		return Double.longBitsToDouble(bits);
	}
	
	private double readDouble(InputStream input, int number) throws IOException {
		byte[] buffer = new byte[number];
		if (read(input, buffer) != number)
			throw new ObjectException("Blocksize mismatch.");
		return doubleDecode(buffer);
	}
	
	private String readHex(InputStream input, int number) throws IOException {
		byte[] buffer = new byte[number];
		if (read(input, buffer) != number)
			throw new ObjectException("Blocksize mismatch.");
		//String result = String.valueOf(Hex.encodeHex(buffer));
		//logger.finest(message("HEX = {%s}", result));
		//return result;
		return String.valueOf(Hex.encodeHex(buffer));
	}
	
	private String readString(InputStream input, int number) throws IOException {
		byte[] buffer = new byte[number];
		if (read(input, buffer) != number)
			throw new ObjectException("Blocksize mismatch.");
		//String result = StringUtils.newStringUsAscii(buffer);
		//String hex = String.valueOf(Hex.encodeHex(buffer));
		//logger.finest(message("String = '%s' {%s}", result, hex));
		//return result;
		return StringUtils.newStringUsAscii(buffer);
	}
	
	private void block(byte[] data) throws IOException {
		InputStream input = new ByteArrayInputStream(data);
		int services = readInteger(input, 2);
		if (services == 0) {
			throw new ObjectException(readString(input, input.available()));
		}
		for (int service = 0; service <services; service++) {
			int size = readInteger(input, 2);
			byte[] buffer = new byte[size];
			if (read(input, buffer) == size)
				service(buffer);
			else
				throw new ObjectException("Blocksize mismatch.");
		}
	}
	
	private void service(byte[] data) throws IOException {
		InputStream input = new ByteArrayInputStream(data);
		int type = readInteger(input, 1);
		int error = readInteger(input, 1);
		switch (type) {
		case 0x01: // service = S4InstrumentInfo
			logger.finest(message("service = S4InstrumentInfo"));
			if (error != 0) {
				int size = readInteger(input, 2); 
				byte[] buffer = new byte[size];
				if (read(input, buffer) == size)
					throw new ObjectException(StringUtils.newStringUsAscii(buffer));
				else
					throw new ObjectException("Blocksize mismatch.");
			}
			int instruments = readInteger(input, 2);
			bid_offer = new String[instruments];
			for (int index = 0; index < instruments; index++) {
				logger.finest(message("Instruments Information #%d", index));
				int size = readInteger(input, 2); 
				byte[] buffer = new byte[size];
				if (read(input, buffer) == size)
					bid_offer[index] = instrumentInfo(buffer);
				else
					throw new ObjectException("Blocksize mismatch.");
			}
			break;
		case 0x03: // service = S4InstrumentTicker
			logger.finest(message("service = S4InstrumentTicker"));
			int instTickers = readInteger(input, 2);
			for (int index = 0; index < instTickers; index++) {
				int size = readInteger(input, 2); 
				byte[] buffer = new byte[size];
				if (read(input, buffer) == size)
					ticker(buffer);
				else
					throw new ObjectException("Blocksize mismatch.");
			}
			break;
		case 0x04: // service = S4MarketSummary
			logger.finest(message("service = S4MarketSummary"));
			marketSummary(input);
			break;
		case 0x05: // service = S4MarketTicker
			logger.finest(message("service = S4MarketTicker"));
			int marketTickers = readInteger(input, 2);
			marketTicker = new String[marketTickers];
			updateSequenceId = true;
			for (int index = 0; index < marketTickers; index++) {
				int size = readInteger(input, 2); 
				byte[] buffer = new byte[size];
				if (read(input, buffer) == size)
					marketTicker[index] = ticker(buffer);
					//ticker(buffer);
				else
					throw new ObjectException("Blocksize mismatch.");
			}
			updateSequenceId = false;
			break;
		default:
			throw new ObjectException(String.format("Unknow type 0x%02X", type));
		}
	}
		
	private void marketSummary(InputStream input) throws IOException {
		int x01 = readInteger(input, 4);
		// SET
		int set_index_last = readInteger(input, 4);
		int set_change_last = readInteger(input, 4);
		int set_index_high = readInteger(input, 4);
		int set_change_high = readInteger(input, 4);
		int set_index_low = readInteger(input, 4);
		int set_change_low = readInteger(input, 4);
		int set_value = readInteger(input, 4);
		int x09 = readInteger(input, 4); //
		int x10 = readInteger(input, 4); //
		int set_gainers = readInteger(input, 4);
		int set_loser = readInteger(input, 4);
		int set_unchanges = readInteger(input, 4);
		// SET50
		int set50_index_last = readInteger(input, 4);
		int set50_change_last = readInteger(input, 4);
		int set50_index_high = readInteger(input, 4);
		int set50_change_high = readInteger(input, 4);
		int set50_index_low = readInteger(input, 4);
		int set50_change_low = readInteger(input, 4);
		int set50_value = readInteger(input, 4);
		int set50_gainers = readInteger(input, 4);
		int set50_loser = readInteger(input, 4);
		int set50_unchanges = readInteger(input, 4);
		// SET100
		int set100_index_last = readInteger(input, 4);
		int set100_change_last = readInteger(input, 4);
		int set100_index_high = readInteger(input, 4);
		int set100_change_high = readInteger(input, 4);
		int set100_index_low = readInteger(input, 4);
		int set100_change_low = readInteger(input, 4);
		int set100_value = readInteger(input, 4);
		int set100_gainers = readInteger(input, 4);
		int set100_loser = readInteger(input, 4);
		int set100_unchanges = readInteger(input, 4);
		// MAI
		int mai_index_last = readInteger(input, 4);
		int mai_change_last = readInteger(input, 4);
		int mai_index_high = readInteger(input, 4);
		int mai_change_high = readInteger(input, 4);
		int mai_index_low = readInteger(input, 4);
		int mai_change_low = readInteger(input, 4);
		int mai_value = readInteger(input, 4);
		int mai_gainers = readInteger(input, 4);
		int mai_loser = readInteger(input, 4);
		int mai_unchanges = readInteger(input, 4);
		// TFEX		
		int tfex_01 = readInteger(input, 4);
		int tfex_02 = readInteger(input, 4);
		int tfex_03 = readInteger(input, 4);
		int tfex_04 = readInteger(input, 4);
		int tfex_05 = readInteger(input, 4);
		int tfex_06 = readInteger(input, 4);
		int tfex_07 = readInteger(input, 4);
		int tfex_08 = readInteger(input, 4);
		int tfex_09 = readInteger(input, 4);
		String tfex_date = readString(input, 8);
		// FTSE Large Cap
		int large_cap_index_last = readInteger(input, 4);
		int large_cap_change_last = readInteger(input, 4);
		int large_cap_index_high = readInteger(input, 4);
		int large_cap_change_high = readInteger(input, 4);
		int large_cap_index_low = readInteger(input, 4);
		int large_cap_change_low = readInteger(input, 4);
		// SETHD
		int sethd_index_last = readInteger(input, 4);
		int sethd_change_last = readInteger(input, 4);
		int sethd_index_high = readInteger(input, 4);
		int sethd_change_high = readInteger(input, 4);
		int sethd_index_low = readInteger(input, 4);
		int sethd_change_low = readInteger(input, 4);
		int sethd_value = readInteger(input, 4);
		int sethd_gainers = readInteger(input, 4);
		int sethd_loser = readInteger(input, 4);
		int sethd_unchanges = readInteger(input, 4);
		String market_status = readString(input, input.available());
		//SET = closed
		//ENERGY Intermission2
		//CURRENCY Close
		//STOCK Close
		//IR Close
		//EQ Close
		//METAL Intermission2
		logger.finer(message("Market:         [%d]", x01));
		logger.finer(message("SET:            %d, %d, %d, %d, %d, %d, %d, [%d], [%d], %d, %d, %d", 
				set_index_last, set_change_last, set_index_high, set_change_high, set_index_low, set_change_low, set_value, x09, x10, set_gainers, set_loser, set_unchanges));
		logger.finer(message("SET50:          %d, %d, %d, %d, %d, %d, %d, %d, %d, %d", 
				set50_index_last, set50_change_last, set50_index_high, set50_change_high, set50_index_low, set50_change_low, set50_value, set50_gainers, set50_loser, set50_unchanges));
		logger.finer(message("SET100:         %d, %d, %d, %d, %d, %d, %d, %d, %d, %d", 
				set100_index_last, set100_change_last, set100_index_high, set100_change_high, set100_index_low, set100_change_low, set100_value, set100_gainers, set100_loser, set100_unchanges));
		logger.finer(message("mai:            %d, %d, %d, %d, %d, %d, %d, %d, %d, %d", 
				mai_index_last, mai_change_last, mai_index_high, mai_change_high, mai_index_low, mai_change_low, mai_value, mai_gainers, mai_loser, mai_unchanges));
		logger.finer(message("TFEX:           %d, %d, %d, %d, %d, %d, %d, %d, %d, %s", 
				tfex_01, tfex_02, tfex_03, tfex_04, tfex_05, tfex_06, tfex_07, tfex_08, tfex_09, tfex_date));
		logger.finer(message("FTSE Large Cap: %d, %d, %d, %d, %d, %d", 
				large_cap_index_last, large_cap_change_last, large_cap_index_high, large_cap_change_high, large_cap_index_low, large_cap_change_low));
		logger.finer(message("SETHD:          %d, %d, %d, %d, %d, %d, %d, %d, %d, %d", 
				sethd_index_last, sethd_change_last, sethd_index_high, sethd_change_high, sethd_index_low, sethd_change_low, sethd_value, sethd_gainers, sethd_loser, sethd_unchanges));
		logger.finer(message("Market Status:  %s", market_status));
		/*
		System.out.printf("[%d:%d:%d], %n" + 
				"SET:            %d, %d, %d, %d, %d, %d, %d, [%d], [%d], %d, %d, %d, %n" + 
				"SET50:          %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %n" + 
				"SET100:         %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %n" + 
				"mai:            %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %n" + 
				"TFEX:           %d, %d, %d, %d, %d, %d, %d, %d, %d, %s, %n" +
				"FTSE Large Cap: %d, %d, %d, %d, %d, %d, %n" +
				"SETHD:          %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %n" +
				"%s%n",
				hour, minute, second,
				set_index_last, set_change_last, set_index_high, set_change_high, set_index_low, set_change_low, set_value, x09, x10, set_gainers, set_loser, set_unchanges,
				set50_index_last, set50_change_last, set50_index_high, set50_change_high, set50_index_low, set50_change_low, set50_value, set50_gainers, set50_loser, set50_unchanges,  
				set100_index_last, set100_change_last, set100_index_high, set100_change_high, set100_index_low, set100_change_low, set100_value, set100_gainers, set100_loser, set100_unchanges, 
				mai_index_last, mai_change_last, mai_index_high, mai_change_high, mai_index_low, mai_change_low, mai_value, mai_gainers, mai_loser, mai_unchanges,
				tfex_01, tfex_02, tfex_03, tfex_04, tfex_05, tfex_06, tfex_07, tfex_08, tfex_09, tfex_date,
				large_cap_index_last, large_cap_change_last, large_cap_index_high, large_cap_change_high, large_cap_index_low, large_cap_change_low, 
				sethd_index_last, sethd_change_last, sethd_index_high, sethd_change_high, sethd_index_low, sethd_change_low, sethd_value, sethd_gainers, sethd_loser, sethd_unchanges,
				market_status);
		*/
	}
		
	private String instrumentInfo(byte[] data) throws IOException {
		InputStream input = new ByteArrayInputStream(data);
		int flag = readInteger(input, 1);
		logger.finest(message("flag = %1$d",flag));
		int type = readInteger(input, 1);
		logger.finest(message("type = %1$d",type));
		int a = readInteger(input, 2);
		logger.finest(message("Unknow: %1$d {%1$04x}",a));
		int n = readInteger(input, 1);
		logger.finest(message("n = %1$d",n));
		double power = Math.pow(10.0, n);
		int last = readInteger(input, 4);
		logger.finest(message("last = %1$.2f",last/power));
		int change = readInteger(input, 4);
		logger.finest(message("Change = %1$.2f",change/power));
		int change_percent = readInteger(input, 4);
		logger.finest(message("Change = %1$.2f%%",change_percent/power));
		int high = readInteger(input, 4);
		logger.finest(message("High = %1$.2f",high/power));
		int low = readInteger(input, 4);
		logger.finest(message("Low = %1$.2f",low/power));
		int avg = readInteger(input, 4);
		logger.finest(message("Avarage = %1$.2f",avg/power));
		//int volume = readInteger(input, 4);
		//logger.finest(message("Volume = %1$d",volume));
		long volume;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Volume = %1$.2f",temp));
			volume = (long) temp;
		} else {
			volume = readInteger(input, 4);
			logger.finest(message("Volume = %1$d",volume));
		}
		int bid_1 = readInteger(input, 4);
		logger.finest(message("Bid #1 = %1$.2f",bid_1/power));
		int bid_2 = readInteger(input, 4);
		logger.finest(message("Bid #2 = %1$.2f",bid_2/power));
		int bid_3 = readInteger(input, 4);
		logger.finest(message("Bid #3 = %1$.2f",bid_3/power));
		int bid_4 = readInteger(input, 4);
		logger.finest(message("Bid #4 = %1$.2f",bid_4/power));
		int bid_5 = readInteger(input, 4);
		logger.finest(message("Bid #5 = %1$.2f",bid_5/power));
		int offer_1 = readInteger(input, 4);
		logger.finest(message("Offer #1 = %1$.2f",offer_1/power));
		int offer_2 = readInteger(input, 4);
		logger.finest(message("Offer #2 = %1$.2f",offer_2/power));
		int offer_3 = readInteger(input, 4);
		logger.finest(message("Offer #3 = %1$.2f",offer_3/power));
		int offer_4 = readInteger(input, 4);
		logger.finest(message("Offer #4 = %1$.2f",offer_4/power));
		int offer_5 = readInteger(input, 4);
		logger.finest(message("Offer #5 = %1$.2f",offer_5/power));
		long bid_vol_1;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Bid Volume #1 = %1$.2f",temp));
			bid_vol_1 = (long) temp;
		} else {
			bid_vol_1 = readInteger(input, 4);
			logger.finest(message("Bid Volume #1 = %1$d",bid_vol_1));
		}
		long bid_vol_2;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Bid Volume #2 = %1$.2f",temp));
			bid_vol_2 = (long) temp;
		} else {
			bid_vol_2 = readInteger(input, 4);
			logger.finest(message("Bid Volume #2 = %1$d",bid_vol_2));
		}
		long bid_vol_3;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Bid Volume #3 = %1$.2f",temp));
			bid_vol_3 = (long) temp;
		} else {
			bid_vol_3 = readInteger(input, 4);
			logger.finest(message("Bid Volume #3 = %1$d",bid_vol_3));
		}
		long bid_vol_4;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Bid Volume #4 = %1$.2f",temp));
			bid_vol_4 = (long) temp;
		} else {
			bid_vol_4 = readInteger(input, 4);
			logger.finest(message("Bid Volume #4 = %1$d",bid_vol_4));
		}
		long bid_vol_5;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Bid Volume #5 = %1$.2f",temp));
			bid_vol_5 = (long) temp;
		} else {
			bid_vol_5 = readInteger(input, 4);
			logger.finest(message("Bid Volume #5 = %1$d",bid_vol_5));
		}
		long offer_vol_1;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Offer Volume #1 = %1$.2f",temp));
			offer_vol_1 = (long) temp;
		} else {
			offer_vol_1 = readInteger(input, 4);
			logger.finest(message("Offer Volume #1 = %1$d",offer_vol_1));
		}
		long offer_vol_2;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Offer Volume #2 = %1$.2f",temp));
			offer_vol_2 = (long) temp;
		} else {
			offer_vol_2 = readInteger(input, 4);
			logger.finest(message("Offer Volume #2 = %1$d",offer_vol_2));
		}
		long offer_vol_3;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Offer Volume #3 = %1$.2f",temp));
			offer_vol_3 = (long) temp;
		} else {
			offer_vol_3 = readInteger(input, 4);
			logger.finest(message("Offer Volume #3 = %1$d",offer_vol_3));
		}
		long offer_vol_4;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Offer Volume #4 = %1$.2f",temp));
			offer_vol_4 = (long) temp;
		} else {
			offer_vol_4 = readInteger(input, 4);
			logger.finest(message("Offer Volume #4 = %1$d",offer_vol_4));
		}
		long offer_vol_5;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Offer Volume #5 = %1$.2f",temp));
			offer_vol_5 = (long) temp;
		} else {
			offer_vol_5 = readInteger(input, 4);
			logger.finest(message("Offer Volume #5 = %1$d",offer_vol_5));
		}
		int b = readInteger(input, 2);
		logger.finest(message("Unknow: %1$d {%1$04x}",b));
		int c = readInteger(input, 4);
		logger.finest(message("Unknow: %1$d {%1$08x}",c));
		int open_1 = readInteger(input, 4);
		logger.finest(message("Open I = %1$.2f",open_1/power));
		int open_2 = readInteger(input, 4);
		logger.finest(message("Open II = %1$.2f",open_2/power));
		int d = readInteger(input, 4);
		logger.finest(message("Unknow: %1$d {%1$08x}",d));
		int e = readInteger(input, 4);
		logger.finest(message("Unknow: %1$d {%1$08x}",e));
		int f = readInteger(input, 1);
		logger.finest(message("Unknow: %1$d {%1$02x}",f));
		int g = readInteger(input, 4);
		logger.finest(message("Unknow: %1$d {%1$08x}",g));
		int h = readInteger(input, 4);
		logger.finest(message("Unknow: %1$d {%1$08x}",h));
		int ceiling = readInteger(input, 4);
		logger.finest(message("Ceiling = %1$.2f",ceiling/power));
		int floor = readInteger(input, 4);
		logger.finest(message("Floor = %1$.2f",floor/power));
		String date = readString(input, 10);
		logger.finest(message("Date = %1s",date));
		long value;
		if ((flag & 0x01) != 0) {
			double temp = readDouble(input, 8);
			logger.finest(message("Value = %1$.2f",temp));
			value = (long) temp;
		} else {
			value = readInteger(input, 4);
			logger.finest(message("Value = %1$d",value));
		}
		int i = readInteger(input, 4);
		logger.finest(message("Unknow: %1$d {%1$08x}",i));
		int avg_buy = readInteger(input, 4);
		logger.finest(message("Average Buy = %1$.2f",avg_buy/power));
		int avg_sell = readInteger(input, 4);
		logger.finest(message("Average Sell = %1$.2f",avg_sell/power));
		int p_per_e = readInteger(input, 4);
		logger.finest(message("P/E = %1$.2f",p_per_e/power));
		int p_per_bv = readInteger(input, 4);
		logger.finest(message("P/BV = %1$.2f",p_per_bv/power));
		int yield = readInteger(input, 4);
		logger.finest(message("Yield = %1$.2f",yield/power));
		int eps = readInteger(input, 4);
		logger.finest(message("EPS = %1$.2f",eps/power));
		int change_percent_1w = readInteger(input, 4);
		logger.finest(message("Change 1Week = %1$.2f%%",change_percent_1w/power));
		int change_percent_1m = readInteger(input, 4);
		logger.finest(message("Change 1Month = %1$.2f%%",change_percent_1m/power));
		int change_percent_3m = readInteger(input, 4);
		logger.finest(message("Change 3Month = %1$.2f%%",change_percent_3m/power));
		int _52w_high = readInteger(input, 4);
		logger.finest(message("52Week High = %1$.2f",_52w_high/power));
		int _52w_low = readInteger(input, 4);
		logger.finest(message("52Week Low = %1$.2f",_52w_low/power));
		switch (type) {
		case 0:
			int x01;
			int x02;
			double exercise_price;
			String exercise_date;
			break;
		case 1: // Warrants
			x01 = readInteger(input, 4);
			logger.finest(message("Unknow x01 = %1$d",x01));
			exercise_price = readDouble(input, 8);
			logger.finest(message("Exercise Price = %1$.5f",exercise_price));
			exercise_date = readString(input, 10);
			logger.finest(message("Exercise Date = %1s",exercise_date));
			break;
		case 2: // Derivative Warrants
			x01 = readInteger(input, 4);
			logger.finest(message("Unknow x01 = %1$d",x01));
			exercise_price = readDouble(input, 8);
			logger.finest(message("Exercise Price = %1$.5f",exercise_price));
			exercise_date = readString(input, 10);
			logger.finest(message("Exercise Date = %1s",exercise_date));
			x02 = readInteger(input, 1);
			logger.finest(message("Unknow x02 = %1$d",x02));
			break;
		case 4: // ETFs
			int inav = readInteger(input, 4);
			logger.finest(message("iNAV = %1$.4f",inav/10000.0));
			break;
		default:
			logger.warning(message("Unknow type = %d", type));
			break;
		}
		String symbol = readString(input, input.available());
		logger.finest(message("Symbol = %1s",symbol));
		String[] symbols = symbol.split("[|]");
		logger.finer(message("%s,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d", symbols[0],
				bid_1/power,bid_vol_1,bid_2/power,bid_vol_2,bid_3/power,bid_vol_3,bid_4/power,bid_vol_4,bid_5/power,bid_vol_5,
				offer_1/power,offer_vol_1,offer_2/power,offer_vol_2,offer_3/power,offer_vol_3,offer_4/power,offer_vol_4,offer_5/power,offer_vol_5));
		return String.format("%s,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d,%.2f,%d", symbols[0],
				bid_1/power,bid_vol_1,bid_2/power,bid_vol_2,bid_3/power,bid_vol_3,bid_4/power,bid_vol_4,bid_5/power,bid_vol_5,
				offer_1/power,offer_vol_1,offer_2/power,offer_vol_2,offer_3/power,offer_vol_3,offer_4/power,offer_vol_4,offer_5/power,offer_vol_5);
	}
	
	private String ticker(byte[] data) throws IOException {
		InputStream input = new ByteArrayInputStream(data);
		// type of equity (market = 255)
		//   0. Common
		//   1. Common Foreign
		//   2. Warrants
		//   3. Derivative Warrants or ETFs
		int type = readInteger(input, 1); 
		int market = readInteger(input, 1); 
		int n = readInteger(input, 1);
		double power= Math.pow(10.0, n);
		String date = readString(input, 8);
		String side = readString(input, 1);
		int price = readInteger(input, 4);
		int close = readInteger(input, 4);
		int change = readInteger(input, 4);
		int changePercent = readInteger(input, 4);
		int sequence = readInteger(input, 4);
		String a = readHex(input, 1); 
		String b = readHex(input, 1);
		int volume = readInteger(input, 4);
		String symbol = readString(input, input.available());
		symbol_date.put(symbol,date);
		//symbols.add(symbol);
		if (updateSequenceId)
			if (market == 255)
				sequenceId = sequence;
			else
				optionSequenceId = sequence;
		logger.finer(message("Ticker: %d, %d, %d, %s, %s, %.2f, %.2f, %.2f, %.2f, %d, {%s}, {%s}, %d, %s", type, market, n, 
				date, side, price/power, close/power, change/power, changePercent/power, sequence,
				a, b,
				volume, symbol));
		return String.format("%s,%s,%d,%s,%s,%.2f,%.2f,%.2f,%.2f,%d,%s,%s,%d,%s", type, market, n, 
				date, side, price/power, close/power, change/power, changePercent/power, sequence,
				a, b,
				volume, symbol);
		//return String.format("%d,%s,%s,%.2f,%d", sequence, symbol, side, price/power, volume);
	}

}
