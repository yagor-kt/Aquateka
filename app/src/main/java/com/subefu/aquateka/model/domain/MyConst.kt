package com.subefu.aquateka.model.domain

abstract class MyConst{
    companion object{
        val PLANNED = "Запланирован"
        val POSTPONED = "Перенесен"
        val COMPLETED = "Завершен"
        val CANCELLED = "Отменен"

        val SHORT_PLANNED = "План"
        val SHORT_POSTPONED = "Перен."
        val SHORT_COMPLETED = "Завер."
        val SHORT_CANCELLED = "Отмен."

        val CLIENT = "CUSTOMER"
        val VISIT_WITH_CLIENT = "VISIT_WITH_CLIENT"
        val CREATE = "CREATE"
        val TYPE = "TYPE"

        val MANDATORY_FIELD = "Это поле обязательно для заполнения"
        val BAD_COORDINATE = "Формат координат неверен"
        val BAD_DATE = "Формат даты неверен"
        val BAD_PRICE = "Цена должна быть числом"
        val BAD_PERIOD = "Период должен быть числом"

        val BAD_PHONE = "Формат номера неверен"
    }
}