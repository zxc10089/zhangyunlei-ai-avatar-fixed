package zxc10089.zyl.lanlan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PersonalizationActivity extends Activity {

    private static final int REQUEST_AI_AVATAR = 200;
    private static final int REQUEST_USER_AVATAR = 201;
    private static final int REQUEST_BACKGROUND = 202;

    private ImageView ivAiAvatar, ivUserAvatar, ivBgPreview;
    private Uri aiAvatarUri, userAvatarUri, backgroundUri;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personalization);

        prefs = getSharedPreferences("app_config", MODE_PRIVATE);

        ivAiAvatar = findViewById(R.id.ivAiAvatar);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        ivBgPreview = findViewById(R.id.ivBgPreview);
        final Button btnAiAvatar = findViewById(R.id.btnAiAvatar);
        final Button btnUserAvatar = findViewById(R.id.btnUserAvatar);
        final Button btnBackground = findViewById(R.id.btnBackground);
        final Button btnBack = findViewById(R.id.btnBack);

        final String aiStr = prefs.getString("ai_avatar", "");
        final String userStr = prefs.getString("user_avatar", "");
        if (!aiStr.isEmpty()) {
            aiAvatarUri = Uri.parse(aiStr + "?t=" + System.currentTimeMillis());
            ivAiAvatar.setImageURI(aiAvatarUri);
        }
        if (!userStr.isEmpty()) {
            userAvatarUri = Uri.parse(userStr + "?t=" + System.currentTimeMillis());
            ivUserAvatar.setImageURI(userAvatarUri);
        }

        final String bgStr = prefs.getString("bg_image", "");
        if (!bgStr.isEmpty()) {
            backgroundUri = Uri.parse(bgStr);
            setBackgroundPreview(backgroundUri);
        }

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
        btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
    }

    private void setBackgroundPreview(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap raw = BitmapFactory.decodeStream(is);
            is.close();
            if (raw != null) {
                Bitmap rotated = rotateBitmapByExif(uri, raw);
                ivBgPreview.setImageBitmap(rotated);
                ivBgPreview.invalidate();
                ivBgPreview.requestLayout();
            }
        } catch (Exception e) {
            ivBgPreview.setImageURI(uri);
            ivBgPreview.invalidate();
            ivBgPreview.requestLayout();
        }
    }

    private Bitmap rotateBitmapByExif(Uri uri, Bitmap bitmap) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(is);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            is.close();
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return bitmap;
        }
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
                final SharedPreferences.Editor editor = prefs.edit();

                if (requestCode == REQUEST_AI_AVATAR) {
                    aiAvatarUri = savedUri;
                    ivAiAvatar.setImageURI(Uri.parse(savedUri.toString() + "?t=" + System.currentTimeMillis()));
                    editor.putString("ai_avatar", savedUri.toString());
                } else if (requestCode == REQUEST_USER_AVATAR) {
                    userAvatarUri = savedUri;
                    ivUserAvatar.setImageURI(Uri.parse(savedUri.toString() + "?t=" + System.currentTimeMillis()));
                    editor.putString("user_avatar", savedUri.toString());
                } else if (requestCode == REQUEST_BACKGROUND) {
                    backgroundUri = savedUri;
                    setBackgroundPreview(savedUri);
                    editor.putString("bg_image", savedUri.toString());
                }
                editor.apply();
            } catch (Exception e) {
                Toast.makeText(this, "读取图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
