/*
 * Copyright (C) 2010 ZXing authors
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

package com.huge.zxingscanner.decoding;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.huge.zxingscanner.Constant;
import com.huge.zxingscanner.ScannerView;
import com.huge.zxingscanner.camera.CameraManager;

import java.util.Hashtable;

final class DecodeHandler extends Handler {

  private static final String TAG = DecodeHandler.class.getSimpleName();

  private final ScannerView scannerView;
  private final MultiFormatReader multiFormatReader;
  private int orientation=Configuration.ORIENTATION_PORTRAIT;

  DecodeHandler(ScannerView scannerView, Hashtable<DecodeHintType, Object> hints) {
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.scannerView = scannerView;
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case Constant.decode:
        //Log.d(TAG, "Got decode message");
        decode((byte[]) message.obj, message.arg1, message.arg2);
        break;
      case Constant.quit:
        Looper.myLooper().quit();
        break;
    }
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  private void decode(byte[] data, int width, int height) {
    long start = System.currentTimeMillis();
    Result rawResult = null;
    com.huge.zxingscanner.camera.PlanarYUVLuminanceSource source =null;
    //��Ϊ������ʾʱ���������µ���
    if (Configuration.ORIENTATION_LANDSCAPE==orientation){
    	try{
    		source = CameraManager.get().buildLuminanceSource(data, width, height);
    	}catch(IllegalArgumentException ex){
    		source=null;
    		Log.e(TAG, ex.getMessage());
    	}
    }else{
    	byte[] rotatedData = new byte[data.length];
    	for (int y = 0; y < height; y++) {
    		for (int x = 0; x < width; x++)
    			rotatedData[x * height + height - y - 1] = data[x + y * width];
    	}
    	int tmp = width; // Here we are swapping, that's the difference to #11
    	width = height;
    	height = tmp;
    	try{
    		source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
    	}catch(IllegalArgumentException ex){
    		source=null;
    		Log.e(TAG,ex.getMessage());
    	}
    }
    BinaryBitmap bitmap = null;
    if (null!=source)
    	bitmap=new BinaryBitmap(new HybridBinarizer(source));
    try {
      if (null!=bitmap)
    	  rawResult = multiFormatReader.decodeWithState(bitmap);
    } catch (ReaderException re) {
      // continue
    } finally {
      multiFormatReader.reset();
    }

    if (rawResult != null) {
      long end = System.currentTimeMillis();
      Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
      Message message = Message.obtain(scannerView.getHandler(), Constant.decode_succeeded, rawResult);
      Bundle bundle = new Bundle();
      bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
      message.setData(bundle);
      Log.d(TAG, "Sending decode succeeded message...");
      message.sendToTarget();
    } else {
      Message message = Message.obtain(scannerView.getHandler(), Constant.decode_failed);
      message.sendToTarget();
      Log.d(TAG,"Sending decode failed message...");
    }
  }
  
  void setOrientation(int orientation){
	  this.orientation =orientation;
  }

}
