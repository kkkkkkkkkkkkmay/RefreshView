
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.kot32.ksimplelibrary.util.tools.DisplayUtil;
import com.kot32.ksimplelibrary.util.tools.FieldUtil;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by kot32 on 15/10/18.
 * 实现下拉刷新的 View
 * KRefreshView{SubView:HeaderView、ListView}
 */
public class KRefreshView extends LinearLayout {

    private Scroller scroller = new Scroller(getContext());

    private TextView default_header_tips;

    private TextView default_bottom_tips;

    private boolean shouldRefresh = false;

    private boolean isRefreshing = false;

    private boolean shouldLoadMore = false;

    private boolean isLoadMoreing = false;

    //是否开启上拉加载更多的开关
    private LoadMoreConfig loadMoreConfig;

    //头部刷新View的父容器
    private RelativeLayout headerContent;
    //尾部『加载更多』 View 的父容器
    private RelativeLayout bottomContent;
    //头部刷新View
    private View headerView;
    //尾部『加载更多』 的 View
    private View bottomView;
    //下拉刷新-控制UI更新
    private RefreshViewHolder refreshViewHolder;
    //下拉刷新-控制事务更新
    private IRefreshAction iRefreshAction;
    //下拉刷新-控制UI更新
    private LoadMoreViewHolder loadMoreViewHolder;
    //下拉刷新-控制事务更新
    private ILoadMoreAction iLoadMoreAction;
    //触摸事件传递接口
    private onRefreshViewTouch onRefreshViewTouch;
    //默认高度
    private int HEADER_HEIGHT = 100;

    private int BOTTOM_HEIGHT = 100;

    private int MAX_LIMIT_SLOT = 50;

    private int totalLimit;

    private AtomicBoolean isInitListViewListener = new AtomicBoolean(false);

    private boolean isShouldShowLoadMore = false;

    public KRefreshView(Context context) {
        super(context);
        initView();
        initData();
        initController();
    }

