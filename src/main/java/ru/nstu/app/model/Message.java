package ru.nstu.app.model;

import android.text.StaticLayout;
import android.text.TextPaint;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.R;
import ru.nstu.app.android.Sticker;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;

import java.util.List;

public class Message {
    private TdApi.Message messageOwner;

    public Message(int id) {
        messageOwner = new TdApi.Message();
        messageOwner.id = id;
    }

    public Message(TdApi.Message message) {
        messageOwner = message;
    }

    public int getId() {
        return messageOwner.id;
    }

    public void setId(int id) {
        messageOwner.id = id;
    }

    public boolean isUnread() {
        return messageOwner != null && messageOwner.id > MessageController.getDialog(messageOwner.chatId).getLastReadOutboxMessageId();
    }

    public TdApi.Message getMessage() {
        return messageOwner;
    }

    public long getDialogId() {
        return messageOwner.chatId;
    }

    public boolean isOut() {
        return messageOwner.fromId == UserController.userId;
    }

    public boolean isSending() {
        return getSendState() == MESSAGE_SEND_STATE_SENDING;
    }

    public boolean isSent() {
        return getSendState() == MESSAGE_SEND_STATE_SENT;
    }

    public boolean isSendError() {
        return getSendState() == MESSAGE_SEND_STATE_SEND_ERROR;
    }

    public int getSendState() {
        if(messageOwner.id >= 1000000000) {
            return MESSAGE_SEND_STATE_SENDING;
        } else if(messageOwner.date == 0) {
            return MESSAGE_SEND_STATE_SEND_ERROR;
        }
        return MESSAGE_SEND_STATE_SENT;
    }



    public int getDate() {
        return messageOwner.date;
    }

    public int getFromId() {
        return messageOwner.fromId;
    }



    public boolean isMediaEmpty() {
        return !isContentService() && !isContentPhoto() &&!isContentVideo() && !isContentAudio() && !isContentContact() && !isContentGeoPoint() && !isContentSticker();
    }






    public static final int MESSAGE_SEND_STATE_SENT = 0;
    public static final int MESSAGE_SEND_STATE_SENDING = 1;
    public static final int MESSAGE_SEND_STATE_SEND_ERROR = 2;





    // ======

    public boolean isContentText() {
        return messageOwner.message instanceof TdApi.MessageText;
    }

    public boolean isContentService() {
        return messageOwner.message instanceof TdApi.MessageChatAddParticipant
                || messageOwner.message instanceof TdApi.MessageChatChangePhoto
                || messageOwner.message instanceof TdApi.MessageChatChangeTitle
                || messageOwner.message instanceof TdApi.MessageChatDeleteParticipant
                || messageOwner.message instanceof TdApi.MessageChatDeletePhoto
                || messageOwner.message instanceof TdApi.MessageGroupChatCreate;
    }

    public boolean isContentPhoto() {
        return messageOwner.message instanceof TdApi.MessagePhoto;
    }

    public boolean isContentVideo() {
        return messageOwner.message instanceof TdApi.MessageVideo;
    }

    public boolean isContentAudio() {
        return messageOwner.message instanceof TdApi.MessageAudio;
    }

    public boolean isContentContact() {
        return messageOwner.message instanceof TdApi.MessageContact;
    }

    public boolean isContentDocument() {
        return messageOwner.message instanceof TdApi.MessageDocument;
    }

    public boolean isContentGeoPoint() {
        return messageOwner.message instanceof TdApi.MessageGeoPoint;
    }

    public boolean isContentSticker() {
        return messageOwner.message instanceof TdApi.MessageSticker;
    }

    public boolean isContentSystemDate() {
        return messageOwner == null && systemDate > 0;
    }

    public boolean isContentSystemNewMessages() {
        return messageOwner == null && systemDate < 0;
    }

    public boolean isForward() {
        return messageOwner.forwardFromId != 0;
    }

    public int getForwardFromId() {
        if(isForward()) {
            return messageOwner.forwardFromId;
        }
        return 0;
    }

    public int getForwardDate() {
        if(isForward()) {
            return messageOwner.forwardDate;
        }
        return 0;
    }

    public void setContent(TdApi.MessageContent messageContent) {
        messageOwner.message = messageContent;
    }

    // ============================ SYSTEM DATE

    private int systemDate;

    public int getContentSystemDate() {
        return systemDate;
    }

    public void setContentSystemDate(int systemDate) {
        this.systemDate = systemDate;
    }

    // ============================ TEXT

