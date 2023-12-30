package ru.nstu.app.model;

import org.drinkless.td.libcore.telegram.TdApi;

public class User {
    private TdApi.User user;

    public User(int id) {
        user = new TdApi.User();
        user.id = id;
    }

    public User(TdApi.User user) {
        this.user = user;
    }

    public int getId() {
        return user.id;
    }

    public String getFirstName() {
        return user.firstName;
    }

    public void setFirstName(String firstName) {
        user.firstName = firstName;
    }

    public String getLastName() {
        return user.lastName;
    }

    public void setLastName(String lastName) {
        user.lastName = lastName;
    }

    public void setUsername(String username) {
        user.username = username;
    }

    public TdApi.File getPhotoSmall() {
        return user.photoSmall;
    }

    public void setPhotoSmall(TdApi.File photoSmall) {
        user.photoSmall = photoSmall;
    }

    public TdApi.UserStatusOnline getUserStatusOnline() {
        if(user.status instanceof TdApi.UserStatusOnline) {
            return (TdApi.UserStatusOnline)user.status;
        }
        return null;
    }

    public TdApi.UserStatus getUserStatus() {
        return user.status;
    }

    public void setUserStatus(TdApi.UserStatus userStatus) {
        user.status = userStatus;
    }

    public String getPhoneNumber() {
        return user.phoneNumber;
    }

    public void setPhoneNumber(String phone) {
        user.phoneNumber = phone;
    }

    public boolean hasSmallPhoto(TdApi.File file) {
        if(getPhotoSmall() instanceof TdApi.FileEmpty && file instanceof TdApi.FileEmpty) {
            return ((TdApi.FileEmpty)getPhotoSmall()).id == ((TdApi.FileEmpty)file).id;
        }
        if(getPhotoSmall() instanceof TdApi.FileLocal && file instanceof TdApi.FileLocal) {
            return ((TdApi.FileLocal)getPhotoSmall()).id == ((TdApi.FileLocal)file).id;
        }
        if(getPhotoSmall() instanceof TdApi.FileEmpty && file instanceof TdApi.FileLocal) {
            return ((TdApi.FileEmpty)getPhotoSmall()).id == ((TdApi.FileLocal)file).id;
        }
        if(getPhotoSmall() instanceof TdApi.FileLocal && file instanceof TdApi.FileEmpty) {
            return ((TdApi.FileLocal)getPhotoSmall()).id == ((TdApi.FileEmpty)file).id;
        }
        return false;
    }

    public User update(TdApi.User user) {
        this.user = user;
        return this;
    }
}
