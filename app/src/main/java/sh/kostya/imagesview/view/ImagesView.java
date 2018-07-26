package sh.kostya.imagesview.view;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sh.kostya.imagesview.R;

public class ImagesView extends GridLayout {

    private static final int MAX_IMAGES_PER_ROW = 4;

    private GridLayout rootView;
    private boolean isImagesRemovable = false;
    private int firstImageHeight = 0;
    private int currentImageHeight = 0;
    private ContentResolver contentResolver;

    private List<ImageContainer> imageContainers = new ArrayList<>();
    private List<TableRow> rows = new ArrayList<>();

    public ImagesView(@NonNull Context context) {
        super(context);
    }

    public ImagesView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View mainView = LayoutInflater.from(context).inflate(R.layout.view_images, this);
        rootView = mainView.findViewById(R.id.root);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImagesView, 0, 0);

        isImagesRemovable = typedArray.getBoolean(R.styleable.ImagesView_removable_images, false);
        firstImageHeight = typedArray.getDimensionPixelSize(R.styleable.ImagesView_first_image_height, 0);

        init();

        typedArray.recycle();
    }

    private void init() {
        currentImageHeight = firstImageHeight;
        contentResolver = getContext().getContentResolver();
        this.setBackgroundColor(Color.GREEN);
    }

    private TableRow newTableRow() {
        TableRow tableRow = new TableRow(getContext());
        rows.add(tableRow);
        rootView.addView(tableRow);
        tableRow.setGravity(Gravity.CENTER);
        tableRow.setBackgroundColor(Color.BLUE);
        TableLayout.LayoutParams tableRowLayoutParams = (TableLayout.LayoutParams) rootView.getLayoutParams();
        tableRowLayoutParams.width = TableLayout.LayoutParams.MATCH_PARENT;
        tableRowLayoutParams.height = LayoutParams.WRAP_CONTENT;
        tableRowLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        tableRow.setLayoutParams(tableRowLayoutParams);
        return tableRow;
    }

    private ImageContainer newImageView(TableRow tableRow, Object image) {
        // Create and insert image view into table row
        ImageContainer imageContainer = new ImageContainer(getContext(), image, currentImageHeight, 10);
        imageContainers.add(imageContainer);
        tableRow.addView(imageContainer);
        return imageContainer;
    }

    private void updateImagesHeight(int from, int to, int height) {
        for (int i = from; i < to; i++) {
            imageContainers.get(i).changeHeight(height);
        }
    }

    private void updateRowsGravity(int from, int to, int gravity) {
        for (int i = from; i < to; i++) {
            rows.get(i).setGravity(gravity);
        }
    }

    private void processAdding(String descriptor, Object image) {
        if (imageContainers.size() < 4) {
            // Create and add table row
            if (imageContainers.size() == 0) {
                newTableRow();
            } else if (imageContainers.size() == 2) {
                newTableRow();
                currentImageHeight = firstImageHeight / 2;
                updateImagesHeight(0, imageContainers.size(), currentImageHeight);
            }
            TableRow tableRow = rows.get(rows.size() - 1);
            newImageView(tableRow, image);
        } else {
            currentImageHeight = firstImageHeight / 4;
            TableRow firstRow = rows.get(0);
            if (firstRow.getChildCount() > 1) {
                ImageContainer secondImageView = imageContainers.get(1);
                firstRow.removeView(imageContainers.get(1));
                rows.get(1).addView(secondImageView);
                updateImagesHeight(0, 0, firstImageHeight);
                updateImagesHeight(1, imageContainers.size(), currentImageHeight);
                updateRowsGravity(1, rows.size(), Gravity.START);
            }

            TableRow lastRow = rows.get(rows.size() - 1);
            if (lastRow.getChildCount() > MAX_IMAGES_PER_ROW - 1) {
                TableRow tableRow = newTableRow();
                newImageView(tableRow, image);
            } else {
                newImageView(lastRow, image);
            }

        }

        // Update root view
        invalidate();
        requestLayout();
    }

    public void add(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
            processAdding(imageUri.toString(), bitmap);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public void add(String drawableDescriptor, Drawable drawable) {
        processAdding(drawableDescriptor, drawable);
    }

    public void clear() {
        rootView.removeAllViews();
        imageContainers.clear();
        rows.clear();
        currentImageHeight = firstImageHeight;
    }

    public void remove(String imageDescriptor) {
        invalidate();
    }

    public int getImagesCount() {
        return imageContainers.size();
    }
}
