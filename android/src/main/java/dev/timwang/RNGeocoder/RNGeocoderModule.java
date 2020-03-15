package dev.timwang.RnGeocoder;

import android.location.Address;
import android.location.Geocoder;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RnGeocoderModule extends ReactContextBaseJavaModule {

    private Geocoder geocoder;
    private int maxResults;

    public RnGeocoderModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNGeocoder";
    }

    @ReactMethod
    public void init(String locale, Integer maxResults) {
        geocoder = new Geocoder(getReactApplicationContext(), new Locale(locale));
        this.maxResults = maxResults;
        if (!geocoder.isPresent()) {
          promise.reject("NOT_AVAILABLE", "Geocoder not available for this platform");
          return;
        }
    }

    @ReactMethod
    public void geocodeAddress(String addressName, float swLat, float swLng, float neLat, float neLng, String language, Promise promise) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressName, 2, swLat, swLng, neLat, neLng, maxResults);
            if(addresses != null && addresses.size() > 0) {
                promise.resolve(transform(addresses));
            } else {
                promise.reject("NOT_AVAILABLE", "Geocoder returned an empty list");
            }
        }
        catch (IOException e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void geocodePosition(ReadableMap position, String language, Promise promise) {
        if (geocoder == null) {
            geocoder = new Geocoder(getReactApplicationContext(), new Locale(language));
        }

        if (!geocoder.isPresent()) {
            promise.reject("NOT_AVAILABLE", "Geocoder not available for this platform");
            return;
        }

        try {
            List<Address> addresses = geocoder.getFromLocation(position.getDouble("lat"), position.getDouble("lng"), maxResults);
            if(addresses != null && addresses.size() > 0) {
                promise.resolve(transform(addresses));
            } else {
                promise.reject("NOT_AVAILABLE", "Geocoder returned an empty list");
            }
        }
        catch (IOException e) {
            promise.reject(e);
        }
    }

    WritableArray transform(List<Address> addresses) {
        WritableArray results = new WritableNativeArray();

        for (Address address: addresses) {
            WritableMap result = new WritableNativeMap();

            WritableMap position = new WritableNativeMap();
            position.putDouble("lat", address.getLatitude());
            position.putDouble("lng", address.getLongitude());
            result.putMap("position", position);

            final String feature_name = address.getFeatureName();
            if (feature_name != null && !feature_name.equals(address.getSubThoroughfare()) &&
                    !feature_name.equals(address.getThoroughfare()) &&
                    !feature_name.equals(address.getLocality())) {

                result.putString("feature", feature_name);
            }
            else {
                result.putString("feature", null);
            }

            result.putString("locality", address.getLocality());
            result.putString("adminArea", address.getAdminArea());
            result.putString("country", address.getCountryName());
            result.putString("countryCode", address.getCountryCode());
            result.putString("locale", address.getLocale().toString());
            result.putString("postalCode", address.getPostalCode());
            result.putString("subAdminArea", address.getSubAdminArea());
            result.putString("subLocality", address.getSubLocality());
            result.putString("streetNumber", address.getSubThoroughfare());
            result.putString("streetName", address.getThoroughfare());

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(address.getAddressLine(i));
            }

            result.putString("formattedAddress", sb.toString());

            results.pushMap(result);
        }

        return results;
    }
}
