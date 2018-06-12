package com.qvc.selenium.data;

import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class StoredValueDataManager {
	private static final Logger logger = Logger.getLogger(StoredValueDataManager.class
			.getName());
	private static ThreadLocal<ConcurrentHashMap<String, Object>> cache = new ThreadLocal<ConcurrentHashMap<String, Object>>() {
		@Override
		protected ConcurrentHashMap<String, Object> initialValue() {
			return new ConcurrentHashMap<String, Object>();
		}
	};

	public StoredValueDataManager() {
	}

	public static StoredValueDataManager instance = new StoredValueDataManager();

	public static StoredValueDataManager getInstance() {
		return instance;
	}

	public Object getStoredData(String type) {
		return cache.get().get(type);
	}

	public void setStoredData(String type, Object data) {
		cache.get().put(type, data);
	}

	public boolean clearCache() {
		cache.get().clear();
		return true;
	}



}