    public CharSequence getNotificationDescription() {
        User currentUser = UserController.getUser(getFromId());
        String actorName = currentUser.getId() == UserController.userId ? LocaleController.getString(R.string.from_you) : UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
        if(isContentText()) {
            return actorName + ": " + getContentText();
        } if(isContentPhoto()) {
            return actorName + ": " + LocaleController.getString(R.string.message_photo);
        } else if(isContentVideo()) {
            return actorName + ": " + LocaleController.getString(R.string.message_video);
        } else if(isContentDocument()) {
            return actorName + ": " + LocaleController.getString(R.string.message_document);
        } else if(isContentGeoPoint()) {
            return actorName + ": " + LocaleController.getString(R.string.message_geo_point);
        } else if(isContentContact()) {
            return actorName + ": " + LocaleController.getString(R.string.message_contact);
        } else if(isContentAudio()) {
            return actorName + ": " + LocaleController.getString(R.string.message_audio);
        } else if(isContentSticker()) {
            return actorName + ": " + LocaleController.getString(R.string.message_sticker);
        } else if(isContentService()) {
            if(isContentServiceAddParticipant()) {
                User user = getContentServiceAddParticipantUser();
                String subjectName = user.getId() == UserController.userId ? LocaleController.getString(R.string.from_you) : UserController.formatName(user.getFirstName(), user.getLastName());
                return actorName + " invited " + subjectName;
            } else if(isContentServiceChangeTitle()) {
                return actorName + " changed group name to \"" + getContentServiceChangeTitleTitle() + "\"";
            } else if(isContentServiceDeleteParticipant()) {
                User user = getContentServiceDeleteParticipantUser();
                String subjectName = user.getId() == UserController.userId ? LocaleController.getString(R.string.from_you) : UserController.formatName(user.getFirstName(), user.getLastName());
                return currentUser.getId() == user.getId() ? actorName + " leaved group" : actorName + " kicked " + subjectName;
            } else if(isContentServiceDeletePhoto()) {
                return actorName + " removed group photo";
            } else if(isContentServiceGroupCreate()) {
                return actorName + " created the group";
            } else if(isContentServiceChangePhoto()) {
                return actorName + " changed group photo";
            }
            return "";
        }
        return "";
    }

    public CharSequence getDescription() {
        if(isContentText()) {
            return getContentText();
        } if(isContentPhoto()) {
            return LocaleController.getString(R.string.message_photo);
        } else if(isContentVideo()) {
            return LocaleController.getString(R.string.message_video);
        } else if(isContentDocument()) {
            return LocaleController.getString(R.string.message_document);
        } else if(isContentGeoPoint()) {
            return LocaleController.getString(R.string.message_geo_point);
        } else if(isContentContact()) {
            return LocaleController.getString(R.string.message_contact);
        } else if(isContentAudio()) {
            return LocaleController.getString(R.string.message_audio);
        } else if(isContentSticker()) {
            return LocaleController.getString(R.string.message_sticker);
        } else if(isContentService()) {
            User currentUser = UserController.getUser(getFromId());
            String actorName = currentUser.getId() == UserController.userId ? LocaleController.getString(R.string.from_you) : UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            if(isContentServiceAddParticipant()) {
                User user = getContentServiceAddParticipantUser();
                String subjectName = user.getId() == UserController.userId ? LocaleController.getString(R.string.from_you) : UserController.formatName(user.getFirstName(), user.getLastName());
                return actorName + " invited " + subjectName;
            } else if(isContentServiceChangeTitle()) {
                return actorName + " changed group name to \"" + getContentServiceChangeTitleTitle() + "\"";
            } else if(isContentServiceDeleteParticipant()) {
                User user = getContentServiceDeleteParticipantUser();
                String subjectName = user.getId() == UserController.userId ? LocaleController.getString(R.string.from_you) : UserController.formatName(user.getFirstName(), user.getLastName());
                return currentUser.getId() == user.getId() ? actorName + " leaved group" : actorName + " kicked " + subjectName;
            } else if(isContentServiceDeletePhoto()) {
                return actorName + " removed group photo";
            } else if(isContentServiceGroupCreate()) {
                return actorName + " created the group";
            } else if(isContentServiceChangePhoto()) {
                return actorName + " changed group photo";
            }
            return "";
        }
//        if(MessageController.dialogsMap.get(messageOwner.chatId).getChatId() != 0) {
//            User user = UserController.getUser(getFromId());
//            if(isContentText()) {
//                return LocaleController.formatString(R.string.message_group_text, UserController.formatName(null, user.getFirstName()), getContentText());
//            } else if(isContentPhoto()) {
//                return LocaleController.formatString(R.string.message_group_photo, UserController.formatName(null, user.getFirstName()));
//            } else if(isContentVideo()) {
//                return LocaleController.formatString(R.string.message_group_video, UserController.formatName(null, user.getFirstName()));
//            } else if(isContentDocument()) {
//                return LocaleController.formatString(R.string.message_group_document, UserController.formatName(null, user.getFirstName()));
//            } else if(isContentGeoPoint()) {
//                return LocaleController.formatString(R.string.message_group_document, UserController.formatName(null, user.getFirstName()));
//            } else if(isContentContact()) {
//                return LocaleController.formatString(R.string.message_group_contact, UserController.formatName(null, user.getFirstName()));
//            } else if(isContentAudio()) {
//                return LocaleController.formatString(R.string.message_group_audio, UserController.formatName(null, user.getFirstName()));
//            } else if(isContentSticker()) {
//                return LocaleController.formatString(R.string.message_group_sticker, UserController.formatName(null, user.getFirstName()));
//            } else if(isContentService()) {
//                return "Service";
//            }
//        } else {
//            if(isContentText()) {
//                return getContentText();
//            } if(isContentPhoto()) {
//                return LocaleController.getString(R.string.message_photo);
//            } else if(isContentVideo()) {
//                return LocaleController.getString(R.string.message_video);
//            } else if(isContentDocument()) {
//                return LocaleController.getString(R.string.message_document);
//            } else if(isContentGeoPoint()) {
//                return LocaleController.getString(R.string.message_geo_point);
//            } else if(isContentContact()) {
//                return LocaleController.getString(R.string.message_contact);
//            } else if(isContentAudio()) {
//                return LocaleController.getString(R.string.message_audio);
//            } else if(isContentSticker()) {
//                return LocaleController.getString(R.string.message_sticker);
//            }
//        }
        return null;
    }

