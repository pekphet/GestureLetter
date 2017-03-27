package cn.fish.gesturechecker.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;

import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.DL;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.DN;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.DR;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.IDLE;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.LT;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.RT;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.UL;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.UP;
import static cn.fish.gesturechecker.view.AlphabetDiscView.GestureDirection.UR;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__A;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__E;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__ERR;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__F;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__H;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__J;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__K;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__L;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__M;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__N;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__T;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__U;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__V;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__W;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__X;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__Y;
import static cn.fish.gesturechecker.view.AlphabetDiscView.Letter.__Z;

/**
 * Created by fish on 17-3-27.
 */

public class AlphabetDiscView extends View implements View.OnTouchListener {
    final int RECORD_SIZE = 16;
    final long TIME_THRESHOLD = 1000L;
    final float DISTANCE_SQ_THRESHOLD = 100.0F;
    final float GRADIENT_MIN_THRESHOLD = 0.577F;
    final float GRADIENT_MAX_THRESHOLD = 1.73F;

    private Vector<GestureDirection> mGestureRecord;
    private GestureNode mPointNode = new GestureNode();
    private GestureNode mCurrentNode = new GestureNode();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private LetterRunnable mLetterRunnable = new LetterRunnable();

    private long mLastUpTime = 0L;

    private GestureDirection mCurrDire = IDLE;
    private LetterCallback mLetterCallback;
    private boolean isPosting = false;

    public AlphabetDiscView(Context context) {
        super(context);
        init();
    }

