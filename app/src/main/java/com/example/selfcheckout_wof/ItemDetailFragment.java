package com.example.selfcheckout_wof;

import android.app.Activity;
import android.os.Bundle;

import com.example.selfcheckout_wof.custom_components.componentActions.ActionForSelectionGUI;
import com.example.selfcheckout_wof.custom_components.SelectionGUIForOrder;
import com.example.selfcheckout_wof.custom_components.UsersSelectedChoice;
import com.example.selfcheckout_wof.data.MainCategories;
import com.example.selfcheckout_wof.data.PurchasableGoods;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The main category selection that this fragment is now representing.
     */
    private MainCategories chosenCategory;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            /**
             * Looking up the ID that we passed from the left hand side main category list
             * and then getting back the category that it actually is. That's an easy
             * lookup since we're dealing with an enum.
             */
            chosenCategory = MainCategories.valueOf(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(chosenCategory.label);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        /*
         * Now display the sub-selection for this main category
         */
        if (chosenCategory != null && chosenCategory.getCategory_content() != null) {
            /*
             * We have a vertical layout in the fragment (vItemLinesHolder).
             * We'll be creating a series of horizontal layouts (hlItemsHolder)
             * and adding them to the vertical layout one by one. Once we get
             * a defined number (3 atm) of widgets in the horizontal layout,
             * we create a new horizontal layout and add that again to the
             * vertical layout.
             */
            int i = 0;
            LinearLayout hlItemsHolder = null;
            int widgetsInRow = 3;

            for (PurchasableGoods pg : chosenCategory.getCategory_content()) {
                if (i % widgetsInRow == 0) {
                    hlItemsHolder = new LinearLayout(getContext());
                    ((LinearLayout) rootView.findViewById(R.id.vItemLinesHolder)).addView(hlItemsHolder);
                }
                i++;
                //ImageView iv = new ImageView(getContext());
                //iv.setImageResource(pg.getImage_resource());
                //hlItemsHolder.addView(iv);
                SelectionGUIForOrder orderingGUI = new SelectionGUIForOrder(pg,
                        new ActionForSelectionGUI(pg),
                        UsersSelectedChoice.itemIsSelected(pg),
                        false,
                        getContext());
                hlItemsHolder.addView(orderingGUI);
            }
        }

        return rootView;
    }
}
