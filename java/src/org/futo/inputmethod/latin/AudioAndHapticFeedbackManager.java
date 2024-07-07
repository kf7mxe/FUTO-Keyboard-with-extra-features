/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.futo.inputmethod.latin;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import org.futo.inputmethod.latin.common.Constants;
import org.futo.inputmethod.latin.settings.SettingsValues;

import java.io.FileNotFoundException;

import static org.futo.inputmethod.latin.uix.settings.ComponentsKt.loadSoundPath;
import static org.futo.inputmethod.latin.uix.settings.ComponentsKt.loadSoundUri;

/**
 * This class gathers audio feedback and haptic feedback functions.
 *
 * It offers a consistent and simple interface that allows LatinIME to forget about the
 * complexity of settings and the like.
 */
public final class AudioAndHapticFeedbackManager {
    private AudioManager mAudioManager;
    private Vibrator mVibrator;

    private SoundPool mSoundPool;
    private int mDeleteSound, mEnterSound, mSpaceSound, mKeySound;

    private SettingsValues mSettingsValues;
    private boolean mSoundOn;

    private Context mContext;

    private static final AudioAndHapticFeedbackManager sInstance =
            new AudioAndHapticFeedbackManager();

    public static AudioAndHapticFeedbackManager getInstance() {
        return sInstance;
    }

    private AudioAndHapticFeedbackManager() {
        // Intentional empty constructor for singleton.
    }

    public static void init(final Context context) {
        sInstance.initInternal(context);
    }

    private void initInternal(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        loadCustomSounds(context);
        mContext = context;
    }

    public void performHapticAndAudioFeedback(final int code,
            final View viewToPerformHapticFeedbackOn) {
        performHapticFeedback(viewToPerformHapticFeedbackOn);
        performAudioFeedback(code);
    }

    public boolean hasVibrator() {
        return mVibrator != null && mVibrator.hasVibrator();
    }

    public void vibrate(final long milliseconds) {
        if (mVibrator == null) {
            return;
        }
        mVibrator.vibrate(milliseconds);
    }

    private boolean reevaluateIfSoundIsOn() {
        if (mSettingsValues == null || !mSettingsValues.mSoundOn || mAudioManager == null) {
            return false;
        }
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    public void performAudioFeedback(final int code) {
        // if mAudioManager is null, we can't play a sound anyway, so return
        if (mAudioManager == null) {
            return;
        }
        if (!mSoundOn) {
            return;
        }
        int sound = -1;
        int customSound = -1;
        switch (code) {
        case Constants.CODE_DELETE:
            if (mDeleteSound != -1) {
                customSound = mDeleteSound;
                break;
            }
                sound = AudioManager.FX_KEYPRESS_DELETE;
            break;
        case Constants.CODE_ENTER:
            if (mEnterSound!= -1) {
                customSound = mEnterSound;
                break;
            }
            sound = AudioManager.FX_KEYPRESS_RETURN;
            break;
        case Constants.CODE_SPACE:
            if (mSpaceSound!= -1) {
                customSound = mSpaceSound;
                break;
            }
            sound = AudioManager.FX_KEYPRESS_SPACEBAR;
            break;
        default:
            if (mKeySound!= -1) {
                customSound = mKeySound;
                break;
            }
            sound = AudioManager.FX_KEYPRESS_STANDARD;
            break;
        }
        if(customSound!= -1) {
            mSoundPool.play(customSound, 1, 1, 0, 0, 1);
        }
        if (sound != -1) {
            mAudioManager.playSoundEffect(sound, mSettingsValues.mKeypressSoundVolume);
        }
    }

    public void performHapticFeedback(final View viewToPerformHapticFeedbackOn) {
        if (!mSettingsValues.mVibrateOn) {
            return;
        }
        if (mSettingsValues.mKeypressVibrationDuration >= 0) {
            vibrate(mSettingsValues.mKeypressVibrationDuration);
            return;
        }
        // Go ahead with the system default
        if (viewToPerformHapticFeedbackOn != null) {
            viewToPerformHapticFeedbackOn.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    public void onSettingsChanged(final SettingsValues settingsValues) {
        mSettingsValues = settingsValues;
        mSoundOn = reevaluateIfSoundIsOn();
        loadCustomSounds(mContext);
    }

    public void onRingerModeChanged() {
        mSoundOn = reevaluateIfSoundIsOn();
    }

    private int loadSoundFromUri(Context context, String key) {
        Uri soundUri = loadSoundUri(context, key);
        if (soundUri != null) {
            try {
                AssetFileDescriptor afd = context.getContentResolver().openAssetFileDescriptor(soundUri, "r");
                return mSoundPool.load(afd, 1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return -1; // Invalid sound
    }

    private void loadCustomSounds(Context context) {
        mDeleteSound = loadSoundFromPath(context, "delete_sound");
        mEnterSound = loadSoundFromPath(context, "enter_sound");
        mSpaceSound = loadSoundFromPath(context, "space_sound");
        mKeySound = loadSoundFromPath(context, "key_sound");
    }

    private int loadSoundFromPath(Context context, String key) {
        String soundPath = loadSoundPath(context, key);
        if (soundPath != null &&!soundPath.isEmpty()) {
            try {
                return mSoundPool.load(soundPath, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1; // Invalid sound
    }

}
