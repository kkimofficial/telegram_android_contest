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

public class AnimatorListenerAdapterProxy {
    protected Object animatorListenerAdapter;

    public AnimatorListenerAdapterProxy() {
        animatorListenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                AnimatorListenerAdapterProxy.this.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                AnimatorListenerAdapterProxy.this.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                AnimatorListenerAdapterProxy.this.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                AnimatorListenerAdapterProxy.this.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                AnimatorListenerAdapterProxy.this.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                AnimatorListenerAdapterProxy.this.onAnimationResume(animation);
            }
        };
    }

    public void onAnimationCancel(Object animation) {

    }

    public void onAnimationEnd(Object animation) {

    }

    public void onAnimationRepeat(Object animation) {

    }

    public void onAnimationStart(Object animation) {

    }

    public void onAnimationPause(Object animation) {

    }

    public void onAnimationResume(Object animation) {

    }
}
