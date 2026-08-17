package com.juanitos.data.money

data class DefaultCategory(val name: String, val description: String?)

val DEFAULT_CATEGORIES: List<DefaultCategory> = listOf(
    DefaultCategory("Rent / Mortgage", "Housing payment"),
    DefaultCategory("Loans", "Personal or student loan payments"),
    DefaultCategory("Electricity", "Power bill"),
    DefaultCategory("Water", "Water bill"),
    DefaultCategory("Gas", "Heating / cooking gas bill"),
    DefaultCategory("Internet", "Home internet / broadband"),
    DefaultCategory("Phone", "Mobile phone plan"),
    DefaultCategory("Groceries", "Supermarket / food shopping"),
    DefaultCategory("Dining Out", "Restaurants, cafes, takeout"),
    DefaultCategory("Car Fuel", "Gasoline, diesel or charging"),
    DefaultCategory("Public Transport", "Bus, train, metro, taxi"),
    DefaultCategory("Car Insurance", "Vehicle insurance premium"),
    DefaultCategory("Health Insurance", "Medical insurance premium"),
    DefaultCategory("Healthcare", "Doctor visits, medicine, pharmacy"),
    DefaultCategory("Subscriptions", "Streaming, software, memberships"),
    DefaultCategory("Entertainment", "Movies, games, leisure activities"),
    DefaultCategory("Shopping", "Clothing and general purchases"),
    DefaultCategory("Savings", "Money set aside or transferred to savings"),
    DefaultCategory("Education", "Tuition, courses, books"),
    DefaultCategory("Personal Care", "Haircuts, cosmetics, gym"),
    DefaultCategory("Gifts & Donations", "Gifts given and charitable donations"),
    DefaultCategory("Miscellaneous", "Anything that doesn't fit elsewhere"),
)
