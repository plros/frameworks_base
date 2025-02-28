/*
 * Copyright (C) 2018 The LineageOS Project
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

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.tuner.TunerService;

import lineageos.providers.LineageSettings;

public class ClockController implements TunerService.Tunable {

    private static final String TAG = "ClockController";

    private static final String CLOCK_POSITION =
            "lineagesystem:" + LineageSettings.System.STATUS_BAR_CLOCK;

    private static final int CLOCK_POSITION_RIGHT = 0;
    private static final int CLOCK_POSITION_CENTER = 1;
    private static final int CLOCK_POSITION_LEFT = 2;

    private Context mContext;
    private Clock mActiveClock, mCenterClock, mLeftClock, mRightClock;

    private int mClockPosition = CLOCK_POSITION_LEFT;
    private boolean mBlackListed = false;

    public ClockController(Context context, View statusBar) {
        mContext = context;

        mCenterClock = statusBar.findViewById(R.id.clock_center);
        mLeftClock = statusBar.findViewById(R.id.clock);
        mRightClock = statusBar.findViewById(R.id.clock_right);

        mClockPosition = LineageSettings.System.getIntForUser(mContext.getContentResolver(),
                    CLOCK_POSITION, CLOCK_POSITION_LEFT, UserHandle.USER_CURRENT);
        updateActiveClock();

        Dependency.get(TunerService.class).addTunable(this,
                StatusBarIconController.ICON_HIDE_LIST, CLOCK_POSITION);
    }

    public Clock getClock() {
        return mActiveClock;
    }

    private void updateActiveClock() {
        switch (mClockPosition) {
            case CLOCK_POSITION_RIGHT:
                mActiveClock = mRightClock;
                mLeftClock.setClockVisibleByUser(false);
                mCenterClock.setClockVisibleByUser(false);
                mRightClock.setClockVisibleByUser(true);
                // Override any previous setting
                mRightClock.setClockVisibleByUser(!mBlackListed);
                break;
            case CLOCK_POSITION_CENTER:
                mActiveClock = mCenterClock;
                mLeftClock.setClockVisibleByUser(false);
                mRightClock.setClockVisibleByUser(false);
                mCenterClock.setClockVisibleByUser(true);
                // Override any previous setting
                mCenterClock.setClockVisibleByUser(!mBlackListed);
                break;
            case CLOCK_POSITION_LEFT:
            default:
                mActiveClock = mLeftClock;
                mCenterClock.setClockVisibleByUser(false);
                mRightClock.setClockVisibleByUser(false);
                mLeftClock.setClockVisibleByUser(true);
                // Override any previous setting
                mLeftClock.setClockVisibleByUser(!mBlackListed);
                break;
        }
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        Log.d(TAG, "onTuningChanged key=" + key + " value=" + newValue);

        if (CLOCK_POSITION.equals(key)) {
            mClockPosition = TunerService.parseInteger(newValue, CLOCK_POSITION_LEFT);
        } else {
            mBlackListed = StatusBarIconController.getIconHideList(
                    mContext, newValue).contains("clock");
        }
        updateActiveClock();
    }
}
