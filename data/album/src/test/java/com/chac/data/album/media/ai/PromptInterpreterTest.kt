package com.chac.data.album.media.ai

import com.chac.domain.album.media.model.PromptCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PromptInterpreterTest {
    private val interpreter = PromptInterpreter()

    @Test
    fun `blank prompt returns null`() {
        val result = interpreter.interpret("   ")

        assertNull(result)
    }

    @Test
    fun `landscape prompt maps to landscape category`() {
        val result = interpreter.interpret("풍경사진 정리해줘")

        assertEquals(setOf(PromptCategory.LANDSCAPE), result?.categories)
    }

    @Test
    fun `animal prompt maps to animal category`() {
        val result = interpreter.interpret("동물 사진만 정리해줘")

        assertEquals(setOf(PromptCategory.ANIMAL), result?.categories)
    }
}
