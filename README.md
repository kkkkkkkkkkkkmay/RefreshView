# RefreshView
能够自定义显示行为的下拉刷新

本来是想学习以下 BGARefresh 下拉刷新这个库的，看代码看着看着就自己上手写了。。

缺点：不支持RecylerView，上拉加载更多暂时只支持 ListView
优点：就这一个类，然后刷新的效果和加载更多的效果可以自己实现，留了接口,实现方式参见京东效果的那个类，很简单的，如果布局不用代码写的话我估计不会超过60行

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
            
            //设置加载更多的配置
            
            KRefreshView.LoadMoreConfig config=new KRefreshView.LoadMoreConfig(true, jdLoadMoreFootView, new KRefreshView.ILoadMoreAction() {
                @Override
                public void loadMore() {
                    for (int i = 0; i <= 10; i++) {
                        studentList.add(new Student("aa" + i, "aa" + i));
                    }
                }
    
                @Override
                public void loadMoreComplete() {
                    adapter.notifyDataSetChanged();
                }}, jdLoadMoreFootView, 80);
            }
            mRefreshLayout.setLoadMoreConfig(config);
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


