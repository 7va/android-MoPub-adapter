package com.mopub.mobileads;

import android.content.Context;
import android.util.Log;

import com.hyperadx.hypernetwork.ads.Ad;
import com.hyperadx.hypernetwork.ads.AdError;
import com.hyperadx.hypernetwork.ads.InterstitialAd;
import com.hyperadx.hypernetwork.ads.InterstitialAdListener;
import com.hyperadx.hypernetwork.ads.VideoInterstitialAd;

import java.util.Date;
import java.util.Map;


public class HyperadxInterstitialMopub extends CustomEventInterstitial {

    private static final String HTML_KEY = "HTML";
    private static final String VIDEO_KEY = "VIDEO";

    private InterstitialAd interstitialAd;
    private VideoInterstitialAd videoInterstitialAd;

    CustomEventInterstitialListener customEventInterstitialListener;
    private Context context;


    @Override
    protected void loadInterstitial(final Context context, final CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {

        this.context = context;
        this.customEventInterstitialListener = customEventInterstitialListener;

        interstitialAd = null;
        videoInterstitialAd = null;

        final String html;
        final String video;

        if (serverExtras != null && serverExtras.containsKey(HTML_KEY)) {
            html = serverExtras.get(HTML_KEY);

        } else {
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if (serverExtras != null && serverExtras.containsKey(VIDEO_KEY)) {
            video = serverExtras.get(VIDEO_KEY);

        } else {
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if ((new Date().getTime() & 1) == 0) {
            getHtml(html);
        } else {
            getVideo(video);
        }


    }

    private void getVideo(String placement) {
        if (videoInterstitialAd != null) {
            videoInterstitialAd.destroy();
            videoInterstitialAd = null;
        }

        videoInterstitialAd = new VideoInterstitialAd(context, placement);

        videoInterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                customEventInterstitialListener.onInterstitialShown();
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                customEventInterstitialListener.onInterstitialDismissed();
            }

            @Override
            public void onVideoCompleted(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (ad != videoInterstitialAd) return;

                MoPubErrorCode moPubErrorCode = MoPubErrorCode.UNSPECIFIED;

                switch (adError.getErrorCode()) {
                    case AdError.INTERNAL_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.INTERNAL_ERROR;
                        break;

                    case AdError.LOAD_TOO_FREQUENTLY_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.SERVER_ERROR;
                        break;

                    case AdError.NETWORK_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.NETWORK_TIMEOUT;
                        break;

                    case AdError.NO_FILL_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.NO_FILL;
                        break;

                    case AdError.SERVER_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.SERVER_ERROR;
                        break;

                    default:
                        moPubErrorCode = MoPubErrorCode.UNSPECIFIED;
                        break;
                }

                customEventInterstitialListener.onInterstitialFailed(moPubErrorCode);

            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (ad == videoInterstitialAd)
                    customEventInterstitialListener.onInterstitialLoaded();

            }

            @Override
            public void onAdClicked(Ad ad) {
                if (ad == videoInterstitialAd)
                    customEventInterstitialListener.onInterstitialClicked();
            }
        });

        videoInterstitialAd.loadAd();
    }

    private void getHtml(String placement) {
        interstitialAd = new InterstitialAd(context, placement); //Interstitial AD constructor
        interstitialAd.setAdListener(new InterstitialAdListener() { // Set Listener
            @Override
            public void onAdLoaded(Ad ad) { // Called when AD is Loaded

                if (ad != interstitialAd) {
                    return; // Race condition, load() called again before last ad was displayed
                }

                //   Toast.makeText(context, "Interstitial Ad loaded", Toast.LENGTH_SHORT).show();
                customEventInterstitialListener.onInterstitialLoaded();

            }

            @Override
            public void onError(Ad Ad, AdError error) { // Called when load is fail
                // Toast.makeText(context, "Interstitial Ad failed to load with error: " + error.getErrorMessage(), Toast.LENGTH_SHORT).show();


                MoPubErrorCode moPubErrorCode = MoPubErrorCode.UNSPECIFIED;

                switch (error.getErrorCode()) {
                    case AdError.INTERNAL_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.INTERNAL_ERROR;
                        break;

                    case AdError.LOAD_TOO_FREQUENTLY_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.SERVER_ERROR;
                        break;

                    case AdError.NETWORK_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.NETWORK_TIMEOUT;
                        break;

                    case AdError.NO_FILL_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.NO_FILL;
                        break;

                    case AdError.SERVER_ERROR_CODE:
                        moPubErrorCode = MoPubErrorCode.SERVER_ERROR;
                        break;

                    default:
                        moPubErrorCode = MoPubErrorCode.UNSPECIFIED;
                        break;
                }

                customEventInterstitialListener.onInterstitialFailed(moPubErrorCode);
            }

            @Override
            public void onInterstitialDisplayed(Ad Ad) { // Called when Ad was impressed
                //    Toast.makeText(context, "Tracked Interstitial Ad impression", Toast.LENGTH_SHORT).show();
                customEventInterstitialListener.onInterstitialShown();
            }

            @Override
            public void onInterstitialDismissed(Ad ad) { // Called when Ad was dissnissed by user
                //   Toast.makeText(context, "Interstitial Ad Dismissed", Toast.LENGTH_SHORT).show();
                customEventInterstitialListener.onInterstitialDismissed();
            }

            @Override
            public void onAdClicked(Ad Ad) { // Called when user click on AD
                //   Toast.makeText(context, "Tracked Interstitial Ad click", Toast.LENGTH_SHORT).show();
                customEventInterstitialListener.onInterstitialClicked();
            }

            @Override
            public void onVideoCompleted(Ad ad) {
                //  Toast.makeText(context, "Interstitial Video Completed", Toast.LENGTH_SHORT).show();

            }

        });

        this.customEventInterstitialListener = customEventInterstitialListener;

        interstitialAd.loadAd(); // Call to load AD
    }


    @Override
    protected void showInterstitial() {
        if (interstitialAd != null && interstitialAd.isAdLoaded()) {
            interstitialAd.show();
        } else if (videoInterstitialAd != null && videoInterstitialAd.isAdLoaded()) {
            videoInterstitialAd.show();
        } else {
            Log.e("HADInterstitialMopub", "The Interstitial AD not ready yet. Try again!");
        }

    }

    @Override
    protected void onInvalidate() {

    }


}
