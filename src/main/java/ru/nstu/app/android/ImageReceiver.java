/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package ru.nstu.app.android;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.model.Message;

public class ImageReceiver {

    private View parentView;
    private Integer tag;
    private Integer thumbTag;
    private Message parentMessage;
    private boolean canceledLoading;

    private TdApi.File currentImageLocation;
    private String currentKey;
    private String currentThumbKey;
    private String currentHttpUrl;
    private String currentFilter;
    private String currentThumbFilter;
    private TdApi.File currentThumbLocation;
    private int currentSize;
    private boolean currentCacheOnly;
    private BitmapDrawable currentImage;
    private BitmapDrawable currentThumb;
    private Drawable staticThumb;

    private boolean needsQualityThumb;
    private boolean shouldGenerateQualityThumb;

    private int imageX, imageY, imageW, imageH;
    private Rect drawRegion = new Rect();
    private boolean isVisible = true;
    private boolean isAspectFit;
    private boolean forcePreview;
    private int roundRadius;
    private BitmapShader bitmapShader;
    private Paint roundPaint;
    private RectF roundRect;
    private RectF bitmapRect;
    private Matrix shaderMatrix;
    private int alpha = 255;
    private boolean isPressed;
    private int orientation;
    private boolean centerRotation;

    public ImageReceiver() {

    }

    public ImageReceiver(View view) {
        parentView = view;
    }

    public void cancelLoadImage() {
        FileController.cancel(this);
        canceledLoading = true;
    }

    public void setImage(TdApi.File path, String filter, Drawable thumb, boolean cacheOnly) {
        setImage(path, null, filter, thumb, null, null, 0, cacheOnly);
    }

    public void setImage(TdApi.File path, String filter, Drawable thumb, int size, boolean cacheOnly) {
        setImage(path, null, filter, thumb, null, null, size, cacheOnly);
    }

    public void setImage(String httpUrl, String filter, Drawable thumb, int size) {
        setImage(null, httpUrl, filter, thumb, null, null, size, true);
    }

    public void setImage(TdApi.File fileLocation, String filter, TdApi.File thumbLocation, String thumbFilter, boolean cacheOnly) {
        setImage(fileLocation, null, filter, null, thumbLocation, thumbFilter, 0, cacheOnly);
    }

    public void setImage(TdApi.File fileLocation, String filter, TdApi.File thumbLocation, String thumbFilter, int size, boolean cacheOnly) {
        setImage(fileLocation, null, filter, null, thumbLocation, thumbFilter, size, cacheOnly);
    }

    public void setImage(TdApi.File fileLocation, String httpUrl, String filter, Drawable thumb, TdApi.File thumbLocation, String thumbFilter, int size, boolean cacheOnly) {
        setTag(FileController.getId(fileLocation), false);
        if ((fileLocation == null && httpUrl == null && thumbLocation == null)
                || (fileLocation != null && !(fileLocation instanceof TdApi.FileLocal) && !(fileLocation instanceof TdApi.FileEmpty))) {
            recycleBitmap(null, false);
            recycleBitmap(null, true);
            currentKey = null;
            currentThumbKey = null;
            currentThumbFilter = null;
            currentImageLocation = null;
            currentHttpUrl = null;
            currentFilter = null;
            currentCacheOnly = false;
            staticThumb = thumb;
            currentThumbLocation = null;
            currentSize = 0;
            currentImage = null;
            bitmapShader = null;
            FileController.cancel(this);
            if (parentView != null) {
                parentView.invalidate();
            }
            return;
        }

        if (!(thumbLocation instanceof TdApi.FileLocal)) {
            thumbLocation = null;
        }

        String key = null;
        if (fileLocation != null) {
            key = String.valueOf(FileController.getId(fileLocation));
        } else if (httpUrl != null) {
            key = Droid.md5(httpUrl);
        }
        if (key != null) {
            if (filter != null) {
                key += "@" + filter;
            }
        }

        if (currentKey != null && key != null && currentKey.equals(key)) {
            if (!canceledLoading && !forcePreview) {
                return;
            }
        }

        String thumbKey = null;
        if (thumbLocation != null) {
            thumbKey = String.valueOf(FileController.getId(thumbLocation));
            if (thumbFilter != null) {
                thumbKey += "@" + thumbFilter;
            }
        }

        recycleBitmap(key, false);
        recycleBitmap(thumbKey, true);

        currentThumbKey = thumbKey;
        currentKey = key;
        currentImageLocation = fileLocation;
        currentHttpUrl = httpUrl;
        currentFilter = filter;
        currentThumbFilter = thumbFilter;
        currentSize = size;
        currentCacheOnly = cacheOnly;
        currentThumbLocation = thumbLocation;
        staticThumb = thumb;
        bitmapShader = null;

        FileController.load(this);
        if (parentView != null) {
            parentView.invalidate();
        }
    }

