package com.study.hashing.consistent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 데이터 관리자
 *
 * @param <T> 노드 타입
 */
public class DataManager<T> {
    private final Map<T, DataStorage> nodeDataMap = new ConcurrentHashMap<>(); // 각 노드의 데이터 저장소 맵

    /**
     * 데이터 저장
     *
     * @param key   키
     * @param value 값
     * @param node  노드
     */
    public void storeData(String key, String value, T node) {
        if (!nodeDataMap.containsKey(node)) {
            nodeDataMap.put(node, new DataStorage());
        }
        nodeDataMap.get(node).store(key, value);
    }

    /**
     * 데이터 조회
     *
     * @param key  키
     * @param node 값
     * @return 값
     */
    public String getData(String key, T node) {
        return nodeDataMap.getOrDefault(node, new DataStorage()).get(key);
    }

    /**
     * 데이터 재배치 </p>
     * 모든 노드의 데이터를 조회 </p>
     * 각 데이터를 새로운 노드에 할당
     *
     * @param consistentHash Consistent Hashing 객체
     */
    public void redistributeData(ConsistentHashingWithVnodes<T> consistentHash) {
        Map<String, String> allData = new HashMap<>();

        for (DataStorage storage : nodeDataMap.values()) {
            allData.putAll(storage.getAllData());
        }

        nodeDataMap.clear();
        for (String dataKey : allData.keySet()) {
            T assignedNode = consistentHash.getNodeForKey(dataKey);
            storeData(dataKey, allData.get(dataKey), assignedNode);
        }
    }
}