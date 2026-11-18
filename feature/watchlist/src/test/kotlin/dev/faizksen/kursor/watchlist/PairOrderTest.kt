package dev.faizksen.kursor.watchlist

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PairOrderTest {

    private val ids = listOf(1L, 2L, 3L, 4L)

    @Test
    fun `a card dropped one place down takes that place`() {
        assertThat(ids.moved(from = 0, to = 1)).containsExactly(2L, 1L, 3L, 4L).inOrder()
    }

    @Test
    fun `a card dropped one place up takes that place`() {
        assertThat(ids.moved(from = 2, to = 1)).containsExactly(1L, 3L, 2L, 4L).inOrder()
    }

    @Test
    fun `a card carried to the far end lands there`() {
        assertThat(ids.moved(from = 0, to = 3)).containsExactly(2L, 3L, 4L, 1L).inOrder()
    }

    @Test
    fun `a card carried to the top lands there`() {
        assertThat(ids.moved(from = 3, to = 0)).containsExactly(4L, 1L, 2L, 3L).inOrder()
    }

    @Test
    fun `dropping a card where it already is changes nothing`() {
        assertThat(ids.moved(from = 2, to = 2)).isEqualTo(ids)
    }

    @Test
    fun `an index off the end changes nothing`() {
        assertThat(ids.moved(from = -1, to = 2)).isEqualTo(ids)
        assertThat(ids.moved(from = 1, to = 9)).isEqualTo(ids)
    }

    @Test
    fun `the header offset cannot push an index out of the list`() {
        assertThat(ids.moved(from = 0 - 1, to = 1 - 1)).isEqualTo(ids)
    }

    @Test
    fun `every card survives the move`() {
        assertThat(ids.moved(from = 1, to = 3)).containsExactlyElementsIn(ids)
    }
}
