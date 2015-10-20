# RefreshView
能够自定义显示行为的下拉刷新

本来是想学习以下 BGARefresh 下拉刷新这个库的，看代码看着看着就自己上手写了。。

缺点：只加入了ListView的支持，没有上拉加载（之后再做）
优点：我感觉还是很精简的，不到300行，懒得引用库的人可以直接把主类 Copy 过来就直接用，然后刷新的效果可以自己实现，留了接口,实现方式参见京东效果的那个类，很简单的，如果布局不用代码写的话我估计不会超过60行

我自己实现的一个效果图：

![将就看吧](http://i11.tietuku.com/02c5ac2cb45222fb.gif)

图片地址：http://i11.tietuku.com/02c5ac2cb45222fb.gif

使用方法：

在activity的xml里面：



    <com.kot32.refresh.RefreshView xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/rl_modulename_refresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ListView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:gravity="center" />

    </com.kot32.refresh.RefreshView>
    
在Activity里：


    public class MainActivity extends AppCompatActivity implements RefreshView.IRefreshAction {
    
        private RefreshView mRefreshLayout;
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            initRefreshLayout();
        }
    
        private void initRefreshLayout() {
            mRefreshLayout = (RefreshView) findViewById(R.id.rl_modulename_refresh);
            mRefreshLayout.setiRefreshAction(this);
            mRefreshLayout.setHeaderHeight(80);//dp
    
            //上方的HeaderView
            JDRefreshHeaderView headerView = new JDRefreshHeaderView(this);
            mRefreshLayout.setHeaderView(headerView, null);
            mRefreshLayout.setRefreshViewHolder(headerView);
        }
    
        @Override
        public void refresh() {
            System.out.println("开始刷新");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
        @Override
        public void refreshComplete() {
            System.out.println("刷新结束");
        }
    }


