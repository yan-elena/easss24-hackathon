package web;

import java.util.List;
import java.util.LinkedList;

public class Path {
    private List<String> path;
    
    public Path(String uri) {
        path = new LinkedList<>();
        for (String segment: uri.split("/")) {
            if (segment.length() > 0) path.add(segment);
        }
    }
    
    public boolean pathEnd() {
        return path.size() == 1;
    }
    
    public String prefix() {
        return path.remove(0);
    }
    
    public int length() {
        return path.size();
    }
    
    public String toString() {
        return path.toString();
    }
}