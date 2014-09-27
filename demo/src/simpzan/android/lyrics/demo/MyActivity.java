package simpzan.android.lyrics.demo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import simpzan.android.lyrics.Lyrics;
import simpzan.android.lyrics.LyricsView;
import simpzan.android.lyrics.Utils;

public class MyActivity extends Activity {
    private Lyrics lyrics;

    MediaPlayer mediaPlayer_;
    LyricsView lyricsView_;
    private ImageView cover_;

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

        lyrics = new Lyrics();
        lyrics.loadFromStream(getResources().openRawResource(R.raw.test8));

        lyricsView_ = (LyricsView) findViewById(R.id.lyrics);
        lyricsView_.setTexts(lyrics.getLyricLines());
        lyricsView_.setLyricsViewListener(lyricsViewListener);

        mediaPlayer_ = MediaPlayer.create(this, R.raw.test81);
    }

    LyricsView.LyricsViewListener lyricsViewListener = new LyricsView.LyricsViewListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            delayedRefresh(0);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            lyricsView_.setVisibility(View.INVISIBLE);
            return true;
        }
    };
    private Handler handler_ = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int timespan = syncLyrics();
            delayedRefresh(timespan);
        }
    };

    private void delayedRefresh(int delay) {
        if (delay < 0) return;
        handler_.sendEmptyMessageDelayed(0, delay);
    }

    private int syncLyrics() {
        int pos = mediaPlayer_.getCurrentPosition();
        Lyrics.SeekInfo info = lyrics.findTimestamp(pos);
        lyricsView_.seekToIndex(info.index);

        return info.remaining;
    }

    public void play(View view) {
        mediaPlayer_.start();

        int timespan = syncLyrics();
        delayedRefresh(timespan);
    }

    public void pause(View view) {
        mediaPlayer_.pause();
        handler_.removeMessages(0);
    }

    public void back(View view) {
        mediaPlayer_.seekTo(0);
    }

    public void forward(View view) {
        Utils.print("not implemented!");
    }
}
