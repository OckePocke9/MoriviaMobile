package com.morivia.app.features.watched

import com.morivia.app.features.tracking.TrackingProviderId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchedItemsStoreTest {
    @Test
    fun `concurrent updates publish coherent item snapshots`() = runBlocking {
        val store = WatchedItemsStore()

        coroutineScope {
            repeat(4) { writer ->
                launch(Dispatchers.Default) {
                    repeat(500) { index ->
                        val key = "$writer:$index"
                        val item = WatchedItem(
                            id = key,
                            type = "movie",
                            name = key,
                            markedAtEpochMs = index.toLong(),
                        )
                        store.update { moriviaItems, providerItems, dirtyMoriviaKeys, dirtyProviderKeys ->
                            moriviaItems[key] = item
                            providerItems
                                .getOrPut(TrackingProviderId.TRAKT, ::mutableMapOf)[key] = item
                            dirtyMoriviaKeys += key
                            dirtyProviderKeys
                                .getOrPut(TrackingProviderId.TRAKT, ::mutableSetOf) += key
                        }
                    }
                }
            }
            repeat(4) {
                launch(Dispatchers.Default) {
                    repeat(500) {
                        store.read { moriviaItems, providerItems, dirtyMoriviaKeys, dirtyProviderKeys ->
                            val moriviaKeys = moriviaItems.keys.toSet()
                            assertEquals(moriviaKeys, providerItems[TrackingProviderId.TRAKT].orEmpty().keys)
                            assertEquals(moriviaKeys, dirtyMoriviaKeys)
                            assertEquals(moriviaKeys, dirtyProviderKeys[TrackingProviderId.TRAKT].orEmpty())
                        }
                    }
                }
            }
        }

        store.read { moriviaItems, providerItems, dirtyMoriviaKeys, dirtyProviderKeys ->
            assertEquals(2_000, moriviaItems.size)
            assertEquals(moriviaItems.keys, providerItems[TrackingProviderId.TRAKT].orEmpty().keys)
            assertEquals(moriviaItems.keys, dirtyMoriviaKeys)
            assertEquals(moriviaItems.keys, dirtyProviderKeys[TrackingProviderId.TRAKT])
        }
    }
}
