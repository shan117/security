/*
 * Copyright (C) The Android Open Source Project
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
package com.example.shan.securityapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shan.securityapp.camera.CameraSourcePreview;
import com.example.shan.securityapp.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
//import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";


    private String filepath = "MyFileStorage";
    File dir;

    Button takePicture;
    TextView counterTxt;
//    ImageView img;

    private CameraSource mCameraSource = null;

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        Log.e(TAG,"in onCreate");

        counterTxt=(TextView)findViewById(R.id.counter);

//        img =(ImageView)findViewById(R.id.imageView);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
//        takePicture=(Button)findViewById(R.id.takePicture);
//         Check for the camera permission before accessing the camera.  If the
//         permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }


//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(FaceTrackerActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//            return;
//        }


//        takePicture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                takePicture();
//            }
//        });
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }


            mCameraSource = new CameraSource.Builder(context, detector)
                    .setRequestedPreviewSize(640, 480)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(30.0f)
                    .build();





    }

//    private void createCameraSource2() {
//
//        Context context = getApplicationContext();
//        FaceDetector detector = new FaceDetector.Builder(context)
//                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
//                .build();
//
//        detector.setProcessor(
//                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
//                        .build());
//
//        if (!detector.isOperational()) {
//            // Note: The first time that an app using face API is installed on a device, GMS will
//            // download a native library to the device in order to do detection.  Usually this
//            // completes before the app is run for the first time.  But if that download has not yet
//            // completed, then the above call will not detect any faces.
//            //
//            // isOperational() can be used to check if the required native library is currently
//            // available.  The detector will automatically become operational once the library
//            // download completes on device.
//            Log.w(TAG, "Face detector dependencies are not yet available.");
//        }
//
//
//        mCameraSource = new CameraSource.Builder(context, detector)
//                .setRequestedPreviewSize(640, 480)
//                .setFacing(CameraSource.CAMERA_FACING_FRONT)
//                .setRequestedFps(30.0f)
//                .build();
//
//
//
//
//
//    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();

        animateTextView(1,10,counterTxt);
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();

    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    private void  stopCameraSource(){
      //  mCameraSource.release();
       // mCameraSource.stop();
        mPreview.stop();
        mPreview.release();
        if(mCameraSource!=null) {
            mCameraSource = null;


        }
       // finish();

        startActivity(new Intent(FaceTrackerActivity.this,BarcodeCaptureActivity.class));





    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
//            Toast.makeText(FaceTrackerActivity.this,"Face detected",Toast.LENGTH_LONG).show();



//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(FaceTrackerActivity.this,"Face detected handler",Toast.LENGTH_LONG).show();
//                }
//            });
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

            Log.e(TAG,"in on update");

            startCounter();



//            createCameraSource(2);
//            onDone();
//            takePicture();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    takePicture();
//                }
//            }, 3000);

//            Toast.makeText(FaceTrackerActivity.this,"Face detected in update",Toast.LENGTH_LONG).show();
//            FaceTrackerActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(FaceTrackerActivity.this,"Face detected onupdate",Toast.LENGTH_LONG).show();
//                }
//            });
//            takePicture();
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    protected void takePicture(){
        Log.e(TAG,"in takePicture");
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            private File imageFile;

            @Override
            public void onPictureTaken(byte[] bytes) {

//                final File file = new File(Environment.getExternalStorageDirectory()+"/pic2.jpg");
//                final File file = new File(FaceTrackerActivity.this.getExternalFilesDir(null), "pic2.jpg");
                //ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
               // File directory = contextWrapper.getDir(getExternalFilesDir(filepath) , "pic5");
                File myInternalFile = new File(getExternalFilesDir(filepath) , "pic5.jpg");
                Log.e(TAG ,"bytes :"+bytes.toString());
                Log.e(TAG ,"bytes :"+bytes.toString());

                String byte3= null;
                try {
                    byte3 = new String(bytes, "ISO-8859-1");
                    byte[] encoded = byte3.getBytes("ISO-8859-1");
                    byte[] encoded1 = byte3.getBytes("ISO-8859-1");

                    String encodedString = Base64.encodeToString(bytes, Base64.NO_WRAP);
                    byte[] decodedBytes = Base64.decode(encodedString, Base64.NO_WRAP);
                    byte[] decodedBytes2 = Base64.decode(encodedString, Base64.NO_WRAP);

                    Bitmap bitmap4 = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);





                    /*try{
                        img.setImageBitmap(bitmap4);
                    }
                    catch (Exception e){
                        Log.e("error",e.getMessage());
                    }*/
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                byte[] byte2= byte3.getBytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(byte2, 0, byte2.length);
                Bitmap bitmap2 = BitmapFactory.decodeByteArray(byte2, 0, byte2.length);
                Bitmap bitmap3 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bitmap4 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bitmap5 = BitmapFactory.decodeFile(myInternalFile.getAbsolutePath());
                Bitmap bitmap6 = BitmapFactory.decodeFile(myInternalFile.getPath());
