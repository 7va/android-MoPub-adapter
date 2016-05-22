package com.dispply.mopubdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

public class MainActivity extends AppCompatActivity implements MoPubInterstitial.InterstitialAdListener {

    MoPubInterstitial mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadInterstitial();
    }

    private void loadInterstitial() {
        mInterstitial = new MoPubInterstitial(this, getString(R.string.mopub_interstitial_ad_unit_id));
        mInterstitial.setInterstitialAdListener(this);
        mInterstitial.load();
    }

    public void showNativeAd(View view) {
        Intent intent = new Intent(this, NativeAdActivity.class);
        startActivity(intent);
    }

    public void showInterstitialAd(View view) {

        if (mInterstitial != null) mInterstitial.show();
        else
            Toast.makeText(this, "The Interstitial AD not ready yet. Try again!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        Log.e("TAG", errorCode.name());
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {

    }
}
