package com.autofacil.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private static final int PICK_FILE = 1001;
    private TextView out;
    private Button run;
    private String resultsText = null;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(26,26,26,26);
        root.setBackgroundColor(Color.rgb(106,27,154));

        TextView title = text("☘  AutoFácil", 30, true);
        TextView sub = text("Lotofácil • jogo escolhido pelo sistema", 16, true);
        Button choose = new Button(this);
        choose.setText("ESCOLHER ARQUIVO DE RESULTADOS");
        choose.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("text/*");
            startActivityForResult(i, PICK_FILE);
        });
        run = new Button(this);
        run.setText("GERAR OS 2 JOGOS");
        run.setOnClickListener(v -> executeEngine());

        out = text("Selecione o TXT/CSV dos resultados da Lotofácil.\n\n1) campeão de 250.000 candidatos sorteados\n2) campeão de TODO o universo: 3.268.760 jogos",16,false);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(out);
        root.addView(title); root.addView(sub); root.addView(choose); root.addView(run);
        root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1f));
        setContentView(root);
    }

    private TextView text(String s, int size, boolean center) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.WHITE);
        t.setPadding(4,12,4,18); if(center)t.setGravity(Gravity.CENTER); return t;
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req,res,data);
        if(req == PICK_FILE && res == RESULT_OK && data != null && data.getData() != null) load(data.getData());
    }

    private void load(Uri uri) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line; while((line=br.readLine()) != null) sb.append(line).append('\n'); br.close();
            resultsText = sb.toString();
            out.setText("Arquivo carregado.\n\nToque em GERAR OS 2 JOGOS.");
        } catch(Exception e) { out.setText("ERRO AO LER ARQUIVO: " + e); }
    }

    private void executeEngine() {
        if(resultsText == null) { out.setText("Primeiro selecione o arquivo de resultados."); return; }
        run.setEnabled(false);
        out.setText("Executando o MESMO motor Python do Pydroid...\n\nPrimeiro: 250.000 sorteados.\nDepois: universo completo 3.268.760.");
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject engine = py.getModule("autofacil_engine");
                String result = engine.callAttr("executar", resultsText).toString();
                runOnUiThread(() -> out.setText(result));
            } catch(Exception e) {
                runOnUiThread(() -> out.setText("ERRO: " + e.toString()));
            } finally {
                runOnUiThread(() -> run.setEnabled(true));
            }
        }).start();
    }
}
