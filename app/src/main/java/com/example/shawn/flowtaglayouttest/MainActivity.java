package com.example.shawn.flowtaglayouttest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements FlowTagsLayout.OnTagChosenCallback, FlowTagsLayout.OnTagClickCallback {
    public static final String TAG ="MainActivityTEst";
    public static final String[] TAG_ITEM_TITLE={"PDD","全英雄联盟","最骚的","骚猪","没有之一","PDD的洪荒之力" +
            "我并不是全英雄联盟最骚的骚猪",
    "AlphaGo","Artificial Intelligent","Deep Mind","人机大战","柯杰加油"};
    private FlowTagsLayout mFlowTagsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        for (int i = 0; i < TAG_ITEM_TITLE.length; i++) {
            mFlowTagsLayout.appendTag(TAG_ITEM_TITLE[i]);
        }
    }

    private void initView() {
        mFlowTagsLayout= (FlowTagsLayout) findViewById(R.id.layout_flow);
        mFlowTagsLayout.setOnTagChosenCallback(this);
        mFlowTagsLayout.setOnTagClickCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    /**
     * 这里为了演示才加入了多种模式,实际开发中一般没有这种需求仈
     * 建议在布局文件中直接定义Mode
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.click:
                mFlowTagsLayout.setTagMode(FlowTagsLayout.MODE_CLICK);
                break;
            case R.id.single:
                mFlowTagsLayout.setTagMode(FlowTagsLayout.MODE_SINGLE);
                break;
            case R.id.multi:
                mFlowTagsLayout.setTagMode(FlowTagsLayout.MODE_MULTI);
                break;
        }
        mFlowTagsLayout.removeAllTags();
        initData();
        return true;
    }

    /**
     * 单选,多选的回调
     * @param those
     */
    @Override
    public void onTagChosen(int... those) {
        for (int i : those) {
            Toast.makeText(this,TAG_ITEM_TITLE[i],Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击的回调
     * @param which
     */
    @Override
    public void onTagClick(int which) {
        Toast.makeText(this,TAG_ITEM_TITLE[which],Toast.LENGTH_SHORT).show();
    }
}