    public CharSequence getContentText() {
        if(messageOwner.message instanceof TdApi.MessageText) {
            return ((TdApi.MessageText)messageOwner.message).text;
        }
        return null;
    }

    // ============================ PHOTO

    public TdApi.PhotoSize[] getContentPhotoSizes() {
        if(messageOwner.message instanceof TdApi.MessagePhoto) {
            return ((TdApi.MessagePhoto)messageOwner.message).photo.photos;
        }
        return null;
    }

    // ============================ DOCUMENT

    public Sticker getContentSticker() {
        if(messageOwner.message instanceof TdApi.MessageSticker) {
            return new Sticker(((TdApi.MessageSticker)messageOwner.message).sticker);
        }
        return null;
    }

    // ============================ STICKER

    public int getContentDocumentId() {
        if(messageOwner.message instanceof TdApi.MessageDocument) {
            TdApi.File document = ((TdApi.MessageDocument)messageOwner.message).document.document;
            return FileController.getId(document);
        }
        return 0;
    }

    public String getContentDocumentName() {
        if(messageOwner.message instanceof TdApi.MessageDocument) {
            return ((TdApi.MessageDocument)messageOwner.message).document.fileName;
        }
        return null;
    }

    public TdApi.PhotoSize getContentDocumentThumb() {
        if(messageOwner.message instanceof TdApi.MessageDocument) {
            return ((TdApi.MessageDocument)messageOwner.message).document.thumb;
        }
        return null;
    }

    public int getContentDocumentSize() {
        if(messageOwner.message instanceof TdApi.MessageDocument) {
            TdApi.File document = ((TdApi.MessageDocument)messageOwner.message).document.document;
            return FileController.getSize(document);
        }
        return 0;
    }

    public String getContentDocumentMimeType() {
        if(messageOwner.message instanceof TdApi.MessageDocument) {
            return ((TdApi.MessageDocument)messageOwner.message).document.mimeType;
        }
        return null;
    }

    public TdApi.File getContentDocumentFile() {
        if(messageOwner.message instanceof TdApi.MessageDocument) {
            return ((TdApi.MessageDocument)messageOwner.message).document.document;
        }
        return null;
    }

    public boolean isContentDocumentPhoto() {
        return getContentDocumentMimeType().contains("image");
    }

    // ============================ VIDEO

    public int getContentVideoDuration() {
        if(messageOwner.message instanceof TdApi.MessageVideo) {
            return ((TdApi.MessageVideo)messageOwner.message).video.duration;
        }
        return 0;
    }

    public int getContentVideoSize() {
        if(messageOwner.message instanceof TdApi.MessageVideo) {
            TdApi.File video = ((TdApi.MessageVideo)messageOwner.message).video.video;
            return FileController.getSize(video);
        }
        return 0;
    }

    public int getContentVideoWidth() {
        if(messageOwner.message instanceof TdApi.MessageVideo) {
            return ((TdApi.MessageVideo)messageOwner.message).video.width;
        }
        return 0;
    }

