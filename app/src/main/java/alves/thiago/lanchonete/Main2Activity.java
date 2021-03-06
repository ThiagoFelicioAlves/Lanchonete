package alves.thiago.lanchonete;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import java.io.*;
import java.net.Socket;
import java.util.*;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.simplify.ink.InkView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.layout.simple_list_item_1;
import static android.R.layout.simple_list_item_2;
import static android.R.layout.simple_spinner_dropdown_item;
import static android.R.layout.simple_spinner_item;

public class Main2Activity extends AppCompatActivity {
    TextView Quantidade;
    EditText Colaborador,Setor;
    TextView Valor;
    Spinner s;
    List<String> item,value;
    ArrayAdapter<String> adapter,adapter2;
    ListView listview;
    String hostIP;
    Map<String, List<String>> MAP;
    HashMap<String, Integer> items;
    Intent intent;
    InkView ink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button Finalizar,Limpar_Dados;
        Finalizar =  findViewById(R.id.Finalizar);
        Limpar_Dados =  findViewById(R.id.Limpar_Dados);
        Setor =  findViewById(R.id.Setor);
        Colaborador =  findViewById(R.id.Colaborador);
        Quantidade =  findViewById(R.id.Quantidade);
        Valor =  findViewById(R.id.Valor);
        ink =  findViewById(R.id.ink);
        MAP = MainActivity.getMAP();
        hostIP = MainActivity.getIP();
        listview  =  findViewById(R.id.list);
        value = new ArrayList<>();
        value.add("");
        items = new HashMap<>();
        intent = new Intent(this, MainActivity.class);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, value);
        listview.setAdapter(adapter2);

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter2.notifyDataSetChanged();
                }
            });
        }catch (Exception e ){
            e.printStackTrace();
        }

       s = findViewById(R.id.spinner);

        String[] stockArr = new String[MAP.keySet().size()];
        stockArr = MAP.keySet().toArray(stockArr);

        adapter = new ArrayAdapter<>(this, simple_spinner_item, stockArr);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parentView, View selectedItemView, final int position, long id) {
                String Tipo = parentView.getItemAtPosition(position).toString();
                setItem(Tipo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        listview.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                        String itemValue = (String) listview.getItemAtPosition(position);
                        Log.d("MyApp",itemValue);

                        String NomeEscolhido = itemValue.split(" [-] ")[0];
                        System.out.println(NomeEscolhido);
                        Float Valor_Separado = Float.valueOf(itemValue.split(" [-] ")[1]);
                        Log.d("MyApp",String.valueOf(Valor_Separado));

                        try{
                            items.put(NomeEscolhido,items.get(NomeEscolhido)+1);
                        } catch (Exception e){
                            items.put(NomeEscolhido,1);
                            e.printStackTrace();
                        }

                        if (Valor.getText().toString().equals("Valor")){
                            Valor.setText("0");
                        }
                        if (Valor.getText() != null){
                            String ValorSoma = Valor.getText().toString();
                            Log.d("MyApp",ValorSoma);
                            Float ValorSoma2 = Float.valueOf(ValorSoma);
                            Float valortotal =  ValorSoma2 + Valor_Separado;
                            Log.d("MyApp",String.valueOf(valortotal));
                            Valor.setText(String.valueOf(valortotal));

                            Quantidade.setText(items.toString().replaceAll("[{]","").replaceAll("[}]",""));
                        }
                    }
                });


        Finalizar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

                AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);

                builder.setMessage("Você confirma que a/o "+ Colaborador.getText() +" do setor " + Setor.getText()
                + " será cadastrado o pedido do valor de R$ "+Valor.getText()+" reais?")
                        .setTitle("Confirmação");

                builder.setPositiveButton("Confirmo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(intent);

                        Thread SendFiscal = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Socket C2 = new Socket(hostIP,7071);
                                    PrintWriter writer = new PrintWriter(C2.getOutputStream());
                                    String fiscal = Colaborador.getText() +";" + Setor.getText() + ";"+Quantidade.getText()+";"+Valor.getText();

                                    int color = Color.TRANSPARENT;
                                    Drawable background = ink.getBackground();
                                    if (background instanceof ColorDrawable)
                                        color = ((ColorDrawable) background).getColor();
                                    Bitmap bitmap = ink.getBitmap(color);

                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                    String toSend = fiscal+"SEPARAPAL"+encoded;
                                    Writer out = new BufferedWriter(new OutputStreamWriter(C2.getOutputStream(), "UTF8"));
                                    out.write(toSend);
                                    out.flush();
                                    out.close();
                                    C2.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        SendFiscal.start();

                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Limpar_Dados.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Quantidade.setText("");
                Setor.setText("");
                Colaborador.setText("");
                Valor.setText("Valor");
                ink.clear();

            }});

    }

    public void setItem(String Tipo){
        try {
            String[] stockArr2 = new String[MAP.get(Tipo).size()];
            stockArr2 = MAP.get(Tipo).toArray(stockArr2);
            Log.d("MyApp",MAP.toString());
            value.clear();
            value.addAll(Arrays.asList(stockArr2));
        } catch (Exception e ){
            e.printStackTrace();
        }

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter2.notifyDataSetChanged();
                }
            });
        }catch (Exception e ){
            e.printStackTrace();
        }
    }
}
