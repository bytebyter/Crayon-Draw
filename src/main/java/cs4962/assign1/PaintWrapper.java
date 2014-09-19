package cs4962.assign1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Craig on 10/17/13.
 */
public class PaintWrapper {
    private BrushHandler brushHandler=BrushHandler.getInstance();
    private WatchWrapper.OnModeChangeListener ModeChanged;
    private LinearLayout Spacer;
    private LineGridLayout PaintLayout;
    private LineGridLayout SideBar;
    private ImageView btnPaintSelect;
    private ImageView btnWatchMode;
    private Bitmap bmpWatchMode;
    private Bitmap bmpPaintSelect;

    private PaintAreaView pArea;
    private Context mainContext;
    public PaintWrapper(Context context,PaintAreaView pArea){
        mainContext=context;
        //Instantiate needed views.
        PaintLayout= new LineGridLayout(context);
        Spacer=new LinearLayout(context);
        btnWatchMode=new ImageView(context);
        btnPaintSelect=new ImageView(context);

        //Load necessary bitmaps for icons.
        bmpWatchMode=BitmapFactory.decodeResource(context.getResources(),R.drawable.watchicon);
        bmpPaintSelect=BitmapFactory.decodeResource(context.getResources(),R.drawable.paintsel);
        btnWatchMode.setImageBitmap(bmpWatchMode);
        btnPaintSelect.setImageBitmap(generateColorIcon(bmpPaintSelect));

        //Generate the Paint Layout View.
        this.pArea=pArea;
        SideBar=new LineGridLayout(context);
        SideBar.addViewNoAdj(btnPaintSelect,15,true);
        SideBar.setBackgroundColor(Color.LTGRAY);
        SideBar.addViewNoAdj(Spacer,70,false);
        SideBar.addViewNoAdj(btnWatchMode,15,true);
        PaintLayout.addViewNoAdj(SideBar,10,false);
        PaintLayout.addViewNoAdj(pArea,90,false);

        //Sets the on click listener for the Watch Mode Icon.
        btnWatchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ModeChanged!=null) ModeChanged.onModeChange();
            }
        });
        //Sets up the on click listener for the paint selection button.
        btnPaintSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(mainContext,PalleteActivity.class);
                mainContext.startActivity(intent);
            }
        });
        //Setup the on color change listener for paint view. (Switches the active color icon.)
        brushHandler.setOnColorChangeListener(new BrushHandler.OnColorChangeListener() {
            @Override
            public void onColorChange() {
                btnPaintSelect.setImageBitmap(generateColorIcon(bmpPaintSelect));
            }
        });
    }

    /*
    Sets the on mode change listener for the paint view (Used for initialization of views)
     */
    public void setOnModeChangeListener(WatchWrapper.OnModeChangeListener l){ ModeChanged=l;}
    /*
    Gets a reference to the paint layouts view.
     */
    public View getView(){ return PaintLayout; }

    /**
     * Generates an icon for the currently active color.
     * @param srcBitmap Source bitmap to use as an overlay.
     * @return A bitmap representing the currently active color.
     */
    private Bitmap generateColorIcon(Bitmap srcBitmap){
        Bitmap retBitmap=Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), srcBitmap.getConfig());
        Paint p=new Paint();
        p.setColor(brushHandler.getColor());
        Canvas c=new Canvas(retBitmap);
        c.drawCircle(srcBitmap.getWidth()/2,srcBitmap.getHeight()/2,(srcBitmap.getWidth()/2)*0.9f,p);
        p.setColor(Color.WHITE);
        c.drawBitmap(srcBitmap,0,0,p);
        return retBitmap;
    }

}
