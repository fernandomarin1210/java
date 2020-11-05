package com.example.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.activity.GrupoActivity;
import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.ConversasAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContatosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment(){ // Construtor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        // Configuracoes iniciais
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        // Configurar adapter
        adapter = new ContatosAdapter( listaContatos, getActivity() );

        // Configurar recycleview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContatos.setLayoutManager( layoutManager );
        recyclerViewListaContatos.setHasFixedSize( true );
        recyclerViewListaContatos.setAdapter( adapter );

        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuariosAtualizada = adapter.getContatos();

                                Usuario usuarioSelecionado = listaUsuariosAtualizada.get( position );
                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                                if ( cabecalho ) {
                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(i);
                                }else{
                                    Intent i = new Intent( getActivity(), ChatActivity.class);
                                    // Passar parametros para activity ChatActivity
                                    i.putExtra("chatContato", usuarioSelecionado);
                                    startActivity( i );
                                }

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

        adicionarMenuNovoGrupo();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }


    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener( valueEventListenerContatos );
    }

    public void limparListaContatos(){
        listaContatos.clear();
        adicionarMenuNovoGrupo();
    }

    public void adicionarMenuNovoGrupo(){

        Usuario itemGrupo = new Usuario();

        itemGrupo.setNome( "Novo Grupo" );
        itemGrupo.setEmail( "" );

        listaContatos.add( itemGrupo );

    }

    public void pesquisarContatos( String texto){

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for ( Usuario usuario : listaContatos){

            String nome = usuario.getNome().toLowerCase();
            if ( nome.contains( texto )){
                listaContatosBusca.add( usuario );
            }

        }

        adapter = new ContatosAdapter( listaContatosBusca, getActivity());
        recyclerViewListaContatos.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }

    public void recarregarContatos(){

        adapter = new ContatosAdapter( listaContatos, getActivity());
        recyclerViewListaContatos.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }

    public void recuperarContatos(){

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                limparListaContatos();

                for ( DataSnapshot dados: snapshot.getChildren()){

                    Usuario usuario = dados.getValue( Usuario.class );

                    // Testa se usuário corrente é diferente do usuário logado para não
                    // adicionar o usuário logado na lista de contatos
                    if (!usuarioAtual.getEmail().equals(usuario.getEmail())) {
                        listaContatos.add( usuario );
                    }
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}