package cs4962.assign1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Craig on 10/14/13.
 */
public class ScrubView extends View {
    private int Max=100;
    private int Value=0;
    private onChangeListener Changed;
    private onScrollBeginListener ScrollBegins;
    private onScrollEndListener ScrollEnds;
    private onScrollListener Scrolling;

    public ScrubView(Context c){
        super(c);

    }


    public void setMax(int Max){
        this.Max=Max;
        invalidate();
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
        setMeasuredDimension(Width,Height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p=new Paint();
        if(getWidth()>getHeight()){
            p.setColor(Color.BLACK);
            RectF bar= new RectF(getWidth()/10,(getHeight()-(getHeight()/4))/2,getWidth()-(getWidth()/10),((getHeight()-(getHeight()/4))/2) + (getHeight()/4));
            float AdjHeight= 2;
            float AdjWidth= 2;
            canvas.drawRoundRect(bar,4f,4f,p);

            bar.left=bar.left + AdjWidth;
            bar.right=bar.right - AdjWidth;
            bar.top=bar.top + AdjHeight;
            bar.bottom=bar.bottom - AdjHeight;
            p.setColor(Color.DKGRAY);
            canvas.drawRoundRect(bar,2f,2f,p);

            bar.left=bar.left + AdjWidth;
            bar.right=bar.right - AdjWidth;
            bar.top=bar.top + AdjHeight;
            bar.bottom=bar.bottom - AdjHeight;
            p.setColor(Color.GRAY);
            canvas.drawRoundRect(bar,1f,1f,p);

            if(Max!=0){
                float leftBound= getWidth()/10;
                float rightBound=(getWidth() - (getWidth()/10));
                float height= (getHeight()/2);
                float top= (getHeight()-height)/2;
                float bottom=top + height;
                float width=(getHeight()/8);
                float left=  (leftBound  +  ((rightBound-leftBound)* ((float)Value / (float)Max)))-(width/2);
                float right= left+width;

                p.setColor(Color.GREEN);
                canvas.drawRect(bar.left + 1, bar.top + 1, left, bar.bottom - 1, p);

                p.setColor(Color.RED);
                canvas.drawRect(left + 1, bar.top + 1, bar.right-1, bar.bottom - 1, p);

                p.setColor(Color.BLACK);
                Rect slider=new Rect((int)left,(int)top,(int)right,(int)bottom);
                canvas.drawRect(slider,p);

                slider=new Rect((int)left+1,(int)top+1,(int)right-1,(int)bottom-1);
                p.setColor(Color.LTGRAY);
                canvas.drawRect(slider,p);

                slider=new Rect((int)left+3,(int)top+3,(int)right-3,(int)bottom-3);
                p.setColor(Color.GRAY);
                canvas.drawRect(slider,p);
            }
            else{
                p.setColor(Color.RED);
                canvas.drawRect(bar.left+ 1, bar.top + 1, bar.right-1, bar.bottom - 1, p);
            }
        }
        else{
            p.setColor(Color.BLACK);
            //RectF bar= new RectF(getWidth()/10,(getHeight()-(getHeight()/4))/2,getWidth()-(getWidth()/10),((getHeight()-(getHeight()/4))/2) + (getHeight()/4));
            RectF bar= new RectF((getWidth()-(getWidth()/4))/2, getHeight()/10,((getWidth()-(getWidth()/4))/2) + (getWidth()/4),getHeight()-(getHeight()/10));
            float AdjHeight= 2;
            float AdjWidth= 2;
            canvas.drawRoundRect(bar,4f,4f,p);

            bar.left=bar.left + AdjWidth;
            bar.right=bar.right - AdjWidth;
            bar.top=bar.top + AdjHeight;
            bar.bottom=bar.bottom - AdjHeight;
            p.setColor(Color.DKGRAY);
            canvas.drawRoundRect(bar,2f,2f,p);

            bar.left=bar.left + AdjWidth;
            bar.right=bar.right - AdjWidth;
            bar.top=bar.top + AdjHeight;
            bar.bottom=bar.bottom - AdjHeight;
            p.setColor(Color.GRAY);
            canvas.drawRoundRect(bar,1f,1f,p);

            if(Max!=0){
                float topBound= getHeight()/10;
                float bottomBound=(getHeight() - (getHeight()/10));
                float width= (getWidth()/2);
                float left= (getWidth()/4);
                float right= (getWidth()-getWidth()/4);
                float height=(getHeight()/8);
                float top=  (topBound  +  ((bottomBound-topBound)* ((float)Value / (float)Max)))-(height/2);
                float bottom= top+height;

                p.setColor(Color.GREEN);
                canvas.drawRect(bar.left + 1, bar.top+1, bar.right-1, top, p);

                p.setColor(Color.RED);
                canvas.drawRect(bar.left+1, bottom, bar.right-1, bar.bottom - 1, p);


                p.setColor(Color.BLACK);
                Rect slider=new Rect((int)left,(int)top,(int)right,(int)bottom);
                canvas.drawRect(slider,p);

                slider=new Rect((int)left+1,(int)top+1,(int)right-1,(int)bottom-1);
                p.setColor(Color.LTGRAY);
                canvas.drawRect(slider,p);

                slider=new Rect((int)left+3,(int)top+3,(int)right-3,(int)bottom-3);
                p.setColor(Color.GRAY);
                canvas.drawRect(slider,p);

            }
            else{
                p.setColor(Color.RED);
                canvas.drawRect(bar.left+ 1, bar.top + 1, bar.right-1, bar.bottom - 1, p);
            }
        }
    }

    public void setValue(int Value){
        setSlider(Value);
        if (Changed!=null) Changed.onChange();
        invalidate();
    }

    public int getValue(){ return Value;}
    private void setSlider(int Value){
        if (Value>this.Max) Value=this.Max;
        else if (Value< 0) Value=0;
        this.Value=Value;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int value=0;

        if (getWidth()>getHeight()){
            float leftBound= getWidth()/10;
            float rightBound=(getWidth() - (getWidth()/10));
            value=(int) (Max * (event.getX() -leftBound) /(rightBound-leftBound));
        }
        else{
            float topBound= getHeight()/10;
            float bottomBound=(getHeight() - (getHeight()/10));
            value=(int) (Max * (event.getY() -topBound) /(bottomBound- topBound));
        }
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_POINTER_DOWN:                       //Check for subsequent Pointer down events.
            case MotionEvent.ACTION_DOWN:{                              //Check for first finger down.
                if(ScrollBegins!=null) ScrollBegins.onScrollBegin();
                if (value!=this.Value & Changed!=null) Changed.onChange();
            }
            case MotionEvent.ACTION_MOVE:{                              //A finger move event has occurred.
                if(Scrolling!=null) Scrolling.onScroll();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:                         //A finger has been lifted.
            case MotionEvent.ACTION_UP:{
                if(ScrollEnds!=null) ScrollEnds.onScrollEnd();
                if (value!=this.Value & Changed!=null) Changed.onChange();
                break;
            }
        }
        setSlider(value);
        this.invalidate();
        return true;
    }

    public interface onScrollBeginListener{
        public void onScrollBegin();
    }
    public interface onScrollListener{
        public void onScroll();
    }
    public interface onScrollEndListener{
        public void onScrollEnd();
    }
    public interface onChangeListener{
        public void onChange();
    }
    public void setOnScrollBeginListener(onScrollBeginListener l) { ScrollBegins=l; }
    public void setOnScrollListener (onScrollListener l) { Scrolling=l; }
    public void setOnScrollEndListener(onScrollEndListener l) { ScrollEnds=l; }
    public void setOnChangeListener (onChangeListener l) { Changed=l; }
}
