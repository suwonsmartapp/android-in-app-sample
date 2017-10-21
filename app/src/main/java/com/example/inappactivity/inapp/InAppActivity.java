
package com.example.inappactivity.inapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by junsuk on 14. 12. 9.. In-App billing 구현 Activity
 * http://theeye.pe.kr/archives/2130 참고
 */
public abstract class InAppActivity extends AppCompatActivity implements
        IabBroadcastReceiver.IabBroadcastListener {

    // TODO : 상품 ID 정의
    // 광고 없애기 상품 : no_ad_month (1개월)
    static final String SKU_NO_AD = "no_ad_month";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    private static final String TAG = InAppActivity.class.getSimpleName();
    // 광고 free 상품 구매 여부
    protected boolean mSubscribedToNoAd = false;

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;

    // 구독 콜백 예시
    abstract public void onQuerySubscribe(boolean isSubscribed);

    // Listener that's called when we finish querying the items and
    // subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // mHelper가 소거되었다면 종료
            if (mHelper == null)
                return;

            // getPurchases()가 실패하였다면 종료
            if (result.isFailure()) {
                // complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * 유저가 보유중인 각각의 아이템을 체크합니다. 여기서 developerPayload가 정상인지 여부를 확인합니다.
             * 자세한 사항은 verifyDeveloperPayload()를 참고하세요.
             */

            // 1년 광고 제거를 구독 중인가?
            Purchase noAdPurchase = inventory.getPurchase(SKU_NO_AD);
            mSubscribedToNoAd = (noAdPurchase != null &&
                    verifyDeveloperPayload(noAdPurchase));

            Log.d(TAG, "User " + (mSubscribedToNoAd ? "HAS" : "DOES NOT HAVE")
                    + " No_Ad subscription.");

            // TODO : 이 부분은 필요에 의해 커스터마이징
            // 구독 여부 콜백
            onQuerySubscribe(mSubscribedToNoAd);

            Log.d(TAG, "Initial inventory query finished");
        }
    };
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                // complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                // complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_NO_AD)) {
                // 광고 제거 상품을 구매 했다면 적용
                Log.d(TAG, "No_Ad subscription purchased.");
                alert("구매가 완료 되었습니다");
                mSubscribedToNoAd = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY (that
         * you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key. Instead of
         * just storing the entire literal string here embedded in the program,
         * construct the key at runtime from pieces or use bit manipulation (for
         * example, XOR with some other string) to hide the actual key. The key
         * itself is not secret information, but we don't want to make it easy
         * for an attacker to replace the public key with one of their own and
         * then fake messages from the server.
         */
        String base64EncodedPublicKey = "public_key 넣으세요";

        // Some sanity checks to see if the developer (that's you!) really
        // followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException(
                    "Please put your app's public key in MainActivity.java. See README.");
        }
        if (getPackageName().startsWith("com.example")) {
            throw new RuntimeException("Please change the sample's package name! See README.");
        }

        // Create the helper, passing it our context and the public key to
        // verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set
        // this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null)
                    return;

                // Important: Dynamically register for broadcast messages about
                // updated purchases.
                // We register the receiver here instead of as a <receiver> in
                // the Manifest
                // because we always call getPurchases() at startup, so
                // therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea,
                // but is done here
                // because this is a SAMPLE. Regardless, the receiver must be
                // registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(InAppActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we
                // own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });

    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    void complain(String message) {
        Log.e(TAG, "**** In-App Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: 위의 그림에서 설명하였듯이 로컬 저장소 또는 원격지로부터 미리 저장해둔 developerPayload값을 꺼내
         * 변조되지 않았는지 여부를 확인합니다. 이 payload의 값은 구매가 시작될 때 랜덤한 문자열을 생성하는것은 충분히 좋은
         * 접근입니다. 하지만 두개의 디바이스를 가진 유저가 하나의 디바이스에서 결제를 하고 다른 디바이스에서 검증을 하는 경우가
         * 발생할 수 있습니다. 이 경우 검증을 실패하게 될것입니다. 그러므로 개발시에 다음의 상황을 고려하여야 합니다. 1. 두명의
         * 유저가 같은 아이템을 구매할 때, payload는 같은 아이템일지라도 달라야 합니다. 두명의 유저간 구매가 이어져서는
         * 안됩니다. 2. payload는 앱을 두대를 사용하는 유저의 경우에도 정상적으로 동작할 수 있어야 합니다. 이
         * payload값을 저장하고 검증할 수 있는 자체적인 서버를 구축하는것을 권장합니다.
         */

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        if (mHelper == null)
            return;

        // 결과를 mHelper를 통해 처리합니다.
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // 처리할 결과물이 아닐경우 이곳으로 빠져 기본처리를 하도록 합니다.
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    public void onBuyNoAdItemClicked() {
        /*
         * TODO: for security, generate your payload here for verification. See
         * the comments on verifyDeveloperPayload() for more info. Since this is
         * a SAMPLE, we just use an empty string, but on a production app you
         * should carefully generate this.
         */
        String payload = "";

        try {
            mHelper.launchSubscriptionPurchaseFlow(this,
                    SKU_NO_AD, RC_REQUEST, mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
            // setWaitScreen(false);
        }
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has
        // changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }
}
