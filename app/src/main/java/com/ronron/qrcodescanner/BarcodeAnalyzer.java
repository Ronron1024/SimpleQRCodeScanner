package com.ronron.qrcodescanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer
{
    private Context context;
    private boolean is_dialog_displayed;

    public BarcodeAnalyzer(Context context)
    {
        this.context = context;
        is_dialog_displayed = false;
    }

    @Override
    public void analyze(@NonNull ImageProxy image_proxy) {
        @SuppressLint("UnsafeExperimentalUsageError")  // getImage() is experimental
                Image media_image = image_proxy.getImage();

        if (media_image != null)
        {
            InputImage image = InputImage.fromMediaImage(media_image, image_proxy.getImageInfo().getRotationDegrees());
            BarcodeScanner barcode_scanner = BarcodeScanning.getClient();

            Task<List<Barcode>> result = barcode_scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>()
                    {
                        @Override
                        public void onSuccess(List<Barcode> barcodes)
                        {
                            int barcode_number = barcodes.size();

                            if (barcode_number > 0 && !is_dialog_displayed) // LOOP !!!
                            {
                                new AlertDialog.Builder(context)
                                    .setCancelable(false) // Bad, better use setOnCancelListener()
                                    .setTitle("Content")
                                    .setMessage(barcodes.get(0).getRawValue())
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            is_dialog_displayed = false;
                                        }
                                    })
                                    .show();
                                is_dialog_displayed = true;

                            }
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() { // Required for fire multiple analyses
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            image_proxy.close();
                        }
                    });
        }
    }
}