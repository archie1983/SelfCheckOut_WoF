package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.selfcheckout_wof.AdminActivity;
import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.custom_components.componentActions.AdmSalesItemAction;
import com.example.selfcheckout_wof.data.AppDatabase;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AdmSalesItemsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdmSalesItemsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdmSalesItemsListFragment newInstance(String param1, String param2) {
        AdmSalesItemsListFragment fragment = new AdmSalesItemsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_adm_sales_items_list, container, false);
        LinearLayout itemListRows = ((LinearLayout)rootView.findViewById(R.id.vAdmSalesItemsListRows));
        //itemListRows.removeAllViews();

        /*
         * Adding a headers' row
         */
        itemListRows.addView(new AdmSalesItemView(getContext()));

        List<SalesItems> sales_items = AdminActivity.getCurrentSalesItemsList();
        if (sales_items != null) {
            for (SalesItems si : AdminActivity.getCurrentSalesItemsList()) {
                /*
                 * Creating a new admin sales item view and an action pertaining to that view
                 * and then adding that view to the collection of rows.
                 */
                itemListRows.addView(
                        new AdmSalesItemView(
                                si,
                                new AdmSalesItemAction(si, getContext()),
                                getContext()));
            }
        }

        return rootView;
    }

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
