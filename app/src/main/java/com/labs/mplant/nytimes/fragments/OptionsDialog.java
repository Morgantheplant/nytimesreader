package com.labs.mplant.nytimes.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.labs.mplant.nytimes.R;
import com.labs.mplant.nytimes.constants.FilterOptions;
import com.labs.mplant.nytimes.constants.NYTimesParams;
import com.labs.mplant.nytimes.constants.NewDeskValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;


public class OptionsDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener  {

    private TextView mDatePicker;
    private Calendar cal;
    private HashMap <String, Boolean> mNewDeskValue = new HashMap<>();

    public HashMap<String, Boolean> getmNewDeskValue() {
        return mNewDeskValue;
    }

    public Calendar getCal() {
        return cal;
    }

    public String getmSortBy() {
        return mSortBy;
    }


    public void setmSortBy(String mSortBy) {
        this.mSortBy = mSortBy;
    }
    public interface OnDismissListener {
        void onDismiss(OptionsDialog myDialogFragment);
    }


    private OnDismissListener onDismissListener;

    public void setDismissListener(OnDismissListener dismissListener) {
        this.onDismissListener = dismissListener;
    }
    private String mSortBy = NYTimesParams.NEWEST;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.options_view, null);
        builder.setView(v);
        mDatePicker = v.findViewById(R.id.datepicker);
        CheckBox art = v.findViewById(R.id.checkbox_art);
        CheckBox fashion = v.findViewById(R.id.checkbox_fashion);
        CheckBox sports = v.findViewById(R.id.checkbox_sports);
        Spinner spinner = v.findViewById(R.id.sort_by);
        Context ctx = getContext();

        Bundle args = getArguments();
        if(args != null){

            Boolean sportsValue = args.getBoolean(NewDeskValue.SPORTS);
            mNewDeskValue.put(NewDeskValue.SPORTS, sportsValue);
            sports.setChecked(sportsValue);

            Boolean fashionValue = args.getBoolean(NewDeskValue.FASHION_STYLE);
            mNewDeskValue.put(NewDeskValue.FASHION_STYLE, fashionValue);
            fashion.setChecked(fashionValue);

            Boolean artValue = args.getBoolean(NewDeskValue.ART);
            mNewDeskValue.put(NewDeskValue.ART, artValue);
            art.setChecked(artValue);

            String beginDate = args.getString(FilterOptions.BEGIN_DATE);
            if(beginDate.equals("")){
                cal = null;
            }
            SimpleDateFormat dateString = new SimpleDateFormat("yyyyMMdd", Locale.US);
            try {
                Calendar calendar = cal != null ? cal : Calendar.getInstance();
                calendar.setTime(dateString.parse(beginDate));
                cal = calendar;
                updateDatePickerText();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mSortBy = args.getString(NYTimesParams.SORT_ORDER);
        }
        if(ctx != null){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.sort_order, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(OptionsDialog.this);
        }

        int spinnerPosition = mSortBy.equals(NYTimesParams.NEWEST) ? 0 : 1;
        spinner.setSelection(spinnerPosition);


        art.setOnCheckedChangeListener(OptionsDialog.this);
        fashion.setOnCheckedChangeListener(OptionsDialog.this);
        sports.setOnCheckedChangeListener(OptionsDialog.this);
        mDatePicker.setOnClickListener(OptionsDialog.this);

        updateDatePickerText();
        return builder.create();
    }



    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        initSelectedDate();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDatePickerText();
    }

    public void updateDatePickerText(){
        if(cal != null){
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy",  Locale.US);
            String S = sdf.format(cal.getTime());
            mDatePicker.setText(S);
        } else {
            mDatePicker.setText(getResources().getString(R.string.date_placeholder));
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.datepicker:
                launchDatePicker();
                break;
        }

    }
    public void launchDatePicker(){
        Context ctx = getContext();
        FragmentManager fm = getFragmentManager();
        if(ctx != null && fm != null){
            initSelectedDate();
            DatePickerDialog datepicker = new DatePickerDialog(ctx,
                    OptionsDialog.this,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            );
            datepicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        clearSelectedDate();
                        updateDatePickerText();
                    }
                }
            });

            datepicker.show();
        }
    }
    @Override
    public void onCheckedChanged(CompoundButton view, boolean checked) {
        switch(view.getId()) {
            case R.id.checkbox_art:
                mNewDeskValue.put(NewDeskValue.ART, checked);
                break;
            case R.id.checkbox_fashion:
                mNewDeskValue.put(NewDeskValue.FASHION_STYLE, checked);
                break;
            case R.id.checkbox_sports:
                mNewDeskValue.put(NewDeskValue.SPORTS, checked);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissListener != null) {
            onDismissListener.onDismiss(this);
        }
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
       String sortBy = pos == 0 ? NYTimesParams.NEWEST : NYTimesParams.OLDEST;
       setmSortBy(sortBy);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        setmSortBy(NYTimesParams.NEWEST);
    }
    public void initSelectedDate(){
        if(cal == null){
            cal = Calendar.getInstance();
        }
    }

    public void clearSelectedDate(){
        this.cal = null;
    }

}