package zxc10089.zyl.lanlan;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AiSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_settings);

        final EditText etApiKey = findViewById(R.id.etApiKey);
        final EditText etApiUrl = findViewById(R.id.etApiUrl);
        final Spinner spinnerModel = findViewById(R.id.spinnerModel);
        final SeekBar seekBarTemperature = findViewById(R.id.seekBarTemperature);
        final TextView tvTemperature = findViewById(R.id.tvTemperature);
        final SeekBar seekBarTopP = findViewById(R.id.seekBarTopP);
        final TextView tvTopP = findViewById(R.id.tvTopP);
        final EditText etMaxTokens = findViewById(R.id.etMaxTokens);
        final SeekBar seekBarFreqPenalty = findViewById(R.id.seekBarFreqPenalty);
        final TextView tvFreqPenalty = findViewById(R.id.tvFreqPenalty);
        final SeekBar seekBarPresPenalty = findViewById(R.id.seekBarPresPenalty);
        final TextView tvPresPenalty = findViewById(R.id.tvPresPenalty);
        final SeekBar seekBarContext = findViewById(R.id.seekBarContext);
        final TextView tvContext = findViewById(R.id.tvContext);
        final Switch switchDeepThinkDefault = findViewById(R.id.switchDeepThinkDefault);
        final Switch switchHideReasoning = findViewById(R.id.switchHideReasoning);
        final Switch switchStream = findViewById(R.id.switchStream);
        final EditText etCustomPrompt = findViewById(R.id.etCustomPrompt);
        final Button btnSaveAi = findViewById(R.id.btnSaveAi);
        final Button btnBack = findViewById(R.id.btnBack);

        final SharedPreferences prefs = getSharedPreferences("app_config", MODE_PRIVATE);

        // 读取设置
        etApiKey.setText(prefs.getString("api_key", ""));
        etApiUrl.setText(prefs.getString("api_url", "https://api.deepseek.com/v1/chat/completions"));
        etCustomPrompt.setText(prefs.getString("custom_prompt", ""));
        switchDeepThinkDefault.setChecked(prefs.getBoolean("deep_think", false));
        switchHideReasoning.setChecked(prefs.getBoolean("hide_reasoning", false));
        switchStream.setChecked(prefs.getBoolean("stream", true));
        etMaxTokens.setText(String.valueOf(prefs.getInt("max_tokens", 4096)));

        String savedModel = prefs.getString("model", "deepseek-v4-flash");
        String[] models = getResources().getStringArray(R.array.models);
        int modelPos = 0;
        for (int i = 0; i < models.length; i++) {
            if (models[i].equals(savedModel)) {
                modelPos = i;
                break;
            }
        }
        spinnerModel.setSelection(modelPos);

        float temp = prefs.getFloat("temperature", 1.0f);
        int tempProgress = Math.round(temp * 10);
        seekBarTemperature.setMax(20);
        seekBarTemperature.setProgress(tempProgress);
        tvTemperature.setText(String.format("%.1f", temp));

        float topP = prefs.getFloat("top_p", 1.0f);
        int topPProgress = Math.round(topP * 100);
        seekBarTopP.setMax(100);
        seekBarTopP.setProgress(topPProgress);
        tvTopP.setText(String.format("%.2f", topP));

        float freqPenalty = prefs.getFloat("frequency_penalty", 0.0f);
        int freqProgress = Math.round((freqPenalty + 2.0f) * 10);
        seekBarFreqPenalty.setMax(40);
        seekBarFreqPenalty.setProgress(freqProgress);
        tvFreqPenalty.setText(String.format("%.1f", freqPenalty));

        float presPenalty = prefs.getFloat("presence_penalty", 0.0f);
        int presProgress = Math.round((presPenalty + 2.0f) * 10);
        seekBarPresPenalty.setMax(40);
        seekBarPresPenalty.setProgress(presProgress);
        tvPresPenalty.setText(String.format("%.1f", presPenalty));

        int contextLen = prefs.getInt("context_length", 20);
        seekBarContext.setMax(40);
        seekBarContext.setProgress(contextLen);
        tvContext.setText(String.valueOf(contextLen));

        seekBarTemperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float value = progress / 10.0f;
					tvTemperature.setText(String.format("%.1f", value));
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			});
        seekBarTopP.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float value = progress / 100.0f;
					tvTopP.setText(String.format("%.2f", value));
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			});
        seekBarFreqPenalty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float value = (progress / 10.0f) - 2.0f;
					tvFreqPenalty.setText(String.format("%.1f", value));
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			});
        seekBarPresPenalty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float value = (progress / 10.0f) - 2.0f;
					tvPresPenalty.setText(String.format("%.1f", value));
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			});
        seekBarContext.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					tvContext.setText(String.valueOf(progress));
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			});

        btnSaveAi.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String apiKey = etApiKey.getText().toString().trim();
					String apiUrl = etApiUrl.getText().toString().trim();
					String model = spinnerModel.getSelectedItem().toString();
					float temperature = seekBarTemperature.getProgress() / 10.0f;
					float topP = seekBarTopP.getProgress() / 100.0f;
					int maxTokens = 4096;
					try { maxTokens = Integer.parseInt(etMaxTokens.getText().toString().trim()); } catch(Exception e) {}
					float freqPenalty = (seekBarFreqPenalty.getProgress() / 10.0f) - 2.0f;
					float presPenalty = (seekBarPresPenalty.getProgress() / 10.0f) - 2.0f;
					int contextLen = seekBarContext.getProgress();
					boolean deepThink = switchDeepThinkDefault.isChecked();
					boolean hideReasoning = switchHideReasoning.isChecked();
					boolean stream = switchStream.isChecked();
					String customPrompt = etCustomPrompt.getText().toString().trim();

					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("api_key", apiKey);
					editor.putString("api_url", apiUrl);
					editor.putString("model", model);
					editor.putFloat("temperature", temperature);
					editor.putFloat("top_p", topP);
					editor.putInt("max_tokens", maxTokens);
					editor.putFloat("frequency_penalty", freqPenalty);
					editor.putFloat("presence_penalty", presPenalty);
					editor.putInt("context_length", contextLen);
					editor.putBoolean("deep_think", deepThink);
					editor.putBoolean("hide_reasoning", hideReasoning);
					editor.putBoolean("stream", stream);
					editor.putString("custom_prompt", customPrompt);
					editor.apply();

					Toast.makeText(AiSettingsActivity.this, "AI 设置已保存", Toast.LENGTH_SHORT).show();
					finish();
				}
			});

        btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
    }
}
