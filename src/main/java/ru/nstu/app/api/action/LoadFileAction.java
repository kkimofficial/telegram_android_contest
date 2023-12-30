package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.controller.FileController;

public class LoadFileAction extends Action {
    private int fileId;

    public LoadFileAction(int fileId) {
        this.fileId = fileId;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.DownloadFile(fileId), new LoadFileCallback());
    }

    public static class LoadFileCallback implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.TLObject tlObject) {
            //FileController.onFileUpdate(tlObject);
            System.out.println("===== loadfileaction callback" + tlObject.getClass());
        }
    }
}
