# HashMap

* loadFactor
* threshold
* capacity
* size

## 什么时候触发resize

1. 往Map中让另外一个Map。如果table不为空&&那个Map的size> threshold的时候进行扩容
 ```java
   final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
           int s = m.size();
           if (s > 0) {
               if (table == null) { // pre-size
                   float ft = ((float)s / loadFactor) + 1.0F;
                   int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                            (int)ft : MAXIMUM_CAPACITY);
                   if (t > threshold)
                       threshold = tableSizeFor(t);
               }
               else if (s > threshold)
                   resize();
               for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                   K key = e.getKey();
                   V value = e.getValue();
                   putVal(hash(key), key, value, false, evict);
               }
           }
       }
 ```

2. HashMap的红黑树判断，如果一条链上挂的节点大于8，尝试转化成红黑树，这个时候有个判断

   ```java
   final void treeifyBin(Node<K,V>[] tab, int hash) {
           int n, index; Node<K,V> e;
     			//如果当前的tab的长度小于MIN_TREEIFY_CAPACITY64的话，
     			//可能是因为hash冲突明显导致的，所以优先扩容
           if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
               resize();
           else if ((e = tab[index = (n - 1) & hash]) != null) {
               TreeNode<K,V> hd = null, tl = null;
               do {
                   TreeNode<K,V> p = replacementTreeNode(e, null);
                   if (tl == null)
                       hd = p;
                   else {
                       p.prev = tl;
                       tl.next = p;
                   }
                   tl = p;
               } while ((e = e.next) != null);
               if ((tab[index] = hd) != null)
                   hd.treeify(tab);
           }
       }
   ```

3. 
