package com.example.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.adapter.ConversasAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConversasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewListaConversas;
    private ConversasAdapter adapter;
    private ArrayList<Conversa> listaConversas = new ArrayList<>();
    private DatabaseReference conversasRef;
    private DatabaseReference database;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment(){ // Construtor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        // Configuracoes iniciais
        recyclerViewListaConversas = view.findViewById(R.id.recyclerViewListaConversas);
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = database.child("conversas")
                               .child( identificadorUsuario );

        // Configurar adapter
        adapter = new ConversasAdapter( listaConversas, getActivity() );

        // Configurar recycleview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaConversas.setLayoutManager( layoutManager );
        recyclerViewListaConversas.setHasFixedSize( true );
        recyclerViewListaConversas.setAdapter( adapter );

        recyclerViewListaConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Conversa> listasConversasAtualizadas = adapter.getConversas();
                                Conversa conversaSelecionada = listasConversasAtualizadas.get( position );

                                if (conversaSelecionada.getIsGroup().equals("true")) {
                                    Intent i = new Intent( getActivity(), ChatActivity.class);
                                    // Passar parametros para activity ChatActivity
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity( i );
                                }else{
                                    Intent i = new Intent( getActivity(), ChatActivity.class);
                                    // Passar parametros para activity ChatActivity
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }


    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener( childEventListenerConversas );
    }

    public void pesquisarConversas( String texto){

        List<Conversa> listaConversasBusca = new ArrayList<>();

        for ( Conversa conversa : listaConversas){

            if ( conversa.getUsuarioExibicao() != null){

                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if ( nome.contains( texto ) || ultimaMsg.contains( texto )){
                    listaConversasBusca.add ( conversa );
                }

            }else{

                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if ( nome.contains( texto ) || ultimaMsg.contains( texto )){
                    listaConversasBusca.add ( conversa );
                }

            }

        }

        adapter = new ConversasAdapter( listaConversasBusca, getActivity());
        recyclerViewListaConversas.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }

    public void recarregarConversas(){

        adapter = new ConversasAdapter( listaConversas, getActivity());
        recyclerViewListaConversas.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }

    public void recuperarConversas(){

        listaConversas.clear();

        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                // Recuperar conversa
                Conversa conversa = snapshot.getValue(Conversa.class);
                listaConversas.add( conversa );
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}