package com.study.hashing.consistent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {
    private final Map<String, String> storage = new ConcurrentHashMap<>(); // 실제 데이터 저장

    // 데이터 저장
    public void store(String dataKey, String dataValue) {
        storage.put(dataKey, dataValue);
    }

    // 데이터 조회
    public String get(String dataKey) {
        return storage.get(dataKey);
    }

    // 모든 데이터 조회 (재배치 시 사용)
    public Map<String, String> getAllData() {
        return new HashMap<>(storage);
    }
}