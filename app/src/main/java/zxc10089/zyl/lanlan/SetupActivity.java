package zxc10089.zyl.lanlan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class SetupActivity extends Activity {

    private static final int REQUEST_AI_AVATAR = 100;
    private static final int REQUEST_USER_AVATAR = 101;
    private static final int REQUEST_BACKGROUND = 102;

    private EditText etApiKey;
    private Button btnVerify;
    private ProgressBar progressVerify;
    private TextView tvApiStatus;
    private LinearLayout llCustomize;
    private ImageView ivAiAvatar, ivUserAvatar;
    private Button btnAiAvatar, btnUserAvatar, btnBackground, btnFinish;

    private Uri aiAvatarUri, userAvatarUri, backgroundUri;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup);

        prefs = getSharedPreferences("app_config", MODE_PRIVATE);

        if (prefs.getBoolean("configured", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etApiKey = findViewById(R.id.etApiKey);
        btnVerify = findViewById(R.id.btnVerify);
        progressVerify = findViewById(R.id.progressVerify);
        tvApiStatus = findViewById(R.id.tvApiStatus);
        llCustomize = findViewById(R.id.llCustomize);
        ivAiAvatar = findViewById(R.id.ivAiAvatar);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        btnAiAvatar = findViewById(R.id.btnAiAvatar);
        btnUserAvatar = findViewById(R.id.btnUserAvatar);
        btnBackground = findViewById(R.id.btnBackground);
        btnFinish = findViewById(R.id.btnFinish);

        ivAiAvatar.setImageResource(R.drawable.ic_ai_avatar);
        ivUserAvatar.setImageResource(R.drawable.ic_user_avatar);

        btnVerify.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					verifyApiKey();
				}
			});

        btnAiAvatar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, REQUEST_AI_AVATAR);
				}
			});
        btnUserAvatar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, REQUEST_USER_AVATAR);
				}
			});
        btnBackground.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, REQUEST_BACKGROUND);
				}
			});
        btnFinish.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveAndFinish();
				}
			});
    }

    private void verifyApiKey() {
        final String key = etApiKey.getText().toString().trim();
        if (key.isEmpty()) {
            tvApiStatus.setText("请输入 API Key");
            return;
        }

        progressVerify.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        final JSONObject body = new JSONObject();
        try {
            body.put("model", "deepseek-chat");
            final JSONArray messages = new JSONArray();
            final JSONObject msg = new JSONObject();
            msg.put("role", "user");
            msg.put("content", "Hello");
            messages.put(msg);
            body.put("messages", messages);
        } catch (Exception e) {}

        final Request request = new Request.Builder()
			.url("https://api.deepseek.com/v1/chat/completions")
			.header("Authorization", "Bearer " + key)
			.header("Content-Type", "application/json")
			.post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
			.build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(final Call call, final IOException e) {
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progressVerify.setVisibility(View.GONE);
								btnVerify.setEnabled(true);
								tvApiStatus.setText("网络错误: " + e.getMessage());
							}
						});
				}

				@Override
				public void onResponse(final Call call, final Response response) throws IOException {
					final boolean isValid = response.code() == 200;
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progressVerify.setVisibility(View.GONE);
								btnVerify.setEnabled(true);
								if (isValid) {
									tvApiStatus.setText("✅ 密钥有效！");
									tvApiStatus.setTextColor(0xFF4CAF50);
									prefs.edit().putString("api_key", key).apply();
									llCustomize.setVisibility(View.VISIBLE);
									btnVerify.setVisibility(View.GONE);
									etApiKey.setEnabled(false);
								} else {
									tvApiStatus.setText("❌ 密钥无效，请检查");
								}
							}
						});
				}
			});
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            final Uri uri = data.getData();
            try {
                final InputStream is = getContentResolver().openInputStream(uri);
                final File dir = new File(getFilesDir(), "avatars");
                if (!dir.exists()) dir.mkdirs();
                final File outFile = new File(dir, requestCode + ".png");
                final FileOutputStream fos = new FileOutputStream(outFile);
                final byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
                final Uri savedUri = Uri.fromFile(outFile);
                if (requestCode == REQUEST_AI_AVATAR) {
                    aiAvatarUri = savedUri;
                    ivAiAvatar.setImageURI(aiAvatarUri);
                } else if (requestCode == REQUEST_USER_AVATAR) {
                    userAvatarUri = savedUri;
                    ivUserAvatar.setImageURI(userAvatarUri);
                } else if (requestCode == REQUEST_BACKGROUND) {
                    backgroundUri = savedUri;
                    Toast.makeText(SetupActivity.this, "背景已选择", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(SetupActivity.this, "读取图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAndFinish() {
        final SharedPreferences.Editor editor = prefs.edit();
        if (aiAvatarUri != null) editor.putString("ai_avatar", aiAvatarUri.toString());
        if (userAvatarUri != null) editor.putString("user_avatar", userAvatarUri.toString());
        if (backgroundUri != null) editor.putString("bg_image", backgroundUri.toString());
        editor.putBoolean("configured", true);
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
