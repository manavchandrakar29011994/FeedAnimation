package com.example.cursormagic.ui.components

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentManager
import com.example.cursormagic.ui.theme.CursorMagicTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RecommendedSeriesBottomSheetDialog(val suggestion: List<EpisodicSuggestion>) :
    BottomSheetDialogFragment() {

    companion object {
        const val TAG = "RecommendedSeriesBottomSheetDialog"
    }

    private var exoPlayer: ExoPlayer? = null

    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                sheet.setBackgroundResource(android.R.color.transparent)
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.skipCollapsed = true
                sheet.post { behavior.state = BottomSheetBehavior.STATE_EXPANDED }
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CursorMagicTheme {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        isVisible = true
                        android.util.Log.d(TAG, "BottomSheet visible")
                    }
                    val context = LocalContext.current
                    var isPlayerReady by remember { mutableStateOf(false) }

                    LaunchedEffect(suggestion) {
                        if (exoPlayer == null && suggestion.isNotEmpty()) {
                            android.util.Log.d(TAG, "Initializing ExoPlayer")
                            exoPlayer = buildPreloadingExoPlayer(context)
                            isPlayerReady = true
                            android.util.Log.d(TAG, "ExoPlayer initialized")
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            android.util.Log.d(TAG, "Releasing ExoPlayer")
                            exoPlayer?.release()
                            exoPlayer = null
                            CacheProvider.release()
                        }
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(250, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing))
                    ) {
                        RecommendedBottomSheet(
                            suggestions = suggestion,
                            exoPlayer = exoPlayer,
                            isPlayerReady = isPlayerReady
                        )
                    }
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        android.util.Log.d(TAG, "BottomSheet dismissed, releasing ExoPlayer")
        exoPlayer?.release()
        exoPlayer = null
        CacheProvider.release()
        parentFragmentManager.setFragmentResult("episodic_sheet_dismiss", Bundle.EMPTY)
    }
}