package cs4962.assign1;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Craig on 10/13/13.
 */
public class LineGridLayout extends ViewGroup {
    private ArrayList<Cell> gridCells= new ArrayList<Cell>();

    private class Cell {
        private View view;
        private boolean scaleBound;
        private float scaleSize;
        public Cell(View view,boolean ScaleToBoundary){
            this.view=view;
            this.scaleBound=ScaleToBoundary;
            this.scaleSize=0;
        }
        public Cell(View view, boolean ScaleToBoundary, float scaleSize){
            this.view=view;
            this.scaleBound=ScaleToBoundary;
            this.scaleSize=scaleSize;
        }
        public void setSize(float size){ this.scaleSize=size; }
        public boolean isScaleToBounds(){ return this.scaleBound; }
        public float getCellSize(){return this.scaleSize; }
        public View getCellView() { return this.view; }
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        gridCells.add(new Cell(child,true));
        for (Cell c: gridCells){
            c.setSize(100/(gridCells.size()));
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        gridCells=new ArrayList<Cell>();
    }

    public void addViewAdj(View child, float size,boolean ScaleToMinBound){
        super.addView(child);
        for (Cell c: gridCells){ c.setSize((100-size)/gridCells.size()); }
        gridCells.add(new Cell(child,ScaleToMinBound,size));
    }
    public void addViewNoAdj(View child, float size,boolean ScaleToMinBound){
        super.addView(child);
        gridCells.add(new Cell(child,ScaleToMinBound,size));
    }
    public void NormalizeCells (){
        float totalSize=0;
        for (Cell c: gridCells){ totalSize=totalSize+c.getCellSize(); }
        for (Cell c: gridCells){ c.setSize(100 * (c.getCellSize()/totalSize)); }
    }


    public LineGridLayout(Context context){
        super(context);
    }

    @Override
    protected void onMeasure(int msWidth, int msHeight) {
        int Width=0;
        int Height=0;
        int rWidth=MeasureSpec.getSize(msWidth);                                //Get the requested width.
        int rHeight=MeasureSpec.getSize(msHeight);                              //Get the requested height.

        //Scale to max width.
        if (MeasureSpec.getMode(msWidth)==MeasureSpec.EXACTLY) Width=msWidth;
        else if (MeasureSpec.getMode(msWidth)==MeasureSpec.AT_MOST){
            Width=Math.max(getSuggestedMinimumWidth(),rWidth);
        }

        //Scale to max height.
        if (MeasureSpec.getMode(msHeight)==MeasureSpec.EXACTLY) Height=MeasureSpec.getSize(msHeight);
        else if (MeasureSpec.getMode(msHeight)==MeasureSpec.AT_MOST){
            Height=Math.max(getSuggestedMinimumHeight(),rHeight);
        }
        for(int i=0; i<getChildCount();i++){
            getChildAt(i).measure(Width,Height);
        }
        setMeasuredDimension(Width,Height);
    }


    @Override
    protected void onLayout(boolean Changed, int Left, int Top, int Right , int Bottom) {
        int left=0 ,right=0 ,top=0 ,bottom=0, size=0, pos=0;

        //setup grid automatically based on orientation.
        if (getWidth()< getHeight()){
            //Grid is setup vertically.
            for(Cell c: gridCells){
                if (c.isScaleToBounds()){
                    left=0;
                    right=getWidth();
                    bottom=bottom + (int)((c.getCellSize()/100) *getHeight());
                    size=Math.min((int)((c.getCellSize()/100) *getHeight()),getWidth());
                    pos=((left+right)-size)/2;
                    c.getCellView().layout(pos,top,pos + size,top + size);

                    top=bottom;
                }
                else{
                    left=0;
                    right=getWidth();
                    bottom=bottom + (int)((c.getCellSize()/100) *getHeight());
                    c.getCellView().layout(left,top,right,bottom);
                    top=bottom;
                    //MeasureSpec.
                    //measureChild(c.getCellView(),MeasureSpec.UNSPECIFIED,MeasureSpec.UNSPECIFIED);
                }
            }
        }
        else{
            //Grid is setup horizontally.
            for(Cell c: gridCells){
                if (c.isScaleToBounds()){
                    top=0;
                    bottom=getHeight();
                    right=right + (int)((c.getCellSize()/100) *getWidth());
                    size=Math.min((int)((c.getCellSize()/100) *getWidth()),getHeight());
                    pos=((top+bottom)-size)/2;
                    c.getCellView().layout(left,pos ,left + size,pos + size);
                    left=right;
                }
                else{
                    top=0;
                    bottom=getHeight();
                    right=right + (int)((c.getCellSize()/100) * getWidth());
                    c.getCellView().layout(left,top,right,bottom);
                    left=right;
                }
            }
        }
    }
}
