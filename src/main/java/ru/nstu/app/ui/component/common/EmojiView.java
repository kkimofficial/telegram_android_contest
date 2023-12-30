/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package ru.nstu.app.ui.component.common;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ru.nstu.app.R;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Emoji;
import ru.nstu.app.android.Sticker;
import ru.nstu.app.api.action.LoadStickersAction;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.component.adapter.StickersAdapter;
import ru.nstu.app.ui.component.panel.StickerPanel;
import ru.nstu.app.ui.component.viewholder.StickerViewHolder;

public class EmojiView extends LinearLayout {

    private ArrayList<Object> adapters = new ArrayList<>();
    private int[] icons = {
            R.drawable.ic_emoji_recent,
            R.drawable.ic_emoji_smile,
            R.drawable.ic_emoji_flower,
            R.drawable.ic_emoji_bell,
            R.drawable.ic_emoji_car,
            R.drawable.ic_emoji_symbol,
            R.drawable.ic_emoji_sticker };
    private Listener listener;
    private ViewPager pager;
    private FrameLayout recentsWrap;
    private ArrayList<View> views = new ArrayList<>();

    public EmojiView(Context paramContext) {
        super(paramContext);
        init();
    }

    public EmojiView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    public EmojiView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void addToRecent(long paramLong) {
        if (this.pager.getCurrentItem() == 0) {
            return;
        }
        ArrayList<Long> localArrayList = new ArrayList<>();
        long[] currentRecent = Emoji.data[0];
        boolean was = false;
        for (long aCurrentRecent : currentRecent) {
            if (paramLong == aCurrentRecent) {
                localArrayList.add(0, paramLong);
                was = true;
            } else {
                localArrayList.add(aCurrentRecent);
            }
        }
        if (!was) {
            localArrayList.add(0, paramLong);
        }
        Emoji.data[0] = new long[Math.min(localArrayList.size(), 50)];
        for (int q = 0; q < Emoji.data[0].length; q++) {
            Emoji.data[0][q] = localArrayList.get(q);
        }
        ((EmojiGridAdapter)adapters.get(0)).data = Emoji.data[0];
        ((EmojiGridAdapter)adapters.get(0)).notifyDataSetChanged();
        saveRecents();
    }

    private String convert(long paramLong) {
        String str = "";
        for (int i = 0; ; i++) {
            if (i >= 4) {
                return str;
            }
            int j = (int)(0xFFFF & paramLong >> 16 * (3 - i));
            if (j != 0) {
                str = str + (char)j;
            }
        }
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < Emoji.data.length; i++) {
            GridView gridView = new GridView(getContext());
            if (DisplayController.isTablet) {
                gridView.setColumnWidth(DisplayController.dp(60));
            } else {
                gridView.setColumnWidth(DisplayController.dp(45));
            }
            gridView.setNumColumns(-1);
            views.add(gridView);

            EmojiGridAdapter localEmojiGridAdapter = new EmojiGridAdapter(Emoji.data[i]);
            gridView.setAdapter(localEmojiGridAdapter);
            //AndroidUtilities.setListViewEdgeEffectColor(gridView, 0xff999999); TODO:
            adapters.add(localEmojiGridAdapter);
        }

        // ===== STICKERS

        RecyclerView stickersRecyclerView = new RecyclerView(getContext()) {
            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if(Sticker.stickersList.size() == 0) {
                    Droid.doAction(new LoadStickersAction((StickersAdapter) getAdapter()));
                }

            }

            @Override
            protected void onMeasure(int widthSpec, int heightSpec) {
                super.onMeasure(widthSpec, heightSpec);
                int width = DisplayController.dp(MeasureSpec.getSize(widthSpec));
                if(width != 0) {
                    int spans = width / StickerPanel.SIZE;
                    if(spans > 0) {
                        ((GridLayoutManager)getLayoutManager()).setSpanCount(spans);
                    }
                }
            }
        };
        views.add(stickersRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 5);
        stickersRecyclerView.setLayoutManager(gridLayoutManager);

        StickersAdapter stickersAdapter = new StickersAdapter();
        adapters.add(stickersAdapter);
        stickersRecyclerView.setAdapter(stickersAdapter);

        stickersRecyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
