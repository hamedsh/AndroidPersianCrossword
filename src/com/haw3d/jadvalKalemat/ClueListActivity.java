/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.haw3d.jadvalKalemat;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.haw3d.jadvalKalemat.io.IO;
import com.haw3d.jadvalKalemat.puz.Playboard.Clue;
import com.haw3d.jadvalKalemat.puz.Playboard.Position;
import com.haw3d.jadvalKalemat.puz.Playboard.Word;
import com.haw3d.jadvalKalemat.puz.Puzzle;
import com.haw3d.jadvalKalemat.view.ClueImageView;
import com.haw3d.jadvalKalemat.view.CrosswordImageView.ClickListener;

public class ClueListActivity extends jadvalKalematActivity {
    private Configuration configuration;
    private File baseFile;
    private ImaginaryTimer timer;
    private KeyboardView keyboardView = null;
    private ListView across;
    private ListView down;
    private Puzzle puz;
    private ClueImageView imageView;
    private TabHost tabHost;
    private boolean useNativeKeyboard = false;
    private boolean hasSetInitialZoom = false;

    private static final Logger LOG = Logger.getLogger("com.haw3d.jadvalKalemat");

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;
        try {
            if (this.prefs.getBoolean("forceKeyboard", false)
                    || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
                    || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        utils.holographic(this);
        utils.finishOnHomeButton(this);
        try {
            configuration = getBaseContext().getResources().getConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.device_configuration_error),
                    Toast.LENGTH_LONG).show();
            finish();
        }

        // Not sure how this can happen, but it's happened at least once
        if (jadvalKalematApplication.BOARD == null) {
            LOG.warning("ClueListActivity: BOARD is null!");
            finish();
            return;
        }

        this.timer = new ImaginaryTimer(jadvalKalematApplication.BOARD.getPuzzle()
                .getTime());

        Uri u = this.getIntent().getData();

        if (u != null) {
            if (u.getScheme().equals("file")) {
                baseFile = new File(u.getPath());
            }
        }

        puz = jadvalKalematApplication.BOARD.getPuzzle();
        timer.start();
        setContentView(R.layout.clue_list);

        int keyboardType = "CONDENSED_ARROWS".equals(prefs.getString(
                "keyboardType", "")) ? R.xml.keyboard_dpad : R.xml.keyboard;
        Keyboard keyboard = new Keyboard(this, keyboardType);
        keyboardView = (KeyboardView) this.findViewById(R.id.clueKeyboard);
        keyboardView.setKeyboard(keyboard);
        this.useNativeKeyboard = "NATIVE".equals(prefs.getString(
                "keyboardType", ""));

        if (this.useNativeKeyboard) {
            keyboardView.setVisibility(View.GONE);
        }

