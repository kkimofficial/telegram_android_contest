package ru.nstu.app.ui.component.actionbar;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import ru.nstu.app.controller.DisplayController;

public class ActionBar extends FrameLayout {

    private ImageView backImageView;
    private ImageView menuImageView;


    public ActionBar(Context context) {
        super(context);
        buildLayout();
    }

    private void buildLayout() {
        setBackgroundColor(0xff54759e);
    }

    // =================

    private void positionBackImageView(int height) {
        if (backImageView != null) {
            LayoutParams layoutParams = (LayoutParams) backImageView.getLayoutParams();
            layoutParams.width = DisplayController.dp(54);
            layoutParams.height = height;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            backImageView.setLayoutParams(layoutParams);
        }
    }

    public void setBackImageView(int resource, OnClickListener onClickListener) {
        boolean reposition = false;
        if (backImageView == null) {
            createBackImageView();
        } else {
            reposition = true;
        }
        backImageView.setVisibility(resource == 0 ? GONE : VISIBLE);
        backImageView.setImageResource(resource);
        if (reposition) {
            //positionTitle(getMeasuredWidth(), getMeasuredHeight());
        }
        backImageView.setOnClickListener(onClickListener);
    }

    private void createBackImageView() {
        if (backImageView != null) {
            return;
        }
        backImageView = new ImageView(getContext());
        //titleFrameLayout.addView(backImageView);
        addView(backImageView);
        backImageView.setScaleType(ImageView.ScaleType.CENTER);
        //backImageView.setBackgroundResource(itemsBackgroundResourceId);
    }

    // ================

    public void positionMenuImageView(int height) {
        if (menuImageView == null) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams)menuImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(54);
        layoutParams.height = height;
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        menuImageView.setLayoutParams(layoutParams);
    }

    public void setMenuImageView(int resource, OnClickListener onClickListener) {
        boolean reposition = false;
        if (menuImageView == null) {
            createMenuImageView();
        } else {
            reposition = true;
        }
        menuImageView.setVisibility(resource == 0 ? GONE : VISIBLE);
        menuImageView.setImageResource(resource);
        if (reposition) {
            //positionTitle(getMeasuredWidth(), getMeasuredHeight());
        }
        menuImageView.setOnClickListener(onClickListener);
    }

    private void createMenuImageView() {
        if (menuImageView != null) {
            return;
        }
        menuImageView = new ImageView(getContext());
        //titleFrameLayout.addView(menuImageView);
        addView(menuImageView);
        menuImageView.setScaleType(ImageView.ScaleType.CENTER);
        //menuImageView.setBackgroundResource(itemsBackgroundResourceId);
    }

    // ================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        positionBackImageView(DisplayController.actionBarHeight);
        positionMenuImageView(DisplayController.actionBarHeight);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(DisplayController.actionBarHeight, MeasureSpec.EXACTLY));
    }
}