    public void setPressed(boolean value) {
        isPressed = value;
    }

    public boolean getPressed() {
        return isPressed;
    }

    public void setOrientation(int angle, boolean center) {
        orientation = angle;
        centerRotation = center;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap != null ? new BitmapDrawable(null, bitmap) : null);
    }

    public void setImageBitmap(Drawable bitmap) {
        FileController.cancel(this);
        recycleBitmap(null, false);
        recycleBitmap(null, true);
        staticThumb = bitmap;
        currentThumbLocation = null;
        currentKey = null;
        currentThumbKey = null;
        currentImage = null;
        currentThumbFilter = null;
        currentImageLocation = null;
        currentHttpUrl = null;
        currentFilter = null;
        currentSize = 0;
        currentCacheOnly = false;
        bitmapShader = null;
        if (parentView != null) {
            parentView.invalidate();
        }
    }

    public void clearImage() {
        recycleBitmap(null, false);
        recycleBitmap(null, true);
        if (needsQualityThumb) {
            FileController.cancel(this);
        }
    }

    public boolean draw(Canvas canvas) {
        try {
            BitmapDrawable bitmapDrawable = null;
            if (!forcePreview && currentImage != null) {
                bitmapDrawable = currentImage;
            } else if (staticThumb instanceof BitmapDrawable) {
                bitmapDrawable = (BitmapDrawable) staticThumb;
            } else if (currentThumb != null) {
                bitmapDrawable = currentThumb;
            }
            if (bitmapDrawable != null) {
                Paint paint = bitmapDrawable.getPaint();
                boolean hasFilter = paint != null && paint.getColorFilter() != null;
                if (hasFilter && !isPressed) {
                    bitmapDrawable.setColorFilter(null);
                    hasFilter = false;
                } else if (!hasFilter && isPressed) {
                    bitmapDrawable.setColorFilter(new PorterDuffColorFilter(0xffdddddd, PorterDuff.Mode.MULTIPLY));
                    hasFilter = true;
                }
                if (bitmapShader != null) {
                    drawRegion.set(imageX, imageY, imageX + imageW, imageY + imageH);
                    if (isVisible) {
                        roundRect.set(drawRegion);
                        shaderMatrix.reset();
                        shaderMatrix.setRectToRect(bitmapRect, roundRect, Matrix.ScaleToFit.FILL);
                        bitmapShader.setLocalMatrix(shaderMatrix);
                        canvas.drawRoundRect(roundRect, roundRadius, roundRadius, roundPaint);
                    }
                } else {
                    int bitmapW;
                    int bitmapH;
                    int originalW = bitmapDrawable.getIntrinsicWidth();
                    int originalH = bitmapDrawable.getIntrinsicHeight();
                    if (orientation == 90 || orientation == 270) {
                        bitmapW = bitmapDrawable.getIntrinsicHeight();
                        bitmapH = bitmapDrawable.getIntrinsicWidth();
                    } else {
                        bitmapW = bitmapDrawable.getIntrinsicWidth();
                        bitmapH = bitmapDrawable.getIntrinsicHeight();
                    }
                    float scaleW = bitmapW / (float) imageW;
                    float scaleH = bitmapH / (float) imageH;

                    if (isAspectFit) {
                        float scale = Math.max(scaleW, scaleH);
                        canvas.save();
                        bitmapW /= scale;
                        bitmapH /= scale;
                        drawRegion.set(imageX + (imageW - bitmapW) / 2, imageY + (imageH - bitmapH) / 2, imageX + (imageW + bitmapW) / 2, imageY + (imageH + bitmapH) / 2);
                        bitmapDrawable.setBounds(drawRegion);
                        try {
                            bitmapDrawable.setAlpha(alpha);
                            bitmapDrawable.draw(canvas);
                        } catch (Exception e) {
                            if (bitmapDrawable == currentImage && currentKey != null) {
                                FileController.remove(currentKey);
                                currentKey = null;
                            } else if (bitmapDrawable == currentThumb && currentThumbKey != null) {
                                FileController.remove(currentThumbKey);
                                currentThumbKey = null;
                            }
                            setImage(currentImageLocation, currentHttpUrl, currentFilter, currentThumb, currentThumbLocation, currentThumbFilter, currentSize, currentCacheOnly);
                            FileLog.e("tmessages", e);
                        }
                        canvas.restore();
                    } else {
                        if (Math.abs(scaleW - scaleH) > 0.00001f) {
                            canvas.save();
                            canvas.clipRect(imageX, imageY, imageX + imageW, imageY + imageH);

                            if (orientation != 0) {
                                if (centerRotation) {
                                    canvas.rotate(orientation, imageW / 2, imageH / 2);
                                } else {
                                    canvas.rotate(orientation, 0, 0);
                                }
                            }

                            if (bitmapW / scaleH > imageW) {
                                bitmapW /= scaleH;
                                originalW /= scaleH;
                                drawRegion.set(imageX - (bitmapW - imageW) / 2, imageY, imageX + (bitmapW + imageW) / 2, imageY + imageH);
                            } else {
                                bitmapH /= scaleW;
                                originalH /= scaleW;
                                drawRegion.set(imageX, imageY - (bitmapH - imageH) / 2, imageX + imageW, imageY + (bitmapH + imageH) / 2);
                            }
                            if (orientation == 90 || orientation == 270) {
                                int width = (drawRegion.right - drawRegion.left) / 2;
                                int height = (drawRegion.bottom - drawRegion.top) / 2;
                                int centerX = (drawRegion.right + drawRegion.left) / 2;
                                int centerY = (drawRegion.top + drawRegion.bottom) / 2;
                                bitmapDrawable.setBounds(centerX - height, centerY - width, centerX + height, centerY + width);
                            } else {
                                bitmapDrawable.setBounds(drawRegion);
                            }
                            if (isVisible) {
                                try {
                                    bitmapDrawable.setAlpha(alpha);
                                    bitmapDrawable.draw(canvas);
                                } catch (Exception e) {
                                    if (bitmapDrawable == currentImage && currentKey != null) {
                                        FileController.remove(currentKey);
                                        currentKey = null;
                                    } else if (bitmapDrawable == currentThumb && currentThumbKey != null) {
                                        FileController.remove(currentThumbKey);
                                        currentThumbKey = null;
                                    }
                                    setImage(currentImageLocation, currentHttpUrl, currentFilter, currentThumb, currentThumbLocation, currentThumbFilter, currentSize, currentCacheOnly);
                                    FileLog.e("tmessages", e);
                                }
                            }

                            canvas.restore();
                        } else {
                            canvas.save();
                            if (orientation != 0) {
                                if (centerRotation) {
                                    canvas.rotate(orientation, imageW / 2, imageH / 2);
                                } else {
                                    canvas.rotate(orientation, 0, 0);
                                }
                            }
                            drawRegion.set(imageX, imageY, imageX + imageW, imageY + imageH);
                            if (orientation == 90 || orientation == 270) {
                                int width = (drawRegion.right - drawRegion.left) / 2;
                                int height = (drawRegion.bottom - drawRegion.top) / 2;
                                int centerX = (drawRegion.right + drawRegion.left) / 2;
                                int centerY = (drawRegion.top + drawRegion.bottom) / 2;
                                bitmapDrawable.setBounds(centerX - height, centerY - width, centerX + height, centerY + width);
                            } else {
                                bitmapDrawable.setBounds(drawRegion);
                            }
                            if (isVisible) {
                                try {
                                    bitmapDrawable.setAlpha(alpha);
                                    bitmapDrawable.draw(canvas);
                                } catch (Exception e) {
                                    if (bitmapDrawable == currentImage && currentKey != null) {
                                        FileController.remove(currentKey);
                                        currentKey = null;
                                    } else if (bitmapDrawable == currentThumb && currentThumbKey != null) {
                                        FileController.remove(currentThumbKey);
                                        currentThumbKey = null;
                                    }
                                    setImage(currentImageLocation, currentHttpUrl, currentFilter, currentThumb, currentThumbLocation, currentThumbFilter, currentSize, currentCacheOnly);
                                    FileLog.e("tmessages", e);
                                }
                            }
                            canvas.restore();
                        }
                    }
                }
                return true;
            } else if (staticThumb != null) {
                drawRegion.set(imageX, imageY, imageX + imageW, imageY + imageH);
                staticThumb.setBounds(drawRegion);
                if (isVisible) {
                    try {
                        staticThumb.setAlpha(alpha);
                        staticThumb.draw(canvas);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return false;
    }

    public Bitmap getBitmap() {
        if (currentImage != null) {
            return currentImage.getBitmap();
        } else if (currentThumb != null) {
            return currentThumb.getBitmap();
        } else if (staticThumb instanceof BitmapDrawable) {
            return ((BitmapDrawable) staticThumb).getBitmap();
        }
        return null;
    }

    public int getBitmapWidth() {
        Bitmap bitmap = getBitmap();
        return orientation == 0 || orientation == 180 ? bitmap.getWidth() : bitmap.getHeight();
    }

    public int getBitmapHeight() {
        Bitmap bitmap = getBitmap();
        return orientation == 0 || orientation == 180 ? bitmap.getHeight() : bitmap.getWidth();
    }

    public void setVisible(boolean value, boolean invalidate) {
        if (isVisible == value) {
            return;
        }
        isVisible = value;
        if (invalidate && parentView != null) {
            parentView.invalidate();
        }
    }

    public boolean getVisible() {
        return isVisible;
    }

    public void setAlpha(float value) {
        alpha = (int)(value * 255.0f);
    }

    public boolean hasImage() {
        return currentImage != null || currentThumb != null || currentKey != null || currentHttpUrl != null || staticThumb != null;
    }

    public void setAspectFit(boolean value) {
        isAspectFit = value;
    }

    public void setParentView(View view) {
        parentView = view;
    }

    public void setImageCoords(int x, int y, int width, int height) {
        imageX = x;
        imageY = y;
        imageW = width;
        imageH = height;
    }

    public int getImageX() {
        return imageX;
    }

    public int getImageX2() {
        return imageX + imageW;
    }

    public int getImageY() {
        return imageY;
    }

    public int getImageY2() {
        return imageY + imageH;
    }

    public int getImageWidth() {
        return imageW;
    }

    public int getImageHeight() {
        return imageH;
    }

    public boolean isInsideImage(float x, float y) {
        return x >= imageX && x <= imageX + imageW && y >= imageY && y <= imageY + imageH;
    }

    public Rect getDrawRegion() {
        return drawRegion;
    }

    public String getFilter() {
        return currentFilter;
    }

    public String getThumbFilter() {
        return currentThumbFilter;
    }

    public String getKey() {
        return currentKey;
    }

    public String getThumbKey() {
        return currentThumbKey;
    }

    public int getSize() {
        return currentSize;
    }

    public TdApi.File getImageLocation() {
        return currentImageLocation;
    }

    public TdApi.File getThumbLocation() {
        return currentThumbLocation;
    }

    public String getHttpImageLocation() {
        return currentHttpUrl;
    }

    public boolean getCacheOnly() {
        return currentCacheOnly;
    }

    public void setForcePreview(boolean value) {
        forcePreview = value;
    }

    public boolean isForcePreview() {
        return forcePreview;
    }

    public void setRoundRadius(int value) {
        roundRadius = value;
        if (roundRadius != 0) {
            if (roundPaint == null) {
                roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                roundRect = new RectF();
                shaderMatrix = new Matrix();
                bitmapRect = new RectF();
            }
        } else {
            roundPaint = null;
            roundRect = null;
            shaderMatrix = null;
            bitmapRect = null;
        }
    }

    public int getRoundRadius() {
        return roundRadius;
    }

    public void setParentMessage(Message message) {
        parentMessage = message;
    }

    public Message getParentMessage() {
        return parentMessage;
    }

    public void setNeedsQualityThumb(boolean value) {
        needsQualityThumb = value;
    }

    public boolean isNeedsQualityThumb() {
        return needsQualityThumb;
    }

    public void setShouldGenerateQualityThumb(boolean value) {
        shouldGenerateQualityThumb = value;
    }

    public boolean isShouldGenerateQualityThumb() {
        return shouldGenerateQualityThumb;
    }

    public Integer getTag(boolean thumb) {
        if (thumb) {
            return thumbTag;
        } else {
            return tag;
        }
    }

    public void setTag(Integer value, boolean thumb) {
        if (thumb) {
            thumbTag = value;
        } else {
            tag = value;
        }
    }

    public void setImage(Bitmap bitmap) {
        currentImage = new BitmapDrawable(bitmap);
        if (roundRadius != 0) {
            Bitmap object = bitmap;
            bitmapShader = new BitmapShader(object, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            roundPaint.setShader(bitmapShader);
            bitmapRect.set(0, 0, object.getWidth(), object.getHeight());
        }
        parentView.invalidate();
    }

    protected void setImageBitmapByKey(BitmapDrawable bitmap, String key, boolean thumb) {
        if (bitmap == null || key == null) {
            return;
        }
        if (!thumb) {
            if (currentKey == null || !key.equals(currentKey)) {
                return;
            }
            FileController.use(currentKey);
            currentImage = bitmap;
            if (roundRadius != 0 && bitmap instanceof BitmapDrawable) {
                Bitmap object = bitmap.getBitmap();
                bitmapShader = new BitmapShader(object, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                roundPaint.setShader(bitmapShader);
                bitmapRect.set(0, 0, object.getWidth(), object.getHeight());
            }
            if (parentView != null) {
                parentView.invalidate();
            }
        } else if (currentThumb == null && (currentImage == null || forcePreview)) {
            if (currentThumbKey == null || !key.equals(currentThumbKey)) {
                return;
            }
            FileController.use(currentThumbKey);
            currentThumb = bitmap;
            if (!(staticThumb instanceof BitmapDrawable) && parentView != null) {
                parentView.invalidate();
            }
        }
    }

    private void recycleBitmap(String newKey, boolean thumb) {
        String key;
        BitmapDrawable image;
        if (thumb) {
            if (currentThumb == null) {
                return;
            }
            key = currentThumbKey;
            image = currentThumb;
        } else {
            if (currentImage == null) {
                return;
            }
            key = currentKey;
            image = currentImage;
        }
        BitmapDrawable newBitmap = null;
        if (newKey != null) {
            //newBitmap = ImageController.getImageFromMemory(newKey);
        }
        if (key == null || image == null || image == newBitmap) {
            return;
        }
        Bitmap bitmap = image.getBitmap();
        boolean canDelete = FileController.ignore(key);
//        bitmap.recycle();
//        if (!ImageController.isInCache(key)) {
//            if (ImageController.runtimeHack != null) {
//                ImageController.runtimeHack.trackAlloc(bitmap.getRowBytes() * bitmap.getHeight());
//            }
//            if (canDelete) {
//                bitmap.recycle();
//                ImageController.callGC();
//            }
//        }
        if (thumb) {
            currentThumb = null;
            currentThumbKey = null;
        } else {
            currentImage = null;
            currentKey = null;
        }
    }
}