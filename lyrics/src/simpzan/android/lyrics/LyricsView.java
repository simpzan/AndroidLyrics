package simpzan.android.lyrics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoqing.zgg on 2014/9/14.
 */
public class LyricsView extends View {

    private static final String TAG = LyricsView.class.getName();

    List<String> texts_;

    List<Integer> ys_ = new ArrayList<Integer>();
    List<StaticLayout> layouts_ = new ArrayList<StaticLayout>();
    int topPadding = 250;
    boolean isPanning = false;
    LyricsViewListener lyricsViewListener_;
    private TextPaint textPaint_;
    private GestureDetector gestureDetector_;
    private OverScroller scroller_;
    private int contentHeight_;
    private int index_ = -1;

    public LyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint_ = new TextPaint();
        textPaint_.setTextSize(30);
        textPaint_.setAntiAlias(true);

        gestureDetector_ = new GestureDetector(getContext(), listener_);

        scroller_ = new OverScroller(getContext());
    }

    public void setLyricsViewListener(LyricsViewListener listener) {
        lyricsViewListener_ = listener;
    }

    public void setTexts(List<String> texts) {
        texts_ = texts;
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo(0, -topPadding);
                prepare();
            }
        });
    }

    private void smoothlyScrollTo(int y) {
        int currentY = getScrollY();
        scroller_.startScroll(0, currentY, 0, y - currentY, 1000);
        post(runnable_);
    }

    public void seekToIndex(int index) {
        index_ = index;

        if (index < 0 || index >= ys_.size()) return;
        if (isPanning) return;

        int y = ys_.get(index);
        smoothlyScrollTo(y - topPadding);
    }

    private void prepare() {
        if (getWidth() == 0) return;

        topPadding = getHeight() / 2;
        Log.i(TAG, "top padding:" + topPadding);

        scrollTo(0, -topPadding);

        ys_.clear();
        layouts_.clear();

        int passageSeparatorHeight = 20;
        int y = 0;
        for (String line : texts_) {
            ys_.add(y);

            StaticLayout layout = createStaticLayout(line);
            layouts_.add(layout);

            y += layout.getHeight() + passageSeparatorHeight;
        }
        contentHeight_ = y - topPadding;
        Log.v(TAG, "y array:" + ys_);
    }

    private StaticLayout createStaticLayout(String line) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        return new StaticLayout(line,
                textPaint_,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                1.0f,
                false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawARGB(180, 0, 0, 0);

        if (layouts_.size() == 0) return;

        int begin = findBegin();
        int end = findEnd(begin);
        for (int i = begin; i < end; ++i) {
            drawPassgeAtIndex(canvas, i);
        }

        Log.v(TAG, "index span:" + begin + " - " + end);
    }

    private void drawPassgeAtIndex(Canvas canvas, int i) {
        if (i == index_) {
            textPaint_.setColor(Color.RED);
        } else {
            textPaint_.setColor(Color.WHITE);
        }

        canvas.save();
        canvas.translate(getPaddingLeft(), ys_.get(i));
        layouts_.get(i).draw(canvas);
        canvas.restore();
    }

    private int findEnd(int begin) {
        int y = getHeight() + getScrollY();
        for (int i = begin; i < ys_.size(); ++i) {
            if (ys_.get(i) > y) return i;
        }
        return ys_.size();
    }

    private int findBegin() {
        int y = getScrollY();
        if (y < 0) return 0;

        for (int i = 0; i < ys_.size(); ++i) {
            if (ys_.get(i) > y) return i - 1;
        }
        return ys_.size() - 1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            springBack();
            isPanning = false;
        } else if (action == MotionEvent.ACTION_DOWN) {
            scroller_.forceFinished(true);
            isPanning = true;
        }
        return gestureDetector_.onTouchEvent(event);
    }

    private void springBack() {
        int y = getScrollY();
        boolean isOutOfRange = y > contentHeight_ || y < -topPadding;

        if (isOutOfRange) {
            scroller_.springBack(getScrollX(), getScrollY(),
                    0, 0,
                    -topPadding, contentHeight_);
            post(runnable_);
        }
    }

    public interface LyricsViewListener {
        public boolean onDoubleTap(MotionEvent e);

        public boolean onSingleTapConfirmed(MotionEvent e);
    }

    private Runnable runnable_ = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "run");
            if (scroller_.computeScrollOffset()) {
                scrollTo(0, scroller_.getCurrY());
                post(runnable_);
            }
        }
    };


    GestureDetector.SimpleOnGestureListener listener_ = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            int y = getScrollY();
            boolean isOutOfRange = y > contentHeight_ || y < -topPadding;
            if (isOutOfRange) v2 /= 2;
            scrollBy(0, (int) v2);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int v = (int) (-velocityY);
            scroller_.fling(0, getScrollY()
                    , 0, v
                    , 0, 0
                    , -topPadding, contentHeight_
                    , 0, topPadding / 2
            );
            post(runnable_);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (lyricsViewListener_ != null) lyricsViewListener_.onDoubleTap(e);
            Utils.print(e);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (lyricsViewListener_ != null)
                lyricsViewListener_.onSingleTapConfirmed(e);

            return super.onSingleTapConfirmed(e);
        }
    };

}
