package ru.nstu.app.ui.component.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.Locale;

import ru.nstu.app.R;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Chat;
import ru.nstu.app.model.User;

public class AvatarImageView extends ImageView {
    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final boolean DEFAULT_BORDER_OVERLAY = false;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;
    private float mBorderRadius;

    private ColorFilter mColorFilter;

    private boolean mReady;
    private boolean mSetupPending;
    private boolean mBorderOverlay;

    private AvatarDrawable avatarDrawable;

    public AvatarImageView(Context context) {
        super(context);

        init();
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

        mBorderWidth = 0;//a.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
        mBorderColor = 0;//a.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);
        mBorderOverlay = false;//a.getBoolean(R.styleable.CircleImageView_border_overlay, DEFAULT_BORDER_OVERLAY);

        //a.recycle();

        init();
    }

    private void init() {
        super.setScaleType(SCALE_TYPE);
        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null || mBitmap == null) {
            if(avatarDrawable != null) {
                avatarDrawable.setBounds(0, 0, getWidth(), getHeight());
                avatarDrawable.draw(canvas);
            }
            return;
        }

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mBitmapPaint);
        if (mBorderWidth != 0) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }

        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public void setBorderColorResource(@ColorRes int borderColorRes) {
        setBorderColor(getContext().getResources().getColor(borderColorRes));
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }

        mBorderWidth = borderWidth;
        setup();
    }

    public boolean isBorderOverlay() {
        return mBorderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) {
        if (borderOverlay == mBorderOverlay) {
            return;
        }

        mBorderOverlay = borderOverlay;
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    public void setInfo(User user) {
        this.avatarDrawable = new AvatarDrawable(user);
        setImageBitmap(null);
    }

    public void setInfo(Chat chat) {
        this.avatarDrawable = new AvatarDrawable(chat);
        setImageBitmap(null);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;
        mBitmapPaint.setColorFilter(mColorFilter);
        invalidate();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        mBorderRect.set(0, 0, getWidth(), getHeight());
        mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2, (mBorderRect.width() - mBorderWidth) / 2);

        mDrawableRect.set(mBorderRect);
        if (!mBorderOverlay) {
            mDrawableRect.inset(mBorderWidth, mBorderWidth);
        }
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    public static class AvatarDrawable extends Drawable {

        private static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private static TextPaint namePaint;
        private static int[] arrColors = {0xffe56555, 0xfff28c48, 0xffeec764, 0xff76c84d, 0xff5fbed5, 0xff549cdd, 0xff8e85ee, 0xfff2749a};
        private static int[] arrColorsProfiles = {0xffd86f65, 0xfff69d61, 0xfffabb3c, 0xff67b35d, 0xff56a2bb, 0xff5c98cd, 0xff8c79d2, 0xfff37fa6};
        private static int[] arrColorsProfilesBack = {0xffca6056, 0xfff18944, 0xff7d6ac4, 0xff56a14c, 0xff4492ac, 0xff4c84b6, 0xff7d6ac4, 0xff4c84b6};
        private static int[] arrColorsProfilesText = {0xfff9cbc5, 0xfffdddc8, 0xffcdc4ed, 0xffc0edba, 0xffb8e2f0, 0xffb3d7f7, 0xffcdc4ed, 0xffb3d7f7};
        private static int[] arrColorsNames = {0xffca5650, 0xffd87b29, 0xff4e92cc, 0xff50b232, 0xff42b1a8, 0xff4e92cc, 0xff4e92cc, 0xff4e92cc};
        private static int[] arrColorsButtons = {R.drawable.bar_selector_red, R.drawable.bar_selector_orange, R.drawable.bar_selector_violet,
                R.drawable.bar_selector_green, R.drawable.bar_selector_cyan, R.drawable.bar_selector_blue, R.drawable.bar_selector_violet, R.drawable.bar_selector_blue};



        private static Drawable broadcastDrawable;
        private static Drawable photoDrawable;

        private int color;
        private StaticLayout textLayout;
        private float textWidth;
        private float textHeight;
        private float textLeft;
        private boolean isProfile;
        private boolean drawBrodcast;
        private boolean drawPhoto;

        public AvatarDrawable() {
            super();

            if (namePaint == null) {
                namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                namePaint.setColor(0xffffffff);
                namePaint.setTextSize(DisplayController.sp(18));

                broadcastDrawable = Droid.activity.getResources().getDrawable(R.drawable.broadcast_w);
            }
        }

        public AvatarDrawable(User user) {
            this(user, false);
        }

        public AvatarDrawable(Chat chat) {
            this(chat, false);
        }

        public AvatarDrawable(User user, boolean profile) {
            this();
            isProfile = profile;
            if (user != null) {
                setInfo(user.getId(), user.getFirstName(), user.getLastName(), false);
            }
        }

        public AvatarDrawable(Chat chat, boolean profile) {
            this();
            isProfile = profile;
            if (chat != null) {
                setInfo(chat.getId(), chat.getTitle(), null, chat.getId() > 0);
            }
        }

        public static int getColorIndex(int id) {
            if (id >= 0 && id < 8) {
                return id;
            }
            try {
                String str;
                if (id >= 0) {
                    str = String.format(Locale.US, "%d%d", id, UserController.userId);
                } else {
                    str = String.format(Locale.US, "%d", id);
                }
                if (str.length() > 15) {
                    str = str.substring(0, 15);
                }
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(str.getBytes());
                int b = digest[Math.abs(id % 16)];
                if (b < 0) {
                    b += 256;
                }
                return Math.abs(b) % arrColors.length;
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            return id % arrColors.length;
        }

        public static int getColorForId(int id) {
            return arrColors[getColorIndex(id)];
        }

        public static int getButtonColorForId(int id) {
            return arrColorsButtons[getColorIndex(id)];
        }

        public static int getProfileColorForId(int id) {
            return arrColorsProfiles[getColorIndex(id)];
        }

        public static int getProfileTextColorForId(int id) {
            return arrColorsProfilesText[getColorIndex(id)];
        }

        public static int getProfileBackColorForId(int id) {
            return arrColorsProfilesBack[getColorIndex(id)];
        }

        public static int getNameColorForId(int id) {
            return arrColorsNames[getColorIndex(id)];
        }

        public void setInfo(User user) {
            if (user != null) {
                setInfo(user.getId(), user.getFirstName(), user.getLastName(), false);
            }
        }

        public void setInfo(Chat chat) {
            if (chat != null) {
                setInfo(chat.getId(), chat.getTitle(), null, chat.getId() > 0);
            }
        }

        public void setColor(int value) {
            color = value;
        }

        public void setInfo(int id, String firstName, String lastName, boolean isBroadcast) {
            if (isProfile) {
                color = arrColorsProfiles[getColorIndex(id)];
            } else {
                color = arrColors[getColorIndex(id)];
            }

            drawBrodcast = isBroadcast;

            String text = "";
            if (firstName != null && firstName.length() > 0) {
                text += firstName.substring(0, 1);
            }
            if (lastName != null && lastName.length() > 0) {
                String lastch = null;
                for (int a = lastName.length() - 1; a >= 0; a--) {
                    if (lastch != null && lastName.charAt(a) == ' ') {
                        break;
                    }
                    lastch = lastName.substring(a, a + 1);
                }
                text += lastch;
            }
            if (text.length() > 0) {
                text = text.toUpperCase();
                try {
                    textLayout = new StaticLayout(text, namePaint, DisplayController.dp(100), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    if (textLayout.getLineCount() > 0) {
                        textLeft = textLayout.getLineLeft(0);
                        textWidth = textLayout.getLineWidth(0);
                        textHeight = textLayout.getLineBottom(0);
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            } else {
                textLayout = null;
            }
        }

        public void setDrawPhoto(boolean value) {
            if (value && photoDrawable == null) {
                photoDrawable = Droid.activity.getResources().getDrawable(R.drawable.photo_w);
            }
            drawPhoto = value;
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            if (bounds == null) {
                return;
            }
            int size = bounds.width();
            paint.setColor(color);
            canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.drawCircle(size / 2, size / 2, size / 2, paint);

            if (drawBrodcast && broadcastDrawable != null) {
                int x = (size - broadcastDrawable.getIntrinsicWidth()) / 2;
                int y = (size - broadcastDrawable.getIntrinsicHeight()) / 2;
                broadcastDrawable.setBounds(x, y, x + broadcastDrawable.getIntrinsicWidth(), y + broadcastDrawable.getIntrinsicHeight());
                broadcastDrawable.draw(canvas);
            } else {
                if (textLayout != null) {
                    canvas.translate((size - textWidth) / 2 - textLeft, (size - textHeight) / 2);
                    textLayout.draw(canvas);
                } else if (drawPhoto && photoDrawable != null) {
                    int x = (size - photoDrawable.getIntrinsicWidth()) / 2;
                    int y = (size - photoDrawable.getIntrinsicHeight()) / 2;
                    photoDrawable.setBounds(x, y, x + photoDrawable.getIntrinsicWidth(), y + photoDrawable.getIntrinsicHeight());
                    photoDrawable.draw(canvas);
                }
            }
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }

        @Override
        public int getIntrinsicWidth() {
            return 0;
        }

        @Override
        public int getIntrinsicHeight() {
            return 0;
        }
    }
}