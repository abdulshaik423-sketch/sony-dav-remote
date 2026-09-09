package com.sony.davdz750k;

import android.app.Activity;
import android.os.Bundle;
import android.hardware.ConsumerIrManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private TextView status;

    private static final int ADDR_80 = 80;
    private static final int ADDR_208 = 208;
    private static final int ADDR_16 = 16;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ir = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
        buildUi();
    }

    private Button btn(String text, final int addr, final int cmd) {
        Button x = new Button(this);
        x.setText(text);
        x.setTextSize(15);
        x.setAllCaps(false);
        x.setOnClickListener(v -> send(addr, cmd, text));
        return x;
    }

    private void buildUi() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(14, 10, 14, 10);

        TextView title = new TextView(this);
        title.setText("Sony DAV-DZ750K");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        outer.addView(title, new LinearLayout.LayoutParams(-1, 60));

        status = new TextView(this);
        status.setText("IR status: checking…");
        status.setGravity(Gravity.CENTER);
        outer.addView(status, new LinearLayout.LayoutParams(-1, 45));

        ScrollView sv = new ScrollView(this);
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row = row();
        row.addView(btn("⏻ Power", ADDR_80, 21));
        row.addView(btn("Mute", ADDR_80, 20));
        p.addView(row);

        row = row();
        row.addView(btn("Vol −", ADDR_80, 19));
        row.addView(btn("Vol +", ADDR_80, 18));
        row.addView(btn("Function", ADDR_208, 105));
        p.addView(row);

        row = row();
        row.addView(btn("DVD", ADDR_80, 125));
        row.addView(btn("Tuner", ADDR_80, 33));
        row.addView(btn("Video", ADDR_80, 34));
        p.addView(row);

        row = row();
        row.addView(btn("◀", ADDR_16, 122));
        row.addView(btn("▲", ADDR_16, 120));
        row.addView(btn("▼", ADDR_16, 121));
        row.addView(btn("▶", ADDR_16, 123));
        row.addView(btn("OK", ADDR_16, 124));
        p.addView(row);

        row = row();
        row.addView(btn("Play", ADDR_16, 50));
        row.addView(btn("Pause", ADDR_16, 57));
        row.addView(btn("Stop", ADDR_16, 56));
        row.addView(btn("Open/Close", ADDR_16, 60));
        p.addView(row);

        row = row();
        row.addView(btn("Prev", ADDR_16, 48));
        row.addView(btn("Next", ADDR_16, 49));
        row.addView(btn("Rewind", ADDR_16, 51));
        row.addView(btn("FF", ADDR_16, 52));
        p.addView(row);

        row = row();
        row.addView(btn("Sound +", ADDR_208, 110));
        row.addView(btn("Sound −", ADDR_208, 111));
        row.addView(btn("Bass", ADDR_208, 77));
        row.addView(btn("Display", ADDR_80, 116));
        p.addView(row);

        TextView note = new TextView(this);
        note.setText("\nDesigned for Sony DAV-DZ750K / RM-ADU006. " +
                "Your phone must have a built-in IR blaster. Point the top of the phone at the home-theatre unit.");
        note.setTextSize(13);
        p.addView(note);

        sv.addView(p);
        outer.addView(sv, new LinearLayout.LayoutParams(-1, -1));
        setContentView(outer);
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setLayoutParams(new LinearLayout.LayoutParams(-1, 60));
        return r;
    }

    private void send(int addr, int cmd, String text) {
        if (ir != null && ir.hasIrEmitter()) {
            try {
                int[] pattern = getPattern(addr, cmd);
                ir.transmit(38000, pattern);
                updateStatus("Sent: " + text);
            } catch (Exception e) {
                updateStatus("Error sending: " + text);
            }
        } else {
            updateStatus("IR not available");
        }
    }

    private int[] getPattern(int addr, int cmd) {
        // Sony SIRCS protocol pattern (simplified)
        List<Integer> pattern = new ArrayList<>();
        pattern.add(2400);
        pattern.add(600);
        
        // Address bits
        for (int i = 0; i < 7; i++) {
            if ((addr & (1 << i)) != 0) {
                pattern.add(1200);
            } else {
                pattern.add(600);
            }
            pattern.add(600);
        }
        
        // Command bits
        for (int i = 0; i < 7; i++) {
            if ((cmd & (1 << i)) != 0) {
                pattern.add(1200);
            } else {
                pattern.add(600);
            }
            pattern.add(600);
        }
        
        pattern.add(600);
        
        int[] result = new int[pattern.size()];
        for (int i = 0; i < pattern.size(); i++) {
            result[i] = pattern.get(i);
        }
        return result;
    }

    private void updateStatus(String msg) {
        runOnUiThread(() -> status.setText("IR status: " + msg));
    }
}
