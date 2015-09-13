package com.markesilva.footballscores;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.markesilva.footballscores.data.DatabaseContract;
import com.markesilva.footballscores.dummy.DummyContent;
import com.markesilva.footballscores.utils.LOG;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class LeagueSelectionFragment extends Fragment implements AbsListView.OnItemClickListener,LoaderManager.LoaderCallbacks<Cursor> {
    private final static String LOG_TAG = LOG.makeLogTag(LeagueSelectionFragment.class);
    private final static int LOADER_ID = 1;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CursorAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LeagueSelectionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content
        mAdapter = new LeagueSelectionAdapter(getActivity(), null, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (null != mListener) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
//        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        // We want all the columns so we can update them later
        return new CursorLoader(getActivity(), DatabaseContract.leagues_table.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        if ((cursor == null) || (cursor.getCount() == 0)) {
            // no data yet, so don't try to do anything with the cursor
            return;
        }
        cursor.moveToFirst();
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }

    public class ViewHolder {
        public TextView mLeagueName;
        public CheckBox mEnabled;
        public String mLeagueId;

        public ViewHolder(View view) {
            mLeagueName = (TextView) view.findViewById(R.id.league_selection_name);
            mEnabled = (CheckBox) view.findViewById(R.id.league_selection_enabled);
        }

    }

    public class LeagueSelectionAdapter extends CursorAdapter {

        public LeagueSelectionAdapter(Context context,Cursor cursor,int flags)
        {
            super(context,cursor,flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.league_selection, parent, false);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();

            holder.mLeagueName.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.leagues_table.NAME_COL)));
            holder.mEnabled.setChecked(cursor.getString(cursor.getColumnIndex(DatabaseContract.leagues_table.ENABLED_COL)).equals("1") ? true : false);
            holder.mLeagueId = cursor.getString(cursor.getColumnIndex(DatabaseContract.leagues_table.LEAGUE_ID_COL));
        }
    }
}