    public KRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        initData();
        initController();
    }

    private void initView() {
        setOrientation(VERTICAL);

        //增加头部View容器
        LinearLayout.LayoutParams headerContentParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(getContext(), HEADER_HEIGHT));
        headerContent = new RelativeLayout(getContext());
        headerContentParams.setMargins(0, -DisplayUtil.dip2px(getContext(), HEADER_HEIGHT), 0, 0);
        headerContent.setLayoutParams(headerContentParams);
        addView(headerContent, 0);

        //增加隐藏的View
        default_header_tips = new TextView(getContext());
        default_header_tips.setText("继续下拉以刷新...");
        RelativeLayout.LayoutParams defaultHeaderTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        defaultHeaderTextParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        default_header_tips.setLayoutParams(defaultHeaderTextParams);
        headerContent.addView(default_header_tips, 0);

        // 尾部View容器
        bottomContent = new RelativeLayout(getContext());
        LayoutParams bottomContentParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(getContext(), BOTTOM_HEIGHT));
        bottomContentParams.setMargins(0, 0, 0, -DisplayUtil.dip2px(getContext(), BOTTOM_HEIGHT));

        bottomContent.setLayoutParams(bottomContentParams);
        default_bottom_tips = new TextView(getContext());
        default_bottom_tips.setText("上拉加载更多..");
        RelativeLayout.LayoutParams defaultBottomTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        defaultBottomTextParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        default_bottom_tips.setLayoutParams(defaultBottomTextParams);
        bottomContent.addView(default_bottom_tips, 0);


    }

    private void initData() {
        refreshViewHolder = new RefreshViewHolder() {

            @Override
            public void pullingTips(View headerView, int progress) {
                default_header_tips.setText("继续下拉以刷新...");
            }

            @Override
            public void willRefreshTips(View headerView) {

                default_header_tips.setText("松开以刷新");
            }

            @Override
            public void refreshingTips(View headerView) {
                default_header_tips.setText("正在刷新...");
            }

            @Override
            public void refreshCompleteTips(View headerView) {
                default_header_tips.setText("刷新成功");
            }
        };

        loadMoreViewHolder = new LoadMoreViewHolder() {
            @Override
            public void loadTips(View headerView, int progress) {
                default_bottom_tips.setText("继续上拉以加载更多");
            }

            @Override
            public void willloadTips(View headerView) {
                default_bottom_tips.setText("松开以加载更多");
            }

            @Override
            public void loadingTips(View headerView) {
                default_bottom_tips.setText("正在加载");
            }

            @Override
            public void loadCompleteTips(View headerView) {
                default_bottom_tips.setText("加载完成");
            }
        };

        totalLimit = DisplayUtil.dip2px(getContext(), HEADER_HEIGHT + MAX_LIMIT_SLOT);
    }

    private void initController() {

    }

    public void initLoadMoreFunc() {
        if (isInitListViewListener.compareAndSet(false, true)) {
            View view = getChildAt(1);
            if (view instanceof ListView) {
                final AbsListView.OnScrollListener oldListener = (AbsListView.OnScrollListener) FieldUtil.getDefedValue("mOnScrollListener", ListView.class, view);
                ((ListView) view).setOnScrollListener(new AbsListView.OnScrollListener() {
                    private boolean scrollFlag = false;// 标记是否滑动
                    private int lastVisibleItemPosition = 0;// 标记上次滑动位置

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (oldListener != null) {
                            oldListener.onScrollStateChanged(view, scrollState);
                        }
                        switch (scrollState) {
                            // 当不滚动时
                            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:// 是当屏幕停止滚动时
                                scrollFlag = false;
                                break;
                            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 滚动时
                                scrollFlag = true;
                                break;
                            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:// 是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                                scrollFlag = false;
                                break;
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (oldListener != null) {
                            oldListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                        }
                        if (view.getLastVisiblePosition() == (view
                                .getCount() - 1)) {
                            if (bottomContent.getParent() == null) {
                                //显示加载更多View
                                KRefreshView.this.addView(bottomContent, 2);

                            }
                            //当滑到最下面时，控制权交给父 View
                            isShouldShowLoadMore = true;
                        } else {
                            isShouldShowLoadMore = false;
                        }

                    }
                });
            }
        }
    }

    private float preY;
    private float tmpY;


    private float Y1;
    private float Y2;

    enum INTERCEPT_ACTION {
        REFRESH, LOAD_MORE
    }

    private INTERCEPT_ACTION intercept_action;

    //判断是否拦截事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //如果是方向向上，且ListView
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Y1 = ev.getRawY();
                preY = ev.getRawY();
                tmpY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                Y2 = ev.getRawY();
                //没有滑动到一定距离，就不拦截
                if (Math.abs(Y2 - Y1) < 3)
                    return false;
                if (Y2 > Y1) {
                    View view = getChildAt(1);
                    if (view instanceof ListView) {
                        //如果ListView可见的第一个index是0，并且还没滑动
                        if (((ListView) view).getFirstVisiblePosition() == 0) {
                            View v = ((ListView) view).getChildAt(0);
                            if ((v == null) || (v != null && v.getTop() == 0)) {
                                intercept_action = INTERCEPT_ACTION.REFRESH;
                                return true;
                            }
                        }
                    } else if (view instanceof ScrollView) {
                        if (view.getScrollY() == 0) {
                            intercept_action = INTERCEPT_ACTION.REFRESH;
                            return true;
                        }
                    } else if (view instanceof WebView) {
                        if (view.getScrollY() == 0) {
                            intercept_action = INTERCEPT_ACTION.REFRESH;
                            return true;
                        }
                    }

                } else {
                    if (loadMoreConfig.canLoadMore) {
                        if (isShouldShowLoadMore) {
                            intercept_action = INTERCEPT_ACTION.LOAD_MORE;
                            return true;
                        }
                    }
                }

                break;
        }
        return false;

    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        boolean isRefreshTiping = false;

        boolean isLoadMoreTiping = false;

        if (onRefreshViewTouch != null) {
            onRefreshViewTouch.onTouch((int) ev.getRawX(), (int) ev.getRawY());
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preY = ev.getRawY();
                tmpY = ev.getRawY();

                isRefreshTiping = false;
                isLoadMoreTiping = false;

                if (refreshViewHolder != null) {
                    if (isRefreshing) break;
                    refreshViewHolder.pullingTips(headerView, 0);
                }

                if (loadMoreViewHolder != null) {
                    if (isLoadMoreing) break;
                    loadMoreViewHolder.loadTips(bottomView, 0);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                float currentY = ev.getRawY();
                float offsetY = currentY - tmpY;
                float dis = currentY - preY;

                //下拉刷新
                if (intercept_action == INTERCEPT_ACTION.REFRESH) {
                    if (dis >= DisplayUtil.dip2px(getContext(), HEADER_HEIGHT)) {
                        if (refreshViewHolder != null) {
                            if (!isRefreshTiping) {
                                //提示只有一次
                                refreshViewHolder.willRefreshTips(headerView);
                                isRefreshTiping = true;
                            }
                        }
                        shouldRefresh = true;
                    } else {
                        shouldRefresh = false;
                        float ratio = dis / DisplayUtil.dip2px(getContext(), HEADER_HEIGHT);
                        if (refreshViewHolder != null && !isRefreshing)
                            refreshViewHolder.pullingTips(headerView, (int) (100 * ratio));
                    }

                    if (dis >= 0 && (dis < totalLimit)) {
                        this.scrollBy(0, -(int) offsetY);
                    }
                    if (dis >= totalLimit) {
                        this.scrollTo(0, -totalLimit);
                    }
                } else if ((intercept_action == INTERCEPT_ACTION.LOAD_MORE) && (dis < 0)) {
                    //上拉加载
                    this.scrollBy(0, -(int) offsetY);

                    if (Math.abs(dis) >= DisplayUtil.dip2px(getContext(), BOTTOM_HEIGHT)) {
                        if (!isLoadMoreTiping) {
                            loadMoreViewHolder.willloadTips(bottomView);
                            isLoadMoreTiping = true;
                        }
                        shouldLoadMore = true;
                    } else {
                        shouldLoadMore = false;
                        float ratio = dis / DisplayUtil.dip2px(getContext(), BOTTOM_HEIGHT);
                        if (loadMoreViewHolder != null && !isLoadMoreing)
                            loadMoreViewHolder.loadTips(headerView, (int) (100 * ratio));
                    }
                }

                tmpY = currentY;
                break;
            case MotionEvent.ACTION_UP:
                smoothToZeroPos();
                break;
            case MotionEvent.ACTION_CANCEL:
                smoothToZeroPos();
                break;
        }
        return true;
    }


    private void startRefresh() {
        if (refreshViewHolder != null) {
            refreshViewHolder.refreshingTips(headerView);
            //开始后台刷新任务
            if (!isRefreshing)
                new RefreshTask().execute();
        }
    }

    private void startLoadMore() {
        if (loadMoreViewHolder != null) {
            loadMoreViewHolder.loadingTips(bottomView);
            if (!isLoadMoreing) {
                new LoadMoreTask().execute();
            }
        }
    }

    private void smoothToZeroPos() {
        //下拉刷新
        if (intercept_action == INTERCEPT_ACTION.REFRESH) {
            if (shouldRefresh)
                startRefresh();
        } else if (intercept_action == INTERCEPT_ACTION.LOAD_MORE) {
            //上拉加载更多
            if (shouldLoadMore) {
                startLoadMore();
            }
        }
        smoothScrollTo(0, 0);
    }

    private void smoothScrollTo(int destX, int destY) {
        int offsetY = destY - getScrollY();
        scroller.startScroll(0, getScrollY(), 0, offsetY, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }


    //下拉刷新-管理UI方面的接口
    public interface RefreshViewHolder {

        //还没到刷新点的提示
        void pullingTips(View headerView, int progress);

        //快要刷新时的提示
        void willRefreshTips(View headerView);

        //正在刷新时的状态
        void refreshingTips(View headerView);

        //刷新完毕
        void refreshCompleteTips(View headerView);
    }

    //下拉刷新-管理事务方面的接口
    public interface IRefreshAction {

        void refresh();

        void refreshComplete();
    }

    //上拉加载-管理UI 接口
    public interface LoadMoreViewHolder {
        //还没到加载点的提示
        void loadTips(View headerView, int progress);

        //快要加载时的提示
        void willloadTips(View headerView);

        //正在加载时的状态
        void loadingTips(View headerView);

        //加载完毕
        void loadCompleteTips(View headerView);
    }

    public interface ILoadMoreAction {

        void loadMore();

        void loadMoreComplete();
    }


    //触摸点传递的接口，可供实现类扩展更多自定义动画
    public interface onRefreshViewTouch {
        void onTouch(int x, int y);
    }


    public View getHeaderView() {
        return headerView;
    }

    public void setHeaderView(View headerView, RelativeLayout.LayoutParams layoutParams) {
        this.headerView = headerView;
        headerContent.removeViewAt(0);
        RelativeLayout.LayoutParams defaultLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        defaultLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        if (layoutParams == null)
            headerView.setLayoutParams(defaultLayoutParams);
        else
            headerView.setLayoutParams(layoutParams);
        headerContent.addView(headerView, 0);
    }

    public void setBottomView(View bottomView, RelativeLayout.LayoutParams layoutParams) {
        this.bottomView = bottomView;
        bottomContent.removeViewAt(0);
        RelativeLayout.LayoutParams defaultLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        defaultLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        if (layoutParams == null)
            bottomView.setLayoutParams(defaultLayoutParams);
        else
            bottomView.setLayoutParams(layoutParams);
        bottomContent.addView(bottomView, 0);
    }

    public RefreshViewHolder getRefreshViewHolder() {
        return refreshViewHolder;
    }

    public void setRefreshViewHolder(RefreshViewHolder refreshViewHolder) {
        this.refreshViewHolder = refreshViewHolder;
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            isRefreshing = true;
            shouldRefresh = false;

            if (iRefreshAction != null) {
                iRefreshAction.refresh();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            post(new Runnable() {
                @Override
                public void run() {
                    if (refreshViewHolder != null) {
                        refreshViewHolder.refreshCompleteTips(headerView);
                        smoothScrollTo(0, 0);
                    }
                }
            });
            if (iRefreshAction != null) {
                iRefreshAction.refreshComplete();
            }

            isRefreshing = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            smoothScrollTo(0, 0);
            shouldRefresh = false;
            isRefreshing = false;
        }
    }

    private class LoadMoreTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isLoadMoreing = true;
            shouldLoadMore = false;

            if (iLoadMoreAction != null) {
                iLoadMoreAction.loadMore();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            super.onPostExecute(aVoid);
            post(new Runnable() {
                @Override
                public void run() {
                    if (loadMoreViewHolder != null) {
                        loadMoreViewHolder.loadCompleteTips(headerView);
                        smoothScrollTo(0, 0);
                    }
                }
            });
            if (iLoadMoreAction != null) {
                iLoadMoreAction.loadMoreComplete();
            }

            isLoadMoreing = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            smoothScrollTo(0, 0);
            shouldLoadMore = false;
            isLoadMoreing = false;
        }
    }

    public void setiRefreshAction(IRefreshAction iRefreshAction) {
        this.iRefreshAction = iRefreshAction;
    }

    public ILoadMoreAction getiLoadMoreAction() {
        return iLoadMoreAction;
    }

    public void setiLoadMoreAction(ILoadMoreAction iLoadMoreAction) {
        this.iLoadMoreAction = iLoadMoreAction;
    }

    public LoadMoreViewHolder getLoadMoreViewHolder() {
        return loadMoreViewHolder;
    }

    public void setLoadMoreViewHolder(LoadMoreViewHolder loadMoreViewHolder) {
        this.loadMoreViewHolder = loadMoreViewHolder;
    }

    public KRefreshView.onRefreshViewTouch getOnRefreshViewTouch() {
        return onRefreshViewTouch;
    }

    public void setOnRefreshViewTouch(KRefreshView.onRefreshViewTouch onRefreshViewTouch) {
        this.onRefreshViewTouch = onRefreshViewTouch;
    }

    public void setHeaderHeight(int headerHeightDp) {
        this.HEADER_HEIGHT = headerHeightDp;
        totalLimit = DisplayUtil.dip2px(getContext(), HEADER_HEIGHT + MAX_LIMIT_SLOT);
    }

    public int getMAX_LIMIT_SLOT() {
        return MAX_LIMIT_SLOT;
    }

    public void setMAX_LIMIT_SLOT(int MAX_LIMIT_SLOT) {
        this.MAX_LIMIT_SLOT = MAX_LIMIT_SLOT;
        totalLimit = DisplayUtil.dip2px(getContext(), HEADER_HEIGHT + MAX_LIMIT_SLOT);
    }

    public LoadMoreConfig getLoadMoreConfig() {
        return loadMoreConfig;
    }

    public void setLoadMoreConfig(LoadMoreConfig loadMoreConfig) {
        this.loadMoreConfig = loadMoreConfig;
        if (loadMoreConfig.canLoadMore) {
            if (loadMoreConfig.loadMoreView != null)
                setBottomView(loadMoreConfig.loadMoreView, null);
            if (loadMoreConfig.iLoadMoreAction != null)
                setiLoadMoreAction(loadMoreConfig.iLoadMoreAction);
            if (loadMoreConfig.loadMoreViewHolder != null)
                setLoadMoreViewHolder(loadMoreConfig.loadMoreViewHolder);
            if (loadMoreConfig.height > 0)
                BOTTOM_HEIGHT = loadMoreConfig.height;
        }
    }

    public static class LoadMoreConfig {
        public boolean canLoadMore;
        public View loadMoreView;
        public ILoadMoreAction iLoadMoreAction;
        public LoadMoreViewHolder loadMoreViewHolder;
        public int height;

        public LoadMoreConfig(boolean canLoadMore, View loadMoreView, ILoadMoreAction iLoadMoreAction, LoadMoreViewHolder loadMoreViewHolder, int height) {
            this.canLoadMore = canLoadMore;
            this.loadMoreView = loadMoreView;
            this.iLoadMoreAction = iLoadMoreAction;
            this.loadMoreViewHolder = loadMoreViewHolder;
            this.height = height;
        }
    }
}
