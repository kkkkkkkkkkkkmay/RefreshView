
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by kot32 on 15/10/19.
 */
public class JDRefreshHeaderView extends RelativeLayout implements RefreshView.RefreshViewHolder {

    private ImageView people;
    private ImageView box;
    private TextView slogen;
    private TextView tips;

    private AnimationDrawable frameAnimation;

    public JDRefreshHeaderView(Context context) {
        super(context);
        initView();
        initData();
        initController();

    }

    private void initView() {

        people = new ImageView(getContext());
        RelativeLayout.LayoutParams peopleParams = new LayoutParams(DisplayUtil.dip2px(getContext(), 40), DisplayUtil.dip2px(getContext(), 90));
        peopleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        peopleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        addView(people, peopleParams);

        box = new ImageView(getContext());
        RelativeLayout.LayoutParams boxParams = new LayoutParams(DisplayUtil.dip2px(getContext(), 20), DisplayUtil.dip2px(getContext(), 20));
        boxParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        boxParams.setMargins(DisplayUtil.dip2px(getContext(), 55), DisplayUtil.dip2px(getContext(), 32), 0, 0);
        box.setImageResource(R.drawable.app_refresh_goods_0);
        addView(box, boxParams);

        slogen = new TextView(getContext());
        slogen.setText("让购物更便捷");
        RelativeLayout.LayoutParams slogenParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slogenParams.setMargins(DisplayUtil.dip2px(getContext(), 86), DisplayUtil.dip2px(getContext(), 40), 0, 0);
        addView(slogen, slogenParams);

        tips = new TextView(getContext());
        tips.setText("下拉更新...");
        tips.setTextSize(12);
        tips.setTextColor(Color.LTGRAY);
        RelativeLayout.LayoutParams tipsParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tipsParams.setMargins(DisplayUtil.dip2px(getContext(), 86), DisplayUtil.dip2px(getContext(), 60), 0, 0);
        addView(tips, tipsParams);

    }

    private void initData() {
        frameAnimation = new AnimationDrawable();
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.app_refresh_people_0), 50);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.app_refresh_people_1), 50);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.app_refresh_people_2), 50);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.app_refresh_people_3), 50);
        frameAnimation.setOneShot(false);
    }

    private void initController() {
        people.setImageDrawable(frameAnimation);
    }

    private int tx = DisplayUtil.dip2px(getContext(), 30);
    private int ty = DisplayUtil.dip2px(getContext(), 14);

    @Override
    public void pullingTips(View headerView, int progress) {

        float scaleRatio = ((float) progress) / 100;
        people.setScaleX(scaleRatio);
        people.setScaleY(scaleRatio);

        box.setScaleX(scaleRatio);
        box.setScaleY(scaleRatio);

        people.setTranslationX(tx * scaleRatio);
        box.setTranslationY(ty * scaleRatio);

        tips.setText("下拉更新...");
        frameAnimation.stop();
    }

    @Override
    public void willRefreshTips(View headerView) {
        tips.setText("松手更新...");
        frameAnimation.start();
    }

    @Override
    public void refreshingTips(View headerView) {
        tips.setText("正在更新...");
    }

    @Override
    public void refreshCompleteTips(View headerView) {
        tips.setText("更新完成");
        frameAnimation.stop();
    }
}
