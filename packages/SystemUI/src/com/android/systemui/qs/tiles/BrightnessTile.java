/*
 * Copyright (C) 2015 The Dirty Unicorns Project
 * Copyright (C) 2018 Benzo Rom
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

package com.android.systemui.qs.tiles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.provider.Settings;
import android.net.Uri;
import android.os.UserHandle;
import android.service.quicksettings.Tile;

import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import javax.inject.Inject;

public class BrightnessTile extends QSTileImpl<BooleanState> {

    private static final String SCREEN_BRIGHTNESS_MODE = "screen_brightness_mode";
    private static final int SCREEN_BRIGHTNESS_MODE_MANUAL = 0;
    private static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1;

    @Inject
    public BrightnessTile(QSHost host) {
        super(host);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    private ContentObserver mObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            refreshState();
        }
    };

    @Override
    public void handleSetListening(boolean listening) {
        if (listening) {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                    false, mObserver);
        } else {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    @Override
    public void handleClick() {
        mHost.collapsePanels();
        mContext.startActivityAsUser(new Intent(
            Intent.ACTION_SHOW_BRIGHTNESS_DIALOG), UserHandle.CURRENT_OR_SELF);
    }

    @Override
    protected void handleSecondaryClick() {
        toggleState();
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public void handleLongClick() {
        toggleState();
        refreshState();
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_brightness_label);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_QUICK_TILES;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean autoBrightness =
            getBrightnessState() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;

        if (autoBrightness) {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_brightness_auto_on);
            state.label = mContext.getString(R.string.quick_settings_auto_brightness_on);
            state.state = Tile.STATE_ACTIVE;
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_brightness_auto_off);
            state.label = mContext.getString(R.string.quick_settings_auto_brightness_off);
            state.state = Tile.STATE_INACTIVE;
        }
    }

    protected void toggleState() {
        int mode = getBrightnessState();
        switch (mode) {
            case SCREEN_BRIGHTNESS_MODE_MANUAL:
                Settings.System.putInt(mContext.getContentResolver(),
                    SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                break;
            case SCREEN_BRIGHTNESS_MODE_AUTOMATIC:
                Settings.System.putInt(mContext.getContentResolver(),
                    SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
                break;
        }
    }

    private int getBrightnessState() {
        return Settings.System.getIntForUser(mContext.getContentResolver(),
            Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
            UserHandle.USER_CURRENT);
    }
}
