package hardingllc.pockethinman_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {

    Boolean isPlaying = false;

    private Context mContext;
    private RelativeLayout mLayout;

//    private Camera mCamera;
//    private CameraPreview mCameraPreview;
//    private static final int PERMISSION_REQUEST_CODE = 200;

    private TextureView textureView;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean flashSupported;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private int DSI_height;
    private int DSI_width;


    private Button playButton;
    private Button photosButton;
    private Button settingsButton;
    private Button cameraButton;
    private Button cancelButton;
    private SeekBar slider;
    private ZoomableViewGroup zoomableViewGroup;
    private ImageView imageView;
    private PopupWindow popupWindow;


    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mLayout = (RelativeLayout) findViewById(R.id.activity_main);

        configureView();

        formatSlider();
        formatZoomView();
        formatImageView();
    }


    // View configuration

    public void configureView() {

        playButton = (Button) findViewById(R.id.playButton);
        photosButton = (Button) findViewById(R.id.photosButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        imageView = (ImageView) findViewById(R.id.imageView);
        zoomableViewGroup = (ZoomableViewGroup) findViewById(R.id.zoomableViewGroup);
        textureView = (TextureView) findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);


        configurePlayButtonAction();
        configurePhotosButtonAction();
        configureSettingsButtonAction();
        configureCameraButtonAction();
        configureCancelButtonAction();
    }

    public void formatZoomView() {
        ZoomableViewGroup zoomableViewGroup = (ZoomableViewGroup) findViewById(R.id.zoomableViewGroup);
    }

    public void formatImageView() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        Float imageAlpha = sharedPref.getFloat("imageAlpha", 0.5f);
        imageView.setAlpha(imageAlpha);
    }

    public String getImageAlpha() {
        Float imageAlpha = imageView.getAlpha();
        return String.format("%.1f", imageAlpha);
    }

    public void setImageAlpha(Float newImageAlpha) {
        imageView.setAlpha(newImageAlpha);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("imageAlpha", newImageAlpha);
        editor.commit();
    }

    public void formatSlider() {
        slider = (SeekBar) findViewById(R.id.seekBar);
    }

    public void configurePlayButtonAction() {

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button playButton = (Button) findViewById(R.id.playButton);
                String text = playButton.getText().toString();
                if (isPlaying) {
                    isPlaying = false;
                    playButton.setBackgroundResource(R.drawable.playbutton);
                    imageView.setVisibility(View.VISIBLE);
                    textureView.setVisibility(View.VISIBLE);
                    formatImageView();
                } else {
                    isPlaying = true;
                    playButton.setBackgroundResource(R.drawable.pausebutton);
                    imageView.setAlpha((float) 1);
                    flicker();
                }
            }
        });
    }

    public void configurePhotosButtonAction() {

        photosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });
    }

    public void configureSettingsButtonAction() {

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow == null) {
                    LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View calloutView = layoutInflater.inflate(R.layout.settings_callout, null);
                    popupWindow = new PopupWindow(calloutView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView alphaLabel = (TextView) calloutView.findViewById(R.id.alphaLabel);
                    alphaLabel.setText("Alpha: " + getImageAlpha());
                    Button alphaStepperMinusButton = (Button) calloutView.findViewById(R.id.alphaStepperMinus);
                    alphaStepperMinusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Float oldImageAlpha = imageView.getAlpha();
                            Float newImageAlpha = oldImageAlpha - 0.1f;
                            if (newImageAlpha < 0f) {
                                newImageAlpha = 0f;
                            }
                            setImageAlpha(newImageAlpha);
                            TextView alphaLabel = (TextView) calloutView.findViewById(R.id.alphaLabel);
                            alphaLabel.setText("Alpha: " + getImageAlpha());
                        }
                    });
                    Button alphaStepperPlusButton = (Button) calloutView.findViewById(R.id.alphaStepperPlus);
                    alphaStepperPlusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Float oldImageAlpha = imageView.getAlpha();
                            Float newImageAlpha = oldImageAlpha + 0.1f;
                            if (newImageAlpha > 1f) {
                                newImageAlpha = 1f;
                            }
                            setImageAlpha(newImageAlpha);
                            TextView alphaLabel = (TextView) calloutView.findViewById(R.id.alphaLabel);
                            alphaLabel.setText("Alpha: " + getImageAlpha());
                        }
                    });
                    Button panResizeButton = (Button) calloutView.findViewById(R.id.panResizeButton);
                    panResizeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, PanAndResizeActivity.class);
                            if (imageView.getDrawable() != null) {
                                intent.putExtra("filePath", file.getAbsolutePath());
                                startActivity(intent);
                            }
                        }
                    });
                    if (Build.VERSION.SDK_INT>=21) {
                        popupWindow.setElevation(5.0f);
                    }
                    popupWindow.showAtLocation(mLayout, Gravity.BOTTOM, 0, 120);
                } else {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
            }
        });
    }

    public void configureCameraButtonAction() {

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    public void configureCancelButtonAction() {

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageResource(0);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Uri targetUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (resultCode == 2) {
            float mScaleFactor = data.getExtras().getFloat("mScaleFactor");
            float mFocusX = data.getExtras().getFloat("mFocusX");
            float mFocusY = data.getExtras().getFloat("mFocusY");
            float mPosX = data.getExtras().getFloat("mPosX");
            float mPosY = data.getExtras().getFloat("mPosY");
            zoomableViewGroup.setMScaleFactor(mScaleFactor);
            zoomableViewGroup.setMFocusX(mFocusX);
            zoomableViewGroup.setMFocusY(mFocusY);
            zoomableViewGroup.setMPosX(mPosX);
            zoomableViewGroup.setMPosY(mPosY);
            zoomableViewGroup.invalidate();
        }
    }

    public void flicker() {

        runOnUiThread(new Runnable(){
            public void run() {
                if (isPlaying) {
                    final double sliderVal = slider.getProgress();
                    if (imageView.getVisibility() == View.INVISIBLE) {
                        imageView.setVisibility(View.VISIBLE);
                        textureView.setVisibility(View.INVISIBLE);
                    } else {
                        imageView.setVisibility(View.INVISIBLE);
                        textureView.setVisibility(View.VISIBLE);
                    }
                    long delay = (long) (Math.pow(1 - sliderVal/500, 20) * 1000);
                    new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                flicker();
                            }
                        },
                        delay
                    );
                }
            }
        });
    }


    // Camera functions

    private void takePicture() {

        if (cameraDevice == null)
            return;

        CameraManager manager  = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));


            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            file = new File(Environment.getExternalStorageDirectory()+"/"+UUID.randomUUID().toString()+".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireNextImage(); //reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    runOnUiThread(new Runnable(){
                        public void run() {
                            Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaStoreUpdateIntent.setData(Uri.fromFile(file));
                            imageView.setImageURI(Uri.fromFile(file));
                        }
                    });
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, backgroundHandler);



        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {

        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            DSI_height = displayMetrics.heightPixels;
            DSI_width = displayMetrics.widthPixels;
            setAspectRatioTextureView(imageDimension.getHeight(),imageDimension.getWidth());


            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {

        }
    }

    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight )
    {
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionWidth)/ResolutionHeight);
            updateTextureViewSize(newWidth,newHeight);

        }else {
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionHeight)/ResolutionWidth);
            updateTextureViewSize(newWidth,newHeight);
        }

    }

    private void updateTextureViewSize(final int viewWidth, final int viewHeight) {
        runOnUiThread(new Runnable(){
            public void run() {
                textureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
            }
        });
    }

    private void updatePreview() {

        if (cameraDevice == null)
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void openCamera() {

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Camera permission required for use.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {

        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }


}