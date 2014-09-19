//Program has multi-touch capability and a painters palette
//with a scrollable color list and deletion/creation capabilities.

package cs4962.assign1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Craig on 9/17/13.
 */
public class PaintView extends View {

    private Paint p=new Paint();
    private int pColor=0;
    private boolean active;

    public PaintView(Context c, int Color) {
        super(c);
        this.pColor=Color;
    }

    public boolean getActive() {return active;}
    public void setActive(boolean state) {
        active=state;
        invalidate();
    }

    protected void onDraw(Canvas c){

        float cx=(getWidth() / 2);
        float cy=(getHeight()/2);
        float radius= Math.min(cx, cy);
        if (active){
          //Invert the color to create an outline
          int scRed= Math.abs(Color.red(pColor) - 255);
          int scGreen=Math.abs(Color.green(pColor)-255);
          int scBlue= Math.abs(Color.blue(pColor) - 255);
          p.setColor(Color.rgb(Color.red(pColor) - 255,Color.green(pColor) - 255,Color.blue(pColor) - 255));
          //Draw the color.
          c.drawCircle(cx,cy,radius,p);
          p.setColor(pColor);
          c.drawCircle(cx,cy,(int)(radius * 0.8),p);
        }
        else {
            //Draw a pseudo indent.
            p.setARGB(255,72,45,30);
            c.drawCircle(cx,cy,radius,p);

            //Draw the paint views color inside the pseudo indent.
            p.setColor(pColor);
            c.drawCircle(cx,cy,(int)(radius*0.9),p);
        }
    }

    public int getColor(){ return pColor; }

    @Override
    protected void onMeasure(int msWidth, int msHeight) {
        int Width=0;
        int Height=0;
        int rWidth=MeasureSpec.getSize(msWidth);
        int rHeight=MeasureSpec.getSize(msHeight);

        //Scales width to either 10 pixels or the requested width.
        if (MeasureSpec.getMode(msWidth)==MeasureSpec.EXACTLY) Width=msWidth;
        else if (MeasureSpec.getMode(msWidth)==MeasureSpec.AT_MOST){
            Width=Math.max(getSuggestedMinimumWidth(),10);
            Width=Math.min(rWidth,Width);
        }

        //Scales the height to either 10 pixels or the requested height.
        if (MeasureSpec.getMode(msHeight)==MeasureSpec.EXACTLY) Height=MeasureSpec.getSize(msHeight);
        else if (MeasureSpec.getMode(msHeight)==MeasureSpec.AT_MOST){
            Height=Math.max(getSuggestedMinimumHeight(),10);
            Height=Math.min(rHeight,Height);
        }
        setMeasuredDimension(Width,Height);
    }
}
