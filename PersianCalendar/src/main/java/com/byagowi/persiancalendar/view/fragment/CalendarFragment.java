package com.byagowi.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.CalendarAdapter;
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.PersianDate;

public class CalendarFragment extends Fragment
        implements View.OnClickListener, ViewPager.OnPageChangeListener {
    FragmentCalendarBinding b;
    private Calendar calendar = Calendar.getInstance();
    private Coordinate coordinate;
    private PrayTimesCalculator prayTimesCalculator;
    private int viewPagerPosition;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        b = DataBindingUtil.inflate(inflater,
                R.layout.fragment_calendar, container, false);
        View view = b.getRoot();
        Utils.clearYearWarnFlag();
        viewPagerPosition = 0;

        coordinate = Utils.getCoordinate(getContext());
        prayTimesCalculator = new PrayTimesCalculator(Utils.getCalculationMethod());

        b.calendarPager.setAdapter(new CalendarAdapter(getChildFragmentManager()));
        b.calendarPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);

        b.calendarPager.addOnPageChangeListener(this);

        b.owghat.setOnClickListener(this);
        b.today.setOnClickListener(this);
        b.todayIcon.setOnClickListener(this);
        b.gregorianDate.setOnClickListener(this);
        b.gregorianDateDay.setOnClickListener(this);
        b.islamicDate.setOnClickListener(this);
        b.islamicDateDay.setOnClickListener(this);
        b.shamsiDate.setOnClickListener(this);
        b.shamsiDateDay.setOnClickListener(this);

        String cityName = Utils.getCityName(getContext(), false);
        if (!TextUtils.isEmpty(cityName)) {
            ((TextView) view.findViewById(R.id.owghat_text))
                    .append(" (" + cityName + ")");
        }

        // This will immediately be replaced by the same functionality on fragment but is here to
        // make sure enough space is dedicated to actionbar's title and subtitle, kinda hack anyway
        PersianDate today = Utils.getToday();
        Utils.setActivityTitleAndSubtitle(getActivity(), Utils.getMonthName(today),
                Utils.formatNumber(today.getYear()));

        return view;
    }

    public void changeMonth(int position) {
        b.calendarPager.setCurrentItem(b.calendarPager.getCurrentItem() + position, true);
    }

    public void selectDay(PersianDate persianDate) {
        b.weekDayName.setText(Utils.getWeekDayName(persianDate));
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        IslamicDate hijriDate = DateConverter.civilToIslamic(civilDate, Utils.getIslamicOffset());

        b.shamsiDateDay.setText(Utils.formatNumber(persianDate.getDayOfMonth()));
        b.shamsiDate.setText(Utils.getMonthName(persianDate) + "\n" + Utils.formatNumber(persianDate.getYear()));

        b.gregorianDateDay.setText(Utils.formatNumber(civilDate.getDayOfMonth()));
        b.gregorianDate.setText(Utils.getMonthName(civilDate) + "\n" + Utils.formatNumber(civilDate.getYear()));

        b.islamicDateDay.setText(Utils.formatNumber(hijriDate.getDayOfMonth()));
        b.islamicDate.setText(Utils.getMonthName(hijriDate) + "\n" + Utils.formatNumber(hijriDate.getYear()));

        if (Utils.getToday().equals(persianDate)) {
            b.today.setVisibility(View.GONE);
            b.todayIcon.setVisibility(View.GONE);
            if (Utils.isIranTime()) {
                b.weekDayName.setText(b.weekDayName.getText() +
                        " (" + getString(R.string.iran_time) + ")");
            }
        } else {
            b.today.setVisibility(View.VISIBLE);
            b.todayIcon.setVisibility(View.VISIBLE);
        }

        setOwghat(civilDate);
        showEvent(persianDate);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void addEventOnCalendar(PersianDate persianDate) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        CivilDate civil = DateConverter.persianToCivil(persianDate);

        intent.putExtra(CalendarContract.Events.DESCRIPTION,
                Utils.dayTitleSummary(getContext(), persianDate));

        Calendar time = Calendar.getInstance();
        time.set(civil.getYear(), civil.getMonth() - 1, civil.getDayOfMonth());

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

        startActivity(intent);
    }

    private void showEvent(PersianDate persianDate) {
        Context context = getContext();
        String holidays = Utils.getEventsTitle(context, persianDate, true);
        String events = Utils.getEventsTitle(context, persianDate, false);

        b.cardEvent.setVisibility(View.GONE);
        b.holidayTitle.setVisibility(View.GONE);
        b.eventTitle.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(holidays)) {
            b.holidayTitle.setText(holidays);
            b.holidayTitle.setVisibility(View.VISIBLE);
            b.cardEvent.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(events)) {
            b.eventTitle.setText(events);
            b.eventTitle.setVisibility(View.VISIBLE);
            b.cardEvent.setVisibility(View.VISIBLE);
        }
    }

    private void setOwghat(CivilDate civilDate) {
        if (coordinate == null) {
            b.owghat.setVisibility(View.GONE);
            return;
        }

        calendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = calendar.getTime();

        Map<PrayTime, Clock> prayTimes = prayTimesCalculator.calculate(date, coordinate);

        b.imsakText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.IMSAK)));
        b.fajrText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.FAJR)));
        b.sunriseText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNRISE)));
        b.dhuhrText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.DHUHR)));
        b.asrText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.ASR)));
        b.sunsetText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNSET)));
        b.maghribText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.MAGHRIB)));
        b.ishaText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.ISHA)));
        b.midnightText.setText(Utils.getPersianFormattedClock(prayTimes.get(PrayTime.MIDNIGHT)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.owghat:

                boolean isOpenCommand = b.sunriseLayout.getVisibility() == View.GONE;

                b.moreOwghat.setImageResource(isOpenCommand
                        ? R.drawable.ic_keyboard_arrow_up
                        : R.drawable.ic_keyboard_arrow_down);
                b.imsakLayout.setVisibility(isOpenCommand ? View.VISIBLE : View.GONE);
                b.sunriseLayout.setVisibility(isOpenCommand ? View.VISIBLE : View.GONE);
                b.asrLayout.setVisibility(isOpenCommand ? View.VISIBLE : View.GONE);
                b.sunsetLayout.setVisibility(isOpenCommand ? View.VISIBLE : View.GONE);
                b.ishaLayout.setVisibility(isOpenCommand ? View.VISIBLE : View.GONE);
                b.midnightLayout.setVisibility(isOpenCommand ? View.VISIBLE : View.GONE);

                break;

            case R.id.today:
            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.shamsi_date:
            case R.id.shamsi_date_day:
                Utils.copyToClipboard(getContext(), b.shamsiDateDay.getText() + " " +
                        b.shamsiDate.getText().toString().replace("\n", " "));
                break;

            case R.id.gregorian_date:
            case R.id.gregorian_date_day:
                Utils.copyToClipboard(getContext(), b.gregorianDateDay.getText() + " " +
                        b.gregorianDate.getText().toString().replace("\n", " "));
                break;

            case R.id.islamic_date:
            case R.id.islamic_date_day:
                Utils.copyToClipboard(getContext(), b.islamicDateDay.getText() + " " +
                        b.islamicDate.getText().toString().replace("\n", " "));
                break;
        }
    }

    private void bringTodayYearMonth() {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        if (b.calendarPager.getCurrentItem() != Constants.MONTHS_LIMIT / 2) {
            b.calendarPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);
        }

        selectDay(Utils.getToday());
    }

    public void bringDate(PersianDate date) {
        PersianDate today = Utils.getToday();
        viewPagerPosition =
                (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();

        b.calendarPager.setCurrentItem(viewPagerPosition + Constants.MONTHS_LIMIT / 2);

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, date.getDayOfMonth());

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        selectDay(date);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        viewPagerPosition = position - Constants.MONTHS_LIMIT / 2;

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        b.today.setVisibility(View.VISIBLE);
        b.todayIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.action_button, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_to:
                SelectDayDialog dialog = new SelectDayDialog();
                dialog.show(getChildFragmentManager(), SelectDayDialog.class.getName());
                break;
            default:
                break;
        }
        return true;
    }

    public int getViewPagerPosition() {
        return viewPagerPosition;
    }
}
