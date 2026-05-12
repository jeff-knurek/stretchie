package com.stretchie.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PoseRepositoryTest {

    private lateinit var repository: PoseRepository

    @Before
    fun setUp() {
        repository = PoseRepository()
    }

    @Test
    fun `getAllPoses returns expected list`() {
        val poses = repository.getAllPoses()
        assertEquals(1, poses.size)
        assertEquals("leg_extension", poses[0].id)
        assertEquals("Leg Extension", poses[0].name)
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
