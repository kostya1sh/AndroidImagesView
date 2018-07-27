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
import android.util.Pair;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sh.kostya.imagesview.R;

public class ImagesView extends LinearLayout {
    private LinearLayout rootView;
    private boolean isImagesRemovable = false;
    private ContentResolver contentResolver;
    private int initialHeight = 0;

    private List<Mask> maskList = new ArrayList<>();
    private List<ImageContainer> imageContainers = new ArrayList<>();
    private List<LinearLayout> rows = new ArrayList<>();

    public ImagesView(@NonNull Context context) {
        super(context);
    }

    public ImagesView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View mainView = LayoutInflater.from(context).inflate(R.layout.view_images, this);
        rootView = mainView.findViewById(R.id.root);


        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImagesView, 0, 0);

        isImagesRemovable = typedArray.getBoolean(R.styleable.ImagesView_removable_images, false);
        initialHeight = typedArray.getDimensionPixelSize(R.styleable.ImagesView_first_image_height, 0);
        maskList = Mask.createTenImagesMaskList(initialHeight);


        init();

        typedArray.recycle();
    }

    private void init() {
        contentResolver = getContext().getContentResolver();
        for (int i = 0; i < 4; i++) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(HORIZONTAL);
            rows.add(linearLayout);
            rootView.addView(linearLayout);
        }
        this.setBackgroundColor(Color.GREEN);
    }

    private void clearImages() {
        for (LinearLayout linearLayout: rows) {
            linearLayout.removeAllViews();
        }
    }

    private ImageContainer newImageView(Object image, String descriptor) {
        // Create and insert image view into table row
        ImageContainer imageContainer = new ImageContainer(getContext(), image, descriptor, 256, 256, 10);
        imageContainers.add(imageContainer);
        return imageContainer;
    }

    private void setImagePos(ImageContainer imageContainer, int col, int row) {
        LinearLayout linearLayout = rows.get(row);
        linearLayout.addView(imageContainer, col);
    }

    private void applyMask(Mask mask) {
        int counter = 0;
        clearImages();
        for (int i = 0; i < mask.getRows(); i++) {
            for (int j = 0; j < mask.getRowParams().get(i).size(); j++) {
                ImageContainer imageContainer = imageContainers.get(counter);
                Mask.Col currentCol = mask.getRowParams().get(i).get(j);
                int width = currentCol.width;
                int height = currentCol.height;

                if (currentCol.width == Mask.Col.RECALCULATE_WIDTH) {
                    if (i > 0 && mask.getRowParams().get(i - 1).size() > mask.getRowParams().get(i).size()) {
                        width = rootView.getMeasuredWidth() / mask.getRowParams().get(i - 1).size();
                    } else {
                        width = rootView.getMeasuredWidth() / mask.getRowParams().get(i).size();
                    }
                }

                if (currentCol.adaptHeight && width > currentCol.height && width < initialHeight) {
                    height = width;
                }

                imageContainer.changeSize(height, width);
                setImagePos(imageContainer, j, i);
                counter++;
            }
        }
    }

    private void processAdding(String descriptor, Object image) {
        if (imageContainers.size() >= 10) return;


        newImageView(image, descriptor);
        applyMask(maskList.get(imageContainers.size() - 1));

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
        clearImages();
        imageContainers.clear();
    }

    public void remove(String imageDescriptor) {
        invalidate();
    }

    public int getImagesCount() {
        return imageContainers.size();
    }

    public static class Mask {
        private int rows;
        private SparseArray<List<Col>> rowParams = new SparseArray<>();

        public Mask(int rows, SparseArray<List<Col>> rowParams) {
            this.rows = rows;
            this.rowParams = rowParams;
            if (rowParams.size() > rows) {
                throw new RuntimeException("Row params size can not be bigger that rows amount.");
            }
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public SparseArray<List<Col>> getRowParams() {
            return rowParams;
        }

        public void setRowParams(SparseArray<List<Col>> rowParams) {
            this.rowParams = rowParams;
        }

        public static List<Mask> createTenImagesMaskList(int initHeight) {
            List<Mask> maskList = new ArrayList<>();

            // 1 image
            List<Col> cols1 = new ArrayList<>();
            cols1.add(new Col(initHeight, -2, false));
            SparseArray<List<Col>> params1 = new SparseArray<>();
            params1.put(0, cols1);
            maskList.add(new Mask(1, params1));

            // 2 images
            List<Col> cols2 = new ArrayList<>();
            cols2.add(new Col(initHeight / 2, -2));
            cols2.add(new Col(initHeight / 2, -2));
            SparseArray<List<Col>> params2 = new SparseArray<>();
            params2.put(0, cols2);
            maskList.add(new Mask(1, params2));

            // 3 images
            List<Col> cols3_1 = new ArrayList<>();
            List<Col> cols3_2 = new ArrayList<>();
            cols3_1.add(new Col(initHeight, -2, false));
            cols3_2.add(new Col(initHeight / 2, -2));
            cols3_2.add(new Col(initHeight / 2, -2));
            SparseArray<List<Col>> params3 = new SparseArray<>();
            params3.put(0, cols3_1);
            params3.put(1, cols3_2);
            maskList.add(new Mask(2, params3));

            // 4 images
            List<Col> cols4_1 = new ArrayList<>();
            List<Col> cols4_2 = new ArrayList<>();
            cols4_1.add(new Col(initHeight / 2, -2));
            cols4_1.add(new Col(initHeight / 2, -2));
            cols4_2.add(new Col(initHeight / 2, -2));
            cols4_2.add(new Col(initHeight / 2, -2));
            SparseArray<List<Col>> params4 = new SparseArray<>();
            params4.put(0, cols4_1);
            params4.put(1, cols4_2);
            maskList.add(new Mask(2, params4));

            // 5 images
            List<Col> cols5_1 = new ArrayList<>();
            List<Col> cols5_2 = new ArrayList<>();
            cols5_1.add(new Col(initHeight, -2, false));
            cols5_2.add(new Col(initHeight / 4, -2));
            cols5_2.add(new Col(initHeight / 4, -2));
            cols5_2.add(new Col(initHeight / 4, -2));
            cols5_2.add(new Col(initHeight / 4, -2));
            SparseArray<List<Col>> params5 = new SparseArray<>();
            params5.put(0, cols5_1);
            params5.put(1, cols5_2);
            maskList.add(new Mask(2, params5));

            // 6 images
            List<Col> cols6_1 = new ArrayList<>();
            List<Col> cols6_2 = new ArrayList<>();
            List<Col> cols6_3 = new ArrayList<>();
            cols6_1.add(new Col(initHeight, -2, false));
            cols6_2.add(new Col(initHeight / 4, -2));
            cols6_2.add(new Col(initHeight / 4, -2));
            cols6_2.add(new Col(initHeight / 4, -2));
            cols6_2.add(new Col(initHeight / 4, -2));
            cols6_3.add(new Col(initHeight / 4, -2));
            SparseArray<List<Col>> params6 = new SparseArray<>();
            params6.put(0, cols6_1);
            params6.put(1, cols6_2);
            params6.put(2, cols6_3);
            maskList.add(new Mask(3, params6));

            // 7 images
            List<Col> cols7_1 = new ArrayList<>();
            List<Col> cols7_2 = new ArrayList<>();
            List<Col> cols7_3 = new ArrayList<>();
            cols7_1.add(new Col(initHeight, -2, false));
            cols7_2.add(new Col(initHeight / 4, -2));
            cols7_2.add(new Col(initHeight / 4, -2));
            cols7_2.add(new Col(initHeight / 4, -2));
            cols7_2.add(new Col(initHeight / 4, -2));
            cols7_3.add(new Col(initHeight / 4, -2));
            cols7_3.add(new Col(initHeight / 4, -2));
            SparseArray<List<Col>> params7 = new SparseArray<>();
            params7.put(0, cols7_1);
            params7.put(1, cols7_2);
            params7.put(2, cols7_3);
            maskList.add(new Mask(3, params7));

            // 8 images
            List<Col> cols8_1 = new ArrayList<>();
            List<Col> cols8_2 = new ArrayList<>();
            List<Col> cols8_3 = new ArrayList<>();
            cols8_1.add(new Col(initHeight, -2, false));
            cols8_2.add(new Col(initHeight / 4, -2));
            cols8_2.add(new Col(initHeight / 4, -2));
            cols8_2.add(new Col(initHeight / 4, -2));
            cols8_2.add(new Col(initHeight / 4, -2));
            cols8_3.add(new Col(initHeight / 4, -2));
            cols8_3.add(new Col(initHeight / 4, -2));
            cols8_3.add(new Col(initHeight / 4, -2));
            SparseArray<List<Col>> params8 = new SparseArray<>();
            params8.put(0, cols8_1);
            params8.put(1, cols8_2);
            params8.put(2, cols8_3);
            maskList.add(new Mask(3, params8));

            // 9 images
            List<Col> cols9_1 = new ArrayList<>();
            List<Col> cols9_2 = new ArrayList<>();
            List<Col> cols9_3 = new ArrayList<>();
            cols9_1.add(new Col(initHeight, -2, false));
            cols9_2.add(new Col(initHeight / 4, -2));
            cols9_2.add(new Col(initHeight / 4, -2));
            cols9_2.add(new Col(initHeight / 4, -2));
            cols9_2.add(new Col(initHeight / 4, -2));
            cols9_3.add(new Col(initHeight / 4, -2));
            cols9_3.add(new Col(initHeight / 4, -2));
            cols9_3.add(new Col(initHeight / 4, -2));
            cols9_3.add(new Col(initHeight / 4, -2));
            SparseArray<List<Col>> params9 = new SparseArray<>();
            params9.put(0, cols9_1);
            params9.put(1, cols9_2);
            params9.put(2, cols9_3);
            maskList.add(new Mask(3, params9));

            // 10 images
            List<Col> cols10_1 = new ArrayList<>();
            List<Col> cols10_2 = new ArrayList<>();
            List<Col> cols10_3 = new ArrayList<>();
            cols10_1.add(new Col(initHeight / 2, -2));
            cols10_1.add(new Col(initHeight / 2, -2));
            cols10_2.add(new Col(initHeight / 4, -2));
            cols10_2.add(new Col(initHeight / 4, -2));
            cols10_2.add(new Col(initHeight / 4, -2));
            cols10_2.add(new Col(initHeight / 4, -2));
            cols10_3.add(new Col(initHeight / 4, -2));
            cols10_3.add(new Col(initHeight / 4, -2));
            cols10_3.add(new Col(initHeight / 4, -2));
            cols10_3.add(new Col(initHeight / 4, -2));
            SparseArray<List<Col>> params10 = new SparseArray<>();
            params10.put(0, cols10_1);
            params10.put(1, cols10_2);
            params10.put(2, cols10_3);
            maskList.add(new Mask(3, params10));

            return maskList;
        }

        public static class Col {
            public static final int RECALCULATE_WIDTH = -2;

            int height;
            int width;
            boolean adaptHeight = true;

            public Col(int height, int width) {
                this.height = height;
                this.width = width;
            }

            public Col(int height, int width, boolean adaptHeight) {
                this.height = height;
                this.width = width;
                this.adaptHeight = adaptHeight;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public boolean isAdaptHeight() {
                return adaptHeight;
            }

            public void setAdaptHeight(boolean adaptHeight) {
                this.adaptHeight = adaptHeight;
            }
        }
    }
}
