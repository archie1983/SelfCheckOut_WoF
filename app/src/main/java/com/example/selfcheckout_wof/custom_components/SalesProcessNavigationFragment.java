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

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.componentActions.ActionForSelectionGUI;
import com.example.selfcheckout_wof.custom_components.utils.SalesItemsCache;
import com.example.selfcheckout_wof.data.DBThread;
import com.example.selfcheckout_wof.data.PurchasableGoods;
import com.example.selfcheckout_wof.data.SalesItems;

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

    /*
     * page number in the db that we want to navigate to.
     */
    private int page_number;

    /*
     * A flag of whether we'll be rendering page navigation buttons
     * or the actual page of items in this fragment.
     */
    private boolean btns_or_items;

    /**
     * Elements on the fragment (both navigation and data fragments), that we will need to use
     */
    private LinearLayout vContentLayout, salesItemsNavigationView;
    private Button btnPreviousPage, btnNextPage, btnStartAgain;

    private OnFragmentInteractionListener mListener;

    public SalesProcessNavigationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param pageNumber Page number that we want to navigate to.
     * @param btns_or_items A flag of whether we want the buttons or the actual items displayed.
     * @return A new instance of fragment SalesProcessNavigationFragment.
     */
    public static SalesProcessNavigationFragment newInstance(int pageNumber, boolean btns_or_items) {
        SalesProcessNavigationFragment fragment = new SalesProcessNavigationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_NUM, pageNumber);
        args.putBoolean(ARG_BTNS_OR_ITEMS, btns_or_items);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page_number = getArguments().getInt(ARG_PAGE_NUM);
            btns_or_items = getArguments().getBoolean(ARG_BTNS_OR_ITEMS);
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
            rootView = inflater.inflate(R.layout.fragment_sales_process_browse, container, false);
            vContentLayout = rootView.findViewById(R.id.vContentLayout);
        }

        loadData();

        return rootView;
    }

    /**
     * Loads the required frame - either data frame or navigation frame
     */
    private void loadData() {
        if (btns_or_items) {
            /*
             * If we're showing the base page, then we don't want navigation buttons, because
             * user has to choose what base category they want
             */
            if (page_number == 0) {
                salesItemsNavigationView.setVisibility(View.INVISIBLE);
            } else {
                salesItemsNavigationView.setVisibility(View.VISIBLE);
                btnNextPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPageLoad(page_number + 1);
                    }
                });

                btnPreviousPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPageLoad(page_number - 1);
                    }
                });

                btnStartAgain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPageLoad(0);
                    }
                });
            }
        } else {
            displayPage(page_number);
        }
    }

    /**
     * Displays the requrested page of sales items. It tries
     * to create as uniform grid as possible.
     *
     * If page_number < 1, then displaying main categories.
     *
     * @param page_number
     */
    private void displayPage(final int page_number) {
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
                List<SalesItems> salesItemsList = SalesItemsCache.getInstance().getSalesItemsPage(page_number);
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
                        if (page_number == 0) {
                            action = new ActionForSelectionGUI(pg) {
                                @Override
                                public boolean onSelected() {
                                    requestPageLoad(page_number + 1);
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




                        hItemsRow.addView(
                                new SelectionGUIForOrder(
                                        pg,
                                        action,
                                        false,
                                        false,
                                        getContext()
                                )
                        );
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

    /**
     * A function to request a page load for both the navigation frame and the sales items list
     * frame
     *
     * @param pageNumber
     */
    private void requestPageLoad(int pageNumber) {
        if (mListener != null) {
            mListener.onFragmentInteraction(SalesProcesses.LOAD_PAGE, pageNumber);
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
        LOAD_PAGE;
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
        void onFragmentInteraction(SalesProcesses process, int pageNumber);
    }
}
