package com.byagowi.persiancalendar.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.MonthAdapter;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;

import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.PersianDate;

public class MonthFragment extends Fragment implements View.OnClickListener {
    private CalendarFragment calendarFragment;
    private PersianDate persianDate;
    private int offset;
    private MonthAdapter adapter;

    static List<DayEntity> getDays(int offset) {
        List<DayEntity> days = new ArrayList<>();
        PersianDate persianDate = Utils.getToday();
        int month = persianDate.getMonth() - offset;
        month -= 1;
        int year = persianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        persianDate.setMonth(month);
        persianDate.setYear(year);
        persianDate.setDayOfMonth(1);

        int dayOfWeek = DateConverter.persianToCivil(persianDate).getDayOfWeek() % 7;

        try {
            PersianDate today = Utils.getToday();
            for (int i = 1; i <= 31; i++) {
                persianDate.setDayOfMonth(i);

                DayEntity dayEntity = new DayEntity();
                dayEntity.setNum(Utils.formatNumber(i));
                dayEntity.setDayOfWeek(dayOfWeek);

                List<AbstractEvent> events = Utils.getEvents(persianDate);

                if (dayOfWeek == 6 || !TextUtils.isEmpty(Utils.getEventsTitle(events, true))) {
                    dayEntity.setHoliday(true);
                }

                if (events.size() > 0) {
                    dayEntity.setEvent(true);
                }

                dayEntity.setPersianDate(persianDate.clone());

                if (persianDate.equals(today)) {
                    dayEntity.setToday(true);
                }

                days.add(dayEntity);
                dayOfWeek++;
                if (dayOfWeek == 7) {
                    dayOfWeek = 0;
                }
            }
        } catch (DayOutOfRangeException e) {
            // okay, it was expected
        }

        return days;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_month, container, false);
        offset = getArguments().getInt(Constants.OFFSET_ARGUMENT);
        List<DayEntity> days = getDays(offset);

        AppCompatImageView prev = view.findViewById(R.id.prev);
        AppCompatImageView next = view.findViewById(R.id.next);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);

        persianDate = Utils.getToday();
        int month = persianDate.getMonth() - offset;
        month -= 1;
        int year = persianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }

        month += 1;
        persianDate.setMonth(month);
        persianDate.setYear(year);
        persianDate.setDayOfMonth(1);

        RecyclerView recyclerView = view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MonthAdapter(getContext(), this, days);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        calendarFragment = (CalendarFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(CalendarFragment.class.getName());

        if (offset == 0 && calendarFragment.getViewPagerPosition() == offset) {
            calendarFragment.selectDay(Utils.getToday());
            updateTitle();
        }

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setCurrentMonthReceiver,
                new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));

        return view;
    }

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getExtras().getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == offset) {
                updateTitle();

                int day = intent.getExtras().getInt(Constants.BROADCAST_FIELD_SELECT_DAY);
                if (day != -1) {
                    adapter.selectDay(day);
                }

            } else if (value == Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY) {
                adapter.clearSelectedDay();
            }
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(setCurrentMonthReceiver);
        super.onDestroy();
    }

    public void onClickItem(PersianDate day) {
        calendarFragment.selectDay(day);
    }

    public void onLongClickItem(PersianDate day) {
        calendarFragment.addEventOnCalendar(day);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.next:
                calendarFragment.changeMonth(1);
                break;

            case R.id.prev:
                calendarFragment.changeMonth(-1);
                break;
        }
    }

    private void updateTitle() {
        Utils.setActivityTitleAndSubtitle(getActivity(), Utils.getMonthName(persianDate),
                Utils.formatNumber(persianDate.getYear()));
    }

}
