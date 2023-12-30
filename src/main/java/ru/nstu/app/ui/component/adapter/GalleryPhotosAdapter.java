package ru.nstu.app.ui.component.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ru.nstu.app.ui.component.panel.GalleryPhotoPanel;
import ru.nstu.app.ui.component.viewholder.GalleryPhotoViewHolder;

public abstract class GalleryPhotosAdapter extends RecyclerView.Adapter<GalleryPhotoViewHolder> {
    private List<String> pathsList = new ArrayList<String>();
    private Set<String> pathsSet = new TreeSet<String>();

    public GalleryPhotosAdapter(List<String> pathsList) {
        super();
        this.pathsList = pathsList;
    }

    @Override
    public void onBindViewHolder(GalleryPhotoViewHolder galleryPhotoViewHolder, int i) {
        galleryPhotoViewHolder.bind(pathsList.get(i), pathsList, pathsSet);
    }

    @Override
    public int getItemCount() {
        return pathsList.size();
    }

    public int getSelectedItemCount() {
        return pathsSet.size();
    }

    public Set<String> getSelectedItems() {
        return pathsSet;
    }
}