        keyboardView
                .setOnKeyboardActionListener(new OnKeyboardActionListener() {
                    private long lastSwipe = 0;

                    public void onKey(int primaryCode, int[] keyCodes) {
                        long eventTime = System.currentTimeMillis();

                        if ((eventTime - lastSwipe) < 500) {
                            return;
                        }

                        KeyEvent event = new KeyEvent(eventTime, eventTime,
                                KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD
                                        | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        ClueListActivity.this.onKeyDown(primaryCode, event);
                    }

                    public void onPress(int primaryCode) {}

                    public void onRelease(int primaryCode){}

                    public void onText(CharSequence text) {}

                    public void swipeDown() {}

                    public void swipeLeft() {
                        long eventTime = System.currentTimeMillis();
                        lastSwipe = eventTime;

                        KeyEvent event = new KeyEvent(eventTime, eventTime,
                                KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD
                                        | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        ClueListActivity.this.onKeyDown(
                                KeyEvent.KEYCODE_DPAD_LEFT, event);
                    }

                    public void swipeRight() {
                        long eventTime = System.currentTimeMillis();
                        lastSwipe = eventTime;

                        KeyEvent event = new KeyEvent(eventTime, eventTime,
                                KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD
                                        | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        ClueListActivity.this.onKeyDown(
                                KeyEvent.KEYCODE_DPAD_RIGHT, event);
                    }

                    public void swipeUp() {
                    }
                });

        imageView = (ClueImageView)this.findViewById(R.id.miniboard);
        imageView.setUseNativeKeyboard(useNativeKeyboard);

        imageView.setClickListener(new ClickListener() {
            public void onClick(Position pos) {
                if (pos == null) {
                    return;
                }
                Word current = jadvalKalematApplication.BOARD.getCurrentWord();
                int newAcross = current.start.across;
                if(current.across){
                    newAcross = current.start.across-(current.length-1);
                }
                int newDown = current.start.down;
                int box = pos.across;

                if (box >= current.length) {
                    return;
                }

                if (tabHost.getCurrentTab() == 0) {
                    newAcross += box;
                } else {
                    newDown += box;
                }

                Position newPos = new Position(newAcross, newDown);

                if (!newPos.equals(jadvalKalematApplication.BOARD.getHighlightLetter())) {
                    jadvalKalematApplication.BOARD.setHighlightLetter(newPos);
                    render();
                }
            }

            public void onDoubleClick(Position pos) {
                // No-op
            }

            public void onLongClick(Position pos) {
                // No-op
            }
        });

        this.tabHost = (TabHost)this.findViewById(R.id.tabhost);
        this.tabHost.setup();

        TabSpec ts = tabHost.newTabSpec("TAB1");

        ts.setIndicator(getResources().getString(R.string.across),
            getResources().getDrawable(R.drawable.across));

        ts.setContent(R.id.acrossList);

        this.tabHost.addTab(ts);

        ts = this.tabHost.newTabSpec("TAB2");

        ts.setIndicator(getResources().getString(R.string.down),
            getResources().getDrawable(R.drawable.down));

        ts.setContent(R.id.downList);
        this.tabHost.addTab(ts);

        this.tabHost.setCurrentTab(jadvalKalematApplication.BOARD.isAcross() ? 0 : 1);

        this.across = (ListView) this.findViewById(R.id.acrossList);
        this.down = (ListView) this.findViewById(R.id.downList);

        across.setAdapter(new ArrayAdapter<Clue>(this,
                android.R.layout.simple_list_item_1, jadvalKalematApplication.BOARD
                        .getAcrossClues()));
        across.setFocusableInTouchMode(true);
        down.setAdapter(new ArrayAdapter<Clue>(this,
                android.R.layout.simple_list_item_1, jadvalKalematApplication.BOARD
                        .getDownClues()));
        across.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                arg0.setSelected(true);
                jadvalKalematApplication.BOARD.jumpTo(arg2, true);
                imageView.setTranslate(0.0f, 0.0f);
                render();

                if (prefs.getBoolean("snapClue", false)) {
                    across.setSelectionFromTop(arg2, 5);
                    across.setSelection(arg2);
                }
            }
        });
        across.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (!jadvalKalematApplication.BOARD.isAcross()
                        || (jadvalKalematApplication.BOARD.getCurrentClueIndex() != arg2)) {
                    jadvalKalematApplication.BOARD.jumpTo(arg2, true);
                    imageView.setTranslate(0.0f, 0.0f);
                    render();

                    if (prefs.getBoolean("snapClue", false)) {
                        across.setSelectionFromTop(arg2, 5);
                        across.setSelection(arg2);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        down.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {
                jadvalKalematApplication.BOARD.jumpTo(arg2, false);
                imageView.setTranslate(0.0f, 0.0f);
                render();

                if (prefs.getBoolean("snapClue", false)) {
                    down.setSelectionFromTop(arg2, 5);
                    down.setSelection(arg2);
                }
            }
        });

