package com.example.selfcheckout_wof.custom_components;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.SalesActivity;
import com.example.selfcheckout_wof.custom_components.componentActions.ActionForSelectionGUI;
import com.example.selfcheckout_wof.custom_components.componentActions.ConfiguredMeal;
import com.example.selfcheckout_wof.custom_components.utils.IntentFactory;
import com.example.selfcheckout_wof.custom_components.utils.CheckOutDBCache;
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
    private static final String ARG_MEAL_OR_ITEMS = "ARG_MEAL_OR_ITEMS"; // whether we're displaying the current order (meal) or the choice for user
    private static final String ARG_PARENT_ID = "ARG_PARENT_ID";
    private static final String ARG_SEE_CHECKOUT = "ARG_SEE_CHECKOUT";

    /**
     * Maximum number of itemst per row in the area where user picks
     * their choice of food items (bases, condiments, etc.)
     */
    private static final int MAX_NUMBER_OF_ITEMS_PER_ROW = 3;

    /**
     * page number in the db that we want to display at the moment.
     */
    private int current_page_number;

    /**
     * Item count in the next page. We'll need this to check sanity of next page request,
     * when that's made.
     */
    private int item_count_in_next_page = -1;

    /**
     * parent ID in the db, which pages we want to load.
     */
    private int parent_ID;

    /**
     * Parent Sales Item corresponding to the parent_ID.
     */
    private SalesItems parentSalesItem = null;

    /**
     * A flag of whether we'll be rendering the current order
     * or the page of items to select in this fragment.
     */
    private boolean current_order_or_items;

    private boolean see_checkout;

    /**
     * Elements on the fragment (both navigation and data fragments), that we will need to use
     */
    private LinearLayout vContentLayout, seeCurrentOrderView;
    //private Button btnPreviousPage, btnNextPage, btnStartAgain,
    private Button btnGoToCheckout;

    /**
     * A button to go back to browsing instead of paying.
     */
    private Button btnContinue;

    private OnFragmentInteractionListener mListener;

    public SalesProcessNavigationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param pageNumber Page number that we want to navigate to.
     * @param seeMealOrItemsForChoice A flag of whether we want the current selection or the
     *                                actual items displayed, that can be selected.
     * @param parentID Parent ID, which pages we want.
     * @param process indicator of whether user needs to be shown the actual meal instead of a
     *                page with items to select or the final order.
     * @return A new instance of fragment SalesProcessNavigationFragment.
     */
    public static SalesProcessNavigationFragment newInstance(int pageNumber,
                                                             int parentID,
                                                             boolean seeMealOrItemsForChoice,
                                                             SalesProcesses process) {
        SalesProcessNavigationFragment fragment = new SalesProcessNavigationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_NUM, pageNumber);
        args.putBoolean(ARG_MEAL_OR_ITEMS, seeMealOrItemsForChoice);
        args.putInt(ARG_PARENT_ID, parentID);

        /*
         * If the process is "GO_TO_CHECKOUT" then we want to load the checkout page.
         */
        if (process == SalesProcesses.GO_TO_CHECKOUT) {
            args.putBoolean(ARG_SEE_CHECKOUT, true);
        } else {
            args.putBoolean(ARG_SEE_CHECKOUT, false);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            current_page_number = getArguments().getInt(ARG_PAGE_NUM);
            current_order_or_items = getArguments().getBoolean(ARG_MEAL_OR_ITEMS);
            parent_ID = getArguments().getInt(ARG_PARENT_ID);
            see_checkout = getArguments().getBoolean(ARG_SEE_CHECKOUT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        if (see_checkout) {
            /*
             * For seeing the checkout.
             */
            rootView = inflater.inflate(R.layout.fragment_sales_process_checkout, container, false);
            vContentLayout = rootView.findViewById(R.id.vContentLayout);
            btnContinue = rootView.findViewById(R.id.btnContinue);

            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPageLoad(0, SalesActivity.TOP_LEVEL_ITEMS);
                }
            });
        } else {
            /**
             * If we want normal sales process, then it's going to be either the current order
             * or items from the menu for choice.
             */
            if (current_order_or_items) {
                rootView = inflater.inflate(R.layout.fragment_sales_process_see_meal, container, false);
//            btnPreviousPage = rootView.findViewById(R.id.btnPreviousPage);
//            btnNextPage = rootView.findViewById(R.id.btnNextPage);
//            btnStartAgain = rootView.findViewById(R.id.btnStartAgain);
                vContentLayout = rootView.findViewById(R.id.vContentLayout);
                btnGoToCheckout = rootView.findViewById(R.id.btnGoToCheckout);
            } else {
                /*
                 * For seeing the order.
                 */
                rootView = inflater.inflate(R.layout.fragment_sales_process_browse, container, false);
                vContentLayout = rootView.findViewById(R.id.vContentLayout);
            }

            /*
             * Loads the required frame - either data frame or current order frame
             */
            displayPage(current_page_number);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Making sure that we can receive intents in this fragment.
         */
        IntentFilter filter = new IntentFilter();
        for (IntentFactory.IntentType intentType : IntentFactory.IntentType.values()) {
            filter.addAction(intentType.getIntent().getAction());
        }

        filter.addCategory(Intent.CATEGORY_DEFAULT);

        mActivity.registerReceiver(selfCheckoutIntentReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.unregisterReceiver(selfCheckoutIntentReceiver);
    }

//    /*
//     * Loads up a linear layout with the navigation buttons that need to be shown
//     */
//    private void displayButtons() {
//        /*
//         * If we're showing the base page, then we don't want navigation buttons, because
//         * user has to choose what base category they want
//         */
//        //System.out.println("PGNM: " + page_number);
//        if (page_number == 0) {
//            salesItemsNavigationView.setVisibility(View.INVISIBLE);
//            //System.out.println("VSB: INVISIBLE");
//        } else {
//            salesItemsNavigationView.setVisibility(View.VISIBLE);
//            //System.out.println("VSB: VISIBLE");
//            btnPreviousPage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    requestPageLoad(page_number - 1, parent_ID);
//                }
//            });
//
//            btnStartAgain.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    /*
//                     * no parent ID required if we want base page.
//                     */
//                    UsersSelectedChoice.clearCurrentMeal();
//                    requestPageLoad(0, 0);
//                }
//            });
//
//            /*
//             * If next page doesn't exist, then the "Next" button should show the built meal
//             * instead of trying to jump to the next page.
//             */
//            DBThread.addTask(new Runnable() {
//                @Override
//                public void run() {
//                    int itemCountInNextPage = SalesItemsCache
//                            .getInstance()
//                            .getNumberOfItemsInPage(page_number + 1, parent_ID);
//
//                    if (itemCountInNextPage == 0) { // need to show user's assembled meal
//                        btnNextPage.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                requestToSeeMeal(page_number + 1, parent_ID);
//                            }
//                        });
//                    } else if (itemCountInNextPage > 0) { // need to show next page
//                        btnNextPage.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                requestPageLoad(page_number + 1, parent_ID);
//                            }
//                        });
//                    } else { // db not available
//                        btnNextPage.setVisibility(View.INVISIBLE);
//                    }
//                }
//            });
//        }
//
//        /*
//         * If we're looking at the selected meal, then we don't want the
//         * "Next" button.
//         */
//        if (seeMeal || seeOrder) {
//            btnNextPage.setVisibility(View.INVISIBLE);
//        } else {
//            btnNextPage.setVisibility(View.VISIBLE);
//        }
//    }

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
         * of user's choice. For seeing sales items selection, we'll need to create
         * that, but either way we want to first clear whatever is there first.
         */
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vContentLayout.removeAllViews();
            }
        });

        if (current_order_or_items) {
            /*
             * First we'll show the current order
             */
            final Iterator<ConfiguredMeal> mealsInOrder = UsersSelectedChoice.getCurrentOrder();
            if (mealsInOrder.hasNext()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vContentLayout.addView(new FinalOrderView(mActivity, mealsInOrder));
                    }
                });
            }

            /*
             * Afterwards we'll show what has been selected, but not yet added to the order.
             * But only if we're not editing a meal at the moment.
             */
            if (!UsersSelectedChoice.isCurrentMealBeingEdited()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vContentLayout.addView(new SelectedMealView(
                                        mActivity,
                                        new ConfiguredMeal(UsersSelectedChoice.getCurrentlySelectedItems(), getString(R.string.current_meal_name), 0)
                                )
                        );
                    }
                });
            }

            /*
             * If we're looking at the selected meal, then we want
             * functionality for the "CheckOut" button.
             */
            btnGoToCheckout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*
                     * Add the curent selection to the order and re-display the order frame.
                     */
                    UsersSelectedChoice.addCurrentMealToOrder(
                            (parentSalesItem == null ? "" : parentSalesItem.getLabel()),
                            parent_ID);
                    //requestToSeeOrder(page_number, parent_ID);
                    requestCheckOut();
