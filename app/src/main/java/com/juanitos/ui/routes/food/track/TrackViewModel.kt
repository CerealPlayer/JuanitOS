package com.juanitos.ui.routes.food.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanitos.data.food.repositories.FoodRepository
import com.juanitos.lib.parseDbDatetimeToLocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DailyFoodData(
    val date: LocalDate,
    val totalCalories: Int,
    val totalProteins: Double
)

class TrackViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {
    val dailyData = foodRepository.getWeekFoodsStream().map { foodDetailsList ->
        foodDetailsList
            .map { it.toFormattedFoodDetails() }
            .groupBy { food ->
                parseDbDatetimeToLocalDate(food.createdAt) ?: LocalDate.now()
            }
            .map { (date, foods) ->
                DailyFoodData(
                    date = date,
                    totalCalories = foods.sumOf { it.totalCalories },
                    totalProteins = foods.sumOf { it.totalProteins }
                )
            }
            .sortedBy { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )
}
