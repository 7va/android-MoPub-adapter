package com.dispply.mopubdemo;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.mopub.nativeads.MoPubAdAdapter;
import com.mopub.nativeads.MoPubNativeAdPositioning;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends ListActivity {

    MoPubAdAdapter mAdAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up your adapter as usual.
        List<String> data = new ArrayList<>(Arrays.asList("Afghanistan", "Bangladesh", "Cambodia", "Denmark", "Egypt", "Fiji", "Germany", "Honduras", "India", "Japan", "Kenya", "Latvia", "Madagascar", "Nepal", "Philippines", "Qatar", "Russia", "Somalia", "Tibet", "United Kingdom", "Vietnam", "Yemen", "Zaire"));
        CustomAdapter myAdapter = new CustomAdapter(this, data);

        // Set up a ViewBinder and MoPubNativeAdRenderer as above.
        ViewBinder viewBinder = new ViewBinder.Builder(R.layout.native_list_item_layout)
                .iconImageId(R.id.native_icon)
                .titleId(R.id.native_title)
                .textId(R.id.native_text)
                .build();

        // Set up the positioning behavior your ads should have.
        MoPubNativeAdPositioning.MoPubServerPositioning adPositioning =
                MoPubNativeAdPositioning.serverPositioning();
        MoPubStaticNativeAdRenderer adRenderer = new MoPubStaticNativeAdRenderer(viewBinder);

        // Set up the MoPubAdAdapter
        mAdAdapter = new MoPubAdAdapter(this, myAdapter, adPositioning);
        mAdAdapter.registerAdRenderer(adRenderer);

        setListAdapter(mAdAdapter);
    }

    @Override
    public void onResume() {
        mAdAdapter.loadAds(getString(R.string.mopub_native_ad_unit_id));
        super.onResume();
    }

    class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter(Context context, List<String> data) {
            super(context, android.R.layout.simple_list_item_1, data);
        }

    }
}
