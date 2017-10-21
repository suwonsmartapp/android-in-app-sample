package com.example.inappactivity;

import android.os.Bundle;
import android.widget.Button;

import com.example.inappactivity.inapp.InAppActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends InAppActivity {

    @BindView(R.id.buy_item_button)
    Button mBuyItemButton;

    @Override
    public void onQuerySubscribe(boolean isSubscribed) {
        // 구독 여부를 알 수 있는 콜백 활용 예시
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
}
