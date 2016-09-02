package me.next.nestedscrollviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    NestedScrollView scrollView;
    NestedScrollWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = (NestedScrollView) findViewById(R.id.scroll_view);
        webView = (NestedScrollWebView) findViewById(R.id.web_view);

        webView.loadUrl("http://www.jianshu.com/p/4535442d568f");

    }
}
