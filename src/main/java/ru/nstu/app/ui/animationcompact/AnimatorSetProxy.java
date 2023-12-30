/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.ui.animationcompact;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.view.animation.Interpolator;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AnimatorSetProxy {

    private Object animatorSet;

    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        return copyOfRange(original, 0, newLength, newType);
    }

    @SuppressWarnings("unchecked")
    public static <T, U> T[] copyOfRange(U[] original, int start, int end, Class<? extends T[]> newType) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        T[] result = (T[]) Array.newInstance(newType.getComponentType(), resultLength);
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public AnimatorSetProxy() {
        animatorSet = new AnimatorSet();
    }

    @SuppressWarnings("unchecked")
    public void playTogether(Object... items) {
        Animator[] animators = copyOf(items, items.length, Animator[].class);
        ((AnimatorSet) animatorSet).playTogether(animators);
    }

    public void playTogether(ArrayList<Object> items) {
        ArrayList<Animator> animators = new ArrayList<Animator>();
        for (Object obj : items) {
            animators.add((Animator)obj);
        }
        ((AnimatorSet) animatorSet).playTogether(animators);
    }

    public AnimatorSetProxy setDuration(long duration) {
        ((AnimatorSet) animatorSet).setDuration(duration);
        return this;
    }

    public void start() {
        ((AnimatorSet) animatorSet).start();
    }

    public void cancel() {
        ((AnimatorSet) animatorSet).cancel();
    }

    public void addListener(AnimatorListenerAdapterProxy listener) {
        ((AnimatorSet) animatorSet).addListener((AnimatorListenerAdapter) listener.animatorListenerAdapter);
    }

    public void setInterpolator(Interpolator interpolator) {
        ((AnimatorSet) animatorSet).setInterpolator(interpolator);
    }

    @Override
    public boolean equals(Object o) {
        return animatorSet == o;
    }
}
