package com.markesilva.alexandria;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.markesilva.alexandria.CameraPreview.CameraSourcePreview;
import com.markesilva.alexandria.CameraPreview.GraphicOverlay;
import com.markesilva.alexandria.CameraPreview.BarcodeTrackerFactory;
import com.markesilva.alexandria.api.BarcodeResultCallback;
import com.markesilva.alexandria.data.AlexandriaContract;
import com.markesilva.alexandria.services.BookService;
import com.markesilva.alexandria.services.DownloadImage;
import com.markesilva.alexandria.utils.LOG;

import java.io.IOException;

public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, BarcodeResultCallback {
    private static final int RC_HANDLE_GMS = 9001;
    private static final String LOG_TAG = LOG.makeLogTag(AddBook.class);
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText mEan;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    // Barcode scanner
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mScannerView;
    private GraphicOverlay mGraphicOverlay;
    private boolean mScannerActive = false;
    private BarcodeDetector mBarcodeDetector;
    private Barcode mBarcode;
    private View.OnClickListener mBarcodeClickListener;

    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mEan !=null) {
            outState.putString(EAN_CONTENT, mEan.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEan = (EditText) rootView.findViewById(R.id.ean);

        mEan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                addBook(s.toString());
            }
        });

        mScannerView = (CameraSourcePreview) rootView.findViewById(R.id.scanner_window);
        mGraphicOverlay = (GraphicOverlay) rootView.findViewById(R.id.barcode_overlay);

        // Create the camera source with barcode detector
        mBarcodeDetector = new BarcodeDetector.Builder(getContext()).build();
        mBarcodeDetector.setProcessor(new MultiProcessor.Builder(new BarcodeTrackerFactory(mGraphicOverlay, this)).build());

        if (!mBarcodeDetector.isOperational()) {
            // The first time an app using the barcode API is installed, GMS will download a
            // native library. Usually this is done before the app is ran
            LOG.W(LOG_TAG, "Detector dependencies are not available yet.");
        }

        mCameraSource = new CameraSource.Builder(getContext(), mBarcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(10.0f)
                .build();

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScannerActive) {
                    mScannerView.setVisibility(View.INVISIBLE);
                    mScannerView.stop();
                    mScannerView.setOnClickListener(null);
                    mScannerActive = false;
                } else {
                    mScannerView.setVisibility(View.VISIBLE);
                    startCameraSource();
                    mScannerView.setOnClickListener(mBarcodeClickListener);
                    mScannerActive = true;
                    if (!mScannerView.cameraFocus(mCameraSource, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        LOG.D(LOG_TAG, "Autofocus not set!");
                    }
                }
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEan.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEan.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEan.setText("");
            }
        });

        mBarcodeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If no barcode, nothing to do
                if (mBarcode == null) {
                    return;
                }

                mScannerView.setVisibility(View.INVISIBLE);
                mScannerView.stop();
                mScannerActive = false;

                addBook(mBarcode.rawValue);
            }
        };

        if(savedInstanceState!=null){
            mEan.setText(savedInstanceState.getString(EAN_CONTENT));
            mEan.setHint("");
        }

        return rootView;
    }

    public void handleResult(Barcode result) {
        mBarcode = result;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleBarcode();
            }
        });
    }

    private void handleBarcode() {

        // If no barcode, nothing to do
        if (mBarcode == null) {
            return;
        }

        mScannerView.setVisibility(View.INVISIBLE);
        mScannerView.stop();
        mScannerActive = false;

        mEan.setText(mBarcode.rawValue);

        addBook(mBarcode.rawValue);
    }
    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mEan.getText().length()==0){
            return null;
        }
        String eanStr= mEan.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        } else {
            ((TextView) rootView.findViewById(R.id.authors)).setLines(1);
            ((TextView) rootView.findViewById(R.id.authors)).setText("N/A");
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void addBook(String ean) {
        //catch isbn10 numbers
        if (ean.length() == 10 && !ean.startsWith("978")) {
            ean = "978" + ean;
        }
        if (ean.length() < 13) {
            clearFields();
            return;
        }
        //Once we have an ISBN, start a book intent
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
        AddBook.this.restartLoader();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getActivity());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mScannerView.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                LOG.E(LOG_TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }}
