package com.mopub.mobileads;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hyperadx.hypernetwork.ads.Ad;
import com.hyperadx.hypernetwork.ads.AdError;
import com.hyperadx.hypernetwork.ads.InterstitialAd;
import com.hyperadx.hypernetwork.ads.InterstitialAdListener;

import java.util.Map;


public class HyperadxInterstitialMopub extends CustomEventInterstitial {

    private static final String PLACEMENT_KEY = "PLACEMENT";

    private InterstitialAd interstitialAd;

    CustomEventInterstitialListener customEventInterstitialListener;


    @Override
    protected void loadInterstitial(final Context context, final CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {

        final String placement;

        if (serverExtras != null && serverExtras.containsKey(PLACEMENT_KEY)) {
            placement = serverExtras.get(PLACEMENT_KEY);

        } else {
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }


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
        if (interstitialAd == null || !interstitialAd.isAdLoaded()) {
            // Ad not ready to show.
            Log.e("HADInterstitialMopub", "The Interstitial AD not ready yet. Try again!");
        } else {
            // Ad was loaded, show it!
            interstitialAd.show();

        }

    }

    @Override
    protected void onInvalidate() {

    }


}
