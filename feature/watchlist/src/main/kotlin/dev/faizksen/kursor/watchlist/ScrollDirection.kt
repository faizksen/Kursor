package dev.faizksen.kursor.watchlist

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

@Composable
internal fun rememberScrollingUp(state: LazyGridState): Boolean {
    var previousIndex by remember(state) { mutableIntStateOf(state.firstVisibleItemIndex) }
    var previousOffset by remember(state) { mutableIntStateOf(state.firstVisibleItemScrollOffset) }
    var scrollingUp by remember(state) { mutableStateOf(true) }

    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                scrollingUp = if (index == previousIndex) {
                    offset <= previousOffset
                } else {
                    index < previousIndex
                }
                previousIndex = index
                previousOffset = offset
            }
    }

    return scrollingUp
}