//                    displayPage(page_number);
//                    /*
//                     * Here we need to ask user if they'd like to add more meals
//                     * or drinks or go to checkout.
//                     */
//                    PopupQuestions.doYouWantToContinueShoppingOrCheckout(
//                            getContext(),
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    /*
//                                     * Here we need to open show user the complete order and
//                                     * then after another confirmation show the checkout
//                                     * activity and start up the card reader.
//                                     */
//                                    requestToSeeOrder(page_number, parent_ID);
//                                }
//                            },
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    UsersSelectedChoice.clearCurrentMeal();
//                                    requestPageLoad(0, 0);
//                                }
//                            }
//                    );
                }
            });
        } else {
            /**
             * Updating the admin list of sales items in a separate thread because
             * Room doesn't allow running db stuff on the main thread.
             */
            DBThread.addTask(new Runnable() {
                @Override
                public void run() {
                    List<SalesItems> salesItemsList = CheckOutDBCache.getInstance().getSalesItemsPage(page_number, parent_ID);
                    int item_count = salesItemsList.size();

                    /**
                     * Updating the global variable item_count_in_next_page with the item count in the next page.
                     */
                    item_count_in_next_page = CheckOutDBCache.getInstance().getNumberOfItemsInPage(page_number + 1, parent_ID);

                    /**
                     * Updating the parent SalesItem.
                     */
                    parentSalesItem = CheckOutDBCache.getInstance().getSalesItemByID(parent_ID);

                    /*
                     * making sure that the row size will be as close as possible to the column count,
                     * but no more than MAX_NUMBER_OF_ITEMS_PER_ROW. MAX_NUMBER_OF_ITEMS_PER_ROW items
                     * in a row is max, that the screen can show atm.
                     *
                     * Also if we end up with number_of_items_per_row == 0, then let's set that to
                     * at least 1, because we may need to display a single default "go to next page"
                     * choice and we'll need that as 1 item in 1 row, if there is nothing else there.
                     */
                    int number_of_items_per_row = (int) Math.ceil(Math.sqrt(item_count));
                    if (number_of_items_per_row > MAX_NUMBER_OF_ITEMS_PER_ROW) {
                        number_of_items_per_row = MAX_NUMBER_OF_ITEMS_PER_ROW;
                    } else if(number_of_items_per_row < 1) {
                        number_of_items_per_row = 1;
                    }

                    /*
                     * We'll find the layout where we want to put the items and start
                     * putting in there horizontal layouts and adding a calculated number
                     * of items in each of the horizontal layouts (rows). For that we'll need
                     * the items_displayed variable which will track the number of items displayed.
                     *
                     * We will start however not with 0, but with -1 because we want to include
                     * the default option for user to go straight to the next page. That's the
                     * (items_displayed == -1) option. That of course is only valid if we're
                     * displaying ordinary choices. If we want to display the top level items
                     * e.g. "Food" or "Drink", then we can't have the default "go to next page"
                     * option as we don't yet know what is the next page.
                     */
                    int items_displayed = -1;

                    /**
                     * items_displayed = -1 of course is only valid if we're
                     * displaying ordinary choices. If we want to display the top level items
                     * e.g. "Food" or "Drink", then we can't have the default "go to next page"
                     * option as we don't yet know what is the next page.
                     */
                    if (parent_ID == SalesActivity.TOP_LEVEL_ITEMS) {
                        items_displayed = 0;
                    }

                    while (item_count > items_displayed) {
                        final LinearLayout hItemsRow = new LinearLayout(mActivity);
                        hItemsRow.setOrientation(LinearLayout.HORIZONTAL);
                        hItemsRow.setGravity(Gravity.CENTER);

                        for (int cnt = 0; (cnt < number_of_items_per_row) && (item_count > items_displayed); cnt++) {
                            /*
                             * Because each SalesItems item is a PurchasableGoods (implements the interface),
                             * we can use it as PurchasableGoods and pass into the constructor
                             * of SelectionGUIForOrder.
                             */
                            final PurchasableGoods pg;
                            final ActionForSelectionGUI action;

                            /**
                             * If we're displaying the default "go to next page" option, then
                             * we want appropriate action for that.
                             *
                             * Otherwise we want to select the categories and add them to the
                             * meal.
                             */
                            if (items_displayed == -1) {
                                //pg = salesItemsList.get(items_displayed);
                                pg = new PurchasableGoods() {
                                    @Override
                                    public long getPrice() {
                                        return 0;
                                    }
                                    @Override
                                    public String getDescription() {
                                        return "Go to next page";
                                    }
                                    @Override
                                    public String getLabel() {
                                        return "Go to next page";
                                    }
                                    @Override
                                    public int getImage_resource() {
                                        return R.drawable.ic_green_arrow_right;
                                    }
                                    @Override
                                    public String getImage_path() {
                                        return "";
                                    }
                                    @Override
                                    public int getID() {
                                        return 0;
                                    }
                                    @Override
                                    public int getParentID() {
                                        return 0;
                                    }
                                    @Override
                                    public int getNumberOfMultiSelectableItems() {
                                        return 0;
                                    }
                                    @Override
                                    public int getPage() {
                                        return 0;
                                    }
                                };

                                action = new ActionForSelectionGUI(pg) {
                                    @Override
                                    public boolean onSelected() {
                                        /**
                                         * If there are no more pages to go to, then we want to
                                         * add the current selection to the order and go back to
                                         * the beginning.
                                         */
                                        if (item_count_in_next_page < 1) {
                                            /**
                                             * If the current meal is being edited, then there's no
                                             * need to add it again to the order.
                                             */
                                            if (!UsersSelectedChoice.isCurrentMealBeingEdited()) {
                                                UsersSelectedChoice.addCurrentMealToOrder(
                                                        (parentSalesItem == null ? "" : parentSalesItem.getLabel()),
                                                        parent_ID);
                                            } else {
                                                UsersSelectedChoice.clearCurrentMeal();
                                            }

                                            requestToSeeOrder(0, SalesActivity.TOP_LEVEL_ITEMS);
                                            requestPageLoad(0, SalesActivity.TOP_LEVEL_ITEMS);
                                        } else {
                                            requestPageLoad(page_number + 1, parent_ID);
                                        }

                                        return true;
                                    }

                                    @Override
                                    public boolean onDeSelected() {
                                        return true;
                                    }
                                };
                            } else {
                                pg = salesItemsList.get(items_displayed);

                                action = new ActionForSelectionGUI(pg) {
                                    @Override
                                    public boolean onSelected() {
                                        /*
                                         * This is an action which will return True for a successful action and False for when it wasn't possible
                                         * to select due to some logic. This onSelected() method though will be called from
                                         * the context of SelectionGUIForOrder component and based on the result value here
                                         * it will know to paint it selected or not. We'll need this value when we want to
                                         * return from here.
                                         */
                                        boolean returnValue = false;

                                        /*
                                         * If we're at the top level (e.g. "Food" or "Drink"),
                                         * a.k.a.: parent_ID == SalesActivity.TOP_LEVEL_ITEMS,
                                         * then we want to display that top level's children.
                                         *
                                         * Otherwise we are deeper in the tree and we don't want
                                         * the immediate item ID as the parent, but rather the
                                         * master parent's ID.
                                         */
                                        if (parent_ID == SalesActivity.TOP_LEVEL_ITEMS) {
                                            requestPageLoad(page_number + 1, pg.getID());
                                            //requestToSeeOrder(page_number, pg.getID());
                                            returnValue = true;
                                        } else {
                                            /**
                                             * Adding this item to the order.
                                             */
                                            returnValue = super.onSelected();

                                            /*
                                             * If this item has to be selected alone, then upon its
                                             * selection we need to move to the next page. For example,
                                             * if we're building a food portion and it can only have one
                                             * base (one of Egg noodles, rice noodles, etc., because
                                             * of food container size), then we need to make sure that
                                             * upon its selection we get to the next page.
                                             *
                                             * That all of course only applies if the selection was successful.
                                             */
                                            if (returnValue && pg.getNumberOfMultiSelectableItems() == 1) {
                                                /**
                                                 * If there are no more pages to go to, then we want to
                                                 * add the current selection to the order and go back to
                                                 * the beginning.
                                                 */
                                                if (item_count_in_next_page < 1) {
                                                    UsersSelectedChoice.addCurrentMealToOrder(
                                                            (parentSalesItem == null ? "" : parentSalesItem.getLabel()),
                                                            parent_ID);
                                                    requestPageLoad(0, SalesActivity.TOP_LEVEL_ITEMS);
                                                } else {
                                                    requestPageLoad(page_number + 1, parent_ID);
                                                }
                                                requestToSeeOrder(0, SalesActivity.TOP_LEVEL_ITEMS);
                                            } else if (returnValue && pg.getNumberOfMultiSelectableItems() > 1) {
                                                /*
                                                 * Otherwise just update the total and allow further
                                                 * selections on the same page. Again - only if the selection
                                                 * was successful in the first place.
                                                 */
                                                requestToSeeOrder(0, SalesActivity.TOP_LEVEL_ITEMS);
                                            }
                                        }

                                        return returnValue;
                                    }

                                    @Override
                                    public boolean onDeSelected() {
                                        boolean result = super.onDeSelected();
                                        requestToSeeOrder(0, SalesActivity.TOP_LEVEL_ITEMS);
                                        return result;
                                    }
                                };
                            }

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hItemsRow.addView(
                                            new SelectionGUIForOrder(
                                                    pg,
                                                    action,
                                                    UsersSelectedChoice.itemIsSelected(pg),
                                                    false,
                                                    mActivity
                                            )
                                    );
                                }
                            });

                            items_displayed++;
                        }
                        mActivity.runOnUiThread(new Runnable() {
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

    /**
     * A reference to activity has to be kept and loaded in onAttach(),
     * because otherwise we get getActivity() == null, when fragment is
     * loaded as a result of an intent fired from SelectedMealView object
     * via intentFactory.
     */
    Activity mActivity = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mActivity =(Activity) context;
        }

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
        GO_TO_CHECKOUT,
        SEE_ORDER;
    }

    /**
     * A function to request a page load for both the navigation frame and the sales items list
     * frame
     *
     * @param newPageNumber
     */
    private void requestPageLoad(int newPageNumber, int parentId) {
        if (mListener != null) {
            /**
             * Check the page numbers for sanity.
             *
             * 1) If we're requesting a page below 0, then load the beginning.
             * 2) If we're requesting the next page, but there are not items
             * in the next page, then also go to the beginning. The only exception
             * here is when the current global parent_ID is the top level item, then
             * we can't possibly know the number of items in the next page, because
             * there is no next page yet- user still needs to decide what top level
             * page will be loaded and that is being decided now in this function call.
             */
            if (newPageNumber < 0
                    || newPageNumber > current_page_number
                        && item_count_in_next_page < 1
                        && parent_ID != SalesActivity.TOP_LEVEL_ITEMS) {
                mListener.onFragmentInteraction(SalesProcesses.LOAD_PAGE, 0, SalesActivity.TOP_LEVEL_ITEMS);
            } else {
                mListener.onFragmentInteraction(SalesProcesses.LOAD_PAGE, newPageNumber, parentId);
            }
        }
    }

    /**
     * A function to request to see the checkout page where user can review the final order
     * and pay.
     */
    private void requestCheckOut() {
        if (mListener != null) {
            mListener.onFragmentInteraction(SalesProcesses.GO_TO_CHECKOUT, 0, 0);
        }
    }

    /**
     * A function to request to see the complete order, that user has chosen.
     * @param pageNumber number of the page - needed for navigation frame
     *                   (even though we're going to show the selected choises
     *                   not a page). Can be passed as 0.
     * @param parentId parent ID needed for navigation frame just like pageNumber.
     *                 Can be passed as 0.
     */
    private void requestToSeeOrder(int pageNumber, int parentId) {
        if (mListener != null) {
            mListener.onFragmentInteraction(SalesProcesses.SEE_ORDER, pageNumber, parentId);
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

    /**
     * In addition to the fragment interaction listener, we'll also have this broadcast receiver
     * for handling intents issued by GUI (e.g. when user presses "Edit" button to edit an already
     * added meal in the order.)
     */
    private BroadcastReceiver selfCheckoutIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            /**
             * Handling the intent generated by one of the main GUI Admin choices
             * (typically generated in SystemChoiceItemView).
             */
            switch(IntentFactory.lookUpByIntent(intent)) {
                case GOTO_FIRST_PAGE_OF_GIVEN_PARENT:
                    Bundle extras = intent.getExtras();

                    if (extras != null) {
                        int parentID = extras.getInt(IntentFactory.PARENT_ID_PARAM_NAME);
                        requestPageLoad(1, parentID);
                    }

                    break;
                case GOTO_BEGINNING_OF_SALES_PROCESS:
                    requestToSeeOrder(0, SalesActivity.TOP_LEVEL_ITEMS);
                    requestPageLoad(0, SalesActivity.TOP_LEVEL_ITEMS);
                case UNKNOWN:
                    break;
            }
        }
    };
}
