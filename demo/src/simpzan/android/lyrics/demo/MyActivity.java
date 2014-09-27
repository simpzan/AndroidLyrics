package simpzan.android.lyrics.demo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import simpzan.android.lyrics.Lyrics;
import simpzan.android.lyrics.LyricsController;
import simpzan.android.lyrics.LyricsView;
import simpzan.android.lyrics.Utils;

public class MyActivity extends Activity {
    MediaPlayer mediaPlayer_;
    LyricsView lyricsView_;
    private ImageView cover_;
    private Button playButton_;
    private Button pauseButton_;
    private LyricsController controller_;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getActionBar().hide();
        initialize();
    }

    private void initialize() {
        cover_ = (ImageView)findViewById(R.id.cover);
        cover_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lyricsView_.setVisibility(View.VISIBLE);
            }
        });

        playButton_ = (Button)findViewById(R.id.play_button);
        pauseButton_ = (Button)findViewById(R.id.pause_button);

        lyricsView_ = (LyricsView) findViewById(R.id.lyrics);
        String file = "/sdcard/Music/23.lrc";
        controller_ = new LyricsController(file, lyricsView_, new LyricsController.PlayerDelegate() {
            @Override
            public int getCurrentPosition() {
                return mediaPlayer_.getCurrentPosition();
            }
        });

        lyricsView_.setLyricsViewListener(lyricsViewListener);

        mediaPlayer_ = MediaPlayer.create(this, R.raw.test81);
    }

    LyricsView.LyricsViewListener lyricsViewListener = new LyricsView.LyricsViewListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            lyricsView_.setVisibility(View.INVISIBLE);
            return true;
        }
    };

    public void play(View view) {
        mediaPlayer_.start();

        controller_.sync();

        pauseButton_.setVisibility(View.VISIBLE);
        playButton_.setVisibility(View.GONE);
    }

    public void pause(View view) {
        mediaPlayer_.pause();

        controller_.stop();

        pauseButton_.setVisibility(View.GONE);
        playButton_.setVisibility(View.VISIBLE);
    }

    public void back(View view) {
        mediaPlayer_.seekTo(0);
    }

    public void forward(View view) {
        Utils.print("not implemented!");
    }
}
