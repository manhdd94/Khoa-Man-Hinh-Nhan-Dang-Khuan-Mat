package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.NativeMethods;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.chung.TinyDB;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.view.CameraBridgeViewBase;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ManHinhNhanDang extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = ManHinhNhanDang.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 3232;

    private CameraBridgeViewBase mNhanDangCameraView;
    private Button btNhanDang;

    private Mat mRgba, mGray;
    private TinyDB tinydb;
    private ArrayList<Mat> images;
    private ArrayList<String> imagesLabels;
    private NativeMethods.TrainFacesTask mTrainFacesTask;
    private String[] uniqueLabels;
    private boolean isSetting;

    private NativeMethods.TrainFacesTask.Callback trainFacesTaskCallback = new NativeMethods.TrainFacesTask.Callback() {
        @Override
        public void onTrainFacesComplete(boolean result) {
            if (result)
                Toast.makeText(ManHinhNhanDang.this, "Training complete", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ManHinhNhanDang.this, "Training failed", Toast.LENGTH_LONG).show();
        }
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    NativeMethods.loadNativeLibraries(); // Load native libraries after(!) OpenCV initialization
                    Log.e(TAG, "OpenCV loaded successfully");
                    mNhanDangCameraView.enableView();

                    if(!isSetting) {
                        // Read images and labels from shared preferences
                        images = tinydb.getListMat("images");
                        imagesLabels = tinydb.getListString("imagesLabels");

                        Log.e(TAG, "Number of images: " + images.size() + ". Number of labels: " + imagesLabels.size());
                        if (!images.isEmpty()) {
                            trainFaces(); // Train images after they are loaded
                            Log.e(TAG, "Images height: " + images.get(0).height() + " Width: " + images.get(0).width() + " total: " + images.get(0).total());
                        }
                        Log.e(TAG, "Labels: " + imagesLabels);
                    }

                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private NativeMethods.MeasureDistTask.Callback measureDistTaskCallback = new NativeMethods.MeasureDistTask.Callback() {
        @Override
        public void onMeasureDistComplete(Bundle bundle) {
//            if (bundle == null) {
//                showToast("Failed to measure distance", Toast.LENGTH_LONG);
//                return;
//            }
//
//            float minDist = bundle.getFloat(NativeMethods.MeasureDistTask.MIN_DIST_FLOAT);
//            if (minDist != -1) {
//                int minIndex = bundle.getInt(NativeMethods.MeasureDistTask.MIN_DIST_INDEX_INT);
//                float faceDist = bundle.getFloat(NativeMethods.MeasureDistTask.DIST_FACE_FLOAT);
//                if (imagesLabels.size() > minIndex) { // Just to be sure
//                    Log.i(TAG, "dist[" + minIndex + "]: " + minDist + ", face dist: " + faceDist + ", label: " + imagesLabels.get(minIndex));
//
//                    String minDistString = String.format(Locale.US, "%.4f", minDist);
//                    String faceDistString = String.format(Locale.US, "%.4f", faceDist);
//
//                    if (faceDist < faceThreshold && minDist < distanceThreshold) // 1. Near face space and near a face class
//                        showToast("Face detected: " + imagesLabels.get(minIndex) + ". Distance: " + minDistString, Toast.LENGTH_LONG);
//                    else if (faceDist < faceThreshold) // 2. Near face space but not near a known face class
//                        showToast("Unknown face. Face distance: " + faceDistString + ". Closest Distance: " + minDistString, Toast.LENGTH_LONG);
//                    else if (minDist < distanceThreshold) // 3. Distant from face space and near a face class
//                        showToast("False recognition. Face distance: " + faceDistString + ". Closest Distance: " + minDistString, Toast.LENGTH_LONG);
//                    else // 4. Distant from face space and not near a known face class.
//                        showToast("Image is not a face. Face distance: " + faceDistString + ". Closest Distance: " + minDistString, Toast.LENGTH_LONG);
//                }
//            } else {
//                Log.w(TAG, "Array is null");
//                if (useEigenfaces || uniqueLabels == null || uniqueLabels.length > 1)
//                    showToast("Keep training...", Toast.LENGTH_SHORT);
//                else
//                    showToast("Fisherfaces needs two different faces", Toast.LENGTH_SHORT);
//            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.man_hinh_nhan_dang);

        isSetting = getIntent().getBooleanExtra("is_setting", true);

        tinydb = new TinyDB(this);

        mNhanDangCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mNhanDangCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mNhanDangCameraView.setVisibility(SurfaceView.VISIBLE);
        mNhanDangCameraView.setCvCameraViewListener(this);

        btNhanDang = (Button) findViewById(R.id.bt_nhan_dang);
        if (isSetting) {
            btNhanDang.setText("Lưu khuân mặt");
        } else {
            btNhanDang.setText("Nhận dạng khuân mặt");
        }
        btNhanDang.setOnClickListener(new View.OnClickListener() {

            NativeMethods.MeasureDistTask mMeasureDistTask;

            @Override
            public void onClick(View view) {
                if (mMeasureDistTask != null && mMeasureDistTask.getStatus() != AsyncTask.Status.FINISHED) {
                    Log.e(TAG, "mMeasureDistTask is still running");
                    return;
                }
                if (mTrainFacesTask != null && mTrainFacesTask.getStatus() != AsyncTask.Status.FINISHED) {
                    Log.e(TAG, "mTrainFacesTask is still running");
                    return;
                }

                Log.i(TAG, "Gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
                if (mGray.total() == 0)
                    return;
                Size imageSize = new Size(200, 200.0f / ((float) mGray.width() / (float) mGray.height())); // Scale image in order to decrease computation time
                Imgproc.resize(mGray, mGray, imageSize);
                Log.i(TAG, "Small gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
                //SaveImage(mGray);

                Mat image = mGray.reshape(0, (int) mGray.total()); // Create column vector

                if (isSetting) {
                    onSetupFace(image);
                } else {
                    mMeasureDistTask = new NativeMethods.MeasureDistTask(false, measureDistTaskCallback);
                    mMeasureDistTask.execute(image);
                }

//                Log.i(TAG, "Vector height: " + image.height() + " Width: " + image.width() + " total: " + image.total());
//                images.add(image); // Add current image to the array
//
//                if (images.size() > maximumImages) {
//                    images.remove(0); // Remove first image
//                    imagesLabels.remove(0); // Remove first label
//                    Log.i(TAG, "The number of images is limited to: " + images.size());
//                }
//
//                // Calculate normalized Euclidean distance
//                mMeasureDistTask = new NativeMethods.MeasureDistTask(false, measureDistTaskCallback);
//                mMeasureDistTask.execute(image);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Request permission if needed
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            loadOpenCV();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNhanDangCameraView != null)
            mNhanDangCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNhanDangCameraView != null)
            mNhanDangCameraView.disableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadOpenCV();
                } else {
                    Toast.makeText(this, "Permission required!", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mGrayTmp = inputFrame.gray();
        Mat mRgbaTmp = inputFrame.rgba();

        // Flip image to get mirror effect
        int orientation = mNhanDangCameraView.getScreenOrientation();
        if (mNhanDangCameraView.isEmulator()) // Treat emulators as a special case
            Core.flip(mRgbaTmp, mRgbaTmp, 1); // Flip along y-axis
        else {
            switch (orientation) { // RGB image
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    if (mNhanDangCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mRgbaTmp, mRgbaTmp, 0); // Flip along x-axis
                    else
                        Core.flip(mRgbaTmp, mRgbaTmp, -1); // Flip along both axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    if (mNhanDangCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mRgbaTmp, mRgbaTmp, 1); // Flip along y-axis
                    break;
            }
            switch (orientation) { // Grayscale image
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    Core.transpose(mGrayTmp, mGrayTmp); // Rotate image
                    if (mNhanDangCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mGrayTmp, mGrayTmp, -1); // Flip along both axis
                    else
                        Core.flip(mGrayTmp, mGrayTmp, 1); // Flip along y-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    Core.transpose(mGrayTmp, mGrayTmp); // Rotate image
                    if (mNhanDangCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK)
                        Core.flip(mGrayTmp, mGrayTmp, 0); // Flip along x-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                    if (mNhanDangCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mGrayTmp, mGrayTmp, 1); // Flip along y-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    Core.flip(mGrayTmp, mGrayTmp, 0); // Flip along x-axis
                    if (mNhanDangCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK)
                        Core.flip(mGrayTmp, mGrayTmp, 1); // Flip along y-axis
                    break;
            }
        }

        mGray = mGrayTmp;
        mRgba = mRgbaTmp;

        return mRgba;
    }

    private void loadOpenCV() {
        if (!OpenCVLoader.initDebug(true)) {
            Log.e(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.e(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * Train faces using stored images.
     *
     * @return Returns false if the task is already running.
     */
    private boolean trainFaces() {
        if (images.isEmpty())
            return true; // The array might be empty if the method is changed in the OnClickListener

        if (mTrainFacesTask != null && mTrainFacesTask.getStatus() != AsyncTask.Status.FINISHED) {
            Log.e(TAG, "mTrainFacesTask is still running");
            return false;
        }

        Mat imagesMatrix = new Mat((int) images.get(0).total(), images.size(), images.get(0).type());
        for (int i = 0; i < images.size(); i++)
            images.get(i).copyTo(imagesMatrix.col(i)); // Create matrix where each image is represented as a column vector

        Log.e(TAG, "Images height: " + imagesMatrix.height() + " Width: " + imagesMatrix.width() + " total: " + imagesMatrix.total());

        // Train the face recognition algorithms in an asynchronous task, so we do not skip any frames

        Log.e(TAG, "Training Fisherfaces");
        Toast.makeText(this, "Training Fisherfaces", Toast.LENGTH_SHORT).show();

        Set<String> uniqueLabelsSet = new HashSet<>(imagesLabels); // Get all unique labels
        uniqueLabels = uniqueLabelsSet.toArray(new String[uniqueLabelsSet.size()]); // Convert to String array, so we can read the values from the indices

        int[] classesNumbers = new int[uniqueLabels.length];
        for (int i = 0; i < classesNumbers.length; i++)
            classesNumbers[i] = i + 1; // Create incrementing list for each unique label starting at 1

        int[] classes = new int[imagesLabels.size()];
        for (int i = 0; i < imagesLabels.size(); i++) {
            String label = imagesLabels.get(i);
            for (int j = 0; j < uniqueLabels.length; j++) {
                if (label.equals(uniqueLabels[j])) {
                    classes[i] = classesNumbers[j]; // Insert corresponding number
                    break;
                }
            }
        }

            /*for (int i = 0; i < imagesLabels.size(); i++)
                Log.i(TAG, "Classes: " + imagesLabels.get(i) + " = " + classes[i]);*/

        Mat vectorClasses = new Mat(classes.length, 1, CvType.CV_32S); // CV_32S == int
        vectorClasses.put(0, 0, classes); // Copy int array into a vector

        mTrainFacesTask = new NativeMethods.TrainFacesTask(imagesMatrix, vectorClasses, trainFacesTaskCallback);

        mTrainFacesTask.execute();

        return true;
    }

    // Lưu khuân mặt mới
    private void onSetupFace(Mat image) {
        images = new ArrayList<>();
        images.add(image);

        imagesLabels = new ArrayList<>();
        imagesLabels.add("Admin");

        tinydb.putListMat("images", images);
        tinydb.putListString("imagesLabels", imagesLabels);

        Toast.makeText(this, "Lưu khuân mặt thành công", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Xử lý nhận dạng khuân mặt
    private void onRecognizeFace(Mat image, NativeMethods.MeasureDistTask mMeasureDistTask) {
        images.add(image);

        mMeasureDistTask = new NativeMethods.MeasureDistTask(false, measureDistTaskCallback);
        mMeasureDistTask.execute(image);
    }

    private void addLabel(String string) {
        String label = string.substring(0, 1).toUpperCase(Locale.US) + string.substring(1).trim().toLowerCase(Locale.US); // Make sure that the name is always uppercase and rest is lowercase
        imagesLabels.add(label); // Add label to list of labels
        Log.i(TAG, "Label: " + label);

        trainFaces(); // When we have finished setting the label, then retrain faces
    }

}