//                Bitmap bitmap7 = BitmapFactory.decodeFile(myInternalFile.getCanonicalPath());

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap8 = BitmapFactory.decodeFile(myInternalFile.getPath(), options);
                Bitmap bitmap9 = BitmapFactory.decodeFile(myInternalFile.getAbsolutePath(), options);








                myInternalFile.getAbsoluteFile();
                myInternalFile.getAbsoluteFile();
                String a=myInternalFile.getAbsolutePath();
                a=myInternalFile.getAbsolutePath();

//            saveDataToSharedPrefetences("faceImage",Base64.encodeToString(bytes, Base64.NO_WRAP));
//            saveDataToSharedPrefetences("ImagePath",myInternalFile.getAbsolutePath());

                OutputStream output = null;
                try {
                    output = new FileOutputStream(myInternalFile);
                    output.write(bytes);



          /*          BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap8 = BitmapFactory.decodeFile(myInternalFile.getPath(), options);
                    Bitmap bitmap9 = BitmapFactory.decodeFile(myInternalFile.getAbsolutePath(), options);*/


                    Toast.makeText(FaceTrackerActivity.this,"Image captured successfully", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stopCameraSource();
                }
                catch(FileNotFoundException E){
                    Log.e(TAG,"FileNotFoundException");
                }
                catch (IOException e) {
                    Log.e(TAG,"IOException");
                }






//                try {
//
//                    Toast.makeText(FaceTrackerActivity.this,"picture taken",Toast.LENGTH_SHORT).show();
//                    // convert byte array into bitmap
//                    Bitmap loadedImage = null;
//                    Bitmap rotatedBitmap = null;
//                    loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
//                            bytes.length);
//
//                    Matrix rotateMatrix = new Matrix();
//                    rotateMatrix.postRotate(0);
//                    rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
//                            loadedImage.getWidth(), loadedImage.getHeight(),
//                            rotateMatrix, false);
//
//                    dir = new File(
//                            Environment.getExternalStoragePublicDirectory(
//                                    Environment.DIRECTORY_PICTURES), "MyPhotos");
//
//                    boolean success = true;
//                    if (!dir.exists())
//                    {
//                        success = dir.mkdirs();
//                    }
//                    if (success) {
//                        java.util.Date date = new java.util.Date();
//                        imageFile = new File(dir.getAbsolutePath()
//                                + File.separator
//                                + new Timestamp(date.getTime()).toString()
//                                + "Image.jpg");
//
//                        imageFile.createNewFile();
//                    } else {
//                        Toast.makeText(getBaseContext(), "Image Not saved",
//                                Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
//
//                    // save image into gallery
//                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
//
//                    FileOutputStream fout = new FileOutputStream(imageFile);
//                    fout.write(ostream.toByteArray());
//                    fout.close();
//                    ContentValues values = new ContentValues();
//
//                    values.put(MediaStore.Images.Media.DATE_TAKEN,
//                            System.currentTimeMillis());
//                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                    values.put(MediaStore.MediaColumns.DATA,
//                            imageFile.getAbsolutePath());
//
//                    FaceTrackerActivity.this.getContentResolver().insert(
//                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//                    //saveToInternalStorage(loadedImage);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });

    }


    public  void saveDataToSharedPrefetences(String key, String value){
        SharedPreferences pref= this.getApplicationContext().getSharedPreferences("dataPref",0);
        pref.edit().putString(key,value).commit();

    }

    public void startCounter(){

    }

    public void animateTextView(final int initialValue, final int finalValue, final TextView textview) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
                valueAnimator.setDuration(10000);

                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {

                        textview.setText(valueAnimator.getAnimatedValue().toString());
                        if((Integer)valueAnimator.getAnimatedValue()==10){
                            takePicture();
                        }

                    }
                });
                valueAnimator.start();
            }
        });



    }



/*    protected void takePicture() {
        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }*/
}
