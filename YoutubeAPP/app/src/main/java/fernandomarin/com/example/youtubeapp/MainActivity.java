package fernandomarin.com.example.youtubeapp;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class MainActivity extends YouTubeBaseActivity
            implements YouTubePlayer.OnInitializedListener{

    private YouTubePlayerView youTubePlayerView;
    private static final String GOOGLE_API_KEY = "AIzaSyDsHavL_d39s-2fnMLGN0nU1C9p6Gqi-Bo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        youTubePlayerView = findViewById(R.id.viewYoutubePlayer);
        youTubePlayerView.initialize( GOOGLE_API_KEY, this );

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean foiRestaurado) {

        // Executa video automaticamente
        //youTubePlayer.loadVideo( "8TXmq4E-0Ns" );

        // Executa video quando usuario pressionar o play somente
        if ( !foiRestaurado ){
            youTubePlayer.cueVideo( "8TXmq4E-0Ns" );
        }

        Toast.makeText(this, "Player iniciado com sucesso!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        Toast.makeText(this, "Erro ao iniciar Player!" + youTubeInitializationResult.toString(), Toast.LENGTH_SHORT).show();

    }
}