package com.study.hashing.consistent;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataStorageTest {
    @Test
    void testNodeAdditionWithDataRebalance() {
        // 초기 노드 생성
        List<String> nodes = Arrays.asList("Node1", "Node2", "Node3", "Node4", "Node5");

        // Consistent Hashing 객체 생성 (가상 노드 8개씩 할당)
        ConsistentHashingWithVnodes<String> consistentHash = new ConsistentHashingWithVnodes<>(8, nodes);
        DataManager<String> dataManager = new DataManager<>();

        // 데이터 저장
        dataManager.storeData("Data1", "Value1", consistentHash.getNodeForKey("Data1"));
        dataManager.storeData("Data2", "Value2", consistentHash.getNodeForKey("Data2"));
        dataManager.storeData("Data3", "Value3", consistentHash.getNodeForKey("Data3"));

        // 데이터가 원래 할당된 노드 확인
        System.out.println("Before adding Node6:");
        System.out.println("Data1 is stored in: " + consistentHash.getNodeForKey("Data1"));
        System.out.println("Data2 is stored in: " + consistentHash.getNodeForKey("Data2"));
        System.out.println("Data3 is stored in: " + consistentHash.getNodeForKey("Data3"));

        // 새로운 노드 추가
        String newNode = "Node6";
        consistentHash.addNode(newNode);

        // 데이터 재배치
        dataManager.redistributeData(consistentHash);

        // 데이터가 재배치되었는지 확인
        System.out.println("\nAfter adding Node6:");
        System.out.println("Data1 is now stored in: " + consistentHash.getNodeForKey("Data1"));
        System.out.println("Data2 is now stored in: " + consistentHash.getNodeForKey("Data2"));
        System.out.println("Data3 is now stored in: " + consistentHash.getNodeForKey("Data3"));

        // 데이터가 실제로 저장된 노드에서 조회되는지 확인
        assertEquals("Value1", dataManager.getData("Data1", consistentHash.getNodeForKey("Data1")));
        assertEquals("Value2", dataManager.getData("Data2", consistentHash.getNodeForKey("Data2")));
        assertEquals("Value3", dataManager.getData("Data3", consistentHash.getNodeForKey("Data3")));
    }
}
