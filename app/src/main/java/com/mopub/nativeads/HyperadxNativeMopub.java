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
