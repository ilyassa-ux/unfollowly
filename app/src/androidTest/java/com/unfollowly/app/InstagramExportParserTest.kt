package com.unfollowly.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unfollowly.app.data.InstagramExportParser
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstagramExportParserTest {
    @Test fun parsesFollowerJson() {
        val json = """[{"string_list_data":[{"value":"alice"},{"value":"bob"}]}]"""
        val result = InstagramExportParser.parse("followers_1.json", json.toByteArray())
        assertEquals(setOf("alice", "bob"), result.followers)
    }
}
