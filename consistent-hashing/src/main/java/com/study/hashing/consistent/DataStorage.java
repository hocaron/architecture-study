package com.study.hashing.consistent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 데이터 저장소
 */
public class DataStorage {
    private final Map<String, String> storage = new ConcurrentHashMap<>();

    /**
     * 데이터 저장
     *
     * @param key   키
     * @param value 값
     */
    public void store(String key, String value) {
        storage.put(key, value);
    }

    /**
     * 데이터 조회
     *
     * @param key 키
     * @return 값
     */
    public String get(String key) {
        return storage.get(key);
    }

    /**
     * 모든 데이터 조회(재배치 시 사용)
     */
    public Map<String, String> getAllData() {
        return new HashMap<>(storage);
    }
}