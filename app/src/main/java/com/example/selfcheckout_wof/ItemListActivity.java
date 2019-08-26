package com.example.selfcheckout_wof;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.example.selfcheckout_wof.data.MainCategories;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * I need a static reference to this class because I'm planning to use
     * its fragment manager capabilities in SelectionGUIForOrder, where it
     * will be used to fill in another fragment- this time the user's
     * selected choice.
     */
    private static ItemListActivity self;

    /**
     * I'm planning to use a static reference to this class for
     * its fragment manager capabilities in SelectionGUIForOrder, where it
     * will be used to fill in another fragment- this time the user's
     * selected choice.
     *
     * @return
     */
    public static ItemListActivity getInstance() {
        return self;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action1", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        self = this;
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, MainCategories.getMainCategoriesAsList(), mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<MainCategories> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainCategories item = (MainCategories) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    /**
                     * ID here will be the name of the enum entry of the MainCategories enum
                     */
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.name());

                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);

                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    /**
                     * ID here will be the name of the enum entry of the MainCategories enum
                     */
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.name());

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<MainCategories> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_selector_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).id);
            holder.label.setText(mValues.get(position).label);
            //holder.cvThisPicture.setBackgroundResource(mValues.get(position).getImage_resource());
            holder.llIconContent.setBackgroundResource(mValues.get(position).getImage_resource());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView label;
            final CardView cvThisPicture;
            final LinearLayout llIconContent;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                label = (TextView) view.findViewById(R.id.label);
                cvThisPicture = (CardView) view.findViewById(R.id.cvMainItemIcon);
                llIconContent = (LinearLayout)view.findViewById(R.id.vlContent);
            }
        }
    }
}
