package com.study.hashing.consistent;

import org.junit.jupiter.api.Test;

import java.util.*;

public class DataDistributionTest {

    @Test
    void distribution() {
        // 노드 생성
        List<String> nodes = Arrays.asList("Node1", "Node2", "Node3", "Node4", "Node5");

        // Consistent Hashing 객체 생성 (가상 노드 3개씩 할당)
        ConsistentHashingWithVnodes<String> consistentHash = new ConsistentHashingWithVnodes<>(8, nodes);

        // 각 노드에 할당된 데이터 개수를 저장하는 맵
        Map<String, Integer> nodeDataCount = new HashMap<>();
        for (String node : nodes) {
            nodeDataCount.put(node, 0);
        }

        // 1억개의 무작위 데이터 생성 및 할당
        Random random = new Random();
        int totalDataCount = 100_000_000;
        for (int i = 0; i < totalDataCount; i++) {
            // 무작위 데이터 생성 (랜덤 숫자로 가정)
            String randomData = "Data" + random.nextInt(Integer.MAX_VALUE);

            // 해당 데이터가 어느 노드에 할당되는지 확인
            String assignedNode = consistentHash.getNodeForKey(randomData);

            // 해당 노드의 데이터 개수 증가
            nodeDataCount.put(assignedNode, nodeDataCount.get(assignedNode) + 1);
        }

        // 결과 출력
        System.out.println("Data distribution across nodes:");
        for (Map.Entry<String, Integer> entry : nodeDataCount.entrySet()) {
            double percentage = (double) entry.getValue() / totalDataCount * 100;
            System.out.println(entry.getKey() + " has " + entry.getValue() + " data items (" + String.format("%.2f", percentage) + "%)");
        }
    }

    @Test
    void distributionAfterAddingNode() {
        // 초기 노드 생성
        List<String> nodes = Arrays.asList("Node1", "Node2", "Node3", "Node4", "Node5");

        // Consistent Hashing 객체 생성 (가상 노드 8개씩 할당)
        ConsistentHashingWithVnodes<String> consistentHash = new ConsistentHashingWithVnodes<>(20, nodes);

        // 각 노드에 할당된 데이터 개수를 저장하는 맵
        Map<String, Integer> nodeDataCount = new HashMap<>();
        for (String node : nodes) {
            nodeDataCount.put(node, 0);
        }

        // 1억개의 무작위 데이터 생성 및 할당
        Random random = new Random();
        int totalDataCount = 100_000_000;
        for (int i = 0; i < totalDataCount; i++) {
            // 무작위 데이터 생성 (랜덤 숫자로 가정)
            String randomData = "Data" + random.nextInt(Integer.MAX_VALUE);

            // 해당 데이터가 어느 노드에 할당되는지 확인
            String assignedNode = consistentHash.getNodeForKey(randomData);

            // 해당 노드의 데이터 개수 증가
            nodeDataCount.put(assignedNode, nodeDataCount.get(assignedNode) + 1);
        }

        // 기존 노드 분포 출력
        System.out.println("Data distribution across nodes before adding a new node:");
        for (Map.Entry<String, Integer> entry : nodeDataCount.entrySet()) {
            double percentage = (double) entry.getValue() / totalDataCount * 100;
            System.out.println(entry.getKey() + " has " + entry.getValue() + " data items (" + String.format("%.2f", percentage) + "%)");
        }

        // 새로운 노드 추가
        String newNode = "Node6";
        consistentHash.addNode(newNode);
        nodeDataCount.put(newNode, 0); // 새로운 노드에 대한 카운트 초기화

        // 노드 추가 후 1억 개의 데이터 다시 할당 및 재분배 확인
        for (int i = 0; i < totalDataCount; i++) {
            String randomData = "Data" + random.nextInt(Integer.MAX_VALUE);
            String assignedNode = consistentHash.getNodeForKey(randomData);
            nodeDataCount.put(assignedNode, nodeDataCount.get(assignedNode) + 1);
        }

        // 새로운 노드 추가 후 데이터 분포 출력
        System.out.println("\nData distribution across nodes after adding a new node:");
        for (Map.Entry<String, Integer> entry : nodeDataCount.entrySet()) {
            double percentage = (double) entry.getValue() / totalDataCount * 100;
            System.out.println(entry.getKey() + " has " + entry.getValue() + " data items (" + String.format("%.2f", percentage) + "%)");
        }
    }
}
