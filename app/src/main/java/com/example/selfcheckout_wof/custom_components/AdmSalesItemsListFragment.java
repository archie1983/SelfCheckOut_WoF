package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.componentActions.AdmSalesItemAction;
import com.example.selfcheckout_wof.custom_components.utils.CheckOutDBCache;
import com.example.selfcheckout_wof.data.SalesItems;

import java.util.List;

/**
 * A simple {@link Fragment} subclass to display a list of sales items for administration
 * purposes (add, remove, view)
 *
 * Activities that contain this fragment must implement the
 * {@link AdmSalesItemsListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdmSalesItemsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdmSalesItemsListFragment extends Fragment {
    // the fragment initialization parameter
    private static final String ARG_HDR_FLAG = "hdr";

    /**
     * A flag of whether this fragment should only display a header row instead of data.
     */
    private boolean isHeader = false;

    private boolean isInstantiated = true;

    private OnFragmentInteractionListener mListener;

    public AdmSalesItemsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param isHeader_ A flag of whether this fragment is supposed to only show the header line
     *                 and no data.
     * @return A new instance of fragment AdmSalesItemsListFragment.
     */
    public static AdmSalesItemsListFragment newInstance(boolean isHeader_) {
        AdmSalesItemsListFragment fragment = new AdmSalesItemsListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HDR_FLAG, isHeader_);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHeader = getArguments().getBoolean(ARG_HDR_FLAG);
            isInstantiated = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView;
        if (isHeader) {
            rootView = inflater.inflate(R.layout.fragment_adm_sales_items_hdr, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_adm_sales_items_list, container, false);
        }


        itemListRows = ((LinearLayout)rootView.findViewById(R.id.vAdmSalesItemsListRows));
        //Thread.dumpStack();
        loadData();

        return rootView;
    }

    LinearLayout itemListRows = null;

    /**
     * Loads data into the fragment.
     */
    public void loadData() {
        if (!isInstantiated) {
            return;
        }

        //LinearLayout itemListRows = ((LinearLayout)rootView.findViewById(R.id.vAdmSalesItemsListRows));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemListRows.removeAllViews();
            }
        });

        /*
         * Adding a headers' row.
         */
        if (isHeader) {
            final AdmSalesItemView header = new AdmSalesItemView(getContext());
            header.setHeaderBackground();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    itemListRows.addView(header);
                }
            });
        } else {
            List<SalesItems> sales_items = CheckOutDBCache.getInstance().getCachedSalesItemsList();
            if (sales_items != null) {
                for (final SalesItems si : sales_items) {
                    /*
                     * Creating a new admin sales item view and an action pertaining to that view
                     * and then adding that view to the collection of rows.
                     */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            itemListRows.addView(
                                new AdmSalesItemView(
                                    si,
                                    new AdmSalesItemAction(si, getContext()),
                                    getContext()));
                        }
                    });
                }
            }
        }
    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        loadData();
//    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
