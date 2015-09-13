package com.markesilva.footballscores;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class LeagueSelectionActivity extends ActionBarActivity {

    private LeagueSelectionFragment mLeagueFrag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_selection);
        if (savedInstanceState == null) {
            mLeagueFrag = new LeagueSelectionFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.select_leagues_container, mLeagueFrag)
                    .commit();
        }
    }
}
