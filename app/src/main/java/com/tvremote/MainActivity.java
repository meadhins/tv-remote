package com.tvremote;

import android.app.Activity;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.widget.Button;
import android.content.Context;

public class MainActivity extends Activity {

    ConsumerIrManager irManager;
    static final int FREQ = 38000;
    static final int ADDR = 0x04;

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

        findViewById(R.id.btnPower).setOnClickListener(v -> send(0x08));
        findViewById(R.id.btnMute).setOnClickListener(v -> send(0x09));
        findViewById(R.id.btnInput).setOnClickListener(v -> send(0x0B));
        findViewById(R.id.btnVolUp).setOnClickListener(v -> send(0x02));
        findViewById(R.id.btnVolDown).setOnClickListener(v -> send(0x03));
        findViewById(R.id.btnChUp).setOnClickListener(v -> send(0x00));
        findViewById(R.id.btnChDown).setOnClickListener(v -> send(0x01));
        findViewById(R.id.btnMenu).setOnClickListener(v -> send(0x43));
        findViewById(R.id.btnHome).setOnClickListener(v -> send(0x79));
        findViewById(R.id.btnBack).setOnClickListener(v -> send(0x28));
        findViewById(R.id.btnUp).setOnClickListener(v -> send(0x40));
        findViewById(R.id.btnDown).setOnClickListener(v -> send(0x41));
        findViewById(R.id.btnLeft).setOnClickListener(v -> send(0x07));
        findViewById(R.id.btnRight).setOnClickListener(v -> send(0x06));
        findViewById(R.id.btnOk).setOnClickListener(v -> send(0x44));
        findViewById(R.id.btn0).setOnClickListener(v -> send(0x10));
        findViewById(R.id.btn1).setOnClickListener(v -> send(0x11));
        findViewById(R.id.btn2).setOnClickListener(v -> send(0x12));
        findViewById(R.id.btn3).setOnClickListener(v -> send(0x13));
        findViewById(R.id.btn4).setOnClickListener(v -> send(0x14));
        findViewById(R.id.btn5).setOnClickListener(v -> send(0x15));
        findViewById(R.id.btn6).setOnClickListener(v -> send(0x16));
        findViewById(R.id.btn7).setOnClickListener(v -> send(0x17));
        findViewById(R.id.btn8).setOnClickListener(v -> send(0x18));
        findViewById(R.id.btn9).setOnClickListener(v -> send(0x19));
    }
}
