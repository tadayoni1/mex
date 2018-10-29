package net.tirgan.mex.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import net.tirgan.mex.R;
import net.tirgan.mex.ui.main.SortByDialogFragment;

import butterknife.BindView;

public class MexEditDialogFragment extends DialogFragment {


    @BindView(R.id.mex_title_tv)
    EditText mDetailEditText;

    @BindView(R.id.detail_rb)
    RatingBar mDetailRatingBar;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(int aSortBy, float aFilterByMinRating);

        void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    SortByDialogFragment.NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SortByDialogFragment.NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_fragment_details_detail_activity, null);


        return super.onCreateDialog(savedInstanceState);
    }


}
