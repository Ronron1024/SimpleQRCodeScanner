package com.ronron.qrcodescanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final int REQUEST_CODE_PERMISSION = 11;
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private ExecutorService camera_executor;
    private PreviewView preview_view;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview_view = findViewById(R.id.camera_view);

        camera_executor = Executors.newSingleThreadExecutor();

        if (permissionGranted())
            startCamera();
        else
        {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        camera_executor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int request_code, String[] permissions, int[] grant_results) {
        if (request_code == REQUEST_CODE_PERMISSION && grant_results[0] == PackageManager.PERMISSION_GRANTED)
            startCamera();
        else
        {
            new AlertDialog.Builder(this)
                    .setCancelable(false) // Bad, better use setOnCancelListener()
                    .setTitle("Permission denied")
                    .setMessage("Acces to the camera is required" +
                            "Simple QR Code scanner will close ...")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
    }

    private boolean permissionGranted()
    {
        return ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> camera_provider_future = ProcessCameraProvider.getInstance(this);

        camera_provider_future.addListener(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ProcessCameraProvider camera_provider = camera_provider_future.get();
                    bindCamerax(camera_provider);
                }
                catch (Exception e)
                {
                    // Should never be reached ...
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamerax(ProcessCameraProvider camera_provider)
    {
        Preview preview = new Preview.Builder().build();
        CameraSelector camera_selector = CameraSelector.DEFAULT_BACK_CAMERA;
        preview.setSurfaceProvider(preview_view.getSurfaceProvider());

        ImageAnalysis image_analyzer = new ImageAnalysis.Builder().build();
        image_analyzer.setAnalyzer(camera_executor, new BarcodeAnalyzer(context));

        camera_provider.bindToLifecycle(this, camera_selector, preview, image_analyzer);
    }

}

