package com.huge.zxingscanner;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;

public class ZXingScannerActivity extends Activity implements OnDecodeCompletionListener {
    private ScannerView scannerView;
    private TextView txtResult;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture);

        scannerView=(ScannerView)findViewById(R.id.scanner_view);
        txtResult = (TextView) findViewById(R.id.txtResult);
        scannerView.setOnDecodeListener(this);

    }

    @Override
    public void onDecodeCompletion(String barcodeFormat,String barcode,Bitmap bitmap){
        txtResult.setText("Barcode Format:"+barcodeFormat+"  Barcode:"+barcode);

    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.onPause();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}