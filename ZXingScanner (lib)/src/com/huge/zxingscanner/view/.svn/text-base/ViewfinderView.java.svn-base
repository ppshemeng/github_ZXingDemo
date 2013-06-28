/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huge.zxingscanner.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.google.zxing.ResultPoint;
import com.huge.zxingscanner.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  private static final String TAG = ViewfinderView.class.getSimpleName();
  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 100L;
  private static final int OPAQUE = 0xFF;

  private  Paint paint;
  private Bitmap resultBitmap;
  private  int maskColor;
  private  int resultColor;
  private  int frameColor;
  private  int laserColor;
  private  int resultPointColor;
  private int scannerAlpha;
  private Collection<ResultPoint> possibleResultPoints;
  private Collection<ResultPoint> lastPossibleResultPoints;
  private int previewWidth=0,previewHeight=0;

  
  public ViewfinderView(Context context){
	  super(context);
	  initColor();
  }
  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initColor();
    
  }

  private void initColor(){
	// Initialize these once for performance rather than calling them every time in onDraw().
	    paint = new Paint();
	    
	    maskColor = 0x60000000;//resources.getColor(R.color.viewfinder_mask);
	    resultColor = 0xb0000000;//resources.getColor(R.color.result_view);
	    frameColor = 0xff000000;//resources.getColor(R.color.viewfinder_frame);
	    laserColor = 0xffff0000;//resources.getColor(R.color.viewfinder_laser);
	    resultPointColor = 0xc0ffff00;//resources.getColor(R.color.possible_result_points);
	    scannerAlpha = 0;
	    possibleResultPoints = new HashSet<ResultPoint>(5);  
  }
  @Override
  public void onDraw(Canvas canvas) {
    
	if (CameraManager.get()!=null){
        //只有在相机被初始化后，才画出中间的方框
		Rect frame = CameraManager.get().getFramingRect();

		if (frame == null) {
			return;
		}

        //在第一次运行时获取宽度和高度即可，如果每次onDraw调用时，都去获取，会有可能在横竖屏切换时拿到不同的结果
		if (previewWidth==0){
			previewWidth=this.getWidth();
			previewHeight=this.getHeight();
			Log.d(TAG,"draw canvas. width="+previewWidth+",height="+previewHeight+",frame="+frame);
		}  	
		

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		canvas.drawRect(0, 0, previewWidth, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, previewWidth, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, previewWidth, previewHeight, paint);

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {
			int linewidht = 10;
			paint.setColor(laserColor);
			canvas.drawRect(-10 + frame.left, -10 + frame.top,
					-10 + (linewidht + frame.left), -10 + (100 + frame.top), paint);
			canvas.drawRect(-10 + frame.left, -10 + frame.top,
					-10 + (100 + frame.left), -10 + (linewidht + frame.top), paint);			
			canvas.drawRect(10 + ((0 - linewidht) + frame.right),
					-10 + frame.top, 10 + (1 + frame.right),
					-10 + (100 + frame.top), paint);
			canvas.drawRect(10 + (-100 + frame.right), -10 + frame.top, 10
					+ frame.right, -10 + (linewidht + frame.top), paint);
			canvas.drawRect(-10 + frame.left, 10 + (-100 + frame.bottom),
					-10 + (linewidht + frame.left), 10 + (1 + frame.bottom),
					paint);
			canvas.drawRect(-10 + frame.left, 10
					+ ((0 - linewidht) + frame.bottom), -10 + (100 + frame.left),
					10 + (1 + frame.bottom), paint);
			canvas.drawRect(10 + ((0 - linewidht) + frame.right), 10
					+ (-100 + frame.bottom), 10 + (1 + frame.right), 10
					+ (1 + frame.bottom), paint);
			canvas.drawRect(10 + (-100 + frame.right),10
					+ ((0 - linewidht) + frame.bottom), 10 + frame.right, 10
					+ (linewidht - (linewidht - 1) + frame.bottom), paint);
			// Draw a two pixel solid black border inside the framing rect
			paint.setColor(frameColor);
			canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
			canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
			canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
			canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);

			// Draw a red "laser scanner" line through the middle to show decoding is active
			paint.setColor(laserColor);
			paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//			int middle = frame.height() / 2 + frame.top;
//			canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
			int middle = frame.height() / 2 + frame.top;
		    int hmiddle=frame.width()/2+frame.left;
		       //中横线
		      canvas.drawRect(hmiddle - 30, middle - 1, hmiddle - 10, middle + 1, paint);
		      canvas.drawRect(hmiddle + 10, middle - 1, hmiddle + 30, middle + 1, paint);
		      //中竖线
		      canvas.drawRect(hmiddle-1, middle - 30, hmiddle+1, middle - 10, paint);
		      canvas.drawRect(hmiddle-1, middle + 10, hmiddle+1, middle + 30, paint);
			
			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
				}
			}

			// Request another update at the animation interval, but only repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
		}
	}
	else{
		if (previewWidth==0){
			previewWidth=this.getWidth();
			previewHeight=this.getHeight();
			
		}  	
		if (previewWidth>0){
			paint.setColor(maskColor);			
			canvas.drawRect(0,0,previewWidth,previewHeight, paint);			
			postInvalidate();
		}
		Log.d(TAG,"draw canvas. mask the whole screen.width="+previewWidth+",height="+previewHeight);
	}
	
    
  }

  public void drawViewfinder() {
    resultBitmap = null;
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    possibleResultPoints.add(point);
  }

}
