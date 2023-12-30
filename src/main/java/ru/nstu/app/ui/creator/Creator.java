package ru.nstu.app.ui.creator;

import android.view.ViewGroup;

import ru.nstu.app.ui.MainActivity;

public abstract class Creator {
    public abstract void create(MainActivity activity);

    public abstract ViewGroup getRootLayout(MainActivity activity);
}
