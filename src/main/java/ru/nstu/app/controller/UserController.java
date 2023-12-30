package ru.nstu.app.controller;

import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.R;
import ru.nstu.app.model.Chat;
import ru.nstu.app.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserController {
    public static volatile int userId;
    public static Map<Integer, User> usersMap = new ConcurrentHashMap<Integer, User>();
    public static Map<Integer, User> contactsMap = new ConcurrentHashMap<Integer, User>();
    public static List<String> sortedUsersSectionsArray = new ArrayList<String>();
    public static Map<String, List<User>> usersSectionsDict = new ConcurrentHashMap<String, List<User>>();

    private static Map<Integer, Chat> chatsMap = new ConcurrentHashMap<Integer, Chat>();

    public static List<Contact> phoneBookContacts;

    public static boolean contactsEndReached = true;
    public static boolean loadingContacts = false;

    public static class Contact {
        public int id;
        public List<String> phones = new ArrayList<String>();
        public List<String> phoneTypes = new ArrayList<String>();
        public List<String> shortPhones = new ArrayList<String>();
        public List<Integer> phoneDeleted = new ArrayList<Integer>();
        public String first_name;
        public String last_name;
    }

    public static String formatName(String firstName, String lastName) {
        String result = firstName;
        if (result == null || result.length() == 0) {
            result = lastName;
        } else if (result.length() != 0 && lastName != null && lastName.length() != 0) {
            result += " " + lastName;
        }
        return result.trim();
    }

    public static String formatUserStatus(User user) {
        if(user == null || user.getUserStatus() == null || user.getUserStatus() == null) {
            return LocaleController.getString(R.string.user_status_long_time_ago);
        }
        if(user.getUserStatus() instanceof TdApi.UserStatusRecently) {
            return LocaleController.getString(R.string.user_status_recently);
        }
        if(user.getUserStatus() instanceof TdApi.UserStatusLastWeek) {
            return LocaleController.getString(R.string.user_status_last_week);
        }
        if(user.getUserStatus() instanceof TdApi.UserStatusLastMonth) {
            return LocaleController.getString(R.string.user_status_last_month);
        }
        if(user.getUserStatus() instanceof TdApi.UserStatusOnline && ((TdApi.UserStatusOnline)user.getUserStatus()).expires > LocaleController.getCurrentTime()) {
            return LocaleController.getString(R.string.online);
        }
        if(user.getUserStatus() instanceof TdApi.UserStatusOffline || user.getUserStatus() instanceof TdApi.UserStatusOnline) {
            if(user.getUserStatus() instanceof TdApi.UserStatusOnline) {
                return LocaleController.formatDate(((TdApi.UserStatusOnline)user.getUserStatus()).expires, LocaleController.DateFormat.CONTACTS_LIST);
            }
            if(user.getUserStatus() instanceof TdApi.UserStatusOffline) {
                return LocaleController.formatDate(((TdApi.UserStatusOffline)user.getUserStatus()).wasOnline, LocaleController.DateFormat.CONTACTS_LIST);
            }
        }
        return "";
    }

    public static String formatChatStatus(Chat chat) {
        return LocaleController.formatString(R.string.members_online, chat.getParticipantsCount(), chat.getOnlineParticipantsCount());
    }

    public static User getUser(int id) {
        User user = usersMap.get(id);
        if(user == null) {
            usersMap.put(id, user = new User(id));
        }
        return user;
    }

    public static Chat getChat(int id) {
        Chat chat = chatsMap.get(id);
        if(chat == null) {
            chatsMap.put(id, chat = new Chat(id));
        }
        return chat;
    }

    public static void processUsers(List<User> users) {
        for (User user : users) {
//            String key = user.getFirstName();
//            if (key == null || key.length() == 0) {
//                key = user.getLastName();
//            }
//            if (key.length() == 0) {
//                key = "#";
//            } else {
//                key = key.toUpperCase();
//            }
//            if (key.length() > 1) {
//                key = key.substring(0, 1);
//            }
//            List<User> arr = UserController.usersSectionsDict.get(key);
//            if (arr == null) {
//                arr = new ArrayList<User>();
//                UserController.usersSectionsDict.put(key, arr);
//                UserController.sortedUsersSectionsArray.add(key);
//            }
//            arr.add(user);
//
//            UserController.usersMap.put(user.getId(), user);
            UserController.contactsMap.put(user.getId(), user);
        }

        Collections.sort(UserController.sortedUsersSectionsArray, new Comparator<String>() {
            @Override
            public int compare(String s, String s2) {
                char cv1 = s.charAt(0);
                char cv2 = s2.charAt(0);
                if (cv1 == '#') {
                    return 1;
                } else if (cv2 == '#') {
                    return -1;
                }
                return s.compareTo(s2);
            }
        });
    }
}
