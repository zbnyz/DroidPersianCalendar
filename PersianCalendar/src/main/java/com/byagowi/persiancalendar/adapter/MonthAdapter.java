package com.byagowi.persiancalendar.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.DayEntity;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.MonthFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
    private final int firstDayDayOfWeek;
    private final int totalDays;
    private Context context;
    private MonthFragment monthFragment;
    private List<DayEntity> days;
    private int selectedDay = -1;
    private boolean persianDigit;
    private TypedValue colorHoliday = new TypedValue(),
            colorTextHoliday = new TypedValue(),
            colorPrimary = new TypedValue(),
            colorDayName = new TypedValue(),
            shapeSelectDay = new TypedValue();

    public MonthAdapter(Context context, MonthFragment monthFragment, List<DayEntity> days) {
        firstDayDayOfWeek = days.get(0).getDayOfWeek();
        totalDays = days.size();
        this.monthFragment = monthFragment;
        this.context = context;
        this.days = days;
        Utils utils = Utils.getInstance(context);
        persianDigit = utils.isPersianDigitSelected();

        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorHoliday, colorHoliday, true);
        theme.resolveAttribute(R.attr.colorTextHoliday, colorTextHoliday, true);
        theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        theme.resolveAttribute(R.attr.colorTextDayName, colorDayName, true);
        theme.resolveAttribute(R.attr.circleSelect, shapeSelectDay, true);
    }

    public void clearSelectedDay() {
        int prevDay = selectedDay;
        selectedDay = -1;
        notifyItemChanged(fixRtlPosition(prevDay));
    }

    public void selectDay(int dayOfMonth) {
        int prevDay = selectedDay;
        selectedDay = dayOfMonth + 6 + firstDayDayOfWeek;
        notifyItemChanged(fixRtlPosition(prevDay));
        notifyItemChanged(fixRtlPosition(selectedDay));
    }

    private boolean isPositionHeader(int position) {
        return position < 7;
    }

    private int fixRtlPosition(int position) {
        position += 6 - (position % 7) * 2;//equal:(6 - position % 7) + position - (position % 7)
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return 7 * 7; // days of week * month view rows
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.num)
        TextView num;
        @BindView(R.id.today)
        View today;
        @BindView(R.id.event)
        View event;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            position = fixRtlPosition(position);
            if (totalDays < position - 6 - firstDayDayOfWeek) {
                setEmpty();
                return;
            }

            if (!isPositionHeader(position)) {
                if (position - 7 - firstDayDayOfWeek >= 0) {
                    num.setText(days.get(position - 7 - days.get(0).getDayOfWeek()).getNum());
                    num.setVisibility(View.VISIBLE);

                    DayEntity day = days.get(position - 7 - firstDayDayOfWeek);

                    if (persianDigit) {
                        num.setTextSize(25);
                    } else {
                        num.setTextSize(20);
                    }

                    if (day.isEvent()) {
                        event.setVisibility(View.VISIBLE);
                    } else {
                        event.setVisibility(View.GONE);
                    }

                    if (day.isToday()) {
                        today.setVisibility(View.VISIBLE);
                    } else {
                        today.setVisibility(View.GONE);
                    }

                    if (position == selectedDay) {
                        num.setBackgroundResource(shapeSelectDay.resourceId);

                        if (day.isHoliday()) {
                            num.setTextColor(ContextCompat.getColor(context, colorTextHoliday.resourceId));
                        } else {
                            num.setTextColor(ContextCompat.getColor(context, colorPrimary.resourceId));
                        }

                    } else {
                        num.setBackgroundResource(0);

                        if (day.isHoliday()) {
                            num.setTextColor(ContextCompat.getColor(context, colorHoliday.resourceId));
                        } else {
                            num.setTextColor(ContextCompat.getColor(context, R.color.dark_text_day));
                        }
                    }

                } else {
                    setEmpty();
                }

            } else {
                num.setText(Constants.FIRST_CHAR_OF_DAYS_OF_WEEK_NAME[position]);
                num.setTextColor(ContextCompat.getColor(context, colorDayName.resourceId));
                num.setTextSize(20);
                today.setVisibility(View.GONE);
                num.setBackgroundResource(0);
                event.setVisibility(View.GONE);
                num.setVisibility(View.VISIBLE);
            }
        }

        private void setEmpty() {
            today.setVisibility(View.GONE);
            num.setVisibility(View.GONE);
            event.setVisibility(View.GONE);
        }

        @OnClick(R.id.rl_root)
        public void onClick() {
            int position = fixRtlPosition(getAdapterPosition());
            if (totalDays < position - 6 - firstDayDayOfWeek) {
                return;
            }

            if (position - 7 - firstDayDayOfWeek >= 0) {
                monthFragment.onClickItem(days
                        .get(position - 7 - firstDayDayOfWeek)
                        .getPersianDate());

                int prevDay = selectedDay;
                selectedDay = position;
                notifyItemChanged(fixRtlPosition(prevDay));
                notifyItemChanged(getAdapterPosition());
            }
        }

        @OnLongClick(R.id.rl_root)
        public boolean onLongClick() {
            int position = fixRtlPosition(getAdapterPosition());
            if (totalDays < position - 6 - firstDayDayOfWeek) {
                return false;
            }


            try {
                monthFragment.onLongClickItem(days
                        .get(position - 7 - firstDayDayOfWeek)
                        .getPersianDate());
            } catch (Exception e) {
                // Ignore it for now
                // I guess it will occur on CyanogenMod phones
                // where Google extra things is not installed
            }

            return false;
        }
    }
}