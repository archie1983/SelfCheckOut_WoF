package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.componentActions.ActionForSelectionGUI;
import com.example.selfcheckout_wof.custom_components.utils.Formatting;
import com.example.selfcheckout_wof.custom_components.utils.SalesItemsCache;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.PurchasableGoods;
import com.example.selfcheckout_wof.data.SalesItems;

import java.util.Iterator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SalesProcessNavigationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SalesProcessNavigationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SalesProcessNavigationFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_PAGE_NUM
    private static final String ARG_PAGE_NUM = "ARG_PAGE_NUM";
    private static final String ARG_BTNS_OR_ITEMS = "ARG_BTNS_OR_ITEMS";
    private static final String ARG_PARENT_ID = "ARG_PARENT_ID";
    private static final String ARG_SEE_MEAL = "ARG_SEE_MEAL";

    /*
     * page number in the db that we want to navigate to.
     */
    private int page_number;

    /*
     * parent ID in the db, which pages we want to load.
     */
    private int parent_ID;

    /*
     * A flag of whether we'll be rendering page navigation buttons
     * or the actual page of items in this fragment.
     */
    private boolean btns_or_items;

    /*
     * A flag of whether user needs to be shown meal (true) or a page of items (false)
     */
    private boolean seeMeal = false;

    /**
     * Elements on the fragment (both navigation and data fragments), that we will need to use
     */
    private LinearLayout vContentLayout, salesItemsNavigationView;
    private Button btnPreviousPage, btnNextPage, btnStartAgain, btnAddToOrder;

    private OnFragmentInteractionListener mListener;

    public SalesProcessNavigationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param pageNumber Page number that we want to navigate to.
     * @param btnsOrItems A flag of whether we want the buttons or the actual items displayed.
     * @param parentID Parent ID, which pages we want.
     * @return A new instance of fragment SalesProcessNavigationFragment.
     */
    public static SalesProcessNavigationFragment newInstance(int pageNumber,
                                                             int parentID,
                                                             boolean btnsOrItems,
                                                             boolean seeMeal) {
        SalesProcessNavigationFragment fragment = new SalesProcessNavigationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_NUM, pageNumber);
        args.putBoolean(ARG_BTNS_OR_ITEMS, btnsOrItems);
        args.putInt(ARG_PARENT_ID, parentID);
        args.putBoolean(ARG_SEE_MEAL, seeMeal);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page_number = getArguments().getInt(ARG_PAGE_NUM);
            btns_or_items = getArguments().getBoolean(ARG_BTNS_OR_ITEMS);
            parent_ID = getArguments().getInt(ARG_PARENT_ID);
            seeMeal = getArguments().getBoolean(ARG_SEE_MEAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        if (btns_or_items) {
            rootView = inflater.inflate(R.layout.fragment_sales_process_navigation, container, false);
            btnPreviousPage = rootView.findViewById(R.id.btnPreviousPage);
            btnNextPage = rootView.findViewById(R.id.btnNextPage);
            btnStartAgain = rootView.findViewById(R.id.btnStartAgain);
            salesItemsNavigationView = rootView.findViewById(R.id.salesItemsNavigationView);
        } else {
            /*
             * If user needs to be shown the selected meal, then we'll have one layout to load,
             * but for browsing the item in a page there will be a different one.
             */
            System.out.println("HER! " + seeMeal);
            if (seeMeal) {
                rootView = inflater.inflate(R.layout.fragment_sales_process_see_meal, container, false);
                btnAddToOrder = rootView.findViewById(R.id.btnAddToOrder);
            } else {
                rootView = inflater.inflate(R.layout.fragment_sales_process_browse, container, false);
            }

            vContentLayout = rootView.findViewById(R.id.vContentLayout);
        }

        /**
         * Loads the required frame - either data frame or navigation frame
         */
        if (btns_or_items) {
            displayButtons();
        } else {
            displayPage(page_number);
        }

        return rootView;
    }

    /*
     * Loads up a linear layout with the navigation buttons that need to be shown
     */
    private void displayButtons() {
        /*
         * If we're showing the base page, then we don't want navigation buttons, because
         * user has to choose what base category they want
         */
        //System.out.println("PGNM: " + page_number);
        if (page_number == 0) {
            salesItemsNavigationView.setVisibility(View.INVISIBLE);
            //System.out.println("VSB: INVISIBLE");
        } else {
            salesItemsNavigationView.setVisibility(View.VISIBLE);
            //System.out.println("VSB: VISIBLE");
            btnPreviousPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestPageLoad(page_number - 1, parent_ID);
                }
            });

            btnStartAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*
                     * no parent ID required if we want base page.
                     */
                    UsersSelectedChoice.clearUsersSelection();
                    requestPageLoad(0, 0);
                }
            });

            /*
             * If next page doesn't exist, then the "Next" button should show the built meal
             * instead of trying to jump to the next page.
             */
            DBThread.addTask(new Runnable() {
                @Override
                public void run() {
                    int itemCountInNextPage = SalesItemsCache
                            .getInstance()
                            .getNumberOfItemsInPage(page_number + 1, parent_ID);

                    if (itemCountInNextPage == 0) { // need to show user's assembled meal
                        btnNextPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestToSeeMeal(page_number + 1, parent_ID);
                            }
                        });
                    } else if (itemCountInNextPage > 0) { // need to show next page
                        btnNextPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestPageLoad(page_number + 1, parent_ID);
                            }
                        });
                    } else { // db not available
                        btnNextPage.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        /*
         * If we're looking at the selected meal, then we don't want the
         * "Next" button.
         */
        if (seeMeal) {
            btnNextPage.setVisibility(View.INVISIBLE);
        } else {
            btnNextPage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Displays the requrested page of sales items or the already chosen meal.
     * In case of a page of items, it tries to create as uniform grid as possible.
     *
     * If page_number < 1, then displaying main categories.
     *
     * @param page_number
     */
    private void displayPage(final int page_number) {
        /*
         * If user needs to be shown the meal, then we'll create a representation
         * of user's choice
         */
        if (seeMeal) {
            vContentLayout.addView(new SelectedMealView(getContext(), UsersSelectedChoice.getCurrentlySelectedItems()));

            /*
             * If we're looking at the selected meal, then we want
             * functionality for the "Add to Order" button.
             */
            btnAddToOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UsersSelectedChoice.addCurrentMealToOrder();
                    /*
                     * Here we need to ask user if they'd like to add more meals
                     * or drinks or go to checkout.
                     */

                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vContentLayout.removeAllViews();
                }
            });

            /**
             * Updating the admin list of sales items in a separate thread because
             * Room doesn't allow running db stuff on the main thread.
             */
            DBThread.addTask(new Runnable() {
                @Override
                public void run() {
                    List<SalesItems> salesItemsList = SalesItemsCache.getInstance().getSalesItemsPage(page_number, parent_ID);
                    int item_count = salesItemsList.size();
                    /*
                     * making sure that the row size will be as close as possible to the column count,
                     * but no more than 4. 4 items in a row is max, that the screen can show atm.
                     */
                    int number_of_items_per_row = (int) Math.ceil(Math.sqrt(item_count));
                    if (number_of_items_per_row > 4) {
                        number_of_items_per_row = 4;
                    }

                    /*
                     * We'll find the layout where we want to put the items and start
                     * putting in there horizontal layouts and adding a calculated number
                     * of items in each of the horizontal layouts (rows)
                     */
                    int items_displayed = 0;

                    while (item_count > items_displayed) {
                        final LinearLayout hItemsRow = new LinearLayout(getContext());
                        hItemsRow.setOrientation(LinearLayout.HORIZONTAL);
                        hItemsRow.setGravity(Gravity.CENTER);

                        for (int cnt = 0; (cnt < number_of_items_per_row) && (item_count > items_displayed); cnt++) {
                            /*
                             * Because each SalesItems item is a PurchasableGoods (implements the interface),
                             * we can use it as PurchasableGoods and pass into the constructor
                             * of SelectionGUIForOrder.
                             */
                            final PurchasableGoods pg = salesItemsList.get(items_displayed);
                            final ActionForSelectionGUI action;

                            /*
                             * If this is base page, then we want the page load subcategories
                             * of the item clicked.
                             *
                             * Otherwise we want to select the categories and add them to the
                             * meal.
                             */
                            if (page_number == 0) {
                                action = new ActionForSelectionGUI(pg) {
                                    @Override
                                    public boolean onSelected() {
                                        requestPageLoad(page_number + 1, pg.getID());
                                        return true;
                                    }

                                    @Override
                                    public boolean onDeSelected() {
                                        return false;
                                    }
                                };
                            } else {
                                action = new ActionForSelectionGUI(pg);
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hItemsRow.addView(
                                            new SelectionGUIForOrder(
                                                    pg,
                                                    action,
                                                    UsersSelectedChoice.itemIsSelected(pg),
                                                    false,
                                                    getContext()
                                            )
                                    );
                                }
                            });

                            items_displayed++;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                vContentLayout.addView(hItemsRow);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
     * Sales processes (requests) that we will be notifying the main activity about.
     */
    public enum SalesProcesses {
        LOAD_PAGE,
        SEE_MEAL;
    }


    /**
     * A function to request a page load for both the navigation frame and the sales items list
     * frame
     *
     * @param pageNumber
     */
    private void requestPageLoad(int pageNumber, int parentId) {
        if (mListener != null) {
            mListener.onFragmentInteraction(SalesProcesses.LOAD_PAGE, pageNumber, parentId);
        }
    }


    /**
     * A function to request to see the meal, that user has chosen.
     * @param pageNumber number of the page - needed for navigation frame
     *                   (even though we're going to show the selected choises
     *                   not a page)
     * @param parentId parent ID needed for navigation frame just like pageNumber
     */
    private void requestToSeeMeal(int pageNumber, int parentId) {
        if (mListener != null) {
            mListener.onFragmentInteraction(SalesProcesses.SEE_MEAL, pageNumber, parentId);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(SalesProcesses process, int pageNumber, int parentId);
    }
}
