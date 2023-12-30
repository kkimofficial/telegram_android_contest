package ru.nstu.app.controller;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.ImageReceiver;
import ru.nstu.app.api.action.LoadFileAction;
import ru.nstu.app.model.Message;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FileController {



    // =================================== GGGGGGGGGG
    public static void load(ImageReceiver imageReceiver) {
        final int fileId = Integer.parseInt(imageReceiver.getKey().substring(0, imageReceiver.getKey().indexOf("@")));
        //final String fileId = imageReceiver.getKey();
        System.out.println("================= IMAGE KEY: " + fileId);
        if(fileId == 0) {
            return;
        }
        if(pathsMap.containsKey(fileId)) {
            load(imageReceiver, pathsMap.get(fileId), fileId);
            return;
        }
        final WeakReference<ImageReceiver> weakReference = new WeakReference<ImageReceiver>(imageReceiver);
        load(fileId, new Callback<TdApi.TLObject>() {
            @Override
            public void call(final TdApi.TLObject update) {
                if (weakReference.get() == null) {
                    return;
                }
                if (!(update instanceof TdApi.UpdateFile)) {
                    return;
                }
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(Droid.activity).load(new File(((TdApi.UpdateFile) update).path)).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (weakReference.get() == null || !weakReference.get().getTag(false).equals(Integer.valueOf(fileId))) {
                                    return;
                                }
                                weakReference.get().setImage(bitmap);
                            }
                        });
                    }
                });
            }
        });
    }

    public static void load(ImageReceiver imageReceiver, String url, final int fileId) {
        final WeakReference<ImageReceiver> weakReference = new WeakReference<ImageReceiver>(imageReceiver);
        final String key = url;
        Droid.doRunnableUI(new Runnable() {
            @Override
            public void run() {
                Glide.with(Droid.activity).load(key).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (weakReference.get() == null || !weakReference.get().getTag(false).equals(Integer.valueOf(fileId))) {
                            return;
                        }
                        weakReference.get().setImage(bitmap);
                    }
                });
            }
        });
    }
    // =================================== GGGGGGGGGG

    public static BitmapDrawable getImage(String key) {
        return null;
    }

    public static void cancel(ImageReceiver imageReceiver) {

    }

    public static void cancel(TdApi.File file) {

    }

    public static void use(String key) {

    }

    public static boolean ignore(String key) {
        return false;
    }

    public static void remove(String key) {

    }

    public static boolean equals(TdApi.File file1, TdApi.File file2) {
        return getId(file1) == getId(file2);
    }

    public static final int AUTODOWNLOAD_MASK_PHOTO = 1;
    public static final int AUTODOWNLOAD_MASK_AUDIO = 2;
    public static final int AUTODOWNLOAD_MASK_VIDEO = 4;
    public static final int AUTODOWNLOAD_MASK_DOCUMENT = 8;

    // =======================================

    private static Map<Integer, Set<Callback>> callbacksMap = new ConcurrentHashMap<Integer, Set<Callback>>();
    private static Map<Integer, Float> progressesMap = new ConcurrentHashMap<Integer, Float>();
    private static Map<Integer, String> pathsMap = new ConcurrentHashMap<Integer, String>();

    public static void load(ImageView imageView, final String url, final Callback callback) {
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(imageView);
        Droid.doRunnableUI(new Runnable() {
            @Override
            public void run() {
                Glide.with(Droid.activity).load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (weakReference.get() == null) {
                            return;
                        }
                        weakReference.get().setImageBitmap(bitmap);
                        callback.call(null);
                    }
                });
            }
        });
    }

    public static void load(ImageView imageView, final String url) {
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(imageView);
        Droid.doRunnableUI(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = weakReference.get();
                if(imageView == null) {
                    return;
                }
                Glide.with(Droid.activity).load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
            }
        });
    }

    public static void loadAndCrop(ImageView imageView, final String url) {
        final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(imageView);
        Droid.doRunnableUI(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = weakReference.get();
                if (imageView == null) {
                    return;
                }
                Glide.with(Droid.activity).load(url).asBitmap().centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
            }
        });
    }

    public static void load(int key, Callback callback) {
        addCallback(key, callback);
        progressesMap.put(key, 0.0f);
        Droid.doAction(new LoadFileAction(key));
    }

    public static synchronized void addCallback(int key, Callback callback) {
//        callbacksMap.put(key, callback);
        Set<Callback> callbacks = callbacksMap.get(key);
        if(callbacks == null) {
            callbacksMap.put(key, callbacks = new HashSet<Callback>());
        }
        callbacks.add(callback);
    }

    public static int getSize(TdApi.File file) {
        if(file instanceof TdApi.FileEmpty) {
            return ((TdApi.FileEmpty)file).size;
        } else if(file instanceof TdApi.FileLocal) {
            return ((TdApi.FileLocal)file).size;
        }
        return 0;
    }

    public static String getPath(TdApi.File file) {
        if(file instanceof TdApi.FileLocal) {
            return ((TdApi.FileLocal)file).path;
        }
        return pathsMap.get(getId(file));
    }

    public static int getId(TdApi.File file) {
        if(file instanceof TdApi.FileEmpty) {
            return ((TdApi.FileEmpty)file).id;
        } else if(file instanceof TdApi.FileLocal) {
            return ((TdApi.FileLocal)file).id;
        }
        return 0;
    }

    public static boolean isExists(TdApi.File file) {
        return getId(file) != 0;
    }

    public static boolean isCached(TdApi.File file) {
        return pathsMap.containsKey(getId(file)) || file instanceof TdApi.FileLocal;
    }

    public static boolean isLoading(TdApi.File file) {
        Float progress = getProgress(getId(file));
        return progress != null && progress < 1.0f;
    }

    public static String formatSize(int size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static Float getProgress(int key) {
        return progressesMap.containsKey(key) ? progressesMap.get(key) : null;
    }

    public static void onFileUpdate(TdApi.TLObject update) {
        int key = 0;

        if(update instanceof TdApi.UpdateFile) {
            key = ((TdApi.UpdateFile)update).fileId;
            pathsMap.put(key, ((TdApi.UpdateFile)update).path);
            System.out.println("============ FILE UPDATE KEY:[" + key + "],  PATH:[" + ((TdApi.UpdateFile)update).path + "]");
            progressesMap.put(key, 1.0f);
        }
        if(update instanceof TdApi.UpdateFileProgress) {
            key = ((TdApi.UpdateFileProgress)update).fileId;
            progressesMap.put(key, 1.0f * ((TdApi.UpdateFileProgress)update).ready / ((TdApi.UpdateFileProgress)update).size);
        }
        if(key == 0) {
            return;
        }
        if(callbacksMap.containsKey(key) && callbacksMap.get(key) != null) {
            for(Callback callback : callbacksMap.get(key)) {
                callback.call(update);
            }
        }
    }
}
