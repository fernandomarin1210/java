package com.example.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.organizze.R;
import com.example.organizze.activity.config.ConfiguracaoFirebase;
import com.example.organizze.activity.helper.Base64Custom;
import com.example.organizze.activity.helper.DateUtil;
import com.example.organizze.activity.model.Movimentacao;
import com.example.organizze.activity.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpCookie;

public class DespesasActivity extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseref = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;
    private Double despesaGerada;
    private Double despesaAtualizada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        campoValor = findViewById(R.id.editValor);
        campoData = findViewById(R.id.editData);
        campoCategoria = findViewById(R.id.editCategoria);
        campoDescricao = findViewById(R.id.editDescricao);

        campoData.setText(DateUtil.dataAtual());

        recuperarDespesaTotal();

    }

    public void salvarDespesa(View view){

        if ( validarCamposDespesa() ){

            movimentacao = new Movimentacao();

            String data = campoData.getText().toString();
            despesaGerada = Double.parseDouble( campoValor.getText().toString() );

            movimentacao.setValor( despesaGerada );
            movimentacao.setCategoria( campoCategoria.getText().toString() );
            movimentacao.setDescricao( campoDescricao.getText().toString() );
            movimentacao.setData( data );
            movimentacao.setTipo( "d" );

            despesaAtualizada = despesaTotal + despesaGerada;

            atualizarDespesa();
            movimentacao.salvar(data);

            finish();

        }

    }

    public void atualizarDespesa(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseref.child("usuarios").child( idUsuario );

        usuarioRef.child("despesaTotal").setValue(despesaAtualizada);

    }

    public Boolean validarCamposDespesa(){

        String textoValor = campoValor.getText().toString();
        String textoData = campoData.getText().toString();
        String textoCategoria = campoCategoria.getText().toString();
        String textoDescricao = campoDescricao.getText().toString();

        if (!textoValor.isEmpty()){
            if (!textoData.isEmpty()){
                if (!textoCategoria.isEmpty()){
                    if (!textoDescricao.isEmpty()){
                        return true;
                    }else{
                        Toast.makeText(DespesasActivity.this, "Descrição deve ser preenchida", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    Toast.makeText(DespesasActivity.this, "Categoria deve ser preenchida", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(DespesasActivity.this, "Data deve ser preenchida", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(DespesasActivity.this, "Valor deve ser preenchido", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public void recuperarDespesaTotal(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseref.child("usuarios").child( idUsuario );

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue( Usuario.class );
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}