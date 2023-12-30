/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.ui.animationcompact;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.animation.Interpolator;

public class ObjectAnimatorProxy {

    private Object objectAnimator;

    public ObjectAnimatorProxy(Object animator) {
        objectAnimator = animator;
    }

    public static Object ofFloat(Object target, String propertyName, float... values) {
        return ObjectAnimator.ofFloat(target, propertyName, values);
    }

    public static Object ofInt(Object target, String propertyName, int... values) {
        return ObjectAnimator.ofInt(target, propertyName, values);
    }

    public static ObjectAnimatorProxy ofFloatProxy(Object target, String propertyName, float... values) {
        return new ObjectAnimatorProxy(ObjectAnimator.ofFloat(target, propertyName, values));
    }

    public static ObjectAnimatorProxy ofIntProxy(Object target, String propertyName, int... values) {
        return new ObjectAnimatorProxy(ObjectAnimator.ofInt(target, propertyName, values));
    }

    public ObjectAnimatorProxy setDuration(long duration) {
        ((ObjectAnimator) objectAnimator).setDuration(duration);
        return this;
    }

    public void setInterpolator(Interpolator value) {
        ((ObjectAnimator) objectAnimator).setInterpolator(value);
    }

    public void start() {
        ((ObjectAnimator) objectAnimator).start();
    }

//    public void setAutoCancel(boolean cancel) {
//        ((ObjectAnimator) objectAnimator).setAutoCancel(cancel);
//    }

    public boolean isRunning() {
        return ((ObjectAnimator) objectAnimator).isRunning();
    }

    public void end() {
        ((ObjectAnimator) objectAnimator).end();
    }

    public void cancel() {
        ((ObjectAnimator) objectAnimator).cancel();
    }

    public ObjectAnimatorProxy addListener(AnimatorListenerAdapterProxy listener) {
        ((ObjectAnimator) objectAnimator).addListener((AnimatorListenerAdapter) listener.animatorListenerAdapter);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return objectAnimator == o;
    }
}
