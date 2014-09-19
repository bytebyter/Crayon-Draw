//Program has multi-touch capability and a painters palette
//with a scrollable color list and deletion/creation capabilities.


package cs4962.assign1;


import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity {
    private BrushHandler brushHandler;
    private PaintAreaView PaintSurface;
    private WatchWrapper WatchMode;
    private PaintWrapper PaintMode;
    private Context MainContext;
    private Timer WatchViewAnimator;
    private int Orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Force Full Screen.
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        MainContext=this;

        //Instantiate views.
        brushHandler= BrushHandler.getInstance();
        PaintSurface=new PaintAreaView(this);
        WatchMode=new WatchWrapper(this,PaintSurface);
        PaintMode=new PaintWrapper(this,PaintSurface);

        //Set the current mode to the paint mode.
        setContentView(PaintMode.getView());

        //Setup the on mode change listener for watchmode.
        WatchMode.setOnModeChangeListener(new WatchWrapper.OnModeChangeListener() {
            @Override
            public void onModeChange() {
                WatchMode.StopandReset();
                PaintSurface.setModetoPaint();
                setContentView(PaintMode.getView());
            }
        });
        //Setup the on mode change listener for the paintmode.
        PaintMode.setOnModeChangeListener(new WatchWrapper.OnModeChangeListener() {
            @Override
            public void onModeChange() {
                PaintSurface.setModetoWatch();
                WatchMode.Update();
                setContentView(WatchMode.getWatchView());
            }
        });
        //Setup the on clip cut listener for the watchmode.
        WatchMode.setOnClipCutListener(new WatchWrapper.OnClipCutListener() {
            @Override
            public void ClipCut() {
                PaintSurface.forcedRedraw(WatchMode.getImagePoints());
            }
        });

        //Sets up the on resume animation listener.
        WatchMode.setOnResumeAnimationListener(new WatchWrapper.onResumeAnimationListener() {
            @Override
            public void Resume() {
                //Kill all previously running animation threads.
                //Warning sometimes this is slow.
                if (WatchViewAnimator!=null){
                    try{ WatchViewAnimator.cancel(); }
                    catch(Exception e){}
                }
                WatchViewAnimator=new Timer();
                //Animate
                WatchViewAnimator.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (WatchMode.getFrame()<WatchMode.getFrameLimit()) {
                                    WatchMode.setCutEnabled(false);
                                    WatchMode.setFrame(WatchMode.getFrame()+1);
                                    WatchMode.Refresh();
                                }
                                else{
                                    try{ cancel(); }
                                    catch(Exception e){ }
                                    WatchMode.PauseAnimation();
                                }
                            }
                        });
                    }
                },0,1000/30);
            }
        });

        //Setup the on pause animation listener.
        WatchMode.setOnPauseAnimationListener(new WatchWrapper.onPauseAnimationListener() {
            @Override
            public void Pause() {
                //Shutdown all threads performing animation.
                //Set up the watchmode view.
                if(WatchViewAnimator!=null){
                    try{ WatchViewAnimator.cancel(); }
                    catch(Exception e){}
                    WatchMode.Refresh();
                    WatchMode.setCutEnabled(true);
                }
            }
        });
    }

    //On pause save the picture.
    @Override
    protected void onPause() {
        super.onPause();
        try{
            Gson gson= new Gson();
            FileOutputStream foStream= openFileOutput("Paint.db",MODE_PRIVATE);
            ObjectOutputStream writer=new ObjectOutputStream(foStream);
            String Points=gson.toJson(PaintSurface.getImagePoints());
            writer.writeObject(Points);
            foStream.close();
        }
        catch (Exception e){}
    }

    //On resume load the picture.
    @Override
    protected void onResume() {
        super.onResume();
        try{
            Gson gson= new Gson();
            FileInputStream fin= openFileInput("Paint.db");
            ObjectInputStream reader= new ObjectInputStream(fin);
            Type Polygons = new TypeToken<ArrayList<Polygon>>(){}.getType();
            ArrayList<Polygon> SavedPoints= (ArrayList<Polygon>)gson.fromJson(reader.readObject().toString(), Polygons);
            PaintSurface.resumeRedraw(SavedPoints);
            WatchMode.setImagePoints(SavedPoints);
            fin.close();
        }
        catch (Exception e){

        }
    }
}
