package ru.nstu.app.ui.component.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.nstu.app.android.Sticker;
import ru.nstu.app.ui.component.panel.StickerPanel;

public class StickerViewHolder extends RecyclerView.ViewHolder {
    private StickerPanel stickerPanel;

    public StickerViewHolder(View itemView) {
        super(itemView);
        stickerPanel = (StickerPanel)itemView;
    }

    public void bind(Sticker sticker) {
        stickerPanel.setData(sticker);
    }
}
