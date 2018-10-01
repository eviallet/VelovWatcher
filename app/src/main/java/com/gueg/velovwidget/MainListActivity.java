package com.gueg.velovwidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.gueg.velovwidget.database_stations.JsonParser;
import com.gueg.velovwidget.map.PinsActivity;
import com.gueg.velovwidget.sorting.SortActivity;



public class MainListActivity extends AppCompatActivity {

    public static final String IS_DATABASE_BUSY = "com.gueg.velovwatcher.is_database_busy";

    private static final int ACTIVITY_PINS = 0;
    private static final int ACTIVITY_SORT = 1;

    private static final String OFFICIAL_APP_PACKAGE = "com.jcdecaux.vls.lyon";

    RecyclerView _list;
    MainListAdapter _adapter;
    SwipeRefreshLayout _swipe;
    ProgressBar _progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JsonParser.loadApiKey(this);

        setContentView(R.layout.activity_list);

        _swipe = findViewById(R.id.widget_list_refresh);
        _list = findViewById(R.id.widget_list_stations);
        _progress = findViewById(R.id.widget_list_progress);

        _adapter = new MainListAdapter(this);
        _adapter.setListener(new MainListAdapter.RefreshListener() {
            @Override public void onRefreshStarted() {
                _swipe.setRefreshing(true);
            }
            @Override public void onProgressChanged(int progress, int max) {
                if(_progress.getVisibility()!= View.VISIBLE)
                    _progress.setVisibility(View.VISIBLE);
                _progress.setMax(max);
                _progress.setProgress(progress);
            }
            @Override public void onRefreshFinished() {
                _progress.setVisibility(View.GONE);
                _swipe.setRefreshing(false);
                if(_progress.getProgress()==0)
                    startActivityForResult(new Intent(MainListActivity.this, PinsActivity.class), ACTIVITY_PINS);
            }
        });
        _list.setAdapter(_adapter);
        _list.setLayoutManager(new LinearLayoutManager(this));

        _swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                _adapter.refresh();
            }
        });

        _adapter.refresh();

        findViewById(R.id.widget_list_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(OFFICIAL_APP_PACKAGE);
                if (intent == null) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + OFFICIAL_APP_PACKAGE));
                }
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK)
            return;
        switch(requestCode) {
            case ACTIVITY_PINS:
                _adapter.refresh();
                break;
            case ACTIVITY_SORT:
                _adapter.refresh();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, PinsActivity.class), ACTIVITY_PINS);
                return true;

            case R.id.action_sort:
                startActivityForResult(new Intent(this, SortActivity.class), ACTIVITY_SORT);
                return true;

            case R.id.action_update:
                _adapter.refresh();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
