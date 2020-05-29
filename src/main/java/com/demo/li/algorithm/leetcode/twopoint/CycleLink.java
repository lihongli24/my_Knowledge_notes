package com.demo.li.algorithm.leetcode.twopoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @author lihongli
 * create：2020/5/28 11:59 下午
 */
public class CycleLink {


    public static void main(String[] args) {
        Link header = createLoopLink();

//        System.out.println(hasLoop(header));

        Link loopHeader = findLoopHeader(header);
        if(null == loopHeader){
            System.out.println("没有环");
        }else {
            System.out.println(loopHeader.getValue());
        }

//        Link current = header;
//        while (current != null){
//            System.out.println(current.getValue());
//            current = current.next;
//        }
    }

    private static Link findLoopHeader(Link header) {
        Link slow = header;
        Link fast = header;

        boolean hasLoop = false;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                hasLoop = true;
                break;
            }
        }


        if (!hasLoop) {
            return null;
        }

        slow = header;
        while (fast != slow) {
            slow = slow.next;
            fast = fast.next;
        }

        return slow;
    }

    /**
     * 判断是否有环
     *
     * @param header
     * @return
     */
    private static boolean hasLoop(Link header) {
        Link slow = header;
        Link fast = header;

        boolean hasLoop = false;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if (slow == fast) {
                hasLoop = true;
                break;
            }
        }
        return hasLoop;
    }


    /**
     * 造出一条带环的链
     *
     * @return
     */
    private static Link createLoopLink() {
        Link header = null, link = null, link2Loop = null, tail = null;
        for (int i = 0; i < 10; i++) {
            link = new Link(i, link);
            if (i == 0) {
                tail = link;
            }
            if (i == 5) {
                link2Loop = link;
            }
            header = link;
        }

        tail.next = link2Loop;
        return header;
    }

    @Data
    @AllArgsConstructor
    @ToString(exclude = "next")
    static class Link {
        private int value;
        private Link next;
    }
}
