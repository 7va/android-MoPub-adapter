
![HyperADX Logo](http://d2n7xvwjxl8766.cloudfront.net/assets/site/logo-e04518160888e1f8b3795f0ce01e1909.png) 
 
![MoPub Logo](https://5018-presscdn-27-91-pagely.netdna-ssl.com/wp-content/themes/mopub/img/logo.png)

You can configure MoPub adapter to serve Hyperadx native and interstitial ads through MoPub's mediation solution. 

Please take the following steps to implement MoPub adapter:

* [Download](https://github.com/hyperads/android-MoPub-adapter/releases) and extract the Mopub adapter if needed.

Setup SDK

* [Integrate](https://github.com/mopub/mopub-android-sdk/wiki/Getting-Started) with Mopub SDK
* [Install HyperADX SDK](https://github.com/hyperads/android-sdk#set-up-the-sdk)

## Setup Mopub Dashboard

* Create a new HyperADX Network in Mopub's dashboard and connect it to your Ad Units.


<img src="/docs/images/1.png" title="sample" width="500" height="240" />

* In Mopub's dashboard select Networks > Add a New network.


<img src="/docs/images/2.png" title="sample" width="500" height="460" />

* Then select Custom Native Network.

### Native ads

* Fill in the fields in compliance with the Ad Unit that you want to use:

<img src="/docs/images/3.png" title="sample" width="500" height="300" />

* Custom Event Class: `com.mopub.nativeads.HyperadxNativeMopub`
* Custom Event Class Data: `{"PLACEMENT":"<YOUR PLACEMENT>"}`


**You can use the test placement "5b3QbMRQ"**


* Add adapter in your project. Create package "com.mopub.nativeads" in your project and put this class in there:

```java
package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.hyperadx.hypernetwork.ads.Ad;
import com.hyperadx.hypernetwork.ads.AdError;
import com.hyperadx.hypernetwork.ads.AdListener;
import com.hyperadx.hypernetwork.ads.HadContent;
import com.hyperadx.hypernetwork.ads.internal.server.ImpressionUrlLoader;
import com.hyperadx.hypernetwork.ads.internal.server.LoadClickUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mopub.nativeads.NativeImageHelper.preCacheImages;


public class HyperadxNativeMopub extends CustomEventNative {

    private static final String PLACEMENT_KEY = "PLACEMENT";


    com.hyperadx.hypernetwork.ads.NativeAd nativeAd;

    @Override
    protected void loadNativeAd(@NonNull final Context context, @NonNull final CustomEventNativeListener customEventNativeListener, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) {


        final String placement;
        if ((serverExtras != null) && serverExtras.containsKey(PLACEMENT_KEY)) {
            placement = serverExtras.get(PLACEMENT_KEY);
        } else {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        nativeAd = new com.hyperadx.hypernetwork.ads.NativeAd(context, placement); //Native AD constructor
        nativeAd.setContent(
                HadContent.TITLE,
                HadContent.ICON,
                HadContent.MAIN);
        nativeAd.allowVideo(false);
        nativeAd.setClass(this.getClass());
        nativeAd.setAdListener(new AdListener() { // Add Listeners

            @Override
            public void onAdLoaded(final Ad ad) {

                if (nativeAd == null || nativeAd != ad) {
                    // Race condition, load() called again before last ad was displayed
                    return;
                }

                List<String> imageUrls = new ArrayList<String>();

                if (isValidURL(nativeAd.getAdCoverImage().getUrl()))
                    imageUrls.add(nativeAd.getAdCoverImage().getUrl());


                if (isValidURL(nativeAd.getAdIcon().getUrl()))
                    imageUrls.add(nativeAd.getAdIcon().getUrl());


                preCacheImages(context, imageUrls, new NativeImageHelper.ImageListener() {
                    @Override
                    public void onImagesCached() {
                        customEventNativeListener.onNativeAdLoaded(new HyperadxNativeAd(nativeAd, context));
                    }

                    @Override
                    public void onImagesFailedToCache(NativeErrorCode errorCode) {
                        customEventNativeListener.onNativeAdFailed(NativeErrorCode.EMPTY_AD_RESPONSE);
                    }
                });

            }

            @Override
            public void onAdClicked(Ad ad) { // Called when user click on AD
                Log.d("TAG", "AD Clicked");
            }

            @Override
            public void onError(Ad nativeAd, AdError error) { // Called when load is fail

                customEventNativeListener.onNativeAdFailed(NativeErrorCode.EMPTY_AD_RESPONSE);

            }


        });

        nativeAd.loadAd();

    }


    class HyperadxNativeAd extends StaticNativeAd {

        com.hyperadx.hypernetwork.ads.NativeAd nativeAd = null;
        ImpressionTracker impressionTracker;
        NativeClickHandler nativeClickHandler;
        Context context;

        public HyperadxNativeAd(@NonNull com.hyperadx.hypernetwork.ads.NativeAd nativeAd, Context context) {

            this.nativeAd = nativeAd;
            this.context = context;
            impressionTracker = new ImpressionTracker(context);
            nativeClickHandler = new NativeClickHandler(context);

            setIconImageUrl(nativeAd.getAdIcon().getUrl());
            setMainImageUrl(nativeAd.getAdCoverImage().getUrl());

            setTitle(nativeAd.getAdTitle());
            setText(nativeAd.getAdBody());
            setCallToAction(nativeAd.getAdCallToAction());

            setClickDestinationUrl(nativeAd.getClickDestinationUrl());
            setImpressionMinTimeViewed(nativeAd.getIntViewability());


            for (int i = 0; i < nativeAd.getImpressionTrackers().size(); i++) {
                new ImpressionUrlLoader(context).execute(nativeAd.getImpressionTrackers().get(i));
            }

        }

        @Override
        public void prepare(final View view) {
            impressionTracker.addView(view, this);
            nativeClickHandler.setOnClickListener(view, this);
        }

        @Override
        public void recordImpression(final View view) {
            notifyAdImpressed();
            for (int i = 0; i < nativeAd.getImpressionTrackers().size(); i++) {
                String tracker = nativeAd.getImpressionTrackers().get(i);

                if (isValidURL(tracker))
                    new ImpressionUrlLoader(context).execute(nativeAd.getImpressionTrackers().get(i));
            }
        }

        @Override
        public void handleClick(final View view) {
            notifyAdClicked();
            nativeClickHandler.openClickDestinationUrl(getClickDestinationUrl(), view);
            if (isValidURL(getClickDestinationUrl()))
                new LoadClickUrl(context).execute(getClickDestinationUrl(), nativeAd.redirectParam);
        }


    }

    public boolean isValidURL(String urlStr) {

        if (urlStr == null || urlStr.isEmpty()) return false;

        try {
            URL url = new URL(urlStr);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

}
```


### Interstitial ads

* Complete the fields in compliance with the Ad Unit that you want to use

<img src="/docs/images/4.png" title="sample" width="500" height="300" />

Custom Event Class: `com.mopub.mobileads.HyperadxInterstitialMopub`

Custom Event Class Data: `{"PLACEMENT":"<YOUR PLACEMENT>"}`

**You can use the test placement `5b3QbMRQ`**

* Add adapter in your project. Create package "com.mopub.mobileads" in your project and put this class in there:

HyperADXInterstitialMopub.java:
```java
package com.mopub.mobileads;

import android.content.Context;
import android.util.Log;

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
```

This is your adapter. Now you can use Mopub as usual.
