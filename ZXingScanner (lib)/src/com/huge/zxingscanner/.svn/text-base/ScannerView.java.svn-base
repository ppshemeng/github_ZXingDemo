package com.huge.zxingscanner;
/**
 * @author Edward Ye edwardye@21cn.com
 * 显示扫描画面的窗口
 * 只需要在Activity中放入这个View，
 * 然后调用setOnDecodeListener()设置实现了OnDecodeCompletionListener的对象，
 * 就可以接收扫描结果了
 *
 */

import android.R.color;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.SurfaceHolder.Callback;
import android.widget.FrameLayout;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.huge.zxingscanner.camera.CameraManager;
import com.huge.zxingscanner.decoding.BitmapLuminanceSource;
import com.huge.zxingscanner.decoding.CaptureActivityHandler;
import com.huge.zxingscanner.decoding.DecodeFormatManager;
import com.huge.zxingscanner.view.ViewfinderView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class ScannerView extends FrameLayout implements Callback{

	private static final String TAG = ScannerView.class.getSimpleName();
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private SurfaceView surfaceView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;	
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.80f;
	private boolean vibrate;
	private Context context;
	private OnDecodeCompletionListener decodeListener=null;	
	private int previewWidth=0,previewHeight=0;
    private SharedPreferences pre;
	private android.hardware.Camera camera;
	private Parameters parameter;
	private Boolean open_sound,open_light,open_vibrate;

	ScannerView(Context context){
		super(context);
		this.context=context;
		constructLayout();	
	}
	public ScannerView(Context context,AttributeSet attrs){
		super(context,attrs);
		this.context=context;	
		constructLayout();
		
	}
	
	private void initSettingdata(){
		 pre = context.getSharedPreferences(context.getPackageName() + "_preferences", context.MODE_WORLD_READABLE );          
		 open_sound=pre.getBoolean("OPEN_SOUND", true);//两个参数,一个是key，就是在PreferenceActivity的xml中设置的,另一个是取不到值时的默认值
	     open_light=pre.getBoolean("OPEN_LIGHT", false);//两个参数,一个是key，就是在PreferenceActivity的xml中设置的,另一个是取不到值时的默认值
	     open_vibrate=pre.getBoolean("OPEN_VIBRATE", true);//两个参数,一个是key，就是在PreferenceActivity的xml中设置的,另一个是取不到值时的默认值
	}

    /**
     * 生成内部控件，并排版
     */
	private void constructLayout(){
		FrameLayout.LayoutParams mainViewParam = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mainViewParam.gravity=Gravity.CENTER;
		surfaceView=new SurfaceView(context);
		surfaceView.setLayoutParams(mainViewParam);
		this.addView(surfaceView);
		
		viewfinderView=new ViewfinderView(context);
		mainViewParam = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mainViewParam.gravity=Gravity.CENTER;		
		viewfinderView.setLayoutParams(mainViewParam);
		viewfinderView.setBackgroundColor(color.transparent);
		this.addView(viewfinderView);
		hasSurface=false;		
	    		
	}

    /**
     * Activity的onResume事件调用本方法
     * 用于设置相机
     */
	public void onResume(){
		
		int oldpreviewWidth=previewWidth;		
		
		if (previewWidth==0){
			previewWidth=this.getMeasuredWidth();
			previewHeight=this.getMeasuredHeight();
		}
		
		if (oldpreviewWidth!=previewWidth)
            Log.d(TAG,"onResume:width="+previewWidth+"，height="+previewHeight);
		
		SurfaceHolder surfaceHolder = surfaceView.getHolder();		
		
		if (hasSurface && previewWidth>0) {
            //当成功获取了宽度和高度，并且已经创建了surfaceView后，才能初始化相机
			initCamera(surfaceHolder);	
		
		} else {			
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);			
			
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}	
		
	    initBeepSound();
		vibrate = true;
		
		
	}
	
	
	private void initCamera(SurfaceHolder surfaceHolder) {
        //避免在横屏显示时，手机的横竖屏切换总会重新设置宽度和高度，所以，只允许宽度和高度只设置一次
		initSettingdata();		
		if (Configuration.ORIENTATION_LANDSCAPE==context.getResources().getConfiguration().orientation)
			Log.e(TAG,"initCamera:Activity orientation is landscape,width="+previewWidth+", height="+previewHeight);
		else
			Log.e(TAG,"initCamera:Activity orientation is portrait,width="+previewWidth+", height="+previewHeight);
		
		try {
            //初始化 CameraManager
			CameraManager.init(context, previewWidth, previewHeight);
			CameraManager.get().openDriver(surfaceHolder);
			
		
			
		} catch (IOException ioe) {
			Log.e(TAG,"initCamera:"+ioe.getMessage());
			return;
		} catch (RuntimeException e) {
			Log.e(TAG,"initCamera:"+e.getMessage());
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
		
	}


    /**
     * Activity的onPause事件调用本方法，用于释放相机资源
     */
	public void onPause(){
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		if (CameraManager.get()!=null)
			CameraManager.get().closeDriver();
		
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {	
		if (!hasSurface) {
			hasSurface = true;
			if (previewWidth==0){				
				previewWidth=this.getMeasuredWidth();
				previewHeight=this.getMeasuredHeight();				
			}
			if (previewWidth>0){				
				initCamera(holder);
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}


	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// 	so we now play on the music stream.
		
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);
			
			try {
				AssetManager am = context.getAssets(); 				
				AssetFileDescriptor file = am.openFd("beepbeep.ogg");
//				AssetFileDescriptor file = context.getResources().openRawResourceFd(R.raw.beep);
				mediaPlayer.setDataSource(file.getFileDescriptor(),
					file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
				java.lang.System.out.println(e.getMessage());
			}
		}
	}
	
	public static void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

    /**
     * 设置扫描成功后的回调接口，通过该接口获得扫描结果
     * @param decodeListener 实现接口的对象
     */
	public void setOnDecodeListener(OnDecodeCompletionListener decodeListener){
		this.decodeListener=decodeListener;
	}
	
	public void handleDecode(Result obj, Bitmap barcode) {		
		
			playBeepSoundAndVibrate();
			
		if (null!=decodeListener){
			decodeListener.onDecodeCompletion(obj.getBarcodeFormat().toString() , obj.getText(),barcode);
			
		}
	}
	
	private static final long VIBRATE_DURATION = 200L;
	private void playBeepSoundAndVibrate()
	{
		initSettingdata();
	    
		//响铃提示
	 if(open_sound)
	 {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
	 }
	  //震动提示
	 if(open_vibrate)
	 {
		if (vibrate) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	 }
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	public int getOrientation(){
		if (previewWidth==0)
			return context.getResources().getConfiguration().orientation ;
		else
            //只要宽度和高度设置好了，就以宽度和高度的比例来判断是横屏还是竖屏
            //因为宽度和高度只设置一次，就不会被后面横竖屏切换时不断变换的宽竖屏返回值扰乱了。
			return previewWidth>previewHeight?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT;
					
	}
	
	
}
