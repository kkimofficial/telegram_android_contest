package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;

import java.util.Locale;

public class MessageGeoPointPanel extends MessagePanel {
    private ImageView geoPointImageView;

    public MessageGeoPointPanel(Context context) {
        super(context);

        geoPointImageView = new ImageView(context);
        addView(geoPointImageView);
        LayoutParams layoutParams = (LayoutParams)geoPointImageView.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.width = DisplayController.dp(175);
        layoutParams.height = DisplayController.dp(150);
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        geoPointImageView.setLayoutParams(layoutParams);
    }

    @Override
    public void update() {
        double lat = message.getContentGeoPointLatitude();
        double lon = message.getContentGeoPointLongtitude();
        FileController.load(geoPointImageView, String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=13&size=175x150&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", lat, lon, Math.min(2, (int)Math.ceil(DisplayController.density)), lat, lon));
    }
}
