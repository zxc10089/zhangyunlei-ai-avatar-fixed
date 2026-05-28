package zxc10089.zyl.lanlan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class MemoryManageActivity extends Activity {

    private LinearLayout memoryListContainer;
    private Button btnAddMemory, btnBack;
    private SharedPreferences prefs;
    private JSONArray memoryArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_manage);

        memoryListContainer = findViewById(R.id.memoryListContainer);
        btnAddMemory = findViewById(R.id.btnAddMemory);
        btnBack = findViewById(R.id.btnBack);
        prefs = getSharedPreferences("app_config", MODE_PRIVATE);

        loadMemoryList();

        btnAddMemory.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showEditDialog(-1, "");
				}
			});
        btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
    }

    private void loadMemoryList() {
        String json = prefs.getString("long_term_memory", "[]");
        try {
            memoryArray = new JSONArray(json);
        } catch (Exception e) {
            memoryArray = new JSONArray();
        }
        memoryListContainer.removeAllViews();
        for (int i = 0; i < memoryArray.length(); i++) {
            try {
                final JSONObject mem = memoryArray.getJSONObject(i);
                final String content = mem.getString("content");
                addMemoryItem(i, content);
            } catch (Exception e) {}
        }
    }

    private void addMemoryItem(final int index, final String content) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 12, 0, 12);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
									   LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvContent = new TextView(this);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
			0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvContent.setLayoutParams(tvParams);
        tvContent.setText(content);
        tvContent.setTextColor(Color.parseColor("#E5E5E7"));
        tvContent.setTextSize(14f);

        Button btnEdit = new Button(this);
        btnEdit.setText("编辑");
        btnEdit.setTextSize(12f);
        btnEdit.setBackgroundColor(Color.TRANSPARENT);
        btnEdit.setTextColor(Color.parseColor("#AAAAAA"));
        btnEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showEditDialog(index, content);
				}
			});

        Button btnDelete = new Button(this);
        btnDelete.setText("删除");
        btnDelete.setTextSize(12f);
        btnDelete.setBackgroundColor(Color.TRANSPARENT);
        btnDelete.setTextColor(Color.parseColor("#FF6B6B"));
        btnDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					memoryArray.remove(index);
					prefs.edit().putString("long_term_memory", memoryArray.toString()).apply();
					loadMemoryList();
					Toast.makeText(MemoryManageActivity.this, "已删除", Toast.LENGTH_SHORT).show();
				}
			});

        itemLayout.addView(tvContent);
        itemLayout.addView(btnEdit);
        itemLayout.addView(btnDelete);
        memoryListContainer.addView(itemLayout);
    }

    private void showEditDialog(final int index, final String oldContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(index == -1 ? "新增记忆" : "编辑记忆");

        final EditText input = new EditText(this);
        input.setText(oldContent);
        input.setTextColor(0xFF000000);
        builder.setView(input);

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newContent = input.getText().toString().trim();
					if (!newContent.isEmpty()) {
						try {
							if (index == -1) {
								JSONObject mem = new JSONObject();
								mem.put("content", newContent);
								mem.put("timestamp", System.currentTimeMillis());
								memoryArray.put(mem);
							} else {
								memoryArray.getJSONObject(index).put("content", newContent);
							}
							prefs.edit().putString("long_term_memory", memoryArray.toString()).apply();
							loadMemoryList();
						} catch (Exception e) {
							Toast.makeText(MemoryManageActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
