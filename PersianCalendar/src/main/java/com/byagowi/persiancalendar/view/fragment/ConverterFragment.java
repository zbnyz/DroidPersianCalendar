package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.ShapedArrayAdapter;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class ConverterFragment extends Fragment {
    @BindViews({R.id.calendarTypeSpinner, R.id.yearSpinner, R.id.monthSpinner, R.id.daySpinner})
    List<AppCompatSpinner> spinners;
    @BindViews({R.id.date0, R.id.date1, R.id.date2})
    List<TextView> dates;
    @BindView(R.id.more_date)
    RelativeLayout moreDate;
    private Utils utils;
    private int startingYearOnYearSpinner = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_converter, container, false);
        ButterKnife.bind(this, view);
        utils = Utils.getInstance(getContext());
        utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.date_converter), "");

        // fill views
        spinners.get(0).setAdapter(new ShapedArrayAdapter<>(getContext(),
                Utils.DROPDOWN_LAYOUT, getResources().getStringArray(R.array.calendar_type)));
        spinners.get(0).setSelection(0);

        startingYearOnYearSpinner = utils.fillYearMonthDaySpinners(getContext(),
                spinners.get(0), spinners.get(1), spinners.get(2), spinners.get(3));
        //
        return view;
    }

    private void fillCalendarInfo() {
        int year = startingYearOnYearSpinner + spinners.get(1).getSelectedItemPosition();
        int month = spinners.get(2).getSelectedItemPosition() + 1;
        int day = spinners.get(3).getSelectedItemPosition() + 1;

        CivilDate civilDate = null;
        PersianDate persianDate;
        IslamicDate islamicDate;

        StringBuilder sb = new StringBuilder();

        try {
            moreDate.setVisibility(View.VISIBLE);

            List<String> calendarsTextList = new ArrayList<>();
            switch (utils.calendarTypeFromPosition(spinners.get(0).getSelectedItemPosition())) {
                case GREGORIAN:
                    civilDate = new CivilDate(year, month, day);
                    islamicDate = DateConverter.civilToIslamic(civilDate, 0);
                    persianDate = DateConverter.civilToPersian(civilDate);

                    calendarsTextList.add(utils.dateToString(civilDate));
                    calendarsTextList.add(utils.dateToString(persianDate));
                    calendarsTextList.add(utils.dateToString(islamicDate));
                    break;

                case ISLAMIC:
                    islamicDate = new IslamicDate(year, month, day);
                    civilDate = DateConverter.islamicToCivil(islamicDate);
                    persianDate = DateConverter.islamicToPersian(islamicDate);

                    calendarsTextList.add(utils.dateToString(islamicDate));
                    calendarsTextList.add(utils.dateToString(civilDate));
                    calendarsTextList.add(utils.dateToString(persianDate));
                    break;

                case SHAMSI:
                    persianDate = new PersianDate(year, month, day);
                    civilDate = DateConverter.persianToCivil(persianDate);
                    islamicDate = DateConverter.persianToIslamic(persianDate);

                    calendarsTextList.add(utils.dateToString(persianDate));
                    calendarsTextList.add(utils.dateToString(civilDate));
                    calendarsTextList.add(utils.dateToString(islamicDate));
                    break;
            }

            sb.append(utils.getWeekDayName(civilDate));
            sb.append(Constants.PERSIAN_COMMA);
            sb.append(" ");
            sb.append(calendarsTextList.get(0));

            dates.get(0).setText(sb.toString());
            dates.get(1).setText(calendarsTextList.get(1));
            dates.get(2).setText(calendarsTextList.get(2));

        } catch (RuntimeException e) {
            moreDate.setVisibility(View.GONE);
            dates.get(0).setText(getString(R.string.date_exception));
        }
    }

    @OnItemSelected({R.id.calendarTypeSpinner, R.id.yearSpinner, R.id.monthSpinner, R.id.daySpinner})
    public void onItemSelected(AdapterView<?> parent) {
        switch (parent.getId()) {
            case R.id.yearSpinner:
            case R.id.monthSpinner:
            case R.id.daySpinner:
                fillCalendarInfo();
                break;

            case R.id.calendarTypeSpinner:
                startingYearOnYearSpinner = utils.fillYearMonthDaySpinners(getContext(),
                        spinners.get(0), spinners.get(1), spinners.get(2), spinners.get(3));
                break;
        }
    }

    @OnClick({R.id.date0, R.id.date1, R.id.date2})
    public void onClick(View view) {
        utils.copyToClipboard(view);
    }
}
