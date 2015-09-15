package com.markesilva.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.markesilva.footballscores.utils.LOG;

public class MainActivity extends ActionBarActivity {
    public static int selected_match_id;
    public static int current_fragment = 2;
    public static String LOG_TAG = LOG.makeLogTag(MainActivity.class);
    private PagerFragment my_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Reached MainActivity onCreate");
        my_main = new PagerFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, my_main)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this, AboutActivity.class);
            startActivity(start_about);
            return true;
        }
        if (id == R.id.action_select_leagues) {
            Intent start_select_leagues = new Intent(this, LeagueSelectionActivity.class);
            startActivity(start_select_leagues);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LOG.V(LOG_TAG, "will save");
        LOG.V(LOG_TAG, "fragment: " + String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        LOG.V(LOG_TAG, "selected id: " + selected_match_id);
        current_fragment = my_main.mPagerHandler.getCurrentItem();
        outState.putInt("Pager_Current", current_fragment);
        outState.putInt("Selected_match", selected_match_id);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LOG.V(LOG_TAG, "will retrieve");
        LOG.V(LOG_TAG, "fragment: " + String.valueOf(savedInstanceState.getInt("Pager_Current")));
        LOG.V(LOG_TAG, "selected id: " + savedInstanceState.getInt("Selected_match"));
        current_fragment = savedInstanceState.getInt("Pager_Current");
        selected_match_id = savedInstanceState.getInt("Selected_match");
        if (my_main != null) {
            my_main.restartLoaders();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
