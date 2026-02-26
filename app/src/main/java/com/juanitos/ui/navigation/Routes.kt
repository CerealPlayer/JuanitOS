package com.juanitos.ui.navigation

enum class Routes(val route: String) {
    Home("home"),
    Food("food"),
    Money("money"),
    MoneySettings("money_settings"),
    NewTransaction("new_transaction"),
    FixedSpending("fixed_spending"),
    NewFixedSpending("new_fixed_spending"),
    Categories("categories"),
    NewCategory("new_category"),
    Workout("workout"),
    NewWorkout("new_workout"),
    EditWorkout("edit_workout/{workoutId}"),
    Exercises("exercises"),
    ExerciseProgress("exercise_progress/{exerciseId}"),
    NewExercise("new_exercise_definition"),
    WorkoutDetail("workout_detail/{workoutId}"),
    Habits("habits");
}
