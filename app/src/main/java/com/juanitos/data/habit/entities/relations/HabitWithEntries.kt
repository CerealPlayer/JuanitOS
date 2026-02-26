package com.juanitos.data.habit.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.HabitEntry

data class HabitWithEntries(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "id",
        entityColumn = "habit_id"
    )
    val entries: List<HabitEntry>,
)
