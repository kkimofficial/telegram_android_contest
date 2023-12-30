package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Sticker;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.ui.component.adapter.StickersAdapter;

public class LoadStickersAction extends Action {
    private StickersAdapter stickersAdapter;

    public LoadStickersAction(StickersAdapter stickersAdapter) {
        this.stickersAdapter = stickersAdapter;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.GetStickers(""), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                if(!(tlObject instanceof TdApi.Stickers)) {
                    return;
                }

                TdApi.Stickers stickers = (TdApi.Stickers)tlObject;
                for(TdApi.Sticker s : stickers.stickers) {
                    Sticker.stickersList.add(new Sticker(s));
                    //FileController.load(FileController.getId(s.sticker), null);
                }

                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        stickersAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}
