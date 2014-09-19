//Program has multi-touch capability and a painters palette
//with a scrollable color list and deletion/creation capabilities.

package cs4962.assign1;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

/**
 * Paint Area view that stores a user draw picture in vector form.
 * Created by Craig on 9/20/13.
 */
public class PaintAreaView extends View {
    private boolean PaintMode=true;
    private int PointLimit=0;

    private Bitmap bmpCrayonTexture;
    private Bitmap bmpPaintArea;
    private BitmapShader crayonShader;
    private Canvas cPaintArea;
    private Paint pntBrush=new Paint();
    private BrushHandler brushHandler=BrushHandler.getInstance();

    private boolean needsRotation;
    private int oldOrientation, newOrientation;
    private int Orientation;

    private Polygon [] pntsUncommited = new Polygon [25];
    private ArrayList<Polygon> pntsCommited=new ArrayList<Polygon>();

    public PaintAreaView(Context c){
        super(c);
        //Create a crayon shader texture.
        //In reality, the best way to do this would this would involve a randomly rotating texture
        //group to create a non-repeating pattern, however the current method looks fairly good.
        bmpCrayonTexture=BitmapFactory.decodeResource(getResources(),R.drawable.crayon3);
        crayonShader= new BitmapShader(bmpCrayonTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        //Setup the brush stroke width,color and set it's shader.
        pntBrush.setStrokeWidth(16);
        pntBrush.setShader(crayonShader);
        setColor(brushHandler.getColor());
    }


    public void setImagePoints(ArrayList<Polygon> pGons){ pntsCommited=pGons; }
    public void resumeRedraw(ArrayList<Polygon> Points){ setImagePoints(Points); }
    public void forcedRedraw(ArrayList<Polygon> Points) {
        setImagePoints(Points);
        onResumeDraw();
    }
    public void scalePoints(int oldOrientation,int newOrientation){
        this.oldOrientation=oldOrientation;
        this.newOrientation=newOrientation;
        needsRotation=true;
    }
    private void onResumeDraw(){
        if (pntsCommited!=null){
            cPaintArea.drawARGB(255,255,255,255);
            for(Polygon p: pntsCommited){
                if (p.isPolyLine()){
                    setColor(p.getColor());
                    cPaintArea.drawCircle(p.getPoints().get(0).x,p.getPoints().get(0).y,pntBrush.getStrokeWidth()/2,pntBrush);
                    for(int i=1; i<p.getPoints().size(); i++){
                        Point prev= p.getPoints().get(i-1);
                        Point current= p.getPoints().get(i);
                        cPaintArea.drawLine(prev.x,prev.y,current.x,current.y,pntBrush);
                        cPaintArea.drawCircle(current.x,current.y,pntBrush.getStrokeWidth()/2,pntBrush);
                    }
                }
                else{
                    setColor(p.getColor());
                    cPaintArea.drawCircle(p.getPoints().get(0).x,p.getPoints().get(0).y,pntBrush.getStrokeWidth()/2,pntBrush);
                }
            }
        }
    }


    public void drawPolygons(int PointLimit){
        this.PointLimit=PointLimit;
        invalidate();
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        if(r-l >0 && b-t >0) {
            if (bmpPaintArea==null){
                int width=r-l;
                int height= b-t;
                if(width<height) {
                    bmpPaintArea = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                    Orientation= Configuration.ORIENTATION_PORTRAIT;
                }
                else{
                    bmpPaintArea = Bitmap.createBitmap(height,width,Bitmap.Config.ARGB_8888);
                    Orientation= Configuration.ORIENTATION_LANDSCAPE;
                }
                cPaintArea=new Canvas(bmpPaintArea);

                if(pntsCommited!=null){
                    onResumeDraw();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int msWidth, int msHeight) {
        //super.onMeasure(msWidth,msHeight);
        int Width=0;
        int Height=0;
        int rWidth=MeasureSpec.getSize(msWidth);    //Get the request width.
        int rHeight=MeasureSpec.getSize(msHeight);  //...height.

        //Scale to max width.
        if (MeasureSpec.getMode(msWidth)==MeasureSpec.EXACTLY) Width=rWidth;
        else if (MeasureSpec.getMode(msWidth)==MeasureSpec.AT_MOST){
            Width=Math.max(getSuggestedMinimumWidth(),rWidth);
        }

        //Scale to max width
        if (MeasureSpec.getMode(msHeight)==MeasureSpec.EXACTLY) Height=rHeight;
        else if (MeasureSpec.getMode(msHeight)==MeasureSpec.AT_MOST){
            Height=Math.max(getSuggestedMinimumHeight(),rHeight);
        }

        setMeasuredDimension(Width,Height);
    }

    public ArrayList<Polygon> getImagePoints(){ return pntsCommited; }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (PaintMode){
            int x,y;
            int PointerId,PointerIndex;
            Point prevPoint;
            //Handle Touch events.
            switch(e.getActionMasked()){
                case MotionEvent.ACTION_POINTER_DOWN:                       //Check for subsequent Pointer down events.
                case MotionEvent.ACTION_DOWN:{                              //Check for first finger down.
                    setColor(brushHandler.getColor());
                    PointerIndex = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    PointerId= e.getPointerId(PointerIndex);
                    x= (int) e.getX(PointerIndex);
                    y= (int) e.getY(PointerIndex);
                    if(Orientation==Configuration.ORIENTATION_LANDSCAPE){
                        int temp=x;
                        x=y;
                        y=temp;
                        //x controls the y coordinate
                        //y controls the x coordinate
                        x=bmpPaintArea.getWidth()-x;
                    }
                    pntsUncommited[PointerId]= new Polygon(x,y,brushHandler.getColor());
                    cPaintArea.drawCircle(x,y,pntBrush.getStrokeWidth()/2,pntBrush);
                    break;
                }
                case MotionEvent.ACTION_MOVE:{                              //A finger move event has occurred.
                    //Cycle through all pointer indexes
                    for(int i=0; i < e.getPointerCount(); i++){
                        PointerId=e.getPointerId(i);                        //Get the pointer Id for that index.
                        if(PointerId< pntsUncommited.length){
                            Point lastPoint= pntsUncommited[PointerId].getLastPoint();
                            x= (int)e.getX(e.findPointerIndex(PointerId));      //Get the x coordinate for the current pointer id.
                            y= (int)e.getY(e.findPointerIndex(PointerId));      //Get the y coordinate for the current pointer id.
                            if(lastPoint.x != x & lastPoint.y!=y){              //Attempt to minimize point addition

                                if(Orientation==Configuration.ORIENTATION_LANDSCAPE){
                                    int temp=x;
                                    x=y;
                                    y=temp;
                                    x=bmpPaintArea.getWidth()-x;
                                }

                                x=Math.max(x,0);                                //Enforce x and y boundaries.
                                x=Math.min(x,bmpPaintArea.getWidth());
                                y=Math.max(y,0);
                                y=Math.min(y,bmpPaintArea.getHeight());


                                prevPoint=pntsUncommited[PointerId].getPoints().get(pntsUncommited[PointerId].getPoints().size()-1);
                                cPaintArea.drawLine(prevPoint.x,prevPoint.y,x,y,pntBrush);
                                cPaintArea.drawCircle(x,y,pntBrush.getStrokeWidth()/2,pntBrush);

                                pntsUncommited[PointerId].getPoints().add(new Point(x,y));       //Add a point to the Associated polygon.
                                if(pntsUncommited[PointerId].getPoints().size()>100){            //Commit every 100 points to maintain a constant speed.
                                    pntsCommited.add(pntsUncommited[PointerId]);
                                    pntsUncommited[PointerId]=new Polygon(x,y,pntsUncommited[PointerId].getColor());//Start a new polygon.

                                }
                            }
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP:                         //A finger has been lifted.
                case MotionEvent.ACTION_UP:{
                    PointerIndex = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    PointerId= e.getPointerId(PointerIndex);

                    pntsCommited.add(pntsUncommited[PointerId]);
                    pntsUncommited[PointerId]=null;
                    break;
                }
            }
            invalidate();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas c) {
        Paint p=new Paint();
        if(!PaintMode){
            cPaintArea.drawARGB(255,255,255,255);
            int PointCount=0;
            if (pntsCommited!=null){
                if(pntsCommited.size()!=0){
                    for(Polygon pgon : pntsCommited){
                        if (pgon.isPolyLine()){
                            PointCount++;
                            if (PointCount>PointLimit) break;
                            setColor(pgon.getColor());
                            cPaintArea.drawCircle(pgon.getPoints().get(0).x,pgon.getPoints().get(0).y,pntBrush.getStrokeWidth()/2,pntBrush);
                            for(int i=1; i<pgon.getPoints().size();i++){
                                PointCount++;
                                if (PointCount>PointLimit) break;
                                Point prev=pgon.getPoints().get(i-1);
                                Point current=pgon.getPoints().get(i);
                                cPaintArea.drawLine(prev.x,prev.y,current.x,current.y,pntBrush);
                                cPaintArea.drawCircle(current.x,current.y,pntBrush.getStrokeWidth()/2,pntBrush);

                            }
                            if (PointCount>PointLimit) break;
                        }
                        else{
                            if (PointCount>PointLimit) break;
                            setColor(pgon.getColor());
                            cPaintArea.drawCircle(pgon.getPoints().get(0).x,pgon.getPoints().get(0).y,pntBrush.getStrokeWidth()/2,pntBrush);
                        }
                    }
                }
            }
        }
        c.drawARGB(255,255,255,255);
        p.setColor(Color.WHITE);
        if(bmpPaintArea!=null) {
            int angle=0;
            if (Orientation==Configuration.ORIENTATION_LANDSCAPE) angle=-90;
            else angle=0;

            Matrix matrix=new Matrix();
            matrix.reset();
            matrix.postTranslate(-bmpPaintArea.getWidth() / 2, -bmpPaintArea.getHeight() / 2); // Centers image
            matrix.postRotate(angle);
            matrix.postTranslate(c.getWidth()/2, c.getHeight()/2);
            c.drawBitmap(bmpPaintArea, matrix, null);

            //c.drawBitmap(bmpPaintArea,0,0,p);
        }


    }
    /**
     * Sets the draw color to the given input color.
     * @param Color Desired drawing color.
     */
    public void setColor(int Color){
        pntBrush.setColor(Color);
        pntBrush.setColorFilter(new LightingColorFilter(android.graphics.Color.WHITE, Color));

    }

    public void setModetoPaint(){ PaintMode=true; }
    public void setModetoWatch(){ PaintMode=false; }
}
