package algorithm.search;

import org.testng.collections.Lists;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 树的遍历
 */
public class TreeItr {

    private Node root;
    private List<String> itrResult = Lists.newArrayList();

    public TreeItr() {
        init();
    }

    //定义节点类，内部类
    public class Node {

        private String data;
        private Node lchild;//定义指向左子树的指针
        private Node rchild;//定义指向右子树的指针

        public Node(String data, Node lchild, Node rchild) {
            this.data = data;
            this.lchild = lchild;
            this.rchild = rchild;
        }
    }

    //树的初始化；先从叶子节点开始，由叶到根
    private void init() {
        Node x = new Node("X", null, null);
        Node y = new Node("y", null, null);
        Node d = new Node("d", x, y);
        Node e = new Node("e", null, null);
        Node f = new Node("f", null, null);
        Node c = new Node("c", e, f);
        Node b = new Node("b", d, null);
        Node a = new Node("a", b, c);
        root = a;
    }

    /**
     * ==================带递归的===========
     */
    /**
     * 前序
     *
     * @param node
     */
    private void preOrder(Node node) {
        itrResult.add(node.data);
        if (null != node.lchild) {
            preOrder(node.lchild);
        }
        if (null != node.rchild) {
            preOrder(node.rchild);
        }
    }

    /**
     * 中序
     *
     * @param node
     */
    private void inOrder(Node node) {
        if (node.lchild != null) {
            inOrder(node.lchild);
        }
        itrResult.add(node.data);
        if (null != node.rchild) {
            inOrder(node.rchild);
        }
    }

    /**
     * 后续
     *
     * @param node
     */
    private void postOrder(Node node) {
        if (node.lchild != null) {
            postOrder(node.lchild);
        }
        if (node.rchild != null) {
            postOrder(node.rchild);
        }
        itrResult.add(node.data);
    }


    /**
     * ==================不带递归的===========
     */

    private void preOrder01(Node node) {
        Stack<Node> stack = new Stack<>();
        while (null != node || stack.size() > 0) {
            if (null != node) {
                itrResult.add(node.data);
                stack.push(node);
                node = node.lchild;
            } else {
                node = stack.pop();
                node = node.rchild;
            }
        }
    }

    private void inOrder01(Node node) {
        Stack<Node> stack = new Stack<>();
        while (node != null || stack.size() > 0) {
            if (null != node) {
                stack.push(node);
                node = node.lchild;
            } else {
                node = stack.pop();
                itrResult.add(node.data);
                node = node.rchild;
            }
        }
    }

    //todo:没写完
    private void postOrder01(Node node) {
        Stack<Node> stack = new Stack<>();
        while (node != null || stack.size() > 0) {
            if (node != null) {
                stack.push(node);
                node = node.lchild;
            } else {
                node = stack.pop();
                if(null == node.rchild){
                    itrResult.add(node.data);
                    node = null;
                }else {
                    node = node.rchild;
                }
            }
        }
    }


    public static void main(String[] args) {
        TreeItr treeItr = new TreeItr();
//        treeItr.preOrder(treeItr.root);
//        treeItr.inOrder(treeItr.root);
//        treeItr.postOrder(treeItr.root);
//        treeItr.preOrder01(treeItr.root);
        treeItr.inOrder01(treeItr.root);
//        treeItr.postOrder01(treeItr.root);
        String result = treeItr.itrResult.stream().collect(Collectors.joining(","));
        System.out.println(result);

    }

}