//                ((StickerViewHolder)viewHolder).bind(null);
            }
        });
        // ======

        setBackgroundColor(0xfff4f5f6);
        pager = new ViewPager(getContext());
        pager.setAdapter(new EmojiPagesAdapter());
        PagerSlidingTabStrip tabs = new PagerSlidingTabStrip(getContext());
        tabs.setViewPager(pager);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColor(0xff33b5e5);
        tabs.setIndicatorHeight(DisplayController.dp(2.0f));
        tabs.setUnderlineHeight(DisplayController.dp(2.0f));
        tabs.setUnderlineColor(0x11000000);
        tabs.setTabBackground(0);
        LinearLayout localLinearLayout = new LinearLayout(getContext());
        localLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        localLinearLayout.addView(tabs, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        ImageView localImageView = new ImageView(getContext());
        localImageView.setImageResource(R.drawable.ic_emoji_backspace);
        localImageView.setScaleType(ImageView.ScaleType.CENTER);
        //localImageView.setBackgroundResource(R.drawable.bg_emoji_bs);
        localImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (EmojiView.this.listener != null) {
                    EmojiView.this.listener.onBackspace();
                }
            }
        });
        localLinearLayout.addView(localImageView, new LinearLayout.LayoutParams(DisplayController.dp(61), LayoutParams.MATCH_PARENT));
        recentsWrap = new FrameLayout(getContext());
        recentsWrap.addView(views.get(0));
        TextView localTextView = new TextView(getContext());
        localTextView.setText(LocaleController.getString(R.string.no_recent));
        localTextView.setTextSize(18.0f);
        localTextView.setTextColor(-7829368);
        localTextView.setGravity(17);
        recentsWrap.addView(localTextView);
        ((GridView)views.get(0)).setEmptyView(localTextView);
        addView(localLinearLayout, new LinearLayout.LayoutParams(-1, DisplayController.dp(48.0f)));
        addView(pager);
        loadRecents();
        if (Emoji.data[0] == null || Emoji.data[0].length == 0) {
            pager.setCurrentItem(1);
        }
    }

    private void saveRecents() {
        ArrayList<Long> localArrayList = new ArrayList<>();
        long[] arrayOfLong = Emoji.data[0];
        int i = arrayOfLong.length;
        for (int j = 0; ; j++) {
            if (j >= i) {
                Droid.save(Droid.PREFERENCES_EMOJI, "recents", TextUtils.join(",", localArrayList));
                return;
            }
            localArrayList.add(arrayOfLong[j]);
        }
    }

    public void loadRecents() {
        String str = (String)Droid.load(Droid.PREFERENCES_EMOJI, "recents", "");
        String[] arrayOfString = null;
        if ((str != null) && (str.length() > 0)) {
            arrayOfString = str.split(",");
            Emoji.data[0] = new long[arrayOfString.length];
        }
        if (arrayOfString != null) {
            for (int i = 0; i < arrayOfString.length; i++) {
                Emoji.data[0][i] = Long.parseLong(arrayOfString[i]);
            }
            ((EmojiGridAdapter)adapters.get(0)).data = Emoji.data[0];
            ((EmojiGridAdapter)adapters.get(0)).notifyDataSetChanged();
        }
    }

    public void onMeasure(int paramInt1, int paramInt2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt2), MeasureSpec.EXACTLY));
    }

    public void setListener(Listener paramListener) {
        this.listener = paramListener;
    }

//    public void invalidateViews() {
//        for (GridView gridView : views) {
//            if (gridView != null) {
//                gridView.invalidateViews();
//            }
//        }
//    }

    private class EmojiGridAdapter extends BaseAdapter {
        long[] data;

        public EmojiGridAdapter(long[] arg2) {
            this.data = arg2;
        }

        public int getCount() {
            return data.length;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return data[i];
        }

        public View getView(int i, View view, ViewGroup paramViewGroup) {
            ImageView imageView = (ImageView)view;
            if (imageView == null) {
                imageView = new ImageView(EmojiView.this.getContext()) {
                    public void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2) {
                        setMeasuredDimension(View.MeasureSpec.getSize(paramAnonymousInt1), View.MeasureSpec.getSize(paramAnonymousInt1));
                    }
                };
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (EmojiView.this.listener != null) {
                            EmojiView.this.listener.onEmojiSelected(EmojiView.this.convert((Long)view.getTag()));
                        }
                        EmojiView.this.addToRecent((Long)view.getTag());
                    }
                });
                imageView.setBackgroundResource(R.drawable.st_list_selector);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
            }
            imageView.setImageDrawable(Emoji.getEmojiBigDrawable(data[i]));
            imageView.setTag(data[i]);
            return imageView;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    private class EmojiPagesAdapter extends PagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
            View localObject;
            if (paramInt == 0) {
                localObject = recentsWrap;
            } else {
                localObject = views.get(paramInt);
            }
            paramViewGroup.removeView(localObject);
        }

        public int getCount() {
            return views.size();
        }

        public int getPageIconResId(int paramInt) {
            return icons[paramInt];
        }

        public Object instantiateItem(ViewGroup paramViewGroup, int paramInt) {
            View localObject;
            if (paramInt == 0) {
                localObject = recentsWrap;
            } else {
                localObject = views.get(paramInt);
            }
            paramViewGroup.addView(localObject);
            return localObject;
        }

        public boolean isViewFromObject(View paramView, Object paramObject) {
            return paramView == paramObject;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    public interface Listener {
        void onBackspace();
        void onEmojiSelected(String paramString);
    }
}