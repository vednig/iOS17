package foundation.e.blisslauncher.core.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiHashMap<K, V> extends HashMap<K, ArrayList<V>> {

    public MultiHashMap() {
    }

    public MultiHashMap(int size) {
        super(size);
    }

    public void addToList(K key, V value) {
        ArrayList<V> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            list.add(value);
            put(key, list);
        } else {
            list.add(value);
        }
    }

    @Override
    public MultiHashMap<K, V> clone() {
        MultiHashMap<K, V> map = new MultiHashMap<>(size());
        for (Entry<K, ArrayList<V>> entry : entrySet()) {
            map.put(entry.getKey(), new ArrayList<V>(entry.getValue()));
        }
        return map;
    }
}
