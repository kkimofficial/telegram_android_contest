/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.ui.component.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.nstu.app.android.Droid;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.component.panel.DividerPanel;
import ru.nstu.app.ui.component.panel.LetterSectionPanel;
import ru.nstu.app.ui.component.panel.TextSettingsPanel;

public class CountriesAdapter extends SectionsAdapter {

    public static class Country {
        public String name;
        public String code;
        public String shortname;
    }

    private Context mContext;
    private HashMap<String, ArrayList<Country>> countries = new HashMap<>();
    private ArrayList<String> sortedCountries = new ArrayList<>();
    private Map<String, List<Country>> countriesMap = new HashMap<>();

    public CountriesAdapter(Context context) {
        mContext = context;

        try {
            InputStream stream = Droid.activity.getResources().getAssets().open("countries.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                Country c = new Country();
                c.name = args[2];
                c.code = args[0];
                c.shortname = args[1];
                String n = c.name.substring(0, 1).toUpperCase();
                ArrayList<Country> arr = countries.get(n);
                if (arr == null) {
                    arr = new ArrayList<>();
                    countries.put(n, arr);
                    sortedCountries.add(n);
                }
                arr.add(c);

                List<Country> countriesList = countriesMap.get(c.code);
                if(countriesList == null) {
                    countriesMap.put(c.code, countriesList = new ArrayList<Country>());
                }
                countriesList.add(c);
            }
            reader.close();
            stream.close();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        Collections.sort(sortedCountries, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        for (ArrayList<Country> arr : countries.values()) {
            Collections.sort(arr, new Comparator<Country>() {
                @Override
                public int compare(Country country, Country country2) {
                    return country.name.compareTo(country2.name);
                }
            });
        }
    }

    public Map<String, List<Country>> getCountries() {
        return countriesMap;
    }

    @Override
    public Country getItem(int section, int position) {
        if (section < 0 || section >= sortedCountries.size()) {
            return null;
        }
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        if (position < 0 || position >= arr.size()) {
            return null;
        }
        return arr.get(position);
    }

    @Override
    public boolean isRowEnabled(int section, int row) {
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        return row < arr.size();
    }

    @Override
    public int getSectionCount() {
        return sortedCountries.size();
    }

    @Override
    public int getCountForSection(int section) {
        int count = countries.get(sortedCountries.get(section)).size();
        if (section != sortedCountries.size() - 1) {
            count++;
        }
        return count;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new LetterSectionPanel(mContext);
            ((LetterSectionPanel) convertView).setPanelHeight(DisplayController.dp(48));
        }
        ((LetterSectionPanel) convertView).setLetter(sortedCountries.get(section).toUpperCase());
        return convertView;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(section, position);
        if (type == 1) {
            if (convertView == null) {
                convertView = new DividerPanel(mContext);
                convertView.setPadding(DisplayController.dp(LocaleController.isRTL ? 24 : 72), 0, DisplayController.dp(LocaleController.isRTL ? 72 : 24), 0);
            }
        } else if (type == 0) {
            if (convertView == null) {
                convertView = new TextSettingsPanel(mContext);
                convertView.setPadding(DisplayController.dp(LocaleController.isRTL ? 16 : 54), 0, DisplayController.dp(LocaleController.isRTL ? 54 : 16), 0);
            }

            ArrayList<Country> arr = countries.get(sortedCountries.get(section));
            Country c = arr.get(position);
            ((TextSettingsPanel) convertView).setTextAndValue(c.name, "+" + c.code, false);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int section, int position) {
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        return position < arr.size() ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}