package com.hyperadx.mopubdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mopub.common.MoPubReward;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements MoPubInterstitial.InterstitialAdListener, MoPubRewardedVideoListener {

    MoPubInterstitial mInterstitial;
    private boolean sRewardedVideoInitialized = false;

    @Nullable
    private Map<String, MoPubReward> mMoPubRewardsMap;
    @Nullable
    private MoPubReward mSelectedReward;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  loadInterstitial();
        loadRewarded();
    }

    private void loadRewarded() {
        if (!sRewardedVideoInitialized) {
            MoPubRewardedVideos.initializeRewardedVideo(this);
            sRewardedVideoInitialized = true;
        }

        MoPubRewardedVideos.setRewardedVideoListener(this);

        mMoPubRewardsMap = new HashMap<>();

        MoPubRewardedVideos.loadRewardedVideo(getString(R.string.mopub_rewarded_ad_unit_id),
                new MoPubRewardedVideoManager.RequestParameters("", null,
                        ""));

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


    public void showRewardedAd(View view) {

        if (MoPubRewardedVideos.hasRewardedVideo(getString(R.string.mopub_rewarded_ad_unit_id)))

            MoPubRewardedVideos.showRewardedVideo(getString(R.string.mopub_rewarded_ad_unit_id));
        else
            Toast.makeText(this, "The Rewarded AD not ready yet. Try again!", Toast.LENGTH_LONG).show();


    }

    @Override
    public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
        Toast.makeText(this, "Already have videos " + MoPubRewardedVideos.getAvailableRewards(getString(R.string.mopub_rewarded_ad_unit_id)).size(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {

    }

    @Override
    public void onRewardedVideoStarted(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {

    }

    @Override
    public void onRewardedVideoClosed(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {

    }
}
