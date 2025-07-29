// ФАЙЛ: TrackSortBy.txt
// ПУТЬ: com/crackmod/flowave/domain/util/TrackSortBy.txt

package com.crackmod.flowave.domain.util

enum class TrackSortBy(val displayName: String) {
    TITLE("Название"),
    ARTIST("Исполнитель"),
    DATE_ADDED("Дата добавления"),
    DURATION("Длительность"),
    YEAR("Год")
}

enum class SortOrder(val displayName: String) {
    ASCENDING("По возрастанию"),
    DESCENDING("По убыванию")
}