        down.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (jadvalKalematApplication.BOARD.isAcross()
                        || (jadvalKalematApplication.BOARD.getCurrentClueIndex() != arg2)) {
                    jadvalKalematApplication.BOARD.jumpTo(arg2, false);
                    imageView.setTranslate(0.0f, 0.0f);
                    render();

                    if (prefs.getBoolean("snapClue", false)) {
                        down.setSelectionFromTop(arg2, 5);
                        down.setSelection(arg2);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        this.render();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasSetInitialZoom) {
            imageView.fitToHeight();
            hasSetInitialZoom = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Word w = jadvalKalematApplication.BOARD.getCurrentWord();
        Position last = new Position(w.start.across
                + (w.across ? (w.length - 1) : 0), w.start.down
                + ((!w.across) ? (w.length - 1) : 0));

        switch (keyCode) {
        case KeyEvent.KEYCODE_MENU:
            return false;

        case KeyEvent.KEYCODE_BACK:
            this.setResult(0);

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:

            if (!jadvalKalematApplication.BOARD.getHighlightLetter().equals(
                    jadvalKalematApplication.BOARD.getCurrentWord().start)) {
                jadvalKalematApplication.BOARD.previousLetter();

                this.render();
            }

            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:

            if (!jadvalKalematApplication.BOARD.getHighlightLetter().equals(last)) {
                jadvalKalematApplication.BOARD.nextLetter();
                this.render();
            }

            return true;

        case KeyEvent.KEYCODE_DEL:
            w = jadvalKalematApplication.BOARD.getCurrentWord();
            jadvalKalematApplication.BOARD.deleteLetter();

            Position p = jadvalKalematApplication.BOARD.getHighlightLetter();

            if (!w.checkInWord(p.across, p.down)) {
                jadvalKalematApplication.BOARD.setHighlightLetter(w.start);
            }

            this.render();

            return true;

        case KeyEvent.KEYCODE_SPACE:

            if (!prefs.getBoolean("spaceChangesDirection", true)) {
                jadvalKalematApplication.BOARD.playLetter(' ');

                Position curr = jadvalKalematApplication.BOARD.getHighlightLetter();

                if (!jadvalKalematApplication.BOARD.getCurrentWord().equals(w)
                        || (jadvalKalematApplication.BOARD.getBoxes()[curr.down][curr.across] == null)) {
                    jadvalKalematApplication.BOARD.setHighlightLetter(last);
                }

                this.render();

                return true;
            }
        }

        char c = Character
                .toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
                        .getDisplayLabel() : ((char) keyCode));

        //if (PlayActivity.ALPHA.indexOf(c) != -1) {
            jadvalKalematApplication.BOARD.playLetter(c);

            Position p = jadvalKalematApplication.BOARD.getHighlightLetter();

            if (!jadvalKalematApplication.BOARD.getCurrentWord().equals(w)
                    || (jadvalKalematApplication.BOARD.getBoxes()[p.down][p.across] == null)) {
                jadvalKalematApplication.BOARD.setHighlightLetter(last);
            }

            this.render();

            if (puz.isSolved() && (timer != null)) {
                timer.stop();
                puz.setTime(timer.getElapsed());
                this.timer = null;
                Intent i = new Intent(ClueListActivity.this, PuzzleFinishedActivity.class);
                this.startActivity(i);

            }

            return true;
        //}

        //return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            this.finish();

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            if ((puz != null) && (baseFile != null)) {
                if ((timer != null) && !puz.isSolved()) {
                    this.timer.stop();
                    puz.setTime(timer.getElapsed());
                    this.timer = null;
                }

                IO.save(puz, baseFile);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (shouldShowKeyboard(configuration)) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(this.imageView.getWindowToken(), 0);
            }
        }
    }

    private void render() {
        if (shouldShowKeyboard(configuration)) {
            if (this.useNativeKeyboard) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            } else {
                this.keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            this.keyboardView.setVisibility(View.GONE);
        }

        imageView.render();
    }
}
