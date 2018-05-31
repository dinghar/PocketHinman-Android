package hardingllc.pockethinman_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    Boolean isPlaying = false;

    private Context context;
    private RelativeLayout layout;

    private Button playButton;
    private Button photosButton;
    private Button settingsButton;
    private Button cameraButton;
    private Button cancelButton;

    private ImageView imageView;

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        layout = (RelativeLayout) findViewById(R.id.activity_main);

        configureView();


        formatSlider();
        formatZoomView();

    }

    public void configureView() {

        playButton = (Button) findViewById(R.id.playButton);
        photosButton = (Button) findViewById(R.id.photosButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        imageView = (ImageView) findViewById(R.id.imageView);


        configurePlayButtonAction();
        configurePhotosButtonAction();
        configureSettingsButtonAction();
        configureCameraButtonAction();
        configureCancelButtonAction();
    }

    public void formatZoomView() {
        ZoomableViewGroup zoomableViewGroup = (ZoomableViewGroup) findViewById(R.id.zoomableViewGroup);
    }

    public void formatSlider() {
        SeekBar slider = (SeekBar) findViewById(R.id.seekBar);
    }

    public void configurePlayButtonAction() {

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button playButton = (Button) findViewById(R.id.playButton);
                String text = playButton.getText().toString();
                if (isPlaying) {
                    isPlaying = false;
                    playButton.setText("Play");
                    flicker();
                } else {
                    isPlaying = true;
                    playButton.setText("Pause");
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
                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View calloutView = layoutInflater.inflate(R.layout.settings_callout, null);
                    popupWindow = new PopupWindow(calloutView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if(Build.VERSION.SDK_INT>=21){
                        popupWindow.setElevation(5.0f);
                    }
                    popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 120);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }



    public void flicker() {

    }

}