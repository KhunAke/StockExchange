package com.javath.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.javath.Object;

public class Storage extends Object {
	
	private final static Map<String,Storage> map;
	private final static String fileDefault;
	
	static {
		map = new HashMap<String, Storage>();
		String filename = path.var + file.separator + "storage.txt";
		Storage storageDefault = new Storage(filename);
		fileDefault = storageDefault.getFilename();
		map.put(fileDefault, storageDefault);
	}
	
	private final Lock lock = new Lock();
	private final Queue<Thread> queue = new ConcurrentLinkedQueue<Thread>();
	private final File filename;
	
	private Storage(String filename) {
		this.filename = new File(filename);
	}
	 
	public static Storage getInstance() {
		return getInstance(fileDefault);
	}
	
	public static Storage getInstance(String filename) {
		File file = new File(filename);
		Storage storage = map.get(file.getAbsolutePath());
		if (storage == null)
			storage = new Storage(filename);
		return storage;
	}
	
	public String getFilename() {
		return filename.getAbsolutePath();
	}
	
	private boolean reserve(boolean lock) {
		boolean wait = false;
		while (true) {
			synchronized (this.lock) {
				if (lock) {
					if (this.lock.isValue())
						wait = true;
					else {
						this.lock.setValue(lock);
						logger.fine(message("Lock filename \"%s\"", filename.getAbsoluteFile()));
						return true;
					}
				} else {
					if (this.lock.isValue()) {
						while (!queue.isEmpty()) {
							Thread thread = queue.poll();
							thread.interrupt();
						}
						this.lock.setValue(lock);
						logger.fine(message("Unlock filename \"%s\"", filename.getAbsoluteFile()));
						return true;
					} else {
						return false;
					}
				}
			}
			
			if (wait) {
				Thread thread = Thread.currentThread();
				queue.offer(thread);
				try {
					logger.fine(message("Waited filename \"%s\"", filename.getAbsoluteFile()));
					Thread.sleep(Integer.MAX_VALUE);
				} catch (InterruptedException e) {
					wait = false;
				}
			}
			
		}
	}
	
	private OutputStream output;
	private InputStream input;
	
	private OutputStream write(boolean append) throws FileNotFoundException {
		reserve(true);
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				logger.warning(message(e));
				//throw new ObjectException(e);
			} finally {
				input = null;
			}
		}
		if (output == null)
			output = new FileOutputStream(filename, append);
		return output;
	}
	
	public OutputStream append() throws FileNotFoundException {
		try {
			return write(true);
		} catch (FileNotFoundException e) {
			File pathParent = new java.io.File(filename.getParent());
			pathParent.mkdirs();
			return write(true);
		}
	}
	
	public OutputStream overwrite() throws FileNotFoundException {
		try {
			return write(false);
		} catch (FileNotFoundException e) {
			java.io.File pathParent = new java.io.File(filename.getParent());
			pathParent.mkdirs();
			return write(false);
		}
	}
	
	public InputStream read() throws FileNotFoundException {
		reserve(true);
		if (output != null) {
			try {
				output.flush();
				output.close();
			} catch (IOException e) {
				logger.warning(message(e));
				//throw new ObjectException(e);
			} finally {
				output = null;
			}
		}
		if (input == null)
			input = new FileInputStream(filename);
		return input;
	}
	
	public void release() {
		try {
			if (output != null)
				output.flush();
		} catch (IOException e) {
			logger.warning(message(e));
			//throw new ObjectException(e);
		} finally {
			reserve(false);
		}
	}
	
	public void remove() {
		map.remove(filename.getAbsolutePath());
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (output != null) {
			output.flush();
			output.close();
			output = null;
		}
		if (input != null) {
			input.close();
			input = null;
		}
		super.finalize();
	}
	
}
