package cs4962.assign1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;
import java.util.ArrayList;



/**
 * Created by Craig on 10/17/13.
 */
public class WatchWrapper {
    private Bitmap bmpPlay;
    private Bitmap bmpPause;
    private Bitmap bmpPaintMode;
    private Bitmap bmpCut;

    private ImageView btnModePaint;
    private ImageView btnPlayPause;
    private ImageView btnCut;
    private ScrubView scrubber;
    private LineGridLayout SideBar;
    private LineGridLayout WatchLayout;
    private PaintAreaView pArea;

    private OnModeChangeListener ModeChanged;
    private OnClipCutListener ClipChanged;
    private onPauseAnimationListener pAnimation;
    private onResumeAnimationListener rAnimation;

    private int Frame=0;
    private int FrameCount=0;
    private boolean Playing=false;
    public WatchWrapper(Context c, PaintAreaView paintArea){

        //Setup Icons for the watchmode view.
        bmpPaintMode=BitmapFactory.decodeResource(c.getResources(), R.drawable.painticon);
        bmpPlay=BitmapFactory.decodeResource(c.getResources(), R.drawable.play);
        bmpPause=BitmapFactory.decodeResource(c.getResources(), R.drawable.pause);
        bmpCut=BitmapFactory.decodeResource(c.getResources(), R.drawable.cutright);
        btnModePaint=new ImageView(c);
        btnPlayPause=new ImageView(c);
        btnCut=new ImageView(c);
        btnModePaint.setImageBitmap(bmpPaintMode);
        btnPlayPause.setImageBitmap(bmpPlay);
        btnCut.setImageBitmap(bmpCut);

        //Instantiate all views.
        WatchLayout=new LineGridLayout(c);
        SideBar=new LineGridLayout(c);
        scrubber=new ScrubView(c);
        this.pArea=new PaintAreaView(c);
        pArea.setImagePoints(paintArea.getImagePoints());
        pArea.setModetoWatch();

        //Setup the sidebar for the watchlayout
        SideBar.addViewNoAdj(btnModePaint,15,true);
        SideBar.addViewNoAdj(btnPlayPause,15,true);
        SideBar.addViewNoAdj(scrubber,55,false);
        SideBar.addViewNoAdj(btnCut,15,true);
        pArea.setBackgroundColor(Color.RED);

        //Setup the watch layout.
        WatchLayout.addViewNoAdj(SideBar, 10, false);
        WatchLayout.addViewAdj(pArea, 90, false);
        pArea.setEnabled(false);
        SideBar.setBackgroundColor(Color.LTGRAY);

        //Setup current views properties.
        FrameCount=getFrameCount();
        scrubber.setMax(FrameCount);

        //Setup the on scroll begin listener for the animation scrubber.
        scrubber.setOnScrollBeginListener(new ScrubView.onScrollBeginListener() {
            @Override
            public void onScrollBegin() {
                if (Playing){
                    pauseAnimation();
                    setFrame(scrubber.getValue());
                    resumeAnimation();
                }
                else{
                    setFrame(scrubber.getValue());
                    pArea.drawPolygons(Frame);
                }
            }
        });
        //Setup the on scrubber scroll end listener.
        scrubber.setOnScrollEndListener(new ScrubView.onScrollEndListener() {
            @Override
            public void onScrollEnd() {
                if (Playing){
                    pauseAnimation();
                    setFrame(scrubber.getValue());
                    resumeAnimation();
                }
                else{
                    setFrame(scrubber.getValue());
                    pArea.drawPolygons(Frame);
                }
            }
        });

        //Set the on click listener for the mode change button.
        btnModePaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StopandReset();//Terminate timer threads, and reset frame to 0.
                if (ModeChanged!=null) ModeChanged.onModeChange();//Inform the parent to swap views.
            }
        });
        //Set the on click listener for the play/pause button.
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Playing){ pauseAnimation(); }
                else{ resumeAnimation(); }
            }
        });
        //set the on click listener for the Cut Clip button.
        btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Playing) {
                    pauseAnimation();//Pause the current animation
                    cutClip();      //Cut the clip.
                    resumeAnimation();//resume the current animation.
                } else {
                    cutClip();
                }
            }
        });
    }

    /**
     * Refreshes the view (Really should have removed this.)
     */
    public void Update(){
        scrubber.setMax(getFrameCount());
        FrameCount=getFrameCount();
        Frame=0;
        pArea.drawPolygons(0);
    }

    /**
     * Cuts the current clip at the current frame. (Dropping all frames preceding the current frame.)
     */
    private void cutClip(){
        //If the current frame is 0, create a new image. Otherwise, cycle through the current
        //clip grabbing all frames up to the current frame. (As set by the scrubber control.)
        if(Frame==0) pArea.setImagePoints(new ArrayList<Polygon>());
        else{
            if (pArea.getImagePoints()!=null){
                ArrayList<Polygon> EditedClip=new ArrayList<Polygon>();
                int PointCount=0;
                for(Polygon pgon: pArea.getImagePoints()){
                    EditedClip.add(new Polygon(pgon.getColor()));
                    for(Point p: pgon.getPoints() ){
                        if(PointCount<Frame){
                            EditedClip.get(EditedClip.size()-1).getPoints().add(p);
                            PointCount++;
                        }
                        else break;
                    }
                    if (PointCount>=Frame) break;
                }
                pArea.setImagePoints(EditedClip);

            }
        }
        //After frames have been grabbed, refresh the view.
        scrubber.setMax(getFrameCount());
        scrubber.setValue(FrameCount);
        pArea.drawPolygons(FrameCount);
        if (ClipChanged!=null) ClipChanged.ClipCut();
    }

    //Sets the on clip cut listener.
    public void setOnClipCutListener(OnClipCutListener l) { ClipChanged=l; }
    //Sets the on Mode change listener.
    public void setOnModeChangeListener(OnModeChangeListener l){ ModeChanged=l; }

    /**
     * Sets the current frame, respecting boundaries of the current animation.
     * @param Frame
     */
    public void setFrame(int Frame){
        if (Frame >FrameCount) Frame =FrameCount;
        else if (Frame < 0) Frame=0;
        this.Frame=Frame;
    }

    /**
     * Gets a reference to the watch layout view.
     * @return A reference to the watch layout view.
     */
    public View getWatchView(){ return WatchLayout; }

    /**
     * Pauses the current animation.
     */
    private void pauseAnimation(){
        Playing=false;
        if (pAnimation!=null) pAnimation.Pause();
        btnPlayPause.setImageBitmap(bmpPlay);
    }

    /**
     * Resumes playing the current animation.
     */
    private void resumeAnimation(){
        Playing=true;
        btnPlayPause.setImageBitmap(bmpPause);
        if(rAnimation!=null) rAnimation.Resume();
    }

    /**
     * Pauses animation and resets it back to the beginning frame.
     */
    public void StopandReset(){
        pauseAnimation();
        Frame=0;
        btnPlayPause.setImageBitmap(bmpPlay);
        Refresh();
    }

    /**
     * Pauses the current animation.
     */
    public void PauseAnimation(){ pauseAnimation(); }

    /**
     * Calculates the frame count based on the number of points in the current image.
     * @return The number of frames for the current image.
     */
    private int getFrameCount(){
        if (pArea.getImagePoints()==null) return 0;
        int Count=0;
        for(Polygon Poly: pArea.getImagePoints()){
            Count+=Poly.getPoints().size();
        }
        return Count;
    }

    /**
     * Gets the current frame number.
     * @return The current frame number.
     */
    public int getFrame(){ return Frame; }

    /**
     * Gets the current Frame Count.
     * @return The current frame count.
     */
    public int getFrameLimit(){ return FrameCount; }

    /**
     * Enables/Disables the cut clop button.
     * @param cutEnabled
     */
    public void setCutEnabled(boolean cutEnabled){
        if(cutEnabled) btnCut.setVisibility(View.VISIBLE);
        else btnCut.setVisibility(View.INVISIBLE);
    }

    /**
     * Draws the current frame and updates the animation scrubber.
     */
    public void Refresh(){
        pArea.drawPolygons(Frame);
        scrubber.setValue(Frame);
    }

    /**
     * Sets the current images data.
     * @param Points Data used to replace the current image.
     */
    public void setImagePoints(ArrayList<Polygon> Points){
        pArea.setImagePoints(Points);
        StopandReset();
    }

    /**
     * Gets the currents images data.
     * @return The current images data.
     */
    public ArrayList<Polygon> getImagePoints(){ return pArea.getImagePoints();}
    //Sets the on pause listener.
    public void setOnPauseAnimationListener(onPauseAnimationListener l){ pAnimation=l; }
    //Sets the on resume listener.
    public void setOnResumeAnimationListener(onResumeAnimationListener l){ rAnimation=l; }

    //setup on mode change listener.
    public interface OnModeChangeListener{
        public void onModeChange();
    }

    //Setup on Resume animation listener interface.
    public interface onResumeAnimationListener{
        public void Resume();
    }
    //Setup animation pause listener interface.
    public interface onPauseAnimationListener{
        public void Pause();
    }
    //Setup cut clip listener interface.
    public interface OnClipCutListener{
        public void ClipCut();
    }
}
