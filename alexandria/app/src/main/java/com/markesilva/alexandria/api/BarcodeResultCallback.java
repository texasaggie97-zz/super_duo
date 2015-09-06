package com.markesilva.alexandria.api;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by mark on 9/5/2015.
 *
 * Call back for when a barcode is found
 */
public interface BarcodeResultCallback {
    void handleResult(Barcode barcode);
}
