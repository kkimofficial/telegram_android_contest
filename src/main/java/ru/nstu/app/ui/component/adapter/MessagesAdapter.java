package ru.nstu.app.ui.component.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import ru.nstu.app.model.Message;
import ru.nstu.app.ui.component.panel.*;
import ru.nstu.app.ui.component.viewholder.MessageViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    public static final int PAGE_SIZE = 20;

    private static final int MESSAGE_CONTENT_TEXT = 1;
    private static final int MESSAGE_CONTENT_PHOTO = 2;
    private static final int MESSAGE_CONTENT_VIDEO = 3;
    private static final int MESSAGE_CONTENT_CONTACT = 4;
    private static final int MESSAGE_CONTENT_DOCUMENT = 5;
    private static final int MESSAGE_CONTENT_GEO_POINT = 6;
    private static final int MESSAGE_CONTENT_AUDIO = 7;
    private static final int MESSAGE_CONTENT_SERVICE = 8;
    private static final int MESSAGE_CONTENT_STICKER = 9;

    private static final int MESSAGE_CONTENT_FORWARD_TEXT = 51;

    private static final int MESSAGE_CONTENT_SYSTEM_DATE = 101;
    private static final int MESSAGE_CONTENT_SYSTEM_NEW_MESSAGES = 102;

    private List<Message> messagesList = new ArrayList<Message>();

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);

        if(message.isContentSystemDate()) {
            return MESSAGE_CONTENT_SYSTEM_DATE;
        } else if(message.isContentSystemNewMessages()) {
            return MESSAGE_CONTENT_SYSTEM_NEW_MESSAGES;
        }

        if(message.isForward() && message.isContentText()) {
            return MESSAGE_CONTENT_FORWARD_TEXT;
        }

        if(message.isContentText()) {
            return MESSAGE_CONTENT_TEXT;
        } else if(message.isContentPhoto()) {
            return MESSAGE_CONTENT_PHOTO;
        } else if(message.isContentVideo()) {
            return MESSAGE_CONTENT_VIDEO;
        } else if(message.isContentContact()) {
            return MESSAGE_CONTENT_CONTACT;
        } else if(message.isContentDocument()) {
            return MESSAGE_CONTENT_DOCUMENT;
        } else if(message.isContentGeoPoint()) {
            return MESSAGE_CONTENT_GEO_POINT;
        } else if(message.isContentAudio()) {
            return MESSAGE_CONTENT_AUDIO;
        } else if(message.isContentService()) {
            return MESSAGE_CONTENT_SERVICE;
        } else if(message.isContentSticker()) {
            return MESSAGE_CONTENT_STICKER;
        }
        return 0;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        MessagePanel messagePanel = null;
        if(i == MESSAGE_CONTENT_TEXT) {
            messagePanel = new MessageTextPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_PHOTO) {
            messagePanel = new MessagePhotoPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_VIDEO) {
            messagePanel = new MessageVideoPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_CONTACT) {
            messagePanel = new MessageContactPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_DOCUMENT) {
            messagePanel = new MessageDocumentPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_GEO_POINT) {
            messagePanel = new MessageGeoPointPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_SERVICE) {
            messagePanel = new MessageServicePanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_SYSTEM_DATE) {
            messagePanel = new SystemDatePanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_FORWARD_TEXT) {
            messagePanel = new MessageForwardTextPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_STICKER) {
            messagePanel = new MessageStickerPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_AUDIO) {
            messagePanel = new MessageAudioPanel(viewGroup.getContext());
        } else if(i == MESSAGE_CONTENT_SYSTEM_NEW_MESSAGES) {
            messagePanel = new SystemNewMessagesPanel(viewGroup.getContext());
        }
        return new MessageViewHolder(messagePanel);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder messageViewHolder, int i) {
        messageViewHolder.bind(messagesList.get(i));
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public void add(List<Message> messages, int index) {
        messagesList.addAll(index, messages);
        notifyItemRangeInserted(index, messages.size());
        notifyItemChanged(index + messages.size());
        if(index > 0) {
            notifyItemChanged(index - 1);
        }
    }

    public void clear() {
        messagesList.clear();
        notifyDataSetChanged();
    }

    public void removeTopMessage() {
        if(messagesList.size() > 0) {
            messagesList.remove(0);
            notifyItemRemoved(0);
        }
    }

    public void remove(int index) {
        messagesList.remove(index);
        notifyItemRemoved(index);
    }

    public Message get(int index) {
        if(index < 0 || index > getItemCount() - 1) {
            return null;
        }
        return messagesList.get(index);
    }

    public int indexOf(Message message) {
        return messagesList.indexOf(message);
    }
}
