package com.example.myapplicationopencv;



import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String    TAG = "OCVSample::Activity";

    private byte[] outarray;
    private byte[] tmparray;

    private int w;

    private int h;


    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        //mRgba = new Mat(height, width, CvType.CV_8UC4);
        outarray = new byte[width*height];
        w=width;
        h=height;

    }

    public void onCameraViewStopped() {
        //mRgba.release();

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat gray =inputFrame.gray();
        MatToArray(gray);
//        gradient();
        sobel();
        //Sutf to do here by the students...

        Mat out=ArrayToMat(gray,outarray);
        return out;
    }

    private Mat ArrayToMat(Mat gray, byte[] grayarray) {
        // TODO Auto-generated method stub
        Mat out = gray.clone();
        out.put(0,0,grayarray);
        return out;
    }

    private void MatToArray(Mat gray) {
        // TODO Auto-generated method stub
        gray.get(0, 0, outarray);

    }

    private void gradient() {
        // allocation de l'array tmp
        tmparray = new byte[w*h];
        // avec i on parcourt la largeur de l'image
        for (int i = 1; i < w-1; i++) {
            //avec j on parcourt la hauteur de l'image
            for (int j = 1; j < h-1; j++) {
                // calcul du gradient horizontal
                int gradH = outarray[index(i - 1, j)] - outarray[index(i + 1, j)];
                // calcul du gradient vertical
                int gradV = outarray[index(i, j - 1)] - outarray[index(i, j + 1)];
                //on affecte la valeur du pixel au tableau temporaire
                tmparray[index(i,j)] = (byte) (gradH + gradV);

            }
        }
        //on remplace outarray par notre tableau temporaire
        outarray = tmparray.clone();
    }

    private void sobel(){
        // allocation de l'array tmp
        tmparray = new byte[w*h];
        // Création des variables Gx et Gy
        int gx;
        int gy;
        // avec i on parcourt la largeur de l'image
        for (int i = 2; i < w-1; i++) {
            //avec j on parcourt la hauteur de l'image
            for (int j = 1; j < h-1; j++) {
                // Calcul de Gx
                gx = (-1*outarray[index(i - 1, j + 1)]) + outarray[index(i + 1, j + 1)]
                        + (-2*outarray[index(i - 1, j)]) + (2*outarray[index(i + 1, j)])
                        + (-1*outarray[index(i -1, j - 1)]) + outarray[index(i + 1, j - 1)];
                // Calcul de Gy
                gy = (-1*outarray[index(i - 1, j + 1)]) + (-2*outarray[index(i, j + 1)])
                        + (-1*outarray[index(i + 1, j + 1)]) + outarray[index(i - 1, j - 1)]
                        + (2*outarray[index(i, j - 1)]) + outarray[index(i + 1, j - 1)];
                //on affecte la valeur du pixel au tableau temporaire
                tmparray[index(i,j)] = (byte) Math.sqrt(Math.pow(gx,gx)+Math.pow(gy,gy));
            }
        }
        //on remplace outarray par notre tableau temporaire
        outarray = tmparray.clone();
    }

    private int index(int x, int y){
        return y*w+x;
    }
}