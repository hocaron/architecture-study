package com.study.hashing.consistent;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashingWithVnodes<T> {
    private final int numberOfReplicas; // 가상 노드의 수
    private final SortedMap<BigInteger, T> circle = new TreeMap<>();

    // 생성자
    public ConsistentHashingWithVnodes(int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        for (T node : nodes) {
            addNode(node);
        }
    }

    // 노드 추가
    public void addNode(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.put(hash(node.toString() + i), node);
        }
    }

    // 노드 제거
    public void removeNode(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.remove(hash(node.toString() + i));
        }
    }

    // 데이터가 어느 노드에 위치하는지 반환
    public T getNodeForKey(String key) {
        if (circle.isEmpty()) {
            return null;
        }

        BigInteger dataHash = hash(key); // 데이터의 전체 해시 값 사용

        // 데이터 해시 값보다 큰 가장 가까운 노드 찾기
        SortedMap<BigInteger, T> tailMap = circle.tailMap(dataHash);

        // tailMap이 비어 있으면, 데이터는 원형 링의 첫 번째 노드에 할당
        if (tailMap.isEmpty()) {
            return circle.get(circle.firstKey());
        }

        // 데이터 해시 값보다 큰 가장 가까운 노드 반환
        return circle.get(tailMap.firstKey());
    }

    // 해시 생성 함수 (SHA-1)
    private BigInteger hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(key.getBytes());
            return new BigInteger(1, md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // 데이터가 어떤 구간에 있는지 확인하는 메서드 추가
    public void printDataKeyRange(String dataKey) {
        BigInteger dataHash = hash(dataKey); // 데이터의 해시 값 (mod 100)

        // 원형 구조를 고려한 구간 출력
        System.out.println(dataKey + " with hash " + dataHash);
    }

    // 노드의 해시 값을 확인할 수 있는 메서드
    public void printNodePositionsSimplified() {
        System.out.println("Simplified Hash Ring Node Positions (mod 100, sorted):");
        circle.entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey()))
                .forEach(entry -> {
                    BigInteger simplifiedHash = entry.getKey();
                    System.out.println("Node " + entry.getValue() + " is at position: " + simplifiedHash);
                });
    }
}