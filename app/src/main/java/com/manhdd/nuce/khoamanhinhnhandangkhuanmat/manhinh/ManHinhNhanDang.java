package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.manhinh;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.DichVuManHinhKhoa;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.NativeMethods;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;
import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.chung.FaceDB;
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
    public final long DISCONNECT_TIMEOUT = 60000;

    public WindowManager winManager;
    public RelativeLayout wrapperView;

    private View rootView;
    private CameraBridgeViewBase mNhanDangCameraView;
    private Button btNhanDang;
    private LinearLayout llMatKhau;

    private Mat mRgba, mGray;
    private FaceDB tinydb;
    private ArrayList<Mat> images;
    private ArrayList<String> imagesLabels;
    private NativeMethods.TrainFacesTask mTrainFacesTask;
    private String[] uniqueLabels;
    private boolean isSetting, isRecognizeOldFace;
    private SharedPreferences sharedPreferences;

    private Handler disconnectHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };

    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    private NativeMethods.TrainFacesTask.Callback trainFacesTaskCallback = new NativeMethods.TrainFacesTask.Callback() {
        @Override
        public void onTrainFacesComplete(boolean result) {
            if (result)
                Log.e(TAG, "Training complete");
            else
                Log.e(TAG, "Training failed");
        }
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    NativeMethods.loadNativeLibraries(); // Sau khi load openCV thi load thu vien nhan dang
                    Log.e(TAG, "OpenCV loaded successfully");
                    mNhanDangCameraView.enableView();

                    // Lay du lieu anh va label tu shared preference
                    images = tinydb.getListMat("images");
                    imagesLabels = tinydb.getListString("imagesLabels");

                    Log.e(TAG, "Number of images: " + images.size() + ". Number of labels: " + imagesLabels.size());
                    if (!images.isEmpty()) {
                        trainFaces(); // Train images after they are loaded
                        Log.e(TAG, "Images height: " + images.get(0).height() + " Width: " + images.get(0).width() + " total: " + images.get(0).total());
                    }
                    Log.e(TAG, "Labels: " + imagesLabels);

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
            if (bundle == null) {
                Toast.makeText(ManHinhNhanDang.this, "Mở khoá không thành công Vui lòng thử lại", Toast.LENGTH_LONG).show();
                return;
            }

            float minDist = bundle.getFloat(NativeMethods.MeasureDistTask.MIN_DIST_FLOAT);
            if (minDist != -1) {
                int minIndex = bundle.getInt(NativeMethods.MeasureDistTask.MIN_DIST_INDEX_INT);
                float faceDist = bundle.getFloat(NativeMethods.MeasureDistTask.DIST_FACE_FLOAT);
                if (imagesLabels.size() > minIndex) { // Just to be sure
                    Log.e(TAG, "dist[" + minIndex + "]: " + minDist + ", face dist: " + faceDist + ", label: " + imagesLabels.get(minIndex));

                    String minDistString = String.format(Locale.US, "%.4f", minDist);
                    String faceDistString = String.format(Locale.US, "%.4f", faceDist);

                    if (faceDist < 0.26f && minDist < 0.1f) { // 1. Near face space and near a face class
                        Log.e("Recognize success", "Face detected: " + imagesLabels.get(minIndex) + ". Distance: " + minDistString);
                        if(isSetting) {
                            isRecognizeOldFace = false;
                            btNhanDang.setText("Lưu khuân mặt");
                            images = new ArrayList<>();
                            imagesLabels = new ArrayList<>();
                            Toast.makeText(ManHinhNhanDang.this, "Nhận dạng thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            moKhoa();
                            Toast.makeText(ManHinhNhanDang.this, "Mở khoá thành công", Toast.LENGTH_SHORT).show();
                        }
                    } else if (faceDist < 0.26f) { // 2. Near face space but not near a known face class
                        if(isSetting) {
                            Toast.makeText(ManHinhNhanDang.this, "Nhận dạng không thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManHinhNhanDang.this, "Mở khoá không thành công Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                        Log.e("Recognize failed", "Unknown face. Face distance: " + faceDistString + ". Closest Distance: " + minDistString);
                    } else if (minDist < 0.1f) { // 3. Distant from face space and near a face class
                        if(isSetting) {
                            Toast.makeText(ManHinhNhanDang.this, "Nhận dạng không thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManHinhNhanDang.this, "Mở khoá không thành công Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                        Log.e("Recognize failed", "False recognition. Face distance: " + faceDistString + ". Closest Distance: " + minDistString);
                    } else { // 4. Distant from face space and not near a known face class.
                        if(isSetting) {
                            Toast.makeText(ManHinhNhanDang.this, "Nhận dạng không thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManHinhNhanDang.this, "Mở khoá không thành công Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                        Log.e("Recognize failed", "Image is not a face. Face distance: " + faceDistString + ". Closest Distance: " + minDistString);
                    }
                }
            } else {
                Log.e(TAG, "Min dist=" + minDist);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sharedPreferences = getSharedPreferences("KhoaManHinhNhanDangKhuanMat", Context.MODE_PRIVATE);

        isSetting = getIntent().getBooleanExtra("is_setting", true);

        if (isSetting) {
            setContentView(R.layout.man_hinh_nhan_dang);
            rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        } else {
            WindowManager.LayoutParams localLayoutParams = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localLayoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
            } else {
                localLayoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
            }
            this.winManager = ((WindowManager) getApplicationContext()
                    .getSystemService(WINDOW_SERVICE));
            this.wrapperView = new RelativeLayout(getBaseContext());
            getWindow().setAttributes(localLayoutParams);
            rootView = View.inflate(this, R.layout.man_hinh_nhan_dang, this.wrapperView);
            this.winManager.addView(this.wrapperView, localLayoutParams);
        }

        tinydb = new FaceDB(this);

        mNhanDangCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.camera_view);
        mNhanDangCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mNhanDangCameraView.setVisibility(SurfaceView.VISIBLE);
        mNhanDangCameraView.setCvCameraViewListener(this); // cai dat cac ham lang nghe su kien

        ImageView ivKhanCap = (ImageView) rootView.findViewById(R.id.iv_khan_cap);

        ImageView ivQuayLai = (ImageView) rootView.findViewById(R.id.iv_quay_lai);
        Drawable icon = getResources().getDrawable(R.drawable.ic_quay_lai);
        icon.mutate();
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        ivQuayLai.setImageDrawable(icon);
        if (isSetting) {
            ivQuayLai.setVisibility(View.GONE);
            ivKhanCap.setVisibility(View.GONE);
        } else {
            ivQuayLai.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (llMatKhau.getVisibility() == View.VISIBLE) {
                        llMatKhau.setVisibility(View.GONE);
                    } else {
                        finish();
                    }
                }
            });

            llMatKhau = (LinearLayout) rootView.findViewById(R.id.ll_mat_khau);
            hienThiCaiDatMatMa();

            String matKhau = sharedPreferences.getString("mat_khau", "");
            if (matKhau.equals("")) {
                ivKhanCap.setVisibility(View.GONE);
            } else {
                ivKhanCap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        llMatKhau.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        btNhanDang = (Button) rootView.findViewById(R.id.bt_nhan_dang);
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
                Imgproc.resize(mGray, mGray, imageSize); // resize lai anh
                Log.i(TAG, "Small gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
                //SaveImage(mGray);

                Mat image = mGray.reshape(0, (int) mGray.total()); // Tao cac column vector

                if (isSetting && !isRecognizeOldFace) {
                    onSetupFace(image);
                } else {
                    onRecognizeFace(image, mMeasureDistTask);
                }
            }
        });

        if (sharedPreferences.getBoolean("is_first_run", true)) {
            hienThiHuongDan();
            btNhanDang.setText("Lưu khuân mặt");
        } else if(isSetting) {
            isRecognizeOldFace = true;
            btNhanDang.setText("Nhận dạng khuân mặt đã lưu");
        } else {
            btNhanDang.setText("Mở khoá");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Hoi quyen truy cap camera
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
    protected void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }

    @Override
    public void onDestroy() {
        if (!isSetting) {
            this.winManager.removeView(this.wrapperView);
            this.wrapperView.removeAllViews();
        }
        super.onDestroy();
        if (mNhanDangCameraView != null) {
            mNhanDangCameraView.disableView();
        }
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
    public void onUserInteraction() {
        resetDisconnectTimer();
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

        // Xoay lai anh cho dung chieu
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
     * train dữ liệu cho phần dữ xử lý
     *
     * @return Tra ve false neu task nay dang chay
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

        // day du lieu vao cho phan xu ly
        mTrainFacesTask = new NativeMethods.TrainFacesTask(imagesMatrix, vectorClasses, trainFacesTaskCallback);

        mTrainFacesTask.execute();

        return true;
    }

    // Lưu khuân mặt mới
    private void onSetupFace(Mat image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(image);

        if (imagesLabels == null) {
            imagesLabels = new ArrayList<>();
        }

        if (images.size() == 3) {
            imagesLabels.add("Admin_2");
            tinydb.putListMat("images", images);
            tinydb.putListString("imagesLabels", imagesLabels);

            if (sharedPreferences.getBoolean("is_first_run", true)) {
                Intent intent = new Intent(this, DichVuManHinhKhoa.class);
                startService(intent);
                sharedPreferences.edit().putBoolean("is_first_run", false).apply();
            }

            Toast.makeText(this, "Lưu khuân mặt thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            imagesLabels.add("Admin_" + images.size());

            Toast.makeText(this, "Chụp lại một lần nữa", Toast.LENGTH_SHORT).show();
        }
    }

    // Xử lý nhận dạng khuân mặt
    private void onRecognizeFace(Mat image, NativeMethods.MeasureDistTask mMeasureDistTask) {
        mMeasureDistTask = new NativeMethods.MeasureDistTask(false, measureDistTaskCallback);
        mMeasureDistTask.execute(image);
    }

    private void moKhoa() {
        finishAffinity();
    }

    public void resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
    }

    private void hienThiHuongDan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hướng dẫn")
                .setMessage("Bạn cần lưu khuân mặt ở 3 góc khác nhau")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void hienThiCaiDatMatMa() {

        final String matKhau = sharedPreferences.getString("mat_khau", "");

        final TextView tvMatKhau = (TextView) rootView.findViewById(R.id.tv_nhap_mat_khau);
        tvMatKhau.setText("Nhập mật khẩu");

        final AppCompatEditText etMatKhau = (AppCompatEditText) rootView.findViewById(R.id.et_mat_khau);
        etMatKhau.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pass = editable.toString();
                if (pass.equals(matKhau)) {
                    moKhoa();
                }
            }
        });

        ImageView ivXoa = (ImageView) rootView.findViewById(R.id.iv_xoa);
        Drawable icon = getResources().getDrawable(R.drawable.ic_xoa);
        icon.mutate();
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        ivXoa.setImageDrawable(icon);

        rootView.findViewById(R.id.tv_so_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "1"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "2"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "3"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "4"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "5"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "6"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "7"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "8"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "9"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.tv_so_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etMatKhau.setText(etMatKhau.getText().insert(etMatKhau.getSelectionEnd(), "0"));
                etMatKhau.setSelection(etMatKhau.getText().length());
            }
        });

        rootView.findViewById(R.id.rl_xoa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = etMatKhau.getText().toString();
                if (pass.length() > 0) {
                    etMatKhau.setText(pass.substring(0, pass.length() - 1));
                }
            }
        });
    }

}
