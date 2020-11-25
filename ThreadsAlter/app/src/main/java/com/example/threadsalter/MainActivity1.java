package com.example.threadsalter;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity1 extends AppCompatActivity {

    private Button botaoIniciar;
    private Button botaoParar;
    private int numero;
    private boolean pararExecucao = false;

    // Objeto handler envia códigos para thread principal
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botaoIniciar = findViewById(R.id.buttonIniciar);
        botaoParar = findViewById(R.id.buttonParar);

    }

    public void iniciarThread(View view){

        pararExecucao = false;
        botaoParar.setText("PARAR THREAD");

        //MyThread thread = new MyThread();
        //thread.start();

        MyRunnable runnable = new MyRunnable();
        new Thread( runnable ).start();

    }

    public void pararThread(View view) {

        pararExecucao = true;

    }

    class MyRunnable implements Runnable{

        @Override
        public void run() {

            for ( int i = 0; i <= 15; i++){

                // EXECUCAO NA THREAD SECUNDARIA CRIADA
                if (pararExecucao){
                    botaoIniciar.setText("INICIAR THREADS");
                    botaoParar.setText("THREAD 2 PARADA");
                    return;

                }

                Log.d("Thread", "contador thread 2: " + i);

                // EXECUCAO NA THREAD PRINCIPAL
                numero = i;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        botaoIniciar.setText("contador thread P: " + numero);
                    }
                }, 3000);
                // FIM EXECUCAO THREAD PRINCIPAL

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    class MyThread extends Thread {

        @Override
        public void run() {

            for ( int i = 0; i <= 15; i++){
                Log.d("Thread", "contador: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}