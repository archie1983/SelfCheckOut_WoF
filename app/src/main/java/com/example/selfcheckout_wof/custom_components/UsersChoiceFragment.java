package com.example.selfcheckout_wof.custom_components;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.selfcheckout_wof.R;
import com.example.selfcheckout_wof.data.PurchasableGoods;

import java.util.Iterator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UsersChoiceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class UsersChoiceFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public UsersChoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_users_choice, container, false);

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_users_choice, container, false);

        LinearLayout invoiceItemLinesHolder = ((LinearLayout)rootView.findViewById(R.id.vInvoiceItemLinesHolder));

        Iterator<PurchasableGoods> it = UsersSelectedChoice.getCurrentlySelectedItems();
        while (it.hasNext()) {
            PurchasableGoods pg = it.next();
            TextView newItem = new TextView(getContext());
            newItem.setText(pg.getLabel() + " " + pg.getPrice());
            invoiceItemLinesHolder.addView(newItem);
        }

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

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
