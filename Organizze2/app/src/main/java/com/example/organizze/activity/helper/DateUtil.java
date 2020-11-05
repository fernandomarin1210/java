package com.example.organizze.activity.helper;

import java.text.SimpleDateFormat;

public class DateUtil {

    public static String dataAtual(){
        long data = System.currentTimeMillis();
        // Data com hora
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        // Data sem hora
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dataString = simpleDateFormat.format(data);
        return dataString;
    }

    public static String mesAnoDataEscolhida(String data){

        String retornoData[] = data.split("/");
        String mesAno = retornoData[1] + retornoData[2];
        return mesAno;

    }

}
