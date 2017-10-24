package com.jsoh.inappactivity;

import android.os.Bundle;
import android.widget.Toast;

import com.jsoh.inappactivity.inapp.InAppActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends InAppActivity {

    @Override
    public void onQuerySubscribe(boolean isSubscribed) {
        // 구독 여부를 알 수 있는 콜백 활용 예시
        if (isSubscribed) {
            Toast.makeText(this, "구독중", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItem1Bye() {
        // 에너지를 늘린다
        Toast.makeText(this, "아이템 구매 완료", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.buy_item_button)
    public void onBuyItemButtonClicked() {
        // 아이템 결재 수행. InAppActivity에 미리 구현해 둠
        onBuyNoAdItemClicked();
    }

    @OnClick(R.id.buy_item2_button)
    public void onViewClicked() {
        onItem1BuyClicked();
    }
}
