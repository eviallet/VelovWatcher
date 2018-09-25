package com.gueg.velovwidget.sorting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.gueg.velovwidget.R;
import com.gueg.velovwidget.Item;
import com.gueg.velovwidget.database_stations.WidgetItemsDatabase;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SortActivity extends AppCompatActivity {

    private static final int VERTICAL_ITEM_SPACE = 15;
    ArrayList<Item> items = new ArrayList<>();


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
        final SortAdapter mAdapter = new SortAdapter(items);
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

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        findViewById(R.id.activity_sort_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<items.size(); i++)
                    items.get(i).rank=i;
                new WidgetItemsDatabase.DatabaseLoader.UpdateItems(getApplicationContext(), items).start();
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
    }
}
