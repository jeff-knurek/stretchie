package com.stretchie.data.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PoseRepositoryTest {

    private lateinit var repository: PoseRepository

    @BeforeEach
    fun setUp() {
        repository = PoseRepository()
    }

    @Test
    fun `getAllPoses returns expected list`() {
        val poses = repository.getAllPoses()
        assertEquals(19, poses.size)
        assertEquals("back_stretch", poses[0].id)
        assertEquals("Back Stretch", poses[0].name)
        assertEquals("it_band", poses[18].id)
        assertEquals("It Band", poses[18].name)
    }

    @Test
    fun `getPoseById returns correct pose for valid ID`() {
        val pose = repository.getPoseById("back_stretch")
        assertNotNull(pose)
        assertEquals("Back Stretch", pose?.name)
    }

    @Test
    fun `getPoseById returns null for invalid ID`() {
        val pose = repository.getPoseById("non_existent_id")
        assertNull(pose)
    }
}
