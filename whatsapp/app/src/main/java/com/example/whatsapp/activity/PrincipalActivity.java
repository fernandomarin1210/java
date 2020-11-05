package com.example.whatsapp.activity;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.fragment.ContatosFragment;
import com.example.whatsapp.fragment.ConversasFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class PrincipalActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar( toolbar );

        // Configurar abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this )
                .add( "Conversas", ConversasFragment.class)
                .add("Contatos", ContatosFragment.class)
                .create()
        );
        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter( adapter );

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager( viewPager );

        // Configuracao da pesquisa
        searchView = findViewById(R.id.materialSearchPrincipal);
        // Listener para o search view
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {  // Quando abre o search view de pesquisa

            }

            @Override
            public void onSearchViewClosed() {  // Quando fecha o search view de pesquisa

                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                fragment.recarregarConversas();

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // Evento ao pressinar ok na pesquisa
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // Evento chamado a cada letra digitada na pesquisa

                // Verifica se pesquisa é na aba de conversas ou contatos
                switch ( viewPager.getCurrentItem() ){
                    case 0:
                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0 );
                        if ( newText != null && !newText.isEmpty()){
                            conversasFragment.pesquisarConversas( newText.toLowerCase() );
                        }else{
                            conversasFragment.recarregarConversas();
                        }
                    break;
                    case 1 :
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1 );
                        if ( newText != null && !newText.isEmpty()){
                            contatosFragment.pesquisarContatos( newText.toLowerCase() );
                        }else{
                            contatosFragment.recarregarContatos();
                        }
                    break;

                }

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_main,  menu );

        // Configurar botão de pesquisa
        MenuItem item = menu.findItem( R.id.menuPesquisa );
        searchView.setMenuItem( item );

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch ( item.getItemId() ) {
            case R.id.menuSair :
                deslogarUsuario();
                finish();
            break;
            case R.id.menuConfiguracoes :
                abrirConfiguracoes();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){

        try {
            autenticacao.signOut();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(PrincipalActivity.this, ConfiguracoesActivity.class);
        startActivity( intent );
    }

}