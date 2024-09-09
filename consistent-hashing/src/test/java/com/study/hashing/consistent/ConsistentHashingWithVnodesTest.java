package com.study.hashing.consistent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConsistentHashingWithVnodesTest {

    private ConsistentHashingWithVnodes<String> consistentHash;

    @BeforeEach
    public void setUp() {
        // 테스트에 사용할 초기 노드들
        List<String> nodes = new ArrayList<>();
        nodes.add("Node1");
        nodes.add("Node2");
        nodes.add("Node3");

        // 가상 노드를 3개씩 할당
        consistentHash = new ConsistentHashingWithVnodes<>(3, nodes);
    }

    @Test
    public void testNodeForKey_beforeAddingAndRemovingNodes() {
        // 노드를 추가/제거하지 않은 상태에서 키 위치 확인
        String nodeForData1 = consistentHash.getNodeForKey("Data1");
        String nodeForData2 = consistentHash.getNodeForKey("Data3");

        assertNotNull(nodeForData1);
        assertNotNull(nodeForData2);
        assertNotEquals(nodeForData1, nodeForData2); // 다른 노드에 분산되었는지 확인

        System.out.println("Key 'Data1' is located at: " + nodeForData1);
        System.out.println("Key 'Data2' is located at: " + nodeForData2);
    }

    @Test
    public void testAddingNode() {
        // 노드 추가 전, 데이터가 어떤 노드에 매핑되는지 확인
        String nodeForData1Before = consistentHash.getNodeForKey("Data1");
        String nodeForData2Before = consistentHash.getNodeForKey("Data3");

        System.out.println("Before adding Node4:");
        System.out.println("Data1 is located at: " + nodeForData1Before);
        consistentHash.printDataKeyRange("Data1");
        System.out.println("Data3 is located at: " + nodeForData2Before);
        consistentHash.printDataKeyRange("Data3");
        consistentHash.printNodePositionsSimplified();

        // 새로운 노드 추가
        consistentHash.addNode("Node4");

        // 노드 추가 후, 데이터가 새로 매핑되는 노드 확인
        String nodeForData1After = consistentHash.getNodeForKey("Data1");
        String nodeForData2After = consistentHash.getNodeForKey("Data3");

        System.out.println("After adding Node4:");
        System.out.println("Key 'Data1' is now located at: " + nodeForData1After);
        System.out.println("Key 'Data2' is now located at: " + nodeForData2After);
        consistentHash.printDataKeyRange(nodeForData1After);
        consistentHash.printDataKeyRange(nodeForData2After);
        consistentHash.printNodePositionsSimplified();
    }

    @Test
    public void testRemovingNode() {
        // 노드 추가 및 제거 시 변화 확인
        consistentHash.addNode("Node4");
        consistentHash.removeNode("Node2"); // Node2를 제거

        // 제거 후, 기존 키들의 위치가 올바르게 재배치되었는지 확인
        String nodeForData1 = consistentHash.getNodeForKey("Data1");
        String nodeForData2 = consistentHash.getNodeForKey("Data2");

        assertNotNull(nodeForData1);
        assertNotNull(nodeForData2);

        System.out.println("After removing Node2:");
        System.out.println("Key 'Data1' is now located at: " + nodeForData1);
        System.out.println("Key 'Data2' is now located at: " + nodeForData2);
    }

    @Test
    public void testReplicationConsistency() {
        // 동일한 키에 대해 노드 추가/삭제 후에도 일관된 결과가 나오는지 확인
        String initialNodeForData1 = consistentHash.getNodeForKey("Data1");

        // Node4 추가 후에도 Data1이 같은 노드에 위치하는지 확인
        consistentHash.addNode("Node4");
        String nodeForData1AfterAddition = consistentHash.getNodeForKey("Data1");

        // Node2 삭제 후에도 동일한 데이터가 동일한 노드에 위치하는지 확인
        consistentHash.removeNode("Node2");
        String nodeForData1AfterRemoval = consistentHash.getNodeForKey("Data1");

        // 새로운 노드가 추가되거나 제거되더라도 동일한 키에 대한 위치는 변하지 않도록 함
        assertEquals(initialNodeForData1, nodeForData1AfterAddition);
        assertEquals(initialNodeForData1, nodeForData1AfterRemoval);

        System.out.println("Key 'Data1' consistency maintained across node addition/removal.");
    }
}