package ru.nstu.app.controller;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import ru.nstu.app.R;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.android.dateformat.FastDateFormat;

import java.util.*;

public class LocaleController {
    public static boolean isRTL;
    public static Locale locale;
    private static boolean is24HourFormat;
    public static FastDateFormat formatDay;
    public static FastDateFormat formatWeek;
    public static FastDateFormat formatMonth;
    public static FastDateFormat formatYear;
    public static FastDateFormat formatMonthYear;
    public static FastDateFormat formatYearMax;
    public static FastDateFormat formatChatDate;
    public static FastDateFormat formatChatFullDate;
    public static FastDateFormat formatMinutes;
    public static long timeDifference;

    public static void init() {
        if (locale == null) {
            locale = Locale.ENGLISH;//Locale.getDefault(); TODO:
        }
        String language = locale.getLanguage();
        if (language == null) {
            language = "en";
        }
        isRTL = language.toLowerCase().equals("ar");
        is24HourFormat = true;

        formatMonth = FastDateFormat.getInstance(getString(R.string.format_month_day), locale);
        formatYear = FastDateFormat.getInstance(getString(R.string.format_day_month_year), locale);
        formatYearMax = FastDateFormat.getInstance(getString(R.string.format_day_month_year_full), locale);
        formatChatDate = FastDateFormat.getInstance(getString(R.string.format_chat_date), locale);
        formatChatFullDate = FastDateFormat.getInstance(getString(R.string.format_chat_date_full), locale);
        formatWeek = FastDateFormat.getInstance(getString(R.string.format_week), locale);
        formatMonthYear = FastDateFormat.getInstance(getString(R.string.format_month_year), locale);
        formatDay = FastDateFormat.getInstance(is24HourFormat ? getString(R.string.format_day_24h) : getString(R.string.format_day_12h), language.toLowerCase().equals("ar") || language.toLowerCase().equals("ko") ? locale : Locale.US);
        formatMinutes = FastDateFormat.getInstance(getString(R.string.format_minutes), locale);

        timeDifference = 0;
    }

    public static int getCurrentTime() {
        return (int)(System.currentTimeMillis() / 1000);
    }

    public static String getString(int resId) {
        return Droid.activity.getString(resId);
    }

    public static String formatString(int resId, Object... args) {
        String value = getString(resId);
        if (locale != null) {
            return String.format(locale, value, args);
        } else {
            return String.format(value, args);
        }
    }

    public static String formatDate(long date, DateFormat dateFormat) {
        if(dateFormat == DateFormat.CONTACTS_LIST) {
            Calendar now = Calendar.getInstance();
            int day = now.get(Calendar.DAY_OF_YEAR);
            int year = now.get(Calendar.YEAR);
            now.setTimeInMillis(date * 1000);
            int dateDay = now.get(Calendar.DAY_OF_YEAR);
            int dateYear = now.get(Calendar.YEAR);
            if (dateDay == day && year == dateYear) {
                return String.format("%s %s %s", LocaleController.getString(R.string.last_seen), LocaleController.getString(R.string.today_at), formatDay.format(new Date(date * 1000)));
            } else if (dateDay + 1 == day && year == dateYear) {
                return String.format("%s %s %s", LocaleController.getString(R.string.last_seen), LocaleController.getString(R.string.yesterday_at), formatDay.format(new Date(date * 1000)));
            } else if (year == dateYear) {
                String format = LocaleController.formatString(R.string.format_date_at_time, formatMonth.format(new Date(date * 1000)), formatDay.format(new Date(date * 1000)));
                return String.format("%s %s", LocaleController.getString(R.string.last_seen), format);
            } else {
                String format = LocaleController.formatString(R.string.format_date_at_time, formatYear.format(new Date(date * 1000)), formatDay.format(new Date(date * 1000)));
                return String.format("%s %s", LocaleController.getString(R.string.last_seen), format);
            }
        } else if(dateFormat == DateFormat.DIALOGS_LIST) {
            Calendar rightNow = Calendar.getInstance();
            int day = rightNow.get(Calendar.DAY_OF_YEAR);
            int year = rightNow.get(Calendar.YEAR);
            rightNow.setTimeInMillis(date * 1000);
            int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
            int dateYear = rightNow.get(Calendar.YEAR);

            if (year != dateYear) {
                return formatYear.format(new Date(date * 1000));
            } else {
                int dayDiff = dateDay - day;
                if(dayDiff == 0 || dayDiff == -1 && (int)(System.currentTimeMillis() / 1000) - date < 60 * 60 * 8) {
                    return formatDay.format(new Date(date * 1000));
                } else if(dayDiff > -7 && dayDiff <= -1) {
                    return formatWeek.format(new Date(date * 1000));
                } else {
                    return formatMonth.format(new Date(date * 1000));
                }
            }
        } else if(dateFormat == DateFormat.TIME_TO_LIVE) {
            String str;
            if (date < 60) {
                str = date + "s";
            } else {
                str = date / 60 + "m";
            }
            return str;
        } else if(dateFormat == DateFormat.MESSAGES_LIST) {
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            now.setTimeInMillis(date * 1000);
            int dateYear = now.get(Calendar.YEAR);
            if(year == dateYear) {
                return formatChatDate.format(date * 1000);
            } else {
                return formatChatFullDate.format(date * 1000);
            }

        } else if(dateFormat == DateFormat.AUDIO) {
            return formatMinutes.format(date * 1000);
        }
        return null;
    }

    public static Spannable replaceTags(String str) {
        try {
            int start = -1;
            int startColor = -1;
            int end = -1;
            StringBuilder stringBuilder = new StringBuilder(str);
            while ((start = stringBuilder.indexOf("<br>")) != -1) {
                stringBuilder.replace(start, start + 4, "\n");
            }
            while ((start = stringBuilder.indexOf("<br/>")) != -1) {
                stringBuilder.replace(start, start + 5, "\n");
            }
            List<Integer> bolds = new ArrayList<Integer>();
            List<Integer> colors = new ArrayList<Integer>();
            while ((start = stringBuilder.indexOf("<b>")) != -1 || (startColor = stringBuilder.indexOf("<c")) != -1) {
                if (start != -1) {
                    stringBuilder.replace(start, start + 3, "");
                    end = stringBuilder.indexOf("</b>");
                    stringBuilder.replace(end, end + 4, "");
                    bolds.add(start);
                    bolds.add(end);
                } else if (startColor != -1) {
                    stringBuilder.replace(startColor, startColor + 2, "");
                    end = stringBuilder.indexOf(">", startColor);
                    int color = Color.parseColor(stringBuilder.substring(startColor, end));
                    stringBuilder.replace(startColor, end + 1, "");
                    end = stringBuilder.indexOf("</c>");
                    stringBuilder.replace(end, end + 4, "");
                    colors.add(startColor);
                    colors.add(end);
                    colors.add(color);
                }
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
            for (int a = 0; a < bolds.size() / 2; a++) {
                spannableStringBuilder.setSpan(DisplayController.typefaceSpan(), bolds.get(a * 2), bolds.get(a * 2 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            for (int a = 0; a < colors.size() / 3; a++) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(colors.get(a * 3 + 2)), colors.get(a * 3), colors.get(a * 3 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannableStringBuilder;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return new SpannableStringBuilder(str);
    }

    public enum DateFormat {
        CONTACTS_LIST,
        DIALOGS_LIST,
        TIME_TO_LIVE,
        MESSAGES_LIST,
        AUDIO
    }
}
