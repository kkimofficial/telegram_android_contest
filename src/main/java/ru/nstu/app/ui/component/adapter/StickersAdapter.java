package ru.nstu.app.ui.component.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import ru.nstu.app.android.Sticker;
import ru.nstu.app.ui.component.panel.StickerPanel;
import ru.nstu.app.ui.component.viewholder.StickerViewHolder;

public class StickersAdapter extends RecyclerView.Adapter<StickerViewHolder> {

    public StickersAdapter() {
        super();
    }

    @Override
    public void onBindViewHolder(StickerViewHolder viewHolder, int i) {
        viewHolder.bind(Sticker.stickersList.get(i));
    }

    @Override
    public int getItemCount() {
        return Sticker.stickersList.size();
    }

    @Override
    public StickerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new StickerViewHolder(new StickerPanel(viewGroup.getContext()));
    }
}
