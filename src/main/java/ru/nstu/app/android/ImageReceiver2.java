package ru.nstu.app.android;

import android.graphics.Rect;
import android.view.View;
import org.drinkless.td.libcore.telegram.TdApi;

public class ImageReceiver2 {
    private int imageX;
    private int imageY;
    private int imageWidth;
    private int imageHeight;
    private View parentView;
    private TdApi.File imageLocation;
    private Rect drawRegion = new Rect();

    public ImageReceiver2(View view) {
        parentView = view;
    }

    public void setImageCoords(int x, int y, int width, int height) {
        imageX = x;
        imageY = y;
        imageWidth = width;
        imageHeight = height;
    }
}
