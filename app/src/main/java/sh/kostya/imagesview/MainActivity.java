package sh.kostya.imagesview;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import sh.kostya.imagesview.view.ImagesView;

public class MainActivity extends AppCompatActivity {
    private ImagesView imagesView;
    private Button addImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagesView = findViewById(R.id.iv);
        addImageButton = findViewById(R.id.btn);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            private int counter = 0;

            @Override
            public void onClick(View view) {
                imagesView.add("image_" + counter, getResources().getDrawable(R.drawable.ic_launcher_background));
                counter++;
            }
        });
        addImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                imagesView.clear();
                return true;
            }
        });

    }
}
