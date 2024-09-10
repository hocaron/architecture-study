package com.study.hashing.consistent;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 안정 해시 알고리즘 (Consistent Hashing) 구현체
 *
 * @param <T> 노드 타입
 */
public class ConsistentHashingWithVnodes<T> {
    private final int numberOfVirtualNodes;                             // 가상 노드의 개수
    private final SortedMap<BigInteger, T> circle = new TreeMap<>();    // 원형 링

    public ConsistentHashingWithVnodes(int numberOfVirtualNodes, Collection<T> nodes) {
        this.numberOfVirtualNodes = numberOfVirtualNodes;
        for (T node : nodes) {
            addNode(node);
        }
    }

    /**
     * 노드 추가
     *
     * @param node 노드
     */
    public void addNode(T node) {
        for (int i = 0; i < numberOfVirtualNodes; i++) {
            circle.put(hash(node.toString() + i), node);
        }
    }

    /**
     * 노드 제거
     *
     * @param node 노드
     */
    public void removeNode(T node) {
        for (int i = 0; i < numberOfVirtualNodes; i++) {
            circle.remove(hash(node.toString() + i));
        }
    }

    /**
     * 데이터에 대한 노드 반환 </p>
     * 데이터 키를 해시하여 가장 가까운 노드를 찾음 </p>
     * 데이터 키가 가장 가까운 노드의 해시 값보다 크면, 첫 번째 노드로 할당 </p>
     * 데이터 키가 가장 가까운 노드의 해시 값보다 작거나 같으면, 해당 노드로 할당 </p>
     *
     * @param key 데이터 키
     * @return 노드
     */
    public T getNodeForKey(String key) {
        if (circle.isEmpty()) {
            return null;
        }

        BigInteger dataHash = hash(key);

        SortedMap<BigInteger, T> tailMap = circle.tailMap(dataHash);

        if (tailMap.isEmpty()) {
            return circle.get(circle.firstKey());
        }

        return circle.get(tailMap.firstKey());
    }

    private BigInteger hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(key.getBytes());
            return new BigInteger(1, md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 데이터 키의 범위 출력
     *
     * @param key 데이터 키
     */
    public void printDataKeyRange(String key) {
        BigInteger dataHash = hash(key);

        System.out.println(key + " with hash " + dataHash);
    }

    /**
     * 노드의 해시 값을 확인할 수 있는 메서드
     */
    public void printNodePositionsSimplified() {
        System.out.println("Hash Ring Node Positions:");
        circle.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    BigInteger simplifiedHash = entry.getKey();
                    System.out.println("Node " + entry.getValue() + " is at position: " + simplifiedHash);
                });
    }
}