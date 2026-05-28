package zxc10089.zyl.lanlan;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
								   LinearLayout.LayoutParams.MATCH_PARENT,
								   LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(Color.parseColor("#1C1C1E"));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        TextView tv = new TextView(this);
        tv.setText("张云雷分身 v1.5\n\n一个基于 DeepSeek 的 AI 角色聊天应用。\n\n开发者：zxc10089（兰兰）\n协助开发者：竹攸\n\n开发故事：\n用 2.5 元委托我开发该软件，\n购买 API 已用 2 元，\n赚了 0.5 元。");
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(18);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(32, 32, 32, 32);

        Button btnBack = new Button(this);
        btnBack.setText("返回");
        btnBack.setTextColor(Color.parseColor("#AAAAAA"));
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

        layout.addView(tv);
        layout.addView(btnBack);
        setContentView(layout);
    }
}
