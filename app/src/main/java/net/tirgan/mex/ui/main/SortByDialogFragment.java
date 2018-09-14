package net.tirgan.mex.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;

import net.tirgan.mex.R;

public class SortByDialogFragment extends DialogFragment {

    private RadioGroup mSortByRadioGroup;
    private RadioButton mSortByRatingRadioButton;

    private RatingBar mFilterByRatingBar;


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(int aSortBy, float aFilterByMinRating);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
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
        View view = inflater.inflate(R.layout.dialog_sort, null);

        mSortByRadioGroup = view.findViewById(R.id.sort_by_rg);
        mSortByRadioGroup.check(R.id.sort_by_rating_rb);


        mFilterByRatingBar = view.findViewById(R.id.filter_by_rating_rb);
        mFilterByRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int sortBy = 1;
                        switch (mSortByRadioGroup.getCheckedRadioButtonId()) {
                            case R.id.sort_by_name_rb:
                                sortBy = 2;
                                break;
                            case R.id.sort_by_rating_rb:
                                sortBy = 3;
                            default:
                                sortBy = 1;
                        }
                        mListener.onDialogPositiveClick(sortBy, mFilterByRatingBar.getRating());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SortByDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
