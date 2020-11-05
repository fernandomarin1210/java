package com.example.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMembrosSelecionados, recyclerMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuariosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;

    public void atualizarMembrosToolbar(){

        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;
        toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);;

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar( toolbar );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuracoes Iniciais recyclerview Contatos
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        recyclerMembros = findViewById(R.id.recyclerMembros);
        fabAvancarCadastro = findViewById(R.id.fabSalvarGrupo);

        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        // Configuracao adapter Contatos
        contatosAdapter = new ContatosAdapter( listaMembros, getApplicationContext() );

        // Configuracao recyclerView Contatos
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerMembros.setLayoutManager( layoutManager );
        recyclerMembros.setHasFixedSize( true );
        recyclerMembros.setAdapter( contatosAdapter );

        recyclerMembros.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembros,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuarioSelecionado = listaMembros.get( position );

                                // Remover usuário selecionado da lista de contatos (view inferior)
                                listaMembros.remove( usuarioSelecionado );
                                contatosAdapter.notifyDataSetChanged();

                                // Adicionar usuário selecionado na lista de membros do grupo (view superior)
                                listaMembrosSelecionados.add( usuarioSelecionado );
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                atualizarMembrosToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        // Configuracao recyclerView Membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter( listaMembrosSelecionados, getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager( layoutManagerHorizontal );
        recyclerMembrosSelecionados.setHasFixedSize( true );
        recyclerMembrosSelecionados.setAdapter( grupoSelecionadoAdapter );

        recyclerMembrosSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembrosSelecionados,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuarioSelecionado = listaMembrosSelecionados.get( position );

                                // Remove usuário selecionado na lista de membros do grupo (view superior)
                                listaMembrosSelecionados.remove( usuarioSelecionado );
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                // Adiciona usuário selecionado da lista de contatos (view inferior)
                                listaMembros.add( usuarioSelecionado );
                                contatosAdapter.notifyDataSetChanged();

                                atualizarMembrosToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        // Configurar floating action button
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);

                i.putExtra( "membros", (Serializable) listaMembrosSelecionados  );
                startActivity( i );

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }


    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener( valueEventListenerMembros );
    }

    public void recuperarContatos(){

        valueEventListenerMembros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for ( DataSnapshot dados: snapshot.getChildren()){

                    Usuario usuario = dados.getValue( Usuario.class );

                    // Testa se usuário corrente é diferente do usuário logado para não
                    // adicionar o usuário logado na lista de contatos
                    if (!usuarioAtual.getEmail().equals(usuario.getEmail())) {
                        listaMembros.add( usuario );
                    }
                }

                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}