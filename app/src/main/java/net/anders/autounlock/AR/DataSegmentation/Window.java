package net.anders.autounlock.AR.DataSegmentation;

import java.util.List;

/**
 * Created by Anders on 22-02-2017.
 */

public class Window {

    int size;
    List<Segment> segments;

    public Window(int size, List<Segment> segments) {
        this.size =  size;
        this.segments = segments;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
