package com.example.retrofit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.retrofit.api.CEPService;
import com.example.retrofit.api.DataService;
import com.example.retrofit.model.CEP;
import com.example.retrofit.model.Foto;
import com.example.retrofit.model.Postagem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Button botaoRecuperar;
    private Button botaoPost;
    private Button botaoPut;
    private Button botaoPatch;
    private Button botaoDel;
    private TextView textoResultado;
    private Retrofit retrofit;
    private DataService service;
    private List<Foto> listaFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botaoRecuperar = findViewById(R.id.buttonRecuperar);
        botaoPost = findViewById(R.id.buttonPost);
        botaoPut = findViewById(R.id.buttonPut);
        botaoPatch = findViewById(R.id.buttonPatch);
        botaoDel = findViewById(R.id.buttonDel);
        textoResultado = findViewById(R.id.textResultado);

        retrofit = new Retrofit.Builder()
                //.baseUrl("https://viacep.com.br/ws/")
                .baseUrl("https://jsonplaceholder.typicode.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(DataService.class);

        botaoRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recuperarListaRetrofit();

            }
        });

        botaoPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                salvarPostagem();

            }
        });

        botaoPut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                atualizarPUT();

            }
        });

        botaoPatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                atualizarPATCH();

            }
        });

        botaoDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deletarPost();

            }
        });

    }

    private void salvarPostagem(){

        Postagem postagem = new Postagem("1234","Título Postagem","Corpo postagem");

        // Metodo para JSON
        Call<Postagem> call = service.salvarPostagem( postagem );
        // Metodo para XML
        //Call<Postagem> call = service.salvarPostagem( "1234", "Título Postagem XML", "Corpo postagem XML" );

        call.enqueue(new Callback<Postagem>() {
            @Override
            public void onResponse(Call<Postagem> call, Response<Postagem> response) {
                if ( response.isSuccessful() ){
                    Postagem postagemResposta = response.body();
                    textoResultado.setText("Código:" + response.code() +
                                           " id: " + postagemResposta.getId() +
                                           " título: " + postagemResposta.getTitle()
                                           );
                }
            }

            @Override
            public void onFailure(Call<Postagem> call, Throwable t) {

            }
        });

    }

    private void atualizarPUT(){

        Postagem postagem = new Postagem("1234",null,"Corpo postagem PUT");
        Call<Postagem> call = service.atualizarPostagem(2, postagem);

        call.enqueue(new Callback<Postagem>() {
            @Override
            public void onResponse(Call<Postagem> call, Response<Postagem> response) {
                if ( response.isSuccessful() ){
                    Postagem postagemResposta = response.body();
                    textoResultado.setText("Código:" + response.code() +
                            " id: " + postagemResposta.getId() +
                            " título: " + postagemResposta.getTitle() +
                            " corpo: " + postagemResposta.getBody()
                    );
                }
            }

            @Override
            public void onFailure(Call<Postagem> call, Throwable t) {

            }
        });

    }

    private void atualizarPATCH(){

        Postagem postagem = new Postagem("1234",null,"Corpo postagem PUT");
        Call<Postagem> call = service.atualizarPostagemPatch(2, postagem);

        call.enqueue(new Callback<Postagem>() {
            @Override
            public void onResponse(Call<Postagem> call, Response<Postagem> response) {
                if ( response.isSuccessful() ){
                    Postagem postagemResposta = response.body();
                    textoResultado.setText("Código:" + response.code() +
                            " id: " + postagemResposta.getId() +
                            " título: " + postagemResposta.getTitle() +
                            " corpo: " + postagemResposta.getBody()
                    );
                }
            }

            @Override
            public void onFailure(Call<Postagem> call, Throwable t) {

            }
        });

    }

    private void deletarPost(){

        Postagem postagem = new Postagem("1234",null,"Corpo postagem PUT");
        Call<Void> call = service.deletarPostagem(2);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if ( response.isSuccessful() ){
                    textoResultado.setText("Status: " + response.code() + " -> Registro excluido com sucesso");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });

    }

    private void recuperarListaRetrofit(){

        Call<List<Foto>> call = service.recuperarFotos();

        call.enqueue(new Callback<List<Foto>>() {
            @Override
            public void onResponse(Call<List<Foto>> call, Response<List<Foto>> response) {
                if ( response.isSuccessful() ){

                    listaFotos = response.body();

                    for (int i = 0; i < listaFotos.size(); i++){
                        Foto foto = listaFotos.get( i );
                        Log.d("resultado",  foto.getId() + " / " + foto.getTitle());
                    }

                }
            }

            @Override
            public void onFailure(Call<List<Foto>> call, Throwable t) {

            }
        });

    }

    private void recuperarCEPRetrofit(){

        CEPService cepService = retrofit.create(CEPService.class);
        Call<CEP> call = cepService.recuperarCEP("88980000");

        call.enqueue(new Callback<CEP>() {
            @Override
            public void onResponse(Call<CEP> call, Response<CEP> response) {
                if ( response.isSuccessful() ){
                    CEP cep = response.body();
                    textoResultado.setText( cep.getCep() + " / " + cep.getLogradouro() + " / " + cep.getBairro());
                }
            }

            @Override
            public void onFailure(Call<CEP> call, Throwable t) {

            }
        });

    }

}