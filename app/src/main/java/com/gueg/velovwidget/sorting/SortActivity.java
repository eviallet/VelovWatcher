package com.gueg.velovwidget.sorting;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gueg.velovwidget.Item;
import com.gueg.velovwidget.R;
import com.gueg.velovwidget.database_stations.WidgetItemsDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.gueg.velovwidget.MainListActivity.IS_DATABASE_BUSY;

public class SortActivity extends AppCompatActivity {

    private static final int VERTICAL_ITEM_SPACE = 15;
    ArrayList<Item> items = new ArrayList<>();
    SortAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);
        setResult(RESULT_CANCELED);


        try {
            items = Item.sort(
                    new WidgetItemsDatabase.DatabaseLoader.PinnedItems().execute(getApplicationContext(), Item.getSelectedContract(getApplicationContext())).get()
            );
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }


        RecyclerView recyclerView = findViewById(R.id.activity_sort_recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        mAdapter = new SortAdapter(items);
        recyclerView.setAdapter(mAdapter);


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.UP| ItemTouchHelper.DOWN) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                if(items.get(viewHolder.getAdapterPosition()).isSeparator()) {
                    final Item copy = items.get(viewHolder.getAdapterPosition());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            WidgetItemsDatabase.getDatabase(viewHolder.itemView.getContext()).widgetItemsDao().delete(copy);
                        }
                    }).start();
                    items.remove(viewHolder.getAdapterPosition());
                }
                mAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        findViewById(R.id.activity_sort_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<items.size(); i++)
                    items.get(i).rank=i;
                new WidgetItemsDatabase.DatabaseLoader.UpdateItems(getApplicationContext(), items, true).start();
                setResult(RESULT_OK);
                finish();
            }
        });

        findViewById(R.id.activity_sort_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.activity_sort_separator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(view.getContext());
                dialog.setContentView(R.layout.dialog_separator);
                dialog.findViewById(R.id.dialog_separator_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.dialog_separator_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        String title = ((EditText)dialog.findViewById(R.id.dialog_separator_edittext)).getText().toString();
                        if(!title.isEmpty()) {
                            final Item sep = new Item(0, Item.getSelectedContract(view.getContext()), title, "", Item.POSITION_SEPARATOR, true, 0);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    PreferenceManager.getDefaultSharedPreferences(view.getContext()).edit().putBoolean(IS_DATABASE_BUSY, true).apply();
                                    WidgetItemsDatabase.getDatabase(view.getContext()).widgetItemsDao().insertAll(sep);
                                    PreferenceManager.getDefaultSharedPreferences(view.getContext()).edit().putBoolean(IS_DATABASE_BUSY, false).apply();
                                }
                            }).start();
                            items.add(0, sep);
                            mAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        } else
                            Toast.makeText(SortActivity.this, getApplicationContext().getResources().getString(R.string.toast_no_title_separator), Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });
    }
}
