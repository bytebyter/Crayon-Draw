package cs4962.assign1;

import android.graphics.Point;
import java.util.ArrayList;

/**
 * Custom class that represents a polygon, storing the polygon as a set of points
 * with a given color.
 */
public class Polygon{
    private int Color;
    private ArrayList<Point> Points=new ArrayList<Point>();

    /**
     * Creates an empty polygon.
     */
    public Polygon(){ };
    public Polygon(int Color){ this.Color=Color; }
    /**
     * Creates a polygon from a list of points and a given color.
     * @param pnts List of points to create a polygon from.
     * @param Color Color of the polygon.
     */
    public Polygon(ArrayList<Point> pnts, int Color){
        Points=new ArrayList<Point>(pnts);
        this.Color=Color;
    }

    /**
     * Creates a polygon with a starting point of x,y and a given color.
     * @param x Starting points x.
     * @param y Starting points y.
     * @param Color Polygons color.
     */
    public Polygon(int x, int y, int Color){
        Points.add(new Point(x,y));
        this.Color=Color;
    };

    public boolean isPolyLine(){ return (Points.size()>1); }
    /**
     * Is the current polygon a polyline?
     * @return True if the current polygon is a polyline, otherwise false.
     */
    public Point getLastPoint(){
        if (Points.size()<1) return null;
        else return Points.get(Points.size()-1);
    }
    public ArrayList<Point> getPoints() {return Points; }
    public int getColor() { return Color; }

}
