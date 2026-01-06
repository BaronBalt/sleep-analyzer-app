/*
 * Copyright 2023 Miklos Vajna
 *
 * SPDX-License-Identifier: MIT
 */

@file:Suppress("unused")

package hu.vmiklos.plees_tracker.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.vmiklos.plees_tracker.R

/**
 * Shows stats, counting sleeps after a certain point in the past.
 */
class StatsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
