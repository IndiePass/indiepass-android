// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.photoeditor.filters;

import ja.burhanrashid52.photoeditor.PhotoFilter;

public interface FilterListener {
    void onFilterSelected(PhotoFilter photoFilter);
}