    public AlphabetDiscView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AlphabetDiscView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        mGestureRecord = new Vector<>(RECORD_SIZE);
        setOnTouchListener(this);
    }

    private void initGesture() {
        if (mGestureRecord == null) {
            mGestureRecord = new Vector<>(RECORD_SIZE);
        } else {
            mGestureRecord.clear();
        }
        mPointNode.init();
        mCurrentNode.init();
    }

    public void setOnLetterDisced(LetterCallback callback) {
        this.mLetterCallback = callback;
    }

    private void postLetter() {
        isPosting = true;
        mHandler.postDelayed(mLetterRunnable, TIME_THRESHOLD);
    }

    private void cancelPost() {
        mHandler.removeCallbacks(mLetterRunnable);
    }

    private float calcVecDistance(GestureNode node, float curX, float curY) {
        return (node.x - curX) * (node.x - curX) + (node.y - curY) * (node.y - curY);
    }

    private void addDist() {
        GestureDirection tmpDiretion = parseDist();
        if (tmpDiretion == mCurrDire) {
            mPointNode.cover(mCurrentNode);
        } else {
            mCurrDire = tmpDiretion;
            mGestureRecord.add(tmpDiretion);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mLetterCallback == null) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - mLastUpTime > TIME_THRESHOLD) {
                initGesture();
            }
            if (isPosting) {
                isPosting = false;
                cancelPost();
            }
            mPointNode.x = event.getRawX();
            mPointNode.y = event.getRawY();
            mCurrentNode.x = event.getRawX();
            mCurrentNode.y = event.getRawY();
            super.onTouchEvent(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (calcVecDistance(mCurrentNode, event.getRawX(), event.getRawY()) > DISTANCE_SQ_THRESHOLD) {
                mCurrentNode.x = event.getRawX();
                mCurrentNode.y = event.getRawY();
                addDist();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mCurrDire = IDLE;
            mLastUpTime = System.currentTimeMillis();
            if (mGestureRecord.size() > 0) {
                mGestureRecord.add(IDLE);
                postLetter();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private GestureDirection parseDist() {
        float gradient = Math.abs((mCurrentNode.x - mPointNode.x) / (mCurrentNode.y - mPointNode.y));
        if (gradient >= GRADIENT_MIN_THRESHOLD && gradient <= GRADIENT_MAX_THRESHOLD) {
            if (mCurrentNode.x > mPointNode.x) {
                return mCurrentNode.y > mPointNode.y ? DR : UR;
            } else {
                return mCurrentNode.y > mPointNode.y ? DL : UL;
            }
        } else {
            if (gradient > GRADIENT_MAX_THRESHOLD) {
                return mCurrentNode.x > mPointNode.x ? RT : LT;
            } else {
                return mCurrentNode.y > mPointNode.y ? DN : UP;
            }
        }
    }


    private Letter parseLetter() {
        ZLogLetter();
        int idleCount = checkCount(mGestureRecord, IDLE);
        int ptr = 0;
        GestureDirection currGD = mGestureRecord.get(ptr++);
        if (currGD == UR || currGD == UP) {
            mGestureRecord.add(1, IDLE);
            idleCount++;
            currGD = DL;
        }
        try {
            switch (idleCount) {
                case 1:             // J, L, U, V, W, Z
                    if (currGD == DN || currGD == DL) {
                        // J, L, U
                        currGD = mGestureRecord.get(ptr++);
                        if (currGD == LT && mGestureRecord.get(ptr++) == IDLE) {
                            return __J;
                        } else if (currGD == RT) {
                            currGD = mGestureRecord.get(ptr++);
                            if (currGD == IDLE) {
                                return __L;
                            } else if (currGD == UP && mGestureRecord.get(ptr++) == IDLE) {
                                return __U;
                            }
                        } else if (currGD == UR) {
                            // V, W
                            currGD = mGestureRecord.get(ptr++);
                            if (currGD == IDLE) {
                                return __V;
                            } else if (currGD == DR && mGestureRecord.get(ptr++) == UR && mGestureRecord.get(ptr) == IDLE) {
                                return __W;
                            }
                        }
                    } else if (currGD == RT) {
                        //Z
                        currGD = mGestureRecord.get(ptr++);
                        if (currGD == DL && mGestureRecord.get(ptr++) == RT && mGestureRecord.get(ptr++) == IDLE) {
                            return __Z;
                        }
                    }
                    break;
                case 2:             // K, M, N, T, X, Y
                    if (currGD == DN || currGD == DL) {
                        // K, M, N, X
                        currGD = mGestureRecord.get(ptr++);
                        if (currGD == IDLE) {
                            currGD = mGestureRecord.get(ptr++);
                            if (currGD == DR) {
                                // M, N, X
                                currGD = mGestureRecord.get(ptr++);
                                if (currGD == IDLE) {
                                    return __X;
                                } else if (currGD == UR) {
                                    currGD = mGestureRecord.get(ptr++);
                                    if (currGD == IDLE) {
                                        return __N;
                                    } else if (currGD == DR && mGestureRecord.get(ptr) == IDLE) {
                                        return __M;
                                    }
                                }
                            } else if (currGD == DL) {
                                currGD = mGestureRecord.get(ptr++);
                                if (currGD == DR && mGestureRecord.get(ptr) == IDLE) {
                                    return __K;
                                }
                            }
                        }
                    } else if (currGD == DR) {
                        //X, Y
                        currGD = mGestureRecord.get(ptr++);
                        if (currGD == IDLE) {
                            currGD = mGestureRecord.get(ptr++);
                            if (currGD == DL) {
                                currGD = mGestureRecord.get(ptr++);
                                if (currGD == IDLE) {
                                    return  __X;
                                } else if (currGD == DN && mGestureRecord.get(ptr) == IDLE) {
                                    return __Y;
                                }
                            }
                        }
                    } else if (currGD == RT) {
                        // T
                        currGD = mGestureRecord.get(ptr++);
                        if (currGD == IDLE) {
                            currGD = mGestureRecord.get(ptr++);
                            if (currGD == DN && mGestureRecord.get(ptr) == IDLE) {
                                return __T;
                            }
                        }
                    }
                    break;
                case 3:             // A, E, F, H, I
                    if (currGD == DN || currGD == DL) {
                        // A,H
                        currGD = mGestureRecord.get(ptr++);
                        if (currGD == IDLE) {
                            currGD = mGestureRecord.get(ptr++);
                            if (currGD == DR && mGestureRecord.get(ptr++) == IDLE && mGestureRecord.get(ptr++) == RT && mGestureRecord.get(ptr) == IDLE) {
                                return __A;
                            } else if (currGD == RT && mGestureRecord.get(ptr++) == IDLE && mGestureRecord.get(ptr++) == DN && mGestureRecord.get(ptr) == IDLE) {
                                return __H;
                            }
                        }
                    } else if (currGD == RT) {
                        //E, F
                        currGD = mGestureRecord.get(ptr++);
                        if (currGD == IDLE) {
                            currGD = mGestureRecord.get(ptr++);
                            if (currGD == DN || currGD == DL) {
                                currGD = mGestureRecord.get(ptr++);
                                if (currGD == IDLE) {
                                    //F
                                    currGD = mGestureRecord.get(ptr++);
                                    if (currGD == RT) {
                                        if (mGestureRecord.get(ptr) == IDLE) {
                                            return __F;
                                        }
                                    }
                                } else if (currGD == RT) {
                                    currGD = mGestureRecord.get(ptr++);
                                    if (currGD == IDLE && mGestureRecord.get(ptr++) == RT && mGestureRecord.get(ptr) == IDLE) {
                                        return __E;
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return __ERR;
        }
        return __ERR;
    }

    private void ZLogLetter() {
        StringBuilder sb = new StringBuilder("{");
        boolean isFirst = true;
        for (GestureDirection gd : mGestureRecord) {
            if(gd == IDLE) {
                sb.append(",");
                isFirst = true;
            } else {
                if (!isFirst) {
                    sb.append("->");
                }
                sb.append(gd.toString());
                isFirst = false;
            }
        }
        sb.append("}");
        Log.e("GESTURE", sb.toString());
    }

    private int checkCount(Vector<GestureDirection> arr, GestureDirection direction) {
        int cnt = 0;
        for (GestureDirection d : arr) {
            if (d == direction) {
                cnt++;
            }
        }
        return cnt;
    }

    private class LetterRunnable implements Runnable {
        private Letter mLetter;

        LetterRunnable() {
        }

        @Override
        public void run() {
            if (mLetterCallback != null && isPosting) {
                mLetter = parseLetter();
                mLetterCallback.onDisced(mLetter);
            }
            isPosting = false;
        }
    }

    private class GestureNode {
        float x, y;

        GestureNode() {
        }

        void init() {
            this.x = 0;
            this.y = 0;
        }

        void cover(GestureNode node) {
            this.x = node.x;
            this.y = node.y;
        }
    }

    public interface LetterCallback {
        void onDisced(Letter letter);
    }

    enum GestureDirection {
        IDLE,
        UP,
        UR,
        RT,
        DR,
        DN,
        DL,
        LT,
        UL,
        CL,
        CD,
        CR,
        CU,
        ICD,
    }

    public enum Letter {
        __ERR,
        __A,
        __B,
        __C,
        __D,
        __E,
        __F,
        __G,
        __H,
        __I,
        __J,
        __K,
        __L,
        __M,
        __N,
        __O,
        __P,
        __Q,
        __R,
        __S,
        __T,
        __U,
        __V,
        __W,
        __X,
        __Y,
        __Z,
    }
}
