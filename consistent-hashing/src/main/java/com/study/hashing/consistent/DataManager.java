package com.study.hashing.consistent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager<T> {
    private final Map<T, DataStorage> nodeDataMap = new ConcurrentHashMap<>(); // 각 노드의 데이터 저장소 맵

    // 데이터 저장
    public void storeData(String dataKey, String dataValue, T node) {
        if (!nodeDataMap.containsKey(node)) {
            nodeDataMap.put(node, new DataStorage());
        }
        nodeDataMap.get(node).store(dataKey, dataValue);
    }

    // 데이터 조회
    public String getData(String dataKey, T node) {
        return nodeDataMap.getOrDefault(node, new DataStorage()).get(dataKey);
    }

    // 데이터 재배치: 노드를 추가한 후 데이터를 재배치
    public void redistributeData(ConsistentHashingWithVnodes<T> consistentHash) {
        Map<String, String> allData = new HashMap<>();

        // 모든 노드의 데이터를 수집
        for (DataStorage storage : nodeDataMap.values()) {
            allData.putAll(storage.getAllData());
        }

        // 노드가 담당하는 구간에 맞게 데이터 재분배
        nodeDataMap.clear(); // 기존 저장소 비우기
        for (String dataKey : allData.keySet()) {
            T assignedNode = consistentHash.getNodeForKey(dataKey); // 데이터의 새로운 노드
            storeData(dataKey, allData.get(dataKey), assignedNode); // 재배치된 데이터를 새로 할당
        }
    }
}