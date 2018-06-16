package com.byagowi.persiancalendar.view.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * About Calendar Activity
 *
 * @author ebraminio
 */
public class AboutFragment extends Fragment {

    @BindView(R.id.version2)
    TextView versionTextView;
    @BindView(R.id.license)
    TextView licenseTextView;

    public String programVersion() {
        try {
            return getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AboutFragment.class.getName(), "Name not found on PersianCalendarUtils.programVersion");
            return "";
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        Utils utils = Utils.getInstance(getContext());
        utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.about), "");

        String version = programVersion();

        versionTextView.setText(MessageFormat.format("{0} {1}", getString(R.string.version), utils.formatNumber(version.split("-")[0])));

        licenseTextView.setText(MessageFormat.format("Android Persian Calendar Version {0}\n{1}", version, utils.readRawResource(R.raw.credits)));

        Linkify.addLinks(licenseTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);

        return view;
    }
}