    public int getContentVideoHeight() {
        if(messageOwner.message instanceof TdApi.MessageVideo) {
            return ((TdApi.MessageVideo)messageOwner.message).video.height;
        }
        return 0;
    }

    public TdApi.PhotoSize getContentVideoThumb() {
        if(messageOwner.message instanceof TdApi.MessageVideo) {
            return ((TdApi.MessageVideo)messageOwner.message).video.thumb;
        }
        return null;
    }

    public TdApi.File getContentVideoFile() {
        if(messageOwner.message instanceof TdApi.MessageVideo) {
            return ((TdApi.MessageVideo)messageOwner.message).video.video;
        }
        return null;
    }

    // ============================ GEOPOINT

    public double getContentGeoPointLatitude() {
        if(messageOwner.message instanceof TdApi.MessageGeoPoint) {
            return ((TdApi.MessageGeoPoint)messageOwner.message).geoPoint.latitude;
        }
        return 0;
    }

    public double getContentGeoPointLongtitude() {
        if(messageOwner.message instanceof TdApi.MessageGeoPoint) {
            return ((TdApi.MessageGeoPoint)messageOwner.message).geoPoint.longitude;
        }
        return 0;
    }

    // ============================ CONTACT

    public int getContentContactUserId() {
        if(messageOwner.message instanceof TdApi.MessageContact) {
            return ((TdApi.MessageContact)messageOwner.message).userId;
        }
        return 0;
    }

    public String getContentContactPhoneNumber() {
        if(messageOwner.message instanceof TdApi.MessageContact) {
            return ((TdApi.MessageContact)messageOwner.message).phoneNumber;
        }
        return null;
    }

    public String getContentContactFirstName() {
        if(messageOwner.message instanceof TdApi.MessageContact) {
            return ((TdApi.MessageContact)messageOwner.message).firstName;
        }
        return null;
    }

    public String getContentContactLastName() {
        if(messageOwner.message instanceof TdApi.MessageContact) {
            return ((TdApi.MessageContact)messageOwner.message).lastName;
        }
        return null;
    }

    // ============================ SERVICE

    public boolean isContentServiceAddParticipant() {
        return messageOwner.message instanceof TdApi.MessageChatAddParticipant;
    }

    public User getContentServiceAddParticipantUser() {
        if(messageOwner.message instanceof TdApi.MessageChatAddParticipant) {
            return UserController.getUser(((TdApi.MessageChatAddParticipant)messageOwner.message).user.id);
        }
        return null;
    }

    public boolean isContentServiceChangePhoto() {
        return messageOwner.message instanceof TdApi.MessageChatChangePhoto;
    }

    public TdApi.File getContentServiceChangePhotoFile() {
        if(messageOwner.message instanceof TdApi.MessageChatChangePhoto) {
            return ((TdApi.MessageChatChangePhoto)messageOwner.message).photo.photos[0].photo;
        }
        return null;
    }

    public boolean isContentServiceChangeTitle() {
        return messageOwner.message instanceof TdApi.MessageChatChangeTitle;
    }

    public String getContentServiceChangeTitleTitle() {
        if(messageOwner.message instanceof TdApi.MessageChatChangeTitle) {
            return ((TdApi.MessageChatChangeTitle)messageOwner.message).title;
        }
        return null;
    }

    public boolean isContentServiceDeleteParticipant() {
        return messageOwner.message instanceof TdApi.MessageChatDeleteParticipant;
    }

    public User getContentServiceDeleteParticipantUser() {
        if(messageOwner.message instanceof TdApi.MessageChatDeleteParticipant) {
            return UserController.getUser(((TdApi.MessageChatDeleteParticipant)messageOwner.message).user.id);
        }
        return null;
    }

    public boolean isContentServiceDeletePhoto() {
        return messageOwner.message instanceof TdApi.MessageChatDeletePhoto;
    }

    public boolean isContentServiceGroupCreate() {
        return messageOwner.message instanceof TdApi.MessageGroupChatCreate;
    }

    public Message update(TdApi.Message message) {
        this.messageOwner = message;
        return this;
    }

    // ============================ AUDIO

    public TdApi.File getContentAudioFile() {
        if(messageOwner.message instanceof TdApi.MessageAudio) {
            return ((TdApi.MessageAudio)messageOwner.message).audio.audio;
        }
        return null;
    }

    public int getContentAudioDuration() {
        if(messageOwner.message instanceof TdApi.MessageAudio) {
            return ((TdApi.MessageAudio)messageOwner.message).audio.duration;
        }
        return 0;
    }

    public String getContentAudioMimeType() {
        if(messageOwner.message instanceof TdApi.MessageAudio) {
            return ((TdApi.MessageAudio)messageOwner.message).audio.mimeType;
        }
        return "";
    }
}
