package cluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnionFind {
    Map<String, String> parents = new HashMap<>();
    Map<String, Integer> representantElements = new HashMap<>();

    public void union(String p0, String p1) {
        String cp0 = find(p0);
        String cp1 = find(p1);
        if (!cp0.equals(cp1)) {
            int size0 = representantElements.get(cp0);
            int size1 = representantElements.get(cp1);
            if (size1 < size0) {
                String swap = cp0;
                cp0 = cp1;
                cp1 = swap;
            }
            representantElements.put(cp0, size0 + size1);
            parents.put(cp1, cp0);
            representantElements.put(cp1, 0);
        }
    }

    public String find(String p) {
        if (!representantElements.containsKey(p)){
            representantElements.put(p, 1);
            return p;
        }
        String result = parents.get(p);
        if (result == null) {
            result = p;
        } else {
            result = find(result);
            parents.put(p, result);
        }
        return result;
    }

    public Set<Set<String>> getPartitions() {
        Map<String, Set<String>> result = new HashMap<>();
        representantElements.forEach((key, value) -> {if (value > 0) {result.put(key, new HashSet<>(value));};});
        representantElements.forEach((key, value) -> result.get(find(key)).add(key));
        return new HashSet<>(result.values());
    }

    public static Set<Set<String>> partitions(String[][] pairs) {
        UnionFind groups = new UnionFind();
        for (String[] pair : pairs) {
            if (pair.length > 1) {
                String first = pair[0];
                for (int i = 1; i < pair.length; i++) {
                    groups.union(first, pair[i]);
                }
            }
        }
        return groups.getPartitions();
    }
    
    public static Set<Set<String>> partitions(Map<String, Set<String>> pairs) {
        UnionFind groups = new UnionFind();
        //for (String[] pair : pairs) {
        for (Map.Entry<String, Set<String>> entry : pairs.entrySet()) {
            String first = entry.getKey();
            for (String pair : entry.getValue()) {
                groups.union(first, pair);
            }
        }
        return groups.getPartitions();
    }

    public static void main(String[] args) {
    	String[][] elements = {{"A","C"},{"B","C"},{"C","D"},{"E","F"}};
    	
    	Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
    	
    	graph.put("A", new HashSet<String>());
    	graph.get("A").add("C");
    	graph.put("B", new HashSet<String>());
    	graph.get("B").add("C");
    	graph.put("C", new HashSet<String>());
    	graph.get("C").add("D");
    	graph.put("E", new HashSet<String>());
    	graph.get("E").add("F");
    	
    	//String[][] elements2 = getArrayFromHash(graph);
    	
    	System.out.println("1");
    	for (Set<String> partition : partitions(elements)) {
            System.out.println(partition);
        }
    	
    	System.out.println("2");
    	for (Set<String> partition : partitions(graph)) {
            System.out.println(partition);
        }
    }
}