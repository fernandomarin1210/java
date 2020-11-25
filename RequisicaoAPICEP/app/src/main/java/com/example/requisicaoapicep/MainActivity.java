package com.example.requisicaoapicep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button botaoRecuperar;
    private TextView textoResultado;
    private EditText campoCep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botaoRecuperar = findViewById(R.id.buttonRecuperar);
        textoResultado = findViewById(R.id.textResultado);

        botaoRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                campoCep = findViewById(R.id.editTextCep);
                MyTask task = new MyTask();
                String cep = campoCep.getText().toString();
                String urlApi = "https://viacep.com.br/ws/" + cep + "/json/";
                task.execute( urlApi );

            }
        });

    }

    class MyTask extends AsyncTask<String, Void, String> {

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

            String logradouro = null;
            String cep = null;
            String complemento = null;
            String bairro = null;
            String localidade = null;
            String uf = null;


            try {
                JSONObject jsonObject = new JSONObject( s );

                logradouro = jsonObject.getString( "logradouro");
                cep = jsonObject.getString( "cep");
                complemento = jsonObject.getString( "complemento");
                bairro = jsonObject.getString( "bairro");
                localidade = jsonObject.getString( "localidade");
                uf = jsonObject.getString( "uf");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            textoResultado.setText( logradouro + " / " + cep + " / " + complemento + " / " + bairro + " / " + localidade + " / " + uf );
        }
    }

}