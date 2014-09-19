package cs4962.assign1;

import android.app.Application;
import android.graphics.Color;

/**
 * Created by Craig on 10/15/13.
 */
public class BrushHandler extends Application {
    private static final BrushHandler instance=new BrushHandler();
    private int color= Color.RED, brushSize=16;
    private OnColorChangeListener OCC;
    //public void getInstance()
    public void setColor(int color) {
        synchronized (this) { this.color=color;}
        if (OCC!=null) { OCC.onColorChange();}
    }
    public int getColor() { return color;}
    public void setBrushSize(int brushSize){
        synchronized (this) { this.brushSize=brushSize; }
    }
    public static BrushHandler getInstance(){ return instance; }
    public void setOnColorChangeListener(OnColorChangeListener l){ OCC=l; }
    public interface OnColorChangeListener{
        public void onColorChange();
    }
}
