package hardingllc.pockethinman_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PanAndResizeActivity extends AppCompatActivity {

    private Button cancelPanResizeButton;
    private Button donePanResizeButton;
    private ImageView resizeImageView;
    private ZoomableViewGroup resizeZoomableViewGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pan_and_resize);

        configureView();

    }

    private void configureView() {

        cancelPanResizeButton = (Button) findViewById(R.id.cancelResizePanButton);
        donePanResizeButton = (Button) findViewById(R.id.doneResizePanButton);
        resizeImageView = (ImageView) findViewById(R.id.resizeImageView);
        resizeZoomableViewGroup = (ZoomableViewGroup) findViewById(R.id.resizeZoomableViewGroup);

        configureCancelButtonAction();
        configureDoneButtonAction();
    }

    public void configureCancelButtonAction() {

        cancelPanResizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void configureDoneButtonAction() {

        donePanResizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
