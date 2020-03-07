package com.demo.li.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lihongli24 on 2020/3/5.
 */
public class TransLinkedList {

    private static String[] names = {"e", "d", "c", "b", "a"};

    public static void main(String[] args) {
        Node node = null;
        Node next = null;
        for (int i = 0; i < 5; i++) {
            node = Node.builder()
                    .value(names[i])
                    .next(next).build();
            next = node;
        }

        Node top = null;
        Node before = node;
        Node nextNode = null;


        while (before.next != null){
            Node node1 = (nextNode == null ? before : nextNode);
            Node node2 = node1.next != null ? node1.next : null;
            if(nextNode == null){
                nextNode = node2.getNext();
                node1.next = nextNode;
                node2.next = node1;
                top = node2;
            } else if(node2 != null){
                nextNode = node2.getNext();
                node1.next = nextNode;
                node2.next = node1;
                before.next = node2;
            }else {
                before.next = node1;
            }
            before = node1;
        }



        node = top;

        while (node != null){

            System.out.println(node.getValue());
            node = node.next;
        }


    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Node {
        private String value;
        private Node next;
    }
}
