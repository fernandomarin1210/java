package com.example.whatsapp.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.adapter.MensagensAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.Permissao;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Mensagem;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private EditText editMensagem;
    private DatabaseReference database;
    private  DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    // identificador usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Grupo grupo;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    // Configuracoes para abrir camera
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.CAMERA
    };
    private ImageView imageCameraChat;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar( toolbar );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensgens);

        // Recuperar dados do usuário remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        // Recuperar dados do usuário vindos por parametro da activity ContatosFragment
        Bundle bundle = getIntent().getExtras();
        if ( bundle != null) {

            if ( bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNome.setText( grupo.getNome() );

                String foto = grupo.getFoto();
                if ( foto != null ){
                    Uri url = Uri.parse( foto );
                    Glide.with( ChatActivity.this )
                            .load( url )
                            .into( circleImageViewFoto );

                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            }else{

                /*************** Inicio Conversa Converncional  **************/
                usuarioDestinatario = (Usuario) bundle.getSerializable( "chatContato");

                textViewNome.setText( usuarioDestinatario.getNome() );
                String foto = usuarioDestinatario.getFoto();
                if ( foto != null ){
                    Uri url = Uri.parse( usuarioDestinatario.getFoto() );
                    Glide.with( ChatActivity.this )
                         .load( url )
                         .into( circleImageViewFoto );


                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

                // Recuperar dados do usuário destinatario
                idUsuarioDestinatario = Base64Custom.codificarBase64( usuarioDestinatario.getEmail() );
                /*************** Final Conversa Converncional  **************/

            }

        }

        // Configuração do adapter
        adapter = new MensagensAdapter( mensagens, getApplicationContext() );

        // Configuração do recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerMensagens.setLayoutManager( layoutManager );
        recyclerMensagens.setHasFixedSize( true );
        recyclerMensagens.setAdapter( adapter );

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFireBaseStorage();
        mensagensRef = database.child("mensagens")
                .child( idUsuarioRemetente )
                .child( idUsuarioDestinatario );

        // Configuracoes para enviar foto
        // Valida permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        imageCameraChat = findViewById(R.id.imageCameraChat);

        imageCameraChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if ( i.resolveActivity(getPackageManager()) != null ){
                    startActivityForResult( i, SELECAO_CAMERA );
                }else{
                    Log.i("imagens", "caiu no else camera");
                    startActivityForResult( i, SELECAO_CAMERA );
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;

            try{
                switch ( requestCode ){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap( getContentResolver(), localImagemSelecionada);
                        break;
                }

                if ( imagem != null ){

                    // Recuperar dados da imagem para Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    // Criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    // Configurar referencias do firebase
                    final StorageReference imagemRef = storage.child("imagens")
                                                        .child("fotos_chat")
                                                        .child( idUsuarioRemetente )
                                                        .child( nomeImagem );
                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, "Erro ao fazer o carregamento da Imagem", Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();

                                    if ( usuarioDestinatario != null ){ // Mensagem particular
                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario( idUsuarioRemetente );
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem( url.toString() );

                                        // Salvar imagem para o remetente
                                        salvarMensagem( idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                                        // Salvar imagem para o destinatario
                                        salvarMensagem( idUsuarioDestinatario, idUsuarioRemetente, mensagem);
                                    }else{ // Mensgem em grupo

                                        for ( Usuario membro : grupo.getMembros()){
                                            String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();
                                            Usuario usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                                            mensagem.setMensagem( "imagem.jpeg" );
                                            mensagem.setNome( usuarioRemetente.getNome() );
                                            mensagem.setImagem( url.toString() );

                                            // Salvar Mensgem para membro de grupos
                                            salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                                            // Salvar conversa
                                            salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true );

                                        }

                                    }

                                }
                            });

                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if ( !textoMensagem.isEmpty() ){

            if ( usuarioDestinatario != null){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario( idUsuarioRemetente );
                mensagem.setMensagem( textoMensagem );


                // Salvar mensagem para o remetente
                salvarMensagem( idUsuarioRemetente, idUsuarioDestinatario, mensagem );
                // Salvar mensagem para o destinatario
                salvarMensagem( idUsuarioDestinatario, idUsuarioRemetente, mensagem );

                // Salvar conversa para o remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false );
                // Salvar conversa para o destinatario
                Usuario usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false );

            }else{

                for ( Usuario membro : grupo.getMembros()){
                    String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();
                    Usuario usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                    mensagem.setMensagem( textoMensagem );
                    mensagem.setNome( usuarioRemetente.getNome() );

                    // Salvar Mensgem para membro de grupos
                    salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    // Salvar conversa
                    salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true );

                }

            }

            editMensagem.setText("");

        }else{
            Toast.makeText(ChatActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_SHORT).show();
        }

    }

    private void salvarMensagem(String idRemetente, String idDestinario,  Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child( idRemetente )
                   .child( idDestinario )
                   .push()
                   .setValue( msg ) ;

    }

    private void salvarConversa(String idRemetente, String idDestinario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup ){

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente( idRemetente );
        conversaRemetente.setIdDestinatario( idDestinario );
        conversaRemetente.setUltimaMensagem( msg.getMensagem() );

        if ( isGroup ) {
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo( grupo );
        }else{
            conversaRemetente.setUsuarioExibicao( usuarioExibicao );
        }

        conversaRemetente.salvar();

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener( childEventListenerMensagens );
    }

    public void recuperarMensagens(){

        mensagens.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue( Mensagem.class );
                mensagens.add( mensagem );
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