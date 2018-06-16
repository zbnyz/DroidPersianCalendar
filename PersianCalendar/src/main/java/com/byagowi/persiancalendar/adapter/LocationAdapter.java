package com.byagowi.persiancalendar.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entity.CityEntity;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private String locale;
    private List<CityEntity> cities;
    private LocationPreferenceDialog locationPreferenceDialog;

    public LocationAdapter(LocationPreferenceDialog locationPreferenceDialog) {
        Utils utils = Utils.getInstance(locationPreferenceDialog.getContext());
        this.locationPreferenceDialog = locationPreferenceDialog;
        this.cities = utils.getAllCities(true);
        this.locale = utils.getAppLanguage();
    }

    @NonNull
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_city_name, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvCity)
        TextView tvCountry;
        @BindView(R.id.tvCountry)
        TextView tvCity;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            tvCity.setText(locale.equals(Constants.LANG_EN)
                    ? cities.get(position).getEn()
                    : cities.get(position).getFa());

            tvCountry.setText(locale.equals(Constants.LANG_EN)
                    ? cities.get(position).getCountryEn()
                    : cities.get(position).getCountryFa());
        }

        @OnClick({R.id.ll_root})
        public void onClick() {
            locationPreferenceDialog.selectItem(cities.get(getAdapterPosition()).getKey());
        }
    }
}