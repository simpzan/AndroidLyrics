package simpzan.android.lyrics;

import android.util.Log;
import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by guoqing.zgg on 2014/9/15.
 */
public class Lyrics {
    private static final String TAG = Lyrics.class.getName();


    public boolean loadFromFile(String lrcFile) {
        String data = readAllFromFile(lrcFile);
        if (data == null) return false;
        return parse(data);
    }

    public List<String> getLyricLines() {
        List<String> result = new ArrayList<String>();
        for (LyricLine line : lyricLines) {
            result.add(line.text);
        }
        return result;
    }

    public class SeekInfo {
        public final int remaining;
        public final int index;

        public SeekInfo(int index, int remaining) {
            this.index = index;
            this.remaining = remaining;
        }
    }

    private int findIndexOfTimestamp(int ms) {
        for (int i = 0; i < lyricLines.size(); ++i) {
            LyricLine line = lyricLines.get(i);
            if (line.timestamp > ms) return i;
        }
        return lyricLines.size();
    }

    public SeekInfo findTimestamp(int ms) {
        int index = findIndexOfTimestamp(ms);
        int nextTs = 0;
        if (index < lyricLines.size()) {
            nextTs = lyricLines.get(index).timestamp;
        }
        int remaining = nextTs - ms;
//        if (ms < 0) {
//        Utils.print("ms:" + ms + " index:" + nextTs);
//        }
        SeekInfo info = new SeekInfo(index - 1, remaining);
        return info;
    }

    String getFileEncoding(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        return encoding;
    }

    byte[] readContentOfFile(String file) {
        File f = new File(file);
        byte[] result = new byte[(int)f.length()];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(f);
            fileInputStream.read(result);
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileInputStream != null) try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readAllFromFile(String file) {
        byte[] bytes = readContentOfFile(file);
        if (bytes == null)  return null;
        String encoding = getFileEncoding(bytes);
        try {
            return new String(bytes, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int TimeString2Int(String time) {
        DateFormat format = new SimpleDateFormat("mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date t = null;
        try {
            t = format.parse(time + "0");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "parsing time string error:" + time);
            return 0;
        }
        int result = (int) t.getTime();
        Log.v(TAG, "result:" + result);
        return result;
    }

    static public class LyricLine {
        public LyricLine(String ts, String text) {
            this.text = text;
            timestamp = TimeString2Int(ts);
        }

        static public LyricLine from(String line) {
            if (line == null) return null;

            line = line.trim();
            if (line.length() < 4 || line.charAt(0) != '[') return null;

            int pos = line.indexOf("]");
            if (pos == -1) return null;

            String time = line.substring(1, pos);
            String text = line.substring(pos + 1);
            LyricLine lyricLine = new LyricLine(time, text);
            return lyricLine;
        }

        int timestamp;
        String text;
    }

    private List<LyricLine> lyricLines = new ArrayList<LyricLine>();

    private boolean parse(String rawData) {
        lyricLines.add(new LyricLine("00:00.00", ""));
        String[] lines = rawData.split("\n");
        for (String line : lines) {
            LyricLine lyricLine = LyricLine.from(line);
            if (lyricLine != null) lyricLines.add(lyricLine);
        }
        return true;
    }
}
