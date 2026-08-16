package com.subefu.aquateka.model.domain

abstract class MyConst{
    companion object{
        val PLANNED = "Запланирован"
        val POSTPONED = "Перенесен"
        val COMPLETED = "Завершен"
        val CANCELLED = "Отменен"

        val CUSTOMER = "CUSTOMER"
        val VISIT_WITH_CLIENT = "VISIT_WITH_CLIENT"
        val CREATE = "CREATE"
        val TYPE = "TYPE"
    }
}