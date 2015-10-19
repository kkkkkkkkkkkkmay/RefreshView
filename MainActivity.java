
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements RefreshView.IRefreshAction {

    private RefreshView mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("PullToRefresh");
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
