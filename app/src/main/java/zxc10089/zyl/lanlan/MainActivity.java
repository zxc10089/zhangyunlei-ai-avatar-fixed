package zxc10089.zyl.lanlan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int REQUEST_IMAGE = 1001;
    private static final int REQUEST_FILE = 1002;

    private DrawerLayout drawerLayout;
    private LinearLayout messageContainer;
    private ScrollView scrollView;
    private EditText etInput;
    private Button btnSend, btnMenu, btnSettings, btnAbout, btnImage, btnFile;
    private Switch switchDeepThink;
    private OkHttpClient client;
    private SharedPreferences prefs;
    private JSONArray messageHistory;
    private JSONArray longTermMemory;
    private Handler mainHandler;

    private String apiKey;
    private Uri aiAvatarUri, userAvatarUri, bgImageUri;
    private String systemPrompt;
    private String apiUrl;
    private String modelName;
    private float temperature, topP, frequencyPenalty, presencePenalty;
    private int maxTokens, contextLength;

    private Bitmap aiAvatarBitmap;
    private Bitmap userAvatarBitmap;

    private LinearLayout previewContainer;
    private ArrayList<AttachmentItem> attachmentList = new ArrayList<>();

    // 用于流式输出的临时视图引用（成员变量，无需final）
    private LinearLayout streamReasoningLayout;
    private TextView streamReasoningTextView;
    private LinearLayout streamContentLayout;
    private TextView streamContentTextView;
    private boolean streamInitialized;

    private static class AttachmentItem {
        String type;
        Uri uri;
        String base64;
        String mimeType;
        String fileName;
        String textContent;
        String imagePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        drawerLayout = findViewById(R.id.drawer_layout);
        messageContainer = findViewById(R.id.messageContainer);
        scrollView = findViewById(R.id.scrollView);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);
        btnMenu = findViewById(R.id.btnMenu);
        switchDeepThink = findViewById(R.id.switchDeepThink);
        btnSettings = findViewById(R.id.btnSettings);
        btnAbout = findViewById(R.id.btnAbout);
        btnImage = findViewById(R.id.btnImage);
        btnFile = findViewById(R.id.btnFile);
        previewContainer = findViewById(R.id.previewContainer);

        client = new OkHttpClient.Builder()
			.connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
			.readTimeout(0, java.util.concurrent.TimeUnit.SECONDS)
			.build();
        prefs = getSharedPreferences("app_config", MODE_PRIVATE);
        mainHandler = new Handler(Looper.getMainLooper());

        apiKey = prefs.getString("api_key", "");
        if (apiKey.isEmpty()) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        apiUrl = prefs.getString("api_url", "https://api.deepseek.com/v1/chat/completions");
        modelName = prefs.getString("model", "deepseek-v4-flash");
        temperature = prefs.getFloat("temperature", 1.0f);
        topP = prefs.getFloat("top_p", 1.0f);
        maxTokens = prefs.getInt("max_tokens", 4096);
        frequencyPenalty = prefs.getFloat("frequency_penalty", 0.0f);
        presencePenalty = prefs.getFloat("presence_penalty", 0.0f);
        contextLength = prefs.getInt("context_length", 20);
        String customPromptAddition = prefs.getString("custom_prompt", "");

        systemPrompt = getDefaultSystemPrompt();
        if (!customPromptAddition.isEmpty()) {
            systemPrompt += "\n\n" + customPromptAddition;
        }
        loadLongTermMemory();
        systemPrompt += "\n\n" + getMemoryText();

        loadAvatarsAndBackground();

        switchDeepThink.setChecked(prefs.getBoolean("deep_think", false));
        switchDeepThink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					prefs.edit().putBoolean("deep_think", isChecked).apply();
				}
			});

        loadHistory();

        btnSend.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { sendMessage(); }
			});
        btnImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, REQUEST_IMAGE);
				}
			});
        btnFile.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("*/*");
					startActivityForResult(intent, REQUEST_FILE);
				}
			});
        btnMenu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { drawerLayout.openDrawer(Gravity.START); }
			});
        btnSettings.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(MainActivity.this, SettingsActivity.class));
					drawerLayout.closeDrawers();
				}
			});
        btnAbout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(MainActivity.this, AboutActivity.class));
					drawerLayout.closeDrawers();
				}
			});
    }

    @Override
    protected void onResume() {
        super.onResume();
        modelName = prefs.getString("model", "deepseek-v4-flash");
        apiUrl = prefs.getString("api_url", "https://api.deepseek.com/v1/chat/completions");
        temperature = prefs.getFloat("temperature", 1.0f);
        topP = prefs.getFloat("top_p", 1.0f);
        maxTokens = prefs.getInt("max_tokens", 4096);
        frequencyPenalty = prefs.getFloat("frequency_penalty", 0.0f);
        presencePenalty = prefs.getFloat("presence_penalty", 0.0f);
        contextLength = prefs.getInt("context_length", 20);
        String customPromptAddition = prefs.getString("custom_prompt", "");
        systemPrompt = getDefaultSystemPrompt();
        if (!customPromptAddition.isEmpty()) {
            systemPrompt += "\n\n" + customPromptAddition;
        }
        loadLongTermMemory();
        systemPrompt += "\n\n" + getMemoryText();

        loadAvatarsAndBackground();
        if (messageHistory != null && messageHistory.length() > 0) {
            messageContainer.removeAllViews();
            final boolean hideReasoning = prefs.getBoolean("hide_reasoning", false);
            for (int i = 0; i < messageHistory.length(); i++) {
                try {
                    final JSONObject msg = messageHistory.getJSONObject(i);
                    final String role = msg.getString("role");
                    final String content = msg.getString("content");
                    final String imagePath = msg.optString("imagePath", null);
                    if ("user".equals(role)) {
                        if (imagePath != null && !imagePath.isEmpty()) {
                            addUserImageBubble(content, imagePath);
                        } else {
                            addBubbleView(role, content);
                        }
                    } else {
                        boolean isThinking = msg.optBoolean("isThinking", false) ||
							content.startsWith("【深度思考】") ||
							content.startsWith("\ud83d\udcad \u601d\u8003: ");
                        if (isThinking) {
                            if (!hideReasoning) {
                                addThinkingBubbleView(content);
                            }
                        } else {
                            addAIBubbleWithButtons(content);
                        }
                    }
                } catch (Exception ignored) {}
            }
            scrollToBottom();
        }
    }

    private void loadAvatarsAndBackground() {
        final String aiStr = prefs.getString("ai_avatar", "");
        final String userStr = prefs.getString("user_avatar", "");
        int defaultAiRes = R.drawable.ic_ai_avatar;
        int defaultUserRes = R.drawable.ic_user_avatar;

        if (!aiStr.isEmpty()) {
            try {
                aiAvatarBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(aiStr)));
                aiAvatarUri = Uri.parse(aiStr);
            } catch (Exception e) {
                aiAvatarBitmap = BitmapFactory.decodeResource(getResources(), defaultAiRes);
                aiAvatarUri = Uri.parse("android.resource://zxc10089.zyl.lanlan/" + defaultAiRes);
            }
        } else {
            aiAvatarBitmap = BitmapFactory.decodeResource(getResources(), defaultAiRes);
            aiAvatarUri = Uri.parse("android.resource://zxc10089.zyl.lanlan/" + defaultAiRes);
        }

        if (!userStr.isEmpty()) {
            try {
                userAvatarBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(userStr)));
                userAvatarUri = Uri.parse(userStr);
            } catch (Exception e) {
                userAvatarBitmap = BitmapFactory.decodeResource(getResources(), defaultUserRes);
                userAvatarUri = Uri.parse("android.resource://zxc10089.zyl.lanlan/" + defaultUserRes);
            }
        } else {
            userAvatarBitmap = BitmapFactory.decodeResource(getResources(), defaultUserRes);
            userAvatarUri = Uri.parse("android.resource://zxc10089.zyl.lanlan/" + defaultUserRes);
        }

        final String bgStr = prefs.getString("bg_image", "");
        if (!bgStr.isEmpty()) {
            bgImageUri = Uri.parse(bgStr);
            try {
                Bitmap bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(bgImageUri));
                Drawable d = new BitmapDrawable(getResources(), bmp);
                findViewById(R.id.main_content).setBackground(d);
            } catch (Exception ignored) {}
        } else {
            findViewById(R.id.main_content).setBackgroundColor(Color.parseColor("#1C1C1E"));
        }
    }

    private String getDefaultSystemPrompt() {
        return Prompt.getSystemPrompt();
    }

    private void loadLongTermMemory() {
        String json = prefs.getString("long_term_memory", "[]");
        try {
            longTermMemory = new JSONArray(json);
        } catch (Exception e) {
            longTermMemory = new JSONArray();
        }
    }

    private String getMemoryText() {
        if (longTermMemory.length() == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("【重要的长期记忆，你必须永远记住】\n");
        for (int i = 0; i < longTermMemory.length(); i++) {
            try {
                JSONObject mem = longTermMemory.getJSONObject(i);
                sb.append("- ").append(mem.getString("content")).append("\n");
            } catch (Exception e) {}
        }
        return sb.toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("AI回复", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    private void addMemory(final String content) {
        try {
            JSONObject memory = new JSONObject();
            memory.put("content", content);
            memory.put("timestamp", System.currentTimeMillis());
            longTermMemory.put(memory);
            prefs.edit().putString("long_term_memory", longTermMemory.toString()).apply();
            Toast.makeText(this, "已添加到记忆", Toast.LENGTH_SHORT).show();
            systemPrompt = getDefaultSystemPrompt() + "\n\n" + getMemoryText();
            String customPromptAddition = prefs.getString("custom_prompt", "");
            if (!customPromptAddition.isEmpty()) {
                systemPrompt += "\n\n" + customPromptAddition;
            }
        } catch (Exception e) {
            Toast.makeText(this, "添加记忆失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreview() {
        previewContainer.removeAllViews();
        for (int i = 0; i < attachmentList.size(); i++) {
            final AttachmentItem item = attachmentList.get(i);
            final int index = i;

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setPadding(8, 4, 8, 4);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 12, 0);
            itemLayout.setLayoutParams(params);

            if ("image".equals(item.type)) {
                final ImageView thumb = new ImageView(this);
                thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
                thumb.setAdjustViewBounds(true);

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    InputStream is = getContentResolver().openInputStream(item.uri);
                    BitmapFactory.decodeStream(is, null, options);
                    is.close();

                    int originalWidth = options.outWidth;
                    int originalHeight = options.outHeight;

                    int maxWidth = 200;
                    int maxHeight = 150;
                    int displayWidth, displayHeight;

                    if (originalWidth > 0 && originalHeight > 0) {
                        float ratio = (float) originalWidth / originalHeight;
                        if (ratio > 1) {
                            displayWidth = maxWidth;
                            displayHeight = (int) (maxWidth / ratio);
                        } else {
                            displayHeight = maxHeight;
                            displayWidth = (int) (maxHeight * ratio);
                        }
                    } else {
                        displayWidth = 120;
                        displayHeight = 120;
                    }

                    LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(displayWidth, displayHeight);
                    thumbParams.setMargins(0, 0, 8, 0);
                    thumb.setLayoutParams(thumbParams);

                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), item.uri);
                    thumb.setImageBitmap(bmp);
                } catch (Exception e) {
                    LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(120, 120);
                    thumbParams.setMargins(0, 0, 8, 0);
                    thumb.setLayoutParams(thumbParams);
                    thumb.setBackgroundColor(Color.GRAY);
                }
                itemLayout.addView(thumb);
            } else {
                TextView fileView = new TextView(this);
                fileView.setText("📄 " + item.fileName);
                fileView.setTextColor(Color.WHITE);
                fileView.setTextSize(12f);
                fileView.setMaxWidth(200);
                fileView.setEllipsize(TextUtils.TruncateAt.END);
                itemLayout.addView(fileView);
            }

            Button removeBtn = new Button(this);
            removeBtn.setText("✕");
            removeBtn.setTextSize(12f);
            removeBtn.setBackgroundColor(Color.TRANSPARENT);
            removeBtn.setTextColor(Color.parseColor("#FF6B6B"));
            removeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						attachmentList.remove(index);
						updatePreview();
					}
				});
            itemLayout.addView(removeBtn);

            previewContainer.addView(itemLayout);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;

        final Uri uri = data.getData();
        final AttachmentItem item = new AttachmentItem();
        item.uri = uri;

        if (requestCode == REQUEST_IMAGE) {
            item.type = "image";
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                byte[] imageBytes = baos.toByteArray();
                item.base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                item.mimeType = "image/jpeg";
            } catch (Exception e) {
                Toast.makeText(this, "读取图片失败", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (requestCode == REQUEST_FILE) {
            item.type = "file";
            item.fileName = getFileName(uri);
            String text = readTextFile(uri);
            if (text != null) {
                item.textContent = text;
            } else {
                item.textContent = "[无法读取文件内容]";
            }
        }
        attachmentList.add(item);
        updatePreview();
    }

    private String getFileName(Uri uri) {
        String name = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) name = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return name != null ? name : "file";
    }

    private String readTextFile(Uri uri) {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            String content = baos.toString("UTF-8");
            if (content.length() > 2000) {
                content = content.substring(0, 2000) + "\n... (内容过长，已截断)";
            }
            return content;
        } catch (Exception e) {
            return null;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void sendMessage() {
        final String userMessage = etInput.getText().toString().trim();
        if (userMessage.isEmpty() && attachmentList.isEmpty()) return;

        final ArrayList<AttachmentItem> sendingAttachments = new ArrayList<>(attachmentList);
        attachmentList.clear();
        updatePreview();

        String displayMessage = userMessage;
        String apiMessage = userMessage;
        String imagePath = null;
        String fileText = null;
        String base64 = null;
        String mimeType = null;

        if (!sendingAttachments.isEmpty()) {
            AttachmentItem firstAttachment = sendingAttachments.get(0);
            if ("image".equals(firstAttachment.type)) {
                base64 = firstAttachment.base64;
                mimeType = firstAttachment.mimeType;
                try {
                    File dir = new File(getFilesDir(), "sent_images");
                    if (!dir.exists()) dir.mkdirs();
                    String fileName = "img_" + System.currentTimeMillis() + ".jpg";
                    File outFile = new File(dir, fileName);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    fos.write(decoded);
                    fos.close();
                    imagePath = outFile.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                displayMessage = userMessage + (userMessage.isEmpty() ? "" : "\n") + "📷 [图片]";
            } else if ("file".equals(firstAttachment.type)) {
                fileText = firstAttachment.textContent;
                displayMessage = userMessage + (userMessage.isEmpty() ? "" : "\n") + "📄 [文件: " + firstAttachment.fileName + "]";
                apiMessage = userMessage.isEmpty() ? fileText : userMessage + "\n" + fileText;
            }
        }

        if (imagePath != null) {
            addUserImageBubble(userMessage, imagePath);
        } else {
            addBubbleView("user", displayMessage);
        }
        addMessageToHistory("user", displayMessage, false, imagePath);

        etInput.setText("");
        scrollToBottom();

        final boolean isDeepThinkOn = switchDeepThink.isChecked();
        String actualModel = modelName;
        if (base64 != null) {
            // For vision tasks, if current model isn't a known vision model, use deepseek-vision
            if (!modelName.contains("vision") && !modelName.equals("deepseek-chat")) {
                actualModel = "deepseek-vision";
            }
        }

        final JSONObject body = buildRequestBody(apiMessage, isDeepThinkOn, base64, mimeType, fileText, actualModel);
        if (body != null) {
            boolean streamEnabled = prefs.getBoolean("stream", true);
            if (base64 != null) streamEnabled = false;
            try { body.put("stream", streamEnabled); } catch (Exception e) {}
            if (streamEnabled) {
                sendStreamRequest(body);
            } else {
                sendNormalRequest(body);
            }
        }
    }

    private JSONObject buildRequestBody(final String userMessage, final boolean isDeepThinkOn,
                                        final String base64, final String mimeType, final String fileText, final String overrideModel) {
        final JSONObject body = new JSONObject();
        try {
            String useModel = overrideModel != null ? overrideModel : modelName;
            body.put("model", useModel);

            JSONObject thinkingConfig = new JSONObject();
            if (isDeepThinkOn) {
                thinkingConfig.put("type", "enabled");
            } else {
                thinkingConfig.put("type", "disabled");
            }
            body.put("thinking", thinkingConfig);

            if (!isDeepThinkOn) {
                body.put("temperature", temperature);
                body.put("top_p", topP);
                body.put("frequency_penalty", frequencyPenalty);
                body.put("presence_penalty", presencePenalty);
            }

            body.put("max_tokens", maxTokens);

            final JSONArray messages = new JSONArray();

            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.put(sysMsg);

            final int historyLength = messageHistory.length();
            final int maxHistory = Math.min(contextLength, historyLength);
            final int start = Math.max(0, historyLength - maxHistory);
            for (int i = start; i < historyLength; i++) {
                try {
                    final JSONObject histMsg = messageHistory.getJSONObject(i);
                    String role = histMsg.getString("role");
                    String content = histMsg.getString("content");
                    boolean isThinking = histMsg.optBoolean("isThinking", false);
                    if (!isThinking && !content.startsWith("\u274c ")) {
                        JSONObject hMsg = new JSONObject();
                        hMsg.put("role", role);
                        hMsg.put("content", content);
                        messages.put(hMsg);
                    }
                } catch (Exception ignored) {}
            }

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");

            if (base64 != null) {
                JSONArray contentArray = new JSONArray();
                if (!userMessage.isEmpty()) {
                    JSONObject textPart = new JSONObject();
                    textPart.put("type", "text");
                    textPart.put("text", userMessage);
                    contentArray.put(textPart);
                }
                JSONObject imagePart = new JSONObject();
                imagePart.put("type", "image_url");
                JSONObject imageUrlObj = new JSONObject();
                imageUrlObj.put("url", "data:" + mimeType + ";base64," + base64);
                imagePart.put("image_url", imageUrlObj);
                contentArray.put(imagePart);
                userMsg.put("content", contentArray);
            } else if (fileText != null) {
                userMsg.put("content", userMessage.isEmpty() ? fileText : userMessage + "\n" + fileText);
            } else {
                userMsg.put("content", userMessage);
            }
            messages.put(userMsg);

            body.put("messages", messages);
        } catch (Exception e) {
            return null;
        }
        return body;
    }

    private void addUserImageBubble(final String text, final String imagePath) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapParams.setMargins(0, 8, 0, 8);
        wrapper.setLayoutParams(wrapParams);
        wrapper.setGravity(Gravity.END);

        if (text != null && !text.isEmpty()) {
            TextView textView = createBubbleText(text, true);
            textView.setGravity(Gravity.END);
            wrapper.addView(textView);
        }

        final ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
			(int)(getResources().getDisplayMetrics().widthPixels * 0.6),
			ViewGroup.LayoutParams.WRAP_CONTENT);
        imgParams.setMargins(0, 4, 0, 0);
        imgParams.gravity = Gravity.END;
        imageView.setLayoutParams(imgParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showImageFullscreen(imagePath);
				}
			});
        wrapper.addView(imageView);
        messageContainer.addView(wrapper);
    }

    private void showImageFullscreen(final String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.setView(imageView);
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
        builder.show();
    }

    private void sendStreamRequest(final JSONObject requestBody) {
        Request request = new Request.Builder()
			.url(apiUrl)
			.header("Authorization", "Bearer " + apiKey)
			.header("Content-Type", "application/json")
			.post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
			.build();

        // 重置流式视图引用
        streamReasoningLayout = null;
        streamReasoningTextView = null;
        streamContentLayout = null;
        streamContentTextView = null;
        streamInitialized = false;

        client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, final IOException e) {
					mainHandler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(MainActivity.this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						});
				}

				@Override
				public void onResponse(Call call, final Response response) throws IOException {
					if (!response.isSuccessful()) {
						mainHandler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(MainActivity.this, "请求失败: " + response.code(), Toast.LENGTH_SHORT).show();
								}
							});
						return;
					}
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
						String line;
						final StringBuilder currentContent = new StringBuilder();
						final StringBuilder currentReasoning = new StringBuilder();
						final boolean isDeepThinkOn = switchDeepThink.isChecked();
						final boolean hideReasoning = prefs.getBoolean("hide_reasoning", false);

						while ((line = reader.readLine()) != null) {
							if (line.startsWith("data: ")) {
								final String data = line.substring(6).trim();
								if ("[DONE]".equals(data)) break;
								try {
									JSONObject chunk = new JSONObject(data);
									JSONArray choices = chunk.getJSONArray("choices");
									if (choices.length() > 0) {
										JSONObject delta = choices.getJSONObject(0).optJSONObject("delta");
										if (delta == null) continue;
										final String reasoning = delta.optString("reasoning_content", null);
										final String content = delta.optString("content", null);
										if (isDeepThinkOn && reasoning != null && !reasoning.isEmpty() && !"null".equals(reasoning.trim())) {
											currentReasoning.append(reasoning);
										}
										if (content != null && !content.isEmpty() && !"null".equals(content.trim())) {
											currentContent.append(content);
										}
										mainHandler.post(new Runnable() {
												@Override
												public void run() {
													if (!streamInitialized) {
														if (isDeepThinkOn && !hideReasoning) {
															streamReasoningLayout = createThinkingBubbleLayout("");
															streamReasoningTextView = getThinkingTextView(streamReasoningLayout);
															messageContainer.addView(streamReasoningLayout);
														}
														streamContentLayout = createAIBubbleLayout("");
														streamContentTextView = (TextView) streamContentLayout.getChildAt(1);
														messageContainer.addView(streamContentLayout);
														streamInitialized = true;
													}
													if (streamReasoningTextView != null) {
														streamReasoningTextView.setText(currentReasoning.toString());
													}
													if (streamContentTextView != null) {
														streamContentTextView.setText(currentContent.toString());
													}
													scrollToBottom();
												}
											});
									}
								} catch (Exception e) { e.printStackTrace(); }
							}
						}

						final String finalReasoning = currentReasoning.toString().trim();
						final String finalContent = currentContent.toString().trim();

						mainHandler.post(new Runnable() {
								@Override
								public void run() {
									if (streamReasoningLayout != null) {
										if (hideReasoning || finalReasoning.isEmpty()) {
											messageContainer.removeView(streamReasoningLayout);
										} else {
											streamReasoningTextView.setText(finalReasoning);
											addMessageToHistory("assistant", finalReasoning, true);
										}
									}
									if (streamContentLayout != null) {
										messageContainer.removeView(streamContentLayout);
										if (!finalContent.isEmpty()) {
											addAIBubbleWithButtons(finalContent);
											addMessageToHistory("assistant", finalContent, false);
										}
									}
									scrollToBottom();
								}
							});
					} finally {
						if (reader != null) {
							try { reader.close(); } catch (Exception ignored) {}
						}
					}
				}
			});
    }

    private void sendNormalRequest(final JSONObject requestBody) {
        Request request = new Request.Builder()
			.url(apiUrl)
			.header("Authorization", "Bearer " + apiKey)
			.header("Content-Type", "application/json")
			.post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
			.build();

        client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, final IOException e) {
					mainHandler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(MainActivity.this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						});
				}

				@Override
				public void onResponse(Call call, final Response response) throws IOException {
					if (!response.isSuccessful()) {
						mainHandler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(MainActivity.this, "请求失败: " + response.code(), Toast.LENGTH_SHORT).show();
								}
							});
						return;
					}
					try {
						String bodyStr = response.body().string();
						final JSONObject json = new JSONObject(bodyStr);
						final JSONArray choices = json.getJSONArray("choices");
						final JSONObject message = choices.getJSONObject(0).getJSONObject("message");
						final String reply = message.getString("content");
						final boolean isDeepThinkOn = switchDeepThink.isChecked();
						final boolean hideReasoning = prefs.getBoolean("hide_reasoning", false);
						if (isDeepThinkOn && message.has("reasoning_content")) {
							final String reasoning = message.getString("reasoning_content");
							mainHandler.post(new Runnable() {
									@Override
									public void run() {
										if (!hideReasoning) {
											addThinkingBubbleView(reasoning);
											addMessageToHistory("assistant", reasoning, true);
										}
										addAIBubbleWithButtons(reply);
										addMessageToHistory("assistant", reply, false);
										scrollToBottom();
									}
								});
						} else {
							mainHandler.post(new Runnable() {
									@Override
									public void run() {
										addAIBubbleWithButtons(reply);
										addMessageToHistory("assistant", reply, false);
										scrollToBottom();
									}
								});
						}
					} catch (final Exception e) {
						mainHandler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(MainActivity.this, "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							});
					}
				}
			});
    }

    private void loadHistory() {
        final String json = prefs.getString("messages", "[]");
        try {
            messageHistory = new JSONArray(json);
        } catch (Exception e) {
            messageHistory = new JSONArray();
        }
        messageContainer.removeAllViews();
        final boolean hideReasoning = prefs.getBoolean("hide_reasoning", false);
        for (int i = 0; i < messageHistory.length(); i++) {
            try {
                final JSONObject msg = messageHistory.getJSONObject(i);
                final String role = msg.getString("role");
                final String content = msg.getString("content");
                final String imagePath = msg.optString("imagePath", null);
                if ("user".equals(role)) {
                    if (imagePath != null && !imagePath.isEmpty()) {
                        addUserImageBubble(content, imagePath);
                    } else {
                        addBubbleView(role, content);
                    }
                } else {
                    boolean isThinking = msg.optBoolean("isThinking", false) ||
						content.startsWith("【深度思考】") ||
						content.startsWith("\ud83d\udcad \u601d\u8003: ");
                    if (isThinking) {
                        if (!hideReasoning) {
                            addThinkingBubbleView(content);
                        }
                    } else {
                        addAIBubbleWithButtons(content);
                    }
                }
            } catch (Exception ignored) {}
        }
        scrollToBottom();
    }

    private void saveHistory() {
        prefs.edit().putString("messages", messageHistory.toString()).apply();
    }

    private void addAIBubbleWithButtons(final String content) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapParams.setMargins(0, 8, 0, 8);
        wrapper.setLayoutParams(wrapParams);

        LinearLayout bubbleRow = new LinearLayout(this);
        bubbleRow.setLayoutParams(new LinearLayout.LayoutParams(
									  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        bubbleRow.setOrientation(LinearLayout.HORIZONTAL);
        bubbleRow.setGravity(Gravity.START);

        ImageView avatar = createAvatarView(true);
        TextView tv = createContentTextView(content);

        bubbleRow.addView(avatar);
        bubbleRow.addView(tv);
        bubbleRow.addView(createSpacer());
        wrapper.addView(bubbleRow);

        LinearLayout btnRow = new LinearLayout(this);
        LinearLayout.LayoutParams btnRowParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnRowParams.setMargins(92, 4, 0, 0);
        btnRow.setLayoutParams(btnRowParams);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        Button copyBtn = new Button(this);
        copyBtn.setText("复制");
        copyBtn.setTextSize(12f);
        copyBtn.setPadding(8, 2, 8, 2);
        copyBtn.setBackgroundColor(Color.TRANSPARENT);
        copyBtn.setTextColor(Color.parseColor("#AAAAAA"));
        copyBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { copyToClipboard(content); }
			});

        Button memoryBtn = new Button(this);
        memoryBtn.setText("添加记忆");
        memoryBtn.setTextSize(12f);
        memoryBtn.setPadding(8, 2, 8, 2);
        memoryBtn.setBackgroundColor(Color.TRANSPARENT);
        memoryBtn.setTextColor(Color.parseColor("#AAAAAA"));
        memoryBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { addMemory(content); }
			});

        btnRow.addView(copyBtn);
        btnRow.addView(memoryBtn);
        wrapper.addView(btnRow);

        messageContainer.addView(wrapper);
    }

    private void addThinkingBubbleView(String content) {
        LinearLayout bubbleRow = createThinkingBubbleLayout(content);
        messageContainer.addView(bubbleRow);
    }

    private LinearLayout createThinkingBubbleLayout(String content) {
        LinearLayout bubbleRow = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 4, 0, 4);
        bubbleRow.setLayoutParams(rowParams);
        bubbleRow.setOrientation(LinearLayout.HORIZONTAL);
        bubbleRow.setGravity(Gravity.START);

        ImageView transparentAvatar = new ImageView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(80, 80);
        transparentAvatar.setLayoutParams(avatarParams);
        transparentAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable transparentBg = new GradientDrawable();
        transparentBg.setShape(GradientDrawable.OVAL);
        transparentBg.setColor(Color.TRANSPARENT);
        transparentAvatar.setBackground(transparentBg);

        TextView tv = new TextView(this);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(12, 0, 12, 0);
        tv.setLayoutParams(tvParams);
        tv.setText(content);
        tv.setTextSize(14f);
        tv.setPadding(24, 12, 24, 12);
        tv.setMaxWidth((int)(getResources().getDisplayMetrics().widthPixels * 0.55));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(24f);
        bg.setColor(Color.parseColor("#3A3A3C"));
        tv.setTextColor(Color.parseColor("#AAAAAA"));
        tv.setBackground(bg);

        bubbleRow.addView(transparentAvatar);
        bubbleRow.addView(tv);
        bubbleRow.addView(createSpacer());
        return bubbleRow;
    }

    private TextView getThinkingTextView(LinearLayout layout) {
        if (layout.getChildCount() >= 2) {
            return (TextView) layout.getChildAt(1);
        }
        return null;
    }

    private LinearLayout createAIBubbleLayout(String content) {
        LinearLayout bubbleRow = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 8, 0, 4);
        bubbleRow.setLayoutParams(rowParams);
        bubbleRow.setOrientation(LinearLayout.HORIZONTAL);
        bubbleRow.setGravity(Gravity.START);

        ImageView avatar = createAvatarView(true);
        TextView tv = createContentTextView(content);

        bubbleRow.addView(avatar);
        bubbleRow.addView(tv);
        bubbleRow.addView(createSpacer());
        return bubbleRow;
    }

    private ImageView createAvatarView(boolean isAI) {
        ImageView avatar = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
        avatar.setLayoutParams(params);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0xFF3A3A3C);
        bg.setStroke(3, 0xFF5A5A5C); // Add border
        avatar.setBackground(bg);
        if (isAI && aiAvatarBitmap != null) {
            avatar.setImageBitmap(aiAvatarBitmap);
        } else if (!isAI && userAvatarBitmap != null) {
            avatar.setImageBitmap(userAvatarBitmap);
        }
        return avatar;
    }

    private TextView createContentTextView(String text) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(12, 0, 12, 0);
        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setTextSize(16f);
        tv.setPadding(24, 12, 24, 12);
        tv.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.5));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(24f);
        bg.setColor(Color.parseColor("#2C2C2E"));
        tv.setTextColor(Color.parseColor("#E5E5E7"));
        tv.setBackground(bg);
        return tv;
    }

    private void addBubbleView(String role, String content) {
        final LinearLayout bubbleLayout = new LinearLayout(this);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 8, 0, 8);
        bubbleLayout.setLayoutParams(layoutParams);
        bubbleLayout.setOrientation(LinearLayout.HORIZONTAL);

        final ImageView avatar = createAvatarView(!role.equals("user")); // Use createAvatarView for consistent style

        if ("user".equals(role)) {
            bubbleLayout.setGravity(Gravity.END);
            if (userAvatarBitmap != null) avatar.setImageBitmap(userAvatarBitmap);
            bubbleLayout.addView(createSpacer());
            bubbleLayout.addView(createBubbleText(content, true));
            bubbleLayout.addView(avatar);
        } else {
            bubbleLayout.setGravity(Gravity.START);
            if (aiAvatarBitmap != null) avatar.setImageBitmap(aiAvatarBitmap);
            bubbleLayout.addView(avatar);
            bubbleLayout.addView(createBubbleText(content, false));
            bubbleLayout.addView(createSpacer());
        }
        messageContainer.addView(bubbleLayout);
    }

    private View createSpacer() {
        final View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        return spacer;
    }

    private TextView createBubbleText(String content, boolean isUser) {
        final TextView tv = new TextView(this);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(12, 0, 12, 0);
        tv.setLayoutParams(params);
        tv.setText(content);
        tv.setTextSize(16f);
        tv.setPadding(24, 12, 24, 12);
        tv.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.5));
        final GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(24f);
        if (isUser) {
            bg.setColor(Color.parseColor("#0A84FF"));
            tv.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.parseColor("#2C2C2E"));
            tv.setTextColor(Color.parseColor("#E5E5E7"));
        }
        tv.setBackground(bg);
        return tv;
    }

    private void addMessageToHistory(String role, String content, boolean isThinking, String imagePath) {
        try {
            final JSONObject msg = new JSONObject();
            msg.put("role", role);
            msg.put("content", content);
            msg.put("isThinking", isThinking);
            if (imagePath != null) {
                msg.put("imagePath", imagePath);
            }
            messageHistory.put(msg);
            saveHistory();
        } catch (Exception ignored) {}
    }

    private void addMessageToHistory(String role, String content, boolean isThinking) {
        addMessageToHistory(role, content, isThinking, null);
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
				@Override
				public void run() {
					scrollView.fullScroll(View.FOCUS_DOWN);
				}
			});
    }
}
