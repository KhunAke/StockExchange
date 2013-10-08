package com.javath.stock.settrade;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.javath.OS;
import com.javath.Object;
import com.javath.ObjectException;
import com.javath.mapping.SetCompanyHome;
import com.javath.mapping.SettradeBoard;
import com.javath.mapping.SettradeBoardHome;
import com.javath.mapping.SettradeBoardId;
import com.javath.util.text.Delimited;

public class CheckPoint extends Object implements Runnable{

	@Override
	public void run() {
		checkBoard();
	}
	
	private void checkBoard() {
		FileFilter filter = new FileFilter() {
			Pattern pattern = Pattern.compile("board.*");

			@Override
			public boolean accept(File file) {
				if (file.isFile() && pattern.matcher(file.getName()).find())
					return true;
				else
					return false;
			}
			
		};
		File[] files = new File(path.var).listFiles(filter);
		Delimited delimited = new Delimited();
		delimited.setDelimiter(",");
		SettradeBoardHome home = new SettradeBoardHome();
		Session session = null;
		for (int index = 0; index < files.length; index++) {
			try {
				InputStream inputStream = new FileInputStream(files[index]);
				delimited.setInputStream(inputStream);
				System.out.println(files[index].getName());
				int line_number = 0;
				String cache = null;
				boolean clear_cache = true;
				while (delimited.hasNextRow()) {
					delimited.nextRow();
					line_number += 1;
					if (delimited.getFieldLength() == 12)
						home.attachDirty(createBoard(delimited.getFields()));
					else {
						String line = delimited.getCurrent();
						// Comment line
						if ((line.charAt(0) == '#') && 
							(line.charAt(1) == '-') &&
							(line.charAt(2) == '-')) {
							System.err.println(getFilenameInComment(line));
							try {
								session.getTransaction().commit();
								session = null;
								if (clear_cache)
									new File(cache).deleteOnExit();
							} catch (org.hibernate.JDBCException e) {
								logger.severe(message("SQL Error %d, SQLState %s: %s", 
										e.getErrorCode(), e.getSQLState(), e.getMessage()));
								session.getTransaction().rollback();
							} catch (NullPointerException e) {}
							//
							cache = getFilenameInComment(line);
							clear_cache = true;
							//
							session = ((SessionFactory) this.getContext("SessionFactory"))
									//	.openSession();
									.getCurrentSession();
							session.beginTransaction();
							continue;
						} else {
							logger.warning(message("\"%s\" in line %d", files[index].getName(),line_number));
							clear_cache = false;
						}
					}
				}
				if (session != null) { 
					try {
						session.getTransaction().commit();
						session = null;
						if (clear_cache)
							new File(cache).deleteOnExit();
					} catch (org.hibernate.JDBCException e) {
						logger.severe(message("SQL Error %d, SQLState %s: %s", 
								e.getErrorCode(), e.getSQLState(), e.getMessage()));
						session.getTransaction().rollback();
					}
				}
			} catch (FileNotFoundException e) {
				logger.severe(message(e));
				throw new ObjectException(e);
			} catch (IOException e) {
				logger.severe(message(e));
				throw new ObjectException(e);
			}
		}
	}

	private String getFilenameInComment(String comment) {
		int beginIndex = comment.indexOf('"', comment.indexOf("cache in")) + 1;
		int endIndex = comment.indexOf('"', beginIndex + 1);
		return comment.substring(beginIndex, endIndex);
	}
	
	private SettradeBoard createBoard(String[] data) {
		SettradeBoardId id = new SettradeBoardId();
		id.setSymbol(data[0]);
		id.setDate(OS.datetime(data[1]));
		SettradeBoard board = new SettradeBoard(id);
		try {
			board.setOpen(Double.valueOf(data[2]));
		} catch (NumberFormatException e) {}
		try {
			board.setHigh(Double.valueOf(data[3]));
		} catch (NumberFormatException e) {}
		try {
			board.setLow(Double.valueOf(data[4]));
		} catch (NumberFormatException e) {}
		try {
			board.setLast(Double.valueOf(data[5]));
		} catch (NumberFormatException e) {}
		try {
			board.setBid(Double.valueOf(data[6]));
		} catch (NumberFormatException e) {}
		try {
			board.setBidVolume(Long.valueOf(data[7]));
		} catch (NumberFormatException e) {}
		try {
			board.setOffer(Double.valueOf(data[8]));
		} catch (NumberFormatException e) {}
		try {
			board.setOfferVolume(Long.valueOf(data[9]));
		} catch (NumberFormatException e) {}
		try {
			board.setVolume(Long.valueOf(data[10]));
		} catch (NumberFormatException e) {}
		try {
			board.setValue(Double.valueOf(data[11]));
		} catch (NumberFormatException e) {}
		return board;
	}
}
