package com.igorwojda.showcase.feature.forecast.presentation.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val dayOfWeekFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
val dayOfMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
val fullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

fun LocalDate.format(formatter: DateTimeFormatter): String = toJavaLocalDate().format(formatter)

fun LocalDateTime.format(formatter: DateTimeFormatter): String = toJavaLocalDateTime().format(formatter)
