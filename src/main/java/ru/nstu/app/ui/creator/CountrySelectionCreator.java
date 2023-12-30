package ru.nstu.app.ui.creator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.api.action.SendCodeAction;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.MainActivity;
import ru.nstu.app.ui.component.adapter.CountriesAdapter;
import ru.nstu.app.ui.component.listview.LetterSectionsListView;
import ru.nstu.app.ui.fragment.actionbar.ActionBar;
import ru.nstu.app.ui.fragment.actionbar.ActionBarMenu;

public class CountrySelectionCreator extends Creator {


    @Override
    public void create(MainActivity activity) {
        buildActionBar(activity);
        buildContent(activity);
    }

    private void buildActionBar(final MainActivity activity) {
        ActionBar actionBar = new ActionBar(activity);
        actionBar.setBackgroundColor(0xff54759e);
        actionBar.setTitle(LocaleController.getString(R.string.country_selection_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            private boolean pressed;

            @Override
            public void onItemClick(final int id) {
                if (id == -1) {
                    synchronized (this) {
                        if (pressed) {
                            return;
                        }
                        activity.onChangeContentView(R.layout.phone_registration);
                        pressed = true;
                    }

                }
            }
        });
        actionBar.setBackButtonImage(R.drawable.ic_back);
        getRootLayout(activity).addView(actionBar);
    }

    private void buildContent(final MainActivity activity) {
        ListView countriesListView = new LetterSectionsListView(activity);
        countriesListView.setAdapter((CountriesAdapter)Box.get(Box.COUNTRIES_ADAPTER));
        countriesListView.setDivider(null);
        countriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CountriesAdapter.Country country = (CountriesAdapter.Country)adapterView.getItemAtPosition(position);
                Box.put(Box.COUNTRY, country);
                activity.onChangeContentView(R.layout.phone_registration);
            }
        });
        getRootLayout(activity).addView(countriesListView);
    }

    @Override
    public ViewGroup getRootLayout(MainActivity activity) {
        return (ViewGroup)activity.findViewById(R.id.country_selection_linear_layout);
    }
}
