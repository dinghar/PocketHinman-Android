package hardingllc.pockethinman_android;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    Boolean isPlaying = false;

    private Context context;

    private Button playButton;
    private Button photosButton;
    private Button settingsButton;
    private Button cameraButton;
    private Button cancelButton;

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        configureView();
        configureSettingsButtonAction();
        configurePlayButtonAction();

        formatSlider();
        formatZoomView();

    }

    public void configureView() {

        playButton = (Button) findViewById(R.id.playButton);
        photosButton = (Button) findViewById(R.id.photosButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
    }

    public void formatZoomView() {
        ZoomableViewGroup zoomableViewGroup = (ZoomableViewGroup) findViewById(R.id.zoomableViewGroup);
    }

    public void formatSlider() {
        SeekBar slider = (SeekBar) findViewById(R.id.seekBar);

    }


    public void configureSettingsButtonAction() {

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow == null) {
                    popupWindow = new PopupWindow();
                } else {
                    popupWindow = null;
                }
            }
        });

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



    public void flicker() {

    }

}