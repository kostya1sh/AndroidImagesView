package sh.kostya.imagesview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import sh.kostya.imagesview.R;

public class ImageContainer extends FrameLayout {
    private FrameLayout rootView;
    private ImageView imageView;
    private int height;
    private int padding;

    public ImageContainer(@NonNull Context context, Object image, int height, int padding) {
        super(context);

        View mainView = LayoutInflater.from(context).inflate(R.layout.view_image_container, this);
        this.rootView = mainView.findViewById(R.id.root);
        this.imageView = mainView.findViewById(R.id.iv);
        this.height = height;
        this.padding = padding;

        init(image);
    }

    private void setImageForImageView(ImageView imageView, Object image) {
        if (image instanceof Drawable) {
            imageView.setImageDrawable((Drawable) image);
        } else if (image instanceof Bitmap) {
            imageView.setImageBitmap((Bitmap) image);
        } else {
            throw new RuntimeException("Image object must be drawable or bitmap.");
        }
    }

    private void init(Object image) {
        setImageForImageView(imageView, image);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) rootView.getLayoutParams();
        params.height = this.height;
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setBackgroundColor(Color.RED);
    }

    public void changeHeight(int height) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) rootView.getLayoutParams();
        params.height = height;
        imageView.setLayoutParams(params);
        this.height = height;
        invalidate();
        requestLayout();
    }

}
