package zxc10089.zyl.lanlan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SettingsActivity extends Activity {

    private static final int REQUEST_BACKUP = 300;
    private static final int REQUEST_RESTORE = 301;

    private Button btnPersonalization, btnAiSettings, btnMemoryManage;
    private Button btnBackup, btnRestore, btnClearHistory, btnReset;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        prefs = getSharedPreferences("app_config", MODE_PRIVATE);

        btnPersonalization = findViewById(R.id.btnPersonalization);
        btnAiSettings = findViewById(R.id.btnAiSettings);
        btnMemoryManage = findViewById(R.id.btnMemoryManage);
        btnBackup = findViewById(R.id.btnBackup);
        btnRestore = findViewById(R.id.btnRestore);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnReset = findViewById(R.id.btnResetWizard);

        if (btnPersonalization != null) {
            btnPersonalization.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(SettingsActivity.this, PersonalizationActivity.class));
					}
				});
        }
        if (btnAiSettings != null) {
            btnAiSettings.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(SettingsActivity.this, AiSettingsActivity.class));
					}
				});
        }
        if (btnMemoryManage != null) {
            btnMemoryManage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(SettingsActivity.this, MemoryManageActivity.class));
					}
				});
        }
        if (btnBackup != null) {
            btnBackup.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setType("application/json");
						intent.putExtra(Intent.EXTRA_TITLE, "张云雷分身_聊天记录_" + System.currentTimeMillis() + ".json");
						startActivityForResult(intent, REQUEST_BACKUP);
					}
				});
        }
        if (btnRestore != null) {
            btnRestore.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setType("application/json");
						startActivityForResult(intent, REQUEST_RESTORE);
					}
				});
        }
        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(SettingsActivity.this)
							.setTitle("确认清除")
							.setMessage("确定要清除所有聊天记录吗？此操作不可恢复。")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									prefs.edit().putString("messages", "[]").apply();
									Toast.makeText(SettingsActivity.this, "聊天记录已清除", Toast.LENGTH_SHORT).show();
								}
							})
							.setNegativeButton("取消", null)
							.show();
					}
				});
        }
        if (btnReset != null) {
            btnReset.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("configured", false);
						editor.apply();
						Intent intent = new Intent(SettingsActivity.this, SetupActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				});
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        final Uri uri = data.getData();
        if (requestCode == REQUEST_BACKUP) {
            final String historyJson = prefs.getString("messages", "[]");
            try {
                OutputStream os = getContentResolver().openOutputStream(uri);
                os.write(historyJson.getBytes("UTF-8"));
                os.close();
                Toast.makeText(this, "聊天记录已备份", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "备份失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_RESTORE) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                is.close();
                String jsonStr = sb.toString().trim();
                new org.json.JSONArray(jsonStr);
                prefs.edit().putString("messages", jsonStr).apply();
                Toast.makeText(this, "聊天记录已恢复，请返回主界面查看", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "恢复失败，文件格式不正确: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
