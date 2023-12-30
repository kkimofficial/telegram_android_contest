package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;

public abstract class Action {
    public abstract void run(Client client) throws Exception;
}
