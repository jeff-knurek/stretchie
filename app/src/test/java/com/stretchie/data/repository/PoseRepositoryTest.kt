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
        assertEquals(2, poses.size)
        assertEquals("leg_extension", poses[0].id)
        assertEquals("Leg Extension", poses[0].name)
        assertEquals("quad-stretch", poses[1].id)
        assertEquals("Quad Stretch", poses[1].name)
    }

    @Test
    fun `getPoseById returns correct pose for valid ID`() {
        val pose = repository.getPoseById("leg_extension")
        assertNotNull(pose)
        assertEquals("Leg Extension", pose?.name)
    }

    @Test
    fun `getPoseById returns null for invalid ID`() {
        val pose = repository.getPoseById("non_existent_id")
        assertNull(pose)
    }
}
