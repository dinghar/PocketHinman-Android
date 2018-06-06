package hardingllc.pockethinman_android;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class PanAndResizeActivity extends AppCompatActivity {

    private Button cancelPanResizeButton;
    private Button donePanResizeButton;
    private ImageView resizeImageView;
    private ZoomableViewGroup resizeZoomableViewGroup;

    private String fileName;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pan_and_resize);

        configureView();
        Intent intent = getIntent();
        fileName = intent.getExtras().getString("filePath");
        file = new File(fileName);
        Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaStoreUpdateIntent.setData(Uri.fromFile(file));
        resizeImageView.setImageURI(Uri.fromFile(file));
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
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("zoomableViewGroup mScaleFactor", resizeZoomableViewGroup.mScaleFactor);
//                resultIntent.putExtra("zoomableViewGroup mFocusX", resizeZoomableViewGroup.mFocusX);
//                resultIntent.putExtra("zoomableViewGroup mFocusY", resizeZoomableViewGroup.mFocusY);
//                resultIntent.putExtra("zoomableViewGroup mPosX", resizeZoomableViewGroup.mPosX);
//                resultIntent.putExtra("zoomableViewGroup mPosY", resizeZoomableViewGroup.mPosY);
//                setResult(2, resultIntent);
                finish();
            }
        });
    }
}
