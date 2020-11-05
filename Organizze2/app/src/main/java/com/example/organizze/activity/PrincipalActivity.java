package com.example.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.security.identity.CipherSuiteNotSupportedException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.organizze.R;
import com.example.organizze.activity.adapter.AdapterMovimentacao;
import com.example.organizze.activity.config.ConfiguracaoFirebase;
import com.example.organizze.activity.helper.Base64Custom;
import com.example.organizze.activity.model.Movimentacao;
import com.example.organizze.activity.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView textoSaudacao, textoSaldo;
    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoTotal = 0.0;

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseref = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;


    private RecyclerView recyclerView;
    private AdapterMovimentacao adapterMovimentacao;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private Movimentacao movimentacao;
    private DatabaseReference movimentacaoRef;
    private String mesAnoSelecionado;
    private DataSnapshot dados1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal3);

        getSupportActionBar().setTitle("Organize");
        getSupportActionBar().setElevation(0);

        textoSaldo = findViewById(R.id.textSaldo);
        textoSaudacao = findViewById(R.id.textSaudacao);

        calendarView = findViewById(R.id.calendarView);
        configuraCalendarView();

        recyclerView = findViewById(R.id.recyclerMovimentos);
        swipe();

        // Configurar um adapter
        adapterMovimentacao = new AdapterMovimentacao( movimentacoes, this);


        // Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter( adapterMovimentacao );

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    public void swipe(){

        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                // Movimento drag and drop inativo
                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                // Permite arrastar para a direita ou para esquerda
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags(dragFlags, swipeFlags);

            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                excluirMovimentacao( viewHolder );

            }
        };

        new ItemTouchHelper( itemTouch ).attachToRecyclerView( recyclerView );

    }

    public void excluirMovimentacao(final RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );
        // Configura Dialog
        alertDialog.setTitle("Excluir movimentação da Conta");
        alertDialog.setMessage("Você tem certeza que deseja realmente excluir essa movimentação?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = movimentacoes.get( position );
                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64( emailUsuario );
                movimentacaoRef = firebaseref.child("movimentacao")
                        .child( idUsuario )
                        .child( mesAnoSelecionado );
                // Remove o nó da chave no Firebase
                movimentacaoRef.child( movimentacao.getKey()).removeValue();
                adapterMovimentacao.notifyItemMoved( position );
                atualizarSaldo();
            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PrincipalActivity.this, "Cancelado", Toast.LENGTH_LONG).show();
                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();

    }

    public void atualizarSaldo(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        usuarioRef = firebaseref.child("usuarios")
                                .child( idUsuario );

        if ( movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue( receitaTotal );
        }

        if ( movimentacao.getTipo().equals("d")){
            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue( despesaTotal );
        }

    }

    public void recuperarMovimentacoes(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        movimentacaoRef = firebaseref.child("movimentacao")
                                    .child( idUsuario )
                                    .child( mesAnoSelecionado );

        valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                movimentacoes.clear();
                //Log.i("anomes", movimentacoes.toString());

                for (DataSnapshot dados: snapshot.getChildren()){

                    Movimentacao movimentacao = dados.getValue( Movimentacao.class );
                    movimentacao.setKey( dados.getKey() ); // dados.getKey está recurando do Firebase
                    movimentacoes.add( movimentacao );

                }

                adapterMovimentacao.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void recuperarResumo(){

        // O correto seria fazer um método para retornar a referencia do objeto
        // em config/ConfiguracaoFirebase para não precisar repetir esse código
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        usuarioRef = firebaseref.child("usuarios")
                                .child( idUsuario );

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue( Usuario.class );

                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                resumoTotal = receitaTotal - despesaTotal;

                // 0.00 mascara para mostrar zeros
                // #.## mascara para não mostrar zeros
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String resultadoFormatado = decimalFormat.format( resumoTotal );

                textoSaudacao.setText("Olá, " + usuario.getNome());
                textoSaldo.setText( "R$ " + resultadoFormatado );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut();
                startActivity((new Intent(this, MainActivity.class)));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void adicionarReceita(View view){
        startActivity(new Intent(this, ReceitasActivity.class));
    }

    public void adicionarDespesa(View view){
        startActivity(new Intent(this, DespesasActivity.class));
    }

    public void configuraCalendarView(){

        CharSequence meses[] = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        calendarView.setTitleMonths( meses );

        // Define um vetor de dias da semana (descrições) para mostar no app
        //CharSequence diasSemana[] = {"Dom","Seg","Ter","Qua","Qui","Sex","Sab"};
        //calendarView.setWeekDayLabels( diasSemana );

        // Evento de navegação (alteração) dos meses no calendário
        CalendarDay dataAtual = calendarView.getCurrentDate();
        mesAnoSelecionado = String.valueOf( String.format("%02d", (dataAtual.getMonth() + 1)) + "" + dataAtual.getYear());
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
            mesAnoSelecionado = String.valueOf( String.format("%02d", (date.getMonth() + 1)) + "" + date.getYear());
            recuperarMovimentacoes();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener( valueEventListenerUsuario );
        movimentacaoRef.removeEventListener( valueEventListenerMovimentacoes );
    }
}