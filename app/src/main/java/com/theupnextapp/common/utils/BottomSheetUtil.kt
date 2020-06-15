package com.theupnextapp.common.utils

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun showBottomSheet(
    bottomSheetFragment: BottomSheetDialogFragment,
    fragmentArguments: Bundle?,
    fragmentManager: FragmentManager?,
    fragmentTag: String
) {
    bottomSheetFragment.arguments = fragmentArguments
    fragmentManager?.let { bottomSheetFragment.show(it, fragmentTag) }
}