package com.example.requisicoeshttp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button botaoRecuperar;
    private TextView textoResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botaoRecuperar = findViewById(R.id.buttonRecuperar);
        textoResultado = findViewById(R.id.textResultado);

        botaoRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyTask task = new MyTask();
                String urlApi = "https://blockchain.info/ticker";
                task.execute( urlApi );

            }
        });

    }

    class MyTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String stringUrl = strings[0];
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            StringBuffer buffer = null;

            try {
                URL url = new URL( stringUrl );
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

                // Recupera dados em Bytes
                inputStream = conexao.getInputStream();

                // inputStreamerReader lê os dados em Bytes e decodifica para caracteres
                inputStreamReader = new InputStreamReader( inputStream );

                BufferedReader reader = new BufferedReader( inputStreamReader );
                buffer = new StringBuffer();

                String linha = "";

                while( (linha = reader.readLine()) != null) {

                    buffer.append( linha );

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String objetoValor = null;
            String valorMoeda = null;
            String simbolo = null;

            try {
                JSONObject jsonObject = new JSONObject( s );

                objetoValor = jsonObject.getString( "BRL");

                JSONObject jsonObjectReal = new JSONObject( objetoValor );

                valorMoeda = jsonObjectReal.getString( "last" );
                simbolo = jsonObjectReal.getString( "symbol" );


            } catch (JSONException e) {
                e.printStackTrace();
            }

            textoResultado.setText( simbolo + ": " + valorMoeda );
        }
    }

}