//Program has multi-touch capability and a painters palette
//with a scrollable color list and deletion/creation capabilities.

package cs4962.assign1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * A palette view is a view that allows for the creation, selection, and deletion of colors.
 * Created by Craig on 9/17/13.
 */
public class PaletteView extends ViewGroup {
    private Context MainContext;
    //Final variables for palette view modes.
    private final int PaintMode=0;
    private final int MixMode=1;

    private ArrayList<PaintView> pntList=new ArrayList<PaintView>();    //Holds a list of paint views.
    private Paint p=new Paint();                                //Paint object to change drawing colors.
    private BrushHandler brushHandler=BrushHandler.getInstance();

    private int Mode=PaintMode;                                 //Holds the current palette view operation mode.
    private int sColorIndex =-1;                                //Holds the currently selected color index.
    private int colorOffset=0;                                  //Holds the current color offset used for scrolling of colors.
    private int maxColorsOnPalette=10;                          //Holds the maximum amount of colors that can be displayed on palette at one time.

    //Image views used for various palette operations.
    private ImageView mixButton=new ImageView(getContext());    //Mix color button.
    private ImageView deleteButton=new ImageView(getContext()); //Delete color button.
    private ImageView scLeftButton=new ImageView(getContext()); //Scroll left button.
    private ImageView scRightButton=new ImageView(getContext());//Scroll right button.

    //Holds various pictures used for palette operation buttons.
    private Bitmap bmpMix=BitmapFactory.decodeResource(getResources(),R.drawable.mixhd);
    private Bitmap bmpMixC=BitmapFactory.decodeResource(getResources(),R.drawable.mixhdc);
    private Bitmap bmpDel= BitmapFactory.decodeResource(getResources(),R.drawable.deletehd);
    private Bitmap bmpMixLeft= BitmapFactory.decodeResource(getResources(),R.drawable.mixlefthd);
    private Bitmap bmpMixRight= BitmapFactory.decodeResource(getResources(),R.drawable.mixrighthd);

    //Paint view click event.
    private OnClickListener pntClick = new OnClickListener() {
        public void onClick(View v) {
            PaintView pnt= (PaintView) v;
            if (Mode==PaintMode){                                                                   //If the current palette mode is selection mode.
                setPaint(pnt);                                                                      //Set the selection to the clicked paint view.
                if (pntList.size()> 3 && sColorIndex>=3) deleteButton.setVisibility(View.VISIBLE);  //If the currently selected color is deletable add the delete button.
                else deleteButton.setVisibility(View.INVISIBLE);                                    //Otherwise hide the delete button.
            }
            else{                                                                                   //If the current palette mode is mix mode.

                //Get the color average.
                //HSV Colorspace. Seems to be alot more accurate the rgb.
                //Also tried cmyk, but the mixing results do not look accurate to the human eye.
                float [] hsvA=new float [3];
                float [] hsvB=new float [3];
                Color.colorToHSV(pnt.getColor(),hsvA);
                Color.colorToHSV(pntList.get(sColorIndex).getColor(),hsvB);
                for(int i=0 ; i<hsvA.length;i++){ hsvB[i]=(hsvA[i]+hsvB[i])/2; }

                // /Add the mixed color to the palette.
                addColor(new PaintView(getContext(),Color.HSVToColor(hsvB)));
                Mode=PaintMode;                                                 //Change the mode back to color selection mode.
                mixButton.setImageBitmap(bmpMix);                               //Change the bitmap for the mixer button back to normal.
                setScrollStates();                                              //Display scroll icons as necessary.
            }
        }
    };

