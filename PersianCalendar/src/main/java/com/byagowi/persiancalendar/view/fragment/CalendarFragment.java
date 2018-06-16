package com.byagowi.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.CalendarAdapter;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.dialog.SelectDayDialog;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import butterknife.Unbinder;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

public class CalendarFragment extends Fragment {
    @BindView(R.id.calendar_pager)
    ViewPager monthViewPager;
    private Utils utils;

    private Calendar calendar = Calendar.getInstance();

    private Coordinate coordinate;

    private PrayTimesCalculator prayTimesCalculator;
    @BindViews({R.id.imsak, R.id.fajr, R.id.sunrise, R.id.dhuhr, R.id.asr,
            R.id.sunset, R.id.maghrib, R.id.isgha, R.id.midnight})
    List<TextView> owghatTextViews;
    final ButterKnife.Setter<TextView, Map<PrayTime, Clock>> SETTEXT = (view, prayTimes, index) ->
            view.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.values()[index])));
    @BindView(R.id.week_day_name)
    TextView weekDayName;
    @BindViews({R.id.gregorian_date, R.id.islamic_date, R.id.shamsi_date})
    List<TextView> dates;
    @BindViews({R.id.event_title, R.id.holiday_title})
    List<TextView> titles;
    @BindView(R.id.today)
    TextView today;
    @BindView(R.id.today_icon)
    AppCompatImageView todayIcon;

    @BindView(R.id.more_owghat)
    AppCompatImageView moreOwghat;

    @BindView(R.id.owghat)
    CardView owghat;
    @BindView(R.id.cardEvent)
    CardView event;

    @BindViews({R.id.imsakLayout, R.id.fajrLayout, R.id.sunriseLayout, R.id.dhuhrLayout,
            R.id.asrLayout, R.id.sunsetLayout, R.id.maghribLayout, R.id.ishaLayout,
            R.id.midnightLayout})
    List<RelativeLayout> owghatRelativeLayouts;
    final ButterKnife.Setter<View, Integer> VISIBILITY = (view, value, index) ->
            view.setVisibility(value);
    private int viewPagerPosition;

    private Unbinder unbinder;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        unbinder = ButterKnife.bind(this, view);
        utils = Utils.getInstance(getContext());
        utils.clearYearWarnFlag();
        viewPagerPosition = 0;

        coordinate = utils.getCoordinate();
        prayTimesCalculator = new PrayTimesCalculator(utils.getCalculationMethod());

        monthViewPager.setAdapter(new CalendarAdapter(getChildFragmentManager()));
        monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);

        String cityName = utils.getCityName(false);
        if (!TextUtils.isEmpty(cityName)) {
            ((TextView) view.findViewById(R.id.owghat_text))
                    .append(" (" + cityName + ")");
        }

        // This will immediately be replaced by the same functionality on fragment but is here to
        // make sure enough space is dedicated to actionbar's title and subtitle, kinda hack anyway
        PersianDate today = utils.getToday();
        utils.setActivityTitleAndSubtitle(getActivity(), utils.getMonthName(today),
                utils.formatNumber(today.getYear()));

        return view;
    }

    public void changeMonth(int position) {
        monthViewPager.setCurrentItem(monthViewPager.getCurrentItem() + position, true);
    }

    public void selectDay(PersianDate persianDate) {
        weekDayName.setText(utils.getWeekDayName(persianDate));
        dates.get(2).setText(utils.dateToString(persianDate));
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        dates.get(0).setText(utils.dateToString(civilDate));
        dates.get(1).setText(utils.dateToString(
                DateConverter.civilToIslamic(civilDate, utils.getIslamicOffset())));

        if (utils.getToday().equals(persianDate)) {
            today.setVisibility(View.GONE);
            todayIcon.setVisibility(View.GONE);
            if (utils.iranTime) {
                weekDayName.setText(weekDayName.getText() +
                        " (" + getString(R.string.iran_time) + ")");
            }
        } else {
            today.setVisibility(View.VISIBLE);
            todayIcon.setVisibility(View.VISIBLE);
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
                utils.dayTitleSummary(persianDate));

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
        String holidays = utils.getEventsTitle(persianDate, true);
        String events = utils.getEventsTitle(persianDate, false);

        event.setVisibility(View.GONE);
        titles.get(1).setVisibility(View.GONE);
        titles.get(0).setVisibility(View.GONE);

        if (!TextUtils.isEmpty(holidays)) {
            titles.get(1).setText(holidays);
            titles.get(1).setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(events)) {
            titles.get(0).setText(events);
            titles.get(0).setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }
    }

    private void setOwghat(CivilDate civilDate) {
        if (coordinate == null) {
            return;
        }

        calendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = calendar.getTime();

        Map<PrayTime, Clock> prayTimes = prayTimesCalculator.calculate(date, coordinate);
        ButterKnife.apply(owghatTextViews, SETTEXT, prayTimes);

        owghat.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.owghat, R.id.today, R.id.today_icon, R.id.islamic_date, R.id.shamsi_date, R.id.gregorian_date})
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.owghat:

                if (owghatRelativeLayouts.get(2).getVisibility() == View.VISIBLE) {
                    ButterKnife.apply(owghatRelativeLayouts, VISIBILITY, View.GONE);
                    moreOwghat.setImageResource(R.drawable.ic_keyboard_arrow_down);
                } else {
                    ButterKnife.apply(owghatRelativeLayouts, VISIBILITY, View.VISIBLE);
                    moreOwghat.setImageResource(R.drawable.ic_keyboard_arrow_up);
                }

                break;

            case R.id.today:
            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.islamic_date:
            case R.id.shamsi_date:
            case R.id.gregorian_date:
                utils.copyToClipboard(v);
                break;
        }
    }

    private void bringTodayYearMonth() {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        if (monthViewPager.getCurrentItem() != Constants.MONTHS_LIMIT / 2) {
            monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);
        }

        selectDay(utils.getToday());
    }

    public void bringDate(PersianDate date) {
        PersianDate today = utils.getToday();
        viewPagerPosition =
                (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();

        monthViewPager.setCurrentItem(viewPagerPosition + Constants.MONTHS_LIMIT / 2);

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, date.getDayOfMonth());

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        selectDay(date);
    }

    @OnPageChange(R.id.calendar_pager)
    public void onPageSelected(int position) {
        viewPagerPosition = position - Constants.MONTHS_LIMIT / 2;

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        today.setVisibility(View.VISIBLE);
        todayIcon.setVisibility(View.VISIBLE);
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
