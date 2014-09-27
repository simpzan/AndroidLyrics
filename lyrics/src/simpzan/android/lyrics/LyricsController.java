package simpzan.android.lyrics;

import android.os.Handler;
import android.os.Message;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by simpzan on 9/27/14.
 */
public class LyricsController {
    private Lyrics lyrics_;
    private LyricsView lyricsView_;
    private PlayerDelegate player_;
    private Handler handler_ = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            sync();
        }
    };

    public LyricsController(String lrcFile, LyricsView view, PlayerDelegate player) {
        lyrics_ = new Lyrics();
        lyrics_.loadFromFile(lrcFile);

        lyricsView_ = view;
        lyricsView_.setTexts(lyrics_.getLyricLines());
        player_ = player;
    }

    public void sync() {
        int timespan = syncLyrics();
        delayedRefresh(timespan);
    }

    public void stop() {
        handler_.removeMessages(0);
    }

    private void delayedRefresh(int delay) {
        if (delay < 0) return;
        handler_.sendEmptyMessageDelayed(0, delay);
    }

    private int syncLyrics() {
        int pos = player_.getCurrentPosition();
        Lyrics.SeekInfo info = lyrics_.findTimestamp(pos);
        lyricsView_.seekToIndex(info.index);

        return info.remaining;
    }

    public interface PlayerDelegate {
        public int getCurrentPosition();
    }
}