    //On click listener for the mixer button.
    private OnClickListener mixClick = new OnClickListener() {
        public void onClick(View v) {
            if (Mode!=MixMode) {
                Mode=MixMode;                                                                               //Change the mode to color mixing mode.
                mixButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.mixhdc)); //Set the mixer button to inverted color image.
            }

        }
    };

    //delete button click listener.
    private OnClickListener delClick = new OnClickListener() {
        public void onClick(View v) {
            if(sColorIndex>=3){                                                     //If we have not selected a color from the original color palette.
                removeColor(pntList.get(sColorIndex));                              //Remove the color from the palette.
                if (pntList.size()<= 3 || (sColorIndex-1) < 3) deleteButton.setVisibility(View.INVISIBLE); //Hide the delete icon if necessary.
                setPaint(sColorIndex-1);                                            //Set the paint to right before the deleted paint.

                scrollAsNeeded();                                                   //Shift/Scroll the palette views color as necessary.
                setScrollStates();                                                  //Hide the scroll buttons if necessary.
            }
        }
    };

    /***
     * Create a palette view and set it's color changed listener.
     * @param c Context
     */
    public PaletteView(Context c){
        super(c);
        setWillNotDraw(false);                                                      //allow for dispatchDraw to be called (onDraw)

        MainContext=c;
        //Set initial images for delete color and mix color buttons.
        scLeftButton.setImageBitmap(bmpMixLeft);
        scRightButton.setImageBitmap(bmpMixRight);
        mixButton.setImageBitmap(bmpMix);
        deleteButton.setImageBitmap(bmpDel);

        //Add paint views and buttons to the current view.
        addColor(new PaintView( getContext(), Color.RED));
        addColor(new PaintView( getContext(), Color.GREEN));
        addColor(new PaintView( getContext(), Color.BLUE));

        //Add buttons to the current view.
        addView(mixButton);
        addView(deleteButton);
        addView(scLeftButton);
        addView(scRightButton);

        //Setup click listeners for the mix button.
        mixButton.setOnClickListener(mixClick);
        deleteButton.setOnClickListener(delClick);
        deleteButton.setVisibility(View.INVISIBLE);

        //On click listener for scroll left button.
        scLeftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) { scrollLeft(); }
        });

        //On click listener for scroll right button.
        scRightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) { scrollRight();}
        });

        sColorIndex =0;                             //Set the selected color index to 0;
        setPaint(sColorIndex);                      //Set the currently selected paint to the color index.
        setScrollStates();                          //Setup the scroll buttons visibility.
    }

    /**
     * Equivalent of on draw method for a view group.
     * @param c Canvas.
     */
    @Override
    protected void dispatchDraw(Canvas c) {
        c.drawARGB(255,0,0,0);                                      //Draw a black background.
        int radius= Math.min((getWidth()/2), (getHeight()/2));      //calculate the radius for the painters palette.
        int cx= getWidth()/2;                                       //calculate the center x coordinate.
        int cy=getHeight()/2;                                       //...center y coordinate.

        //Draw the base of the painters palette.
        p.setARGB(255,136,85,57);
        c.drawCircle(cx,cy,radius,p);

        //Draw the thumb hole and make the base look more like a painters palette.
        p.setARGB(255,0,0,0);
        c.drawCircle((int)(cx-(radius * 0.85)),cy,(int)(radius*0.4),p);
        c.drawCircle((int)(cx-(radius *0.3)),cy,(int)(radius*0.1),p);
        super.dispatchDraw(c);                                      //Call to allow for transparencies etc...
    }

    @Override
    protected void onMeasure(int msWidth, int msHeight) {
        int Width=0;
        int Height=0;
        int rWidth=MeasureSpec.getSize(msWidth);                                //Get the requested width.
        int rHeight=MeasureSpec.getSize(msHeight);                              //Get the requested height.

        //Scale width to 1/3 or force to requested width if necessary.
        if (MeasureSpec.getMode(msWidth)==MeasureSpec.EXACTLY) Width=msWidth;
        else if (MeasureSpec.getMode(msWidth)==MeasureSpec.AT_MOST){
            Width=Math.max(getSuggestedMinimumWidth(),rWidth);
        }

        //Scale height to 1/3 or force to requested height if necessary.
        if (MeasureSpec.getMode(msHeight)==MeasureSpec.EXACTLY) Height=MeasureSpec.getSize(msHeight);
        else if (MeasureSpec.getMode(msHeight)==MeasureSpec.AT_MOST){
            Height=Math.max(getSuggestedMinimumHeight(),rHeight);
        }
        setMeasuredDimension(Width,Height);
    }

    protected void onLayout(boolean changed, int left, int right, int top, int bottom){
        int radius=Math.min(getWidth()/2, getHeight()/2);           //Calculate the radius.
        int cPerRow=5;
        int Rows= (getChildCount()/cPerRow);                        //5 children per row. Calculate the number of rows
        float centerX,centerY;
        int childWidth=(radius/3);                                  // Set the size of the buttons to be 1/5th size of painters palette.
        int childHeight=(radius/3);                                 //...
        float AngleInc=(float)((2*Math.PI) -(Math.PI/3.4)) /cPerRow;//5 children per row. PI/3.4 is unusable.
        float  zeroOffset= (float)(Math.PI-(Math.PI/3.4));          //Set the zero offset.
        Rect dRect= new Rect();                                     //Destination rectangle.

        //Calculate boundaries for scrolling purposes.
        int rBound=Math.min( pntList.size() ,colorOffset + maxColorsOnPalette);
        int MaxOffset=Math.max(pntList.size() - maxColorsOnPalette,0);
        if (colorOffset>MaxOffset) colorOffset=MaxOffset;

        for(int i=colorOffset ; i< rBound;i++){
            int Row=(i-colorOffset)/cPerRow;                                           //Calculate what row we are currently drawing to.
            int childCol=(i-colorOffset)-(Row*cPerRow);                                //Calculate the column by subtracting the current child index by the row size * current row number
            float childAngle= zeroOffset - (AngleInc * childCol);                       // Calculate what the angle of the child is relative to the zero offset.
            if (childAngle<0) childAngle=(float)((2*Math.PI) + childAngle);             //Handle angle <0 situations.

            View child= pntList.get(i);                              //Get the current child.
            centerX= (float)((getWidth()/2) + (radius - ((Row +1)*childWidth ) ) * (FloatMath.cos(childAngle)));   //Calculate left position relative to row, offset, and angle
            centerY= (float)((getHeight()/2) - (radius - ((Row+1) *childHeight))* FloatMath.sin(childAngle));       //Calculate right position relative to row, offset, and angle.

            dRect.left=(int)(centerX-(childWidth/2));
            dRect.top=(int)(centerY-(childHeight/2));
            dRect.right=dRect.left + childWidth;
            dRect.bottom=dRect.top + childHeight;

            child.layout(dRect.left ,dRect.top,dRect.right,dRect.bottom);
        }

        //Layout the scroll buttons and mixer/delete buttons.
        scLeftButton.layout(0,0,(int)(getWidth()/3.5),(int)(getHeight()/3.5));
        scRightButton.layout((int)(getWidth()-(getWidth()/3.5)),0,getWidth(),(int)(getHeight()/3.5));
        mixButton.layout(0,(int) (getHeight()-(getHeight()/3.5)),(int)(getWidth()/3.5),getHeight());
        deleteButton.layout((int)(getWidth()-(getWidth()/3.5)),(int)(getHeight()-(getHeight()/3.5)),getWidth(),getHeight());
    }

    /**
     * Sets the current color using the given index.
     * @param index Index to set the color to.
     */
    private void setPaint(int index){
        sColorIndex=index;
        brushHandler.setColor(pntList.get(sColorIndex).getColor());
        for (int i=0; i<pntList.size(); i++){
            if(i != index) pntList.get(i).setActive(false);         //De-select if not the input index.
            else {
                pntList.get(i).setActive(true);                     //Otherwise select.
            }
        }
    }

    /**
     * Sets the current color using the given paint view.
     * @param pnt Paint view selected.
     */
    private void setPaint(PaintView pnt){
        for (int i=0; i<pntList.size();i++){
            if (!pnt.equals(pntList.get(i))) pntList.get(i).setActive(false);   //De-select if not input paint view.
            else{
                pntList.get(i).setActive(true);                                 //Otherwise select.
                sColorIndex=i;
                brushHandler.setColor(pntList.get(sColorIndex).getColor());
            }
        }
    }

    /**
     * Update the color palette. (Not very efficient)
     */
    private void updateColors(){
        for(PaintView pntV: pntList)  removeView(pntV);
        for(int i=colorOffset;i<Math.min(pntList.size(),colorOffset+maxColorsOnPalette) ;i++) addView(pntList.get(i));
    }

    /**
     * Dynamically scroll if needed.
     */
    private void scrollAsNeeded(){
        if (colorOffset + maxColorsOnPalette > pntList.size()) {
            colorOffset=Math.max(0,pntList.size()- maxColorsOnPalette);
            setScrollStates();
            updateColors();
        }
    }

    /**
     * Scroll to the right.
     */
    private void scrollRight(){
        if ((colorOffset + maxColorsOnPalette) <pntList.size())  {
            colorOffset++;
            setScrollStates();
            updateColors();
        }
    }

    /**
     * Scroll to the left.
     */
    private void scrollLeft(){
        if (colorOffset>0) {
            colorOffset--;
            setScrollStates();
            updateColors();
        }
    }

    /**
     * Set scroll icon visibility.
     */
    private void setScrollStates(){
        if (colorOffset+maxColorsOnPalette < pntList.size()) scRightButton.setVisibility(View.VISIBLE);
        else scRightButton.setVisibility(View.INVISIBLE);

        if (colorOffset >0 ) scLeftButton.setVisibility(View.VISIBLE);
        else scLeftButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Adds a color to the pallete view group.
     * @param p Paint View to be added.
     */
    public void addColor(PaintView p){
        p.setOnClickListener(pntClick);
        pntList.add(p);
        addView(p);
    }

    /**
     * Removes the given color from the palette view group.
     * @param p Color being removed.
     */
    public void removeColor(PaintView p){
        if (pntList.contains(p)){
            pntList.remove(p);
            removeView(p);
        }
    }
    public void SaveAttributes(){
        //Save the current palette.
        try{
                Gson gson= new Gson();
                FileOutputStream foStream= MainContext.openFileOutput("PaintColors.db",Context.MODE_PRIVATE);
                ObjectOutputStream writer=new ObjectOutputStream(foStream);
                writer.writeInt(sColorIndex);

                ArrayList<Integer> WriteMe= new ArrayList<Integer>();
                if(pntList.size()>3){
                    for(int i=3; i< pntList.size();i++){
                        WriteMe.add(pntList.get(i).getColor());
                    }
                }
                String Colors=gson.toJson(WriteMe);
                writer.writeObject(Colors);
                foStream.close();

        }
        catch(Exception e){ }

    }
    public void LoadAttributes(){
        //Attempt to load the previous palette.
        try{
            Gson gson= new Gson();
            FileInputStream fiStream= MainContext.openFileInput("PaintColors.db");
            ObjectInputStream reader=new ObjectInputStream(fiStream);
            int savedColor=reader.readInt();
            Type PaintViews = new TypeToken<ArrayList<Integer>>(){}.getType();
            ArrayList<Integer> SavedColors= (ArrayList<Integer>)gson.fromJson(reader.readObject().toString(), PaintViews);
            for(int pntVC: SavedColors ){ addColor(new PaintView(MainContext,pntVC)); }
            setPaint(savedColor);
            setScrollStates();
            if (sColorIndex>2) deleteButton.setVisibility(VISIBLE);
            fiStream.close();
        }
        catch(Exception e){ }
    }
}