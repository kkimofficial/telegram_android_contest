package ru.nstu.app.ui.component.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;
import java.util.Set;

import ru.nstu.app.ui.component.panel.GalleryPhotoPanel;

public class GalleryPhotoViewHolder extends RecyclerView.ViewHolder {
    private GalleryPhotoPanel galleryPhotoPanel;

    public GalleryPhotoViewHolder(View itemView) {
        super(itemView);
        galleryPhotoPanel = (GalleryPhotoPanel)itemView;
    }

    public void bind(String path, List<String> pathsList, Set<String> pathsSet) {
        galleryPhotoPanel.setData(path, pathsList, pathsSet);
    }
}
