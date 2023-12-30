package ru.nstu.app.android;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

public class Sticker {
    private TdApi.Sticker sticker;

    public Sticker(TdApi.Sticker sticker) {
        this.sticker = sticker;
    }

    public TdApi.File getFile() {
        return sticker.sticker;
    }

    public TdApi.PhotoSize getThumb() {
        return sticker.thumb;
    }

    public static List<Sticker> stickersList = new ArrayList<Sticker>();

    public int getWidth() {
        return sticker.width;
    }

    public int getHeight() {
        return sticker.height;
    }
}
