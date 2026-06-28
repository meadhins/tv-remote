package com.tvremote;

import android.app.Activity;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends Activity {

    ConsumerIrManager irManager;
    int currentIndex = 0;
    SharedPreferences prefs;
    int[][] patterns;
    int[] frequencies;
    String[] labels;

    int[] necCode(int address, int command) {
        int[] p = new int[67];
        int idx = 0;
        p[idx++] = 9000; p[idx++] = 4500;
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((address >> i) & 1) == 1 ? 1690 : 560; }
        int ia = (~address) & 0xFF;
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((ia >> i) & 1) == 1 ? 1690 : 560; }
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((command >> i) & 1) == 1 ? 1690 : 560; }
        int ic = (~command) & 0xFF;
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((ic >> i) & 1) == 1 ? 1690 : 560; }
        p[idx] = 560;
        return p;
    }

    void buildCodes() {
        int[] cmds = {0x08, 0x00, 0x01, 0x02};
        int total = 256 * cmds.length;
        patterns = new int[total][];
        frequencies = new int[total];
        labels = new String[total];
        int n = 0;
        for (int cmd : cmds) {
            for (int addr = 0; addr < 256; addr++) {
                patterns[n] = necCode(addr, cmd);
                frequencies[n] = 38000;
                labels[n] = "Addr:0x" + String.format("%02X", addr) + " Cmd:0x" + String.format("%02X", cmd);
                n++;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildCodes();

        irManager = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
        prefs = getSharedPreferences("tvremote", MODE_PRIVATE);
        currentIndex = prefs.getInt("lastIndex", 0);

        Button powerBtn = findViewById(R.id.powerBtn);
        Button foundBtn = findViewById(R.id.foundBtn);
        Button prevBtn = findViewById(R.id.prevBtn);
        Button jumpBtn = findViewById(R.id.jumpBtn);
        EditText jumpInput = findViewById(R.id.jumpInput);
        TextView statusText = findViewById(R.id.statusText);
        TextView codeText = findViewById(R.id.codeText);

        if (irManager == null || !irManager.hasIrEmitter()) {
            statusText.setText("ERROR: No IR blaster found!");
            powerBtn.setEnabled(false);
            return;
        }

        updateDisplay(statusText, codeText);

        powerBtn.setOnClickListener(v -> {
            if (currentIndex >= patterns.length) {
                statusText.setText("All " + patterns.length + " codes tried!");
                return;
            }
            irManager.transmit(frequencies[currentIndex], patterns[currentIndex]);
            statusText.setText("Fired #" + (currentIndex + 1) + " of " + patterns.length);
            codeText.setText(labels[currentIndex] + "\nIndex: " + currentIndex);
            currentIndex++;
            prefs.edit().putInt("lastIndex", currentIndex).apply();
        });

        prevBtn.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                irManager.transmit(frequencies[currentIndex], patterns[currentIndex]);
                statusText.setText("Re-fired #" + (currentIndex + 1));
                codeText.setText(labels[currentIndex] + "\nIndex: " + currentIndex);
                prefs.edit().putInt("lastIndex", currentIndex).apply();
            }
        });

        jumpBtn.setOnClickListener(v -> {
            try {
                int n = Integer.parseInt(jumpInput.getText().toString().trim());
                if (n >= 0 && n < patterns.length) {
                    currentIndex = n;
                    prefs.edit().putInt("lastIndex", currentIndex).apply();
                    updateDisplay(statusText, codeText);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(jumpInput.getWindowToken(), 0);
                } else {
                    statusText.setText("Enter 0 to " + (patterns.length - 1));
                }
            } catch (NumberFormatException e) {
                statusText.setText("Enter a valid number");
            }
        });

        foundBtn.setOnClickListener(v -> {
            int worked = Math.max(currentIndex - 1, 0);
            statusText.setText("SUCCESS! Code #" + worked + " worked!");
            codeText.setText(labels[worked] + "\nIndex: " + worked + "\n\nScreenshot and send to Claude!");
        });
    }

    void updateDisplay(TextView statusText, TextView codeText) {
        statusText.setText("Resuming at #" + (currentIndex + 1) + " of " + patterns.length);
        if (currentIndex < patterns.length) {
            codeText.setText("Next: " + labels[currentIndex] + "\nIndex: " + currentIndex);
        }
    }
}
