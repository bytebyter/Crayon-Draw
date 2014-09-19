package cs4962.assign1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Craig on 10/15/13.
 */
public class PalleteActivity extends Activity {
    LineGridLayout l;
    LineGridLayout lOpt;
    LinearLayout Divider;
    private PaletteView paletteView;
    private Context MainContext;
    private Activity activity;
    private ImageView btnNewPalette;
    private ImageView btnClose;
    private Bitmap bmpNew;
    private Bitmap bmpClose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Force full screen.
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Setup references to this activities context and activity.
        MainContext=this;
        activity=this;

        //Load needed bitmaps.
        bmpNew= BitmapFactory.decodeResource(getResources(),R.drawable.pnew);
        bmpClose=BitmapFactory.decodeResource(getResources(),R.drawable.pclose);

        //Instantiate all views.
        l=new LineGridLayout(this);
        lOpt= new LineGridLayout(this);
        paletteView=new PaletteView(this);
        Divider=new LinearLayout(this);
        //Setup each views layout.
        l.addViewNoAdj(paletteView,90,false);
        l.addViewNoAdj(lOpt,10,false);

        //Setup the close button.
        btnClose= new ImageView(this);
        btnClose.setImageBitmap(bmpClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
            }
        });

        //Setup the new palette button.
        btnNewPalette= new ImageView(this);
        btnNewPalette.setImageBitmap(bmpNew);
        btnNewPalette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                l.removeAllViews();
                paletteView=new PaletteView(MainContext);
                l.addViewNoAdj(paletteView, 90, false);
                lOpt.removeAllViews();
                lOpt.addViewNoAdj(btnNewPalette, 15, false);
                lOpt.addViewNoAdj(Divider,55,false);
                lOpt.addViewNoAdj(btnClose, 30, false);
                l.addViewNoAdj(lOpt,10,false);
            }
        });
        lOpt.addViewNoAdj(btnNewPalette, 15, false);
        lOpt.addViewNoAdj(Divider,55,false);
        lOpt.addViewNoAdj(btnClose, 30, false);

        //Set the contents of the view for this activity
        setContentView(l);
    }

    //On pause call save the palette.
    @Override
    protected void onPause() {
        super.onPause();
        paletteView.SaveAttributes();
    }

    //On resume load the palette.
    @Override
    protected void onResume() {
        super.onResume();
        paletteView.LoadAttributes();
    }
}
