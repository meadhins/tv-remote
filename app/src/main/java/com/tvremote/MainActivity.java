package com.tvremote;

import android.app.Activity;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.content.Context;
import android.widget.TextView;

public class MainActivity extends Activity {

    ConsumerIrManager irManager;
    static final int FREQ = 38000;
    static final int ADDR = 0x04;
    
    int internalVolume = 28;
    int internalChannel = 125;

    int[] necCode(int command) {
        int[] p = new int[67];
        int idx = 0;
        p[idx++] = 9000; p[idx++] = 4500;
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((ADDR >> i) & 1) == 1 ? 1690 : 560; }
        int ia = (~ADDR) & 0xFF;
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((ia >> i) & 1) == 1 ? 1690 : 560; }
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((command >> i) & 1) == 1 ? 1690 : 560; }
        int ic = (~command) & 0xFF;
        for (int i = 0; i < 8; i++) { p[idx++] = 560; p[idx++] = ((ic >> i) & 1) == 1 ? 1690 : 560; }
        p[idx] = 560;
        return p;
    }

    void send(int command) {
        if (irManager != null && irManager.hasIrEmitter()) {
            irManager.transmit(FREQ, necCode(command));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        irManager = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);

        TextView txtVolNum = findViewById(R.id.txtVolNum);
        TextView txtChNum = findViewById(R.id.txtChNum);

        // Core Command Sets
        findViewById(R.id.btnPower).setOnClickListener(v -> send(0x08));
        findViewById(R.id.btnMute).setOnClickListener(v -> send(0x09));
        findViewById(R.id.btnInput).setOnClickListener(v -> send(0x0B));
        
        findViewById(R.id.btnVolUp).setOnClickListener(v -> {
            if (internalVolume < 100) internalVolume += 2;
            txtVolNum.setText(String.valueOf(internalVolume));
            send(0x02);
        });
        
        findViewById(R.id.btnVolDown).setOnClickListener(v -> {
            if (internalVolume > 0) internalVolume -= 2;
            txtVolNum.setText(String.valueOf(internalVolume));
            send(0x03);
        });
        
        findViewById(R.id.btnChUp).setOnClickListener(v -> {
            internalChannel++;
            txtChNum.setText(String.valueOf(internalChannel));
            send(0x00);
        });
        
        findViewById(R.id.btnChDown).setOnClickListener(v -> {
            if (internalChannel > 1) internalChannel--;
            txtChNum.setText(String.valueOf(internalChannel));
            send(0x01);
        });

        // Navigation Engine Keys
        findViewById(R.id.btnMenu).setOnClickListener(v -> send(0x43));
        findViewById(R.id.btnHome).setOnClickListener(v -> send(0x79));
        findViewById(R.id.btnBack).setOnClickListener(v -> send(0x28));
        findViewById(R.id.btnUp).setOnClickListener(v -> send(0x40));
        findViewById(R.id.btnDown).setOnClickListener(v -> send(0x41));
        findViewById(R.id.btnLeft).setOnClickListener(v -> send(0x07));
        findViewById(R.id.btnRight).setOnClickListener(v -> send(0x06));
        findViewById(R.id.btnOk).setOnClickListener(v -> send(0x44));
    }
}
