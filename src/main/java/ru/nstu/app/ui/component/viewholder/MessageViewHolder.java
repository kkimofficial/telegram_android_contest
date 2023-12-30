package ru.nstu.app.ui.component.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import ru.nstu.app.model.Message;
import ru.nstu.app.ui.component.panel.MessagePanel;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    private MessagePanel messagePanel;

    public MessageViewHolder(View itemView) {
        super(itemView);
        messagePanel = (MessagePanel)itemView;
    }

    public void bind(Message message) {
        messagePanel.setData(message);
    }
}
