package com.subefu.aquateka.model.data.db.utill

import com.subefu.aquateka.model.data.entity.ClientEntity
import com.subefu.aquateka.model.data.entity.VisitEntity
import com.subefu.aquateka.model.data.entity.VisitWithClientEntity
import com.subefu.aquateka.model.domain.utill.MyConst
import org.junit.Assert.*
import org.junit.Test

class MapperKtTest {

    private fun visitEntity(id: Int? = null) = VisitEntity(
        id = id,
        clientId = 2,
        address = "Moscow, engelsa, 43b",
        latitude = 54.325422,
        longitude = 34.543342,
        plannedDay = 6,
        plannedMonth = 4,
        plannedYear = 2027,
        actualDate = 0,
        status = MyConst.APPROVE,
        workType = MyConst.WORK_TYPE[3],
        price = 6500,
        parts = "plant, neils",
        period = 6,
        comment = "call first",
        color = MyConst.VISIT_COLOR[2],
    )

    private fun clientEntity(id: Int? = null) = ClientEntity(
        clientId =  id,
        name = "Alex",
        phone = "89876948625",
        address = "Moscow, engelsa, 43b",
        latitude = 54.325422,
        longitude = 34.543342,
        periodMonth = 3,
        comment = "debt is 2000",
    )

    @Test
    fun `VisitEntity to model check fields`(){
        val model = visitEntity(5).toModel()

        assertEquals(5, model.id)
        assertEquals(2, model.clientId)
        assertEquals("Moscow, engelsa, 43b", model.address)
        assertEquals(54.325422, model.latitude, 0.00000001)
        assertEquals(34.543342, model.longitude, 0.00000001)
        assertEquals(6, model.planned_day)
        assertEquals(4, model.planned_month)
        assertEquals(2027, model.planned_year)
        assertEquals(0, model.actual_date)
        assertEquals(MyConst.APPROVE, model.status)
        assertEquals(MyConst.WORK_TYPE[3], model.work_type)
        assertEquals(6500, model.price)
        assertEquals("plant, neils", model.parts)
        assertEquals(6, model.period)
        assertEquals("call first", model.comment)
        assertEquals(MyConst.VISIT_COLOR[2], model.color)
    }

    @Test
    fun `Visit to Entity check fields`(){
        val entity = visitEntity(7).toModel().toEntity()

        assertEquals(7, entity.id)
        assertEquals(2, entity.clientId)
        assertEquals("Moscow, engelsa, 43b", entity.address)
        assertEquals(54.325422, entity.latitude, 0.00000001)
        assertEquals(34.543342, entity.longitude, 0.00000001)
        assertEquals(6, entity.plannedDay)
        assertEquals(4, entity.plannedMonth)
        assertEquals(2027, entity.plannedYear)
        assertEquals(0, entity.actualDate)
        assertEquals(MyConst.APPROVE, entity.status)
        assertEquals(MyConst.WORK_TYPE[3], entity.workType)
        assertEquals(6500, entity.price)
        assertEquals("plant, neils", entity.parts)
        assertEquals(6, entity.period)
        assertEquals("call first", entity.comment)
        assertEquals(MyConst.VISIT_COLOR[2], entity.color)
    }

    @Test
    fun `VisitEntity id null transform to 0`(){
        assertEquals(0, visitEntity().toModel().id)
    }

    @Test
    fun `Visit id 0 transform to null`(){
        val entity = visitEntity(0).toModel().toEntity()

        assertNull(entity.id)
    }

    @Test
    fun `Visit save id after transform`(){
        assertEquals(4, visitEntity(4).toModel().toEntity().id)
    }

    @Test
    fun `Visit round trip return original entity`(){
        val original = visitEntity(5)

        assertEquals(original, original.toModel().toEntity())
    }

    @Test
    fun `ClientEntity to model check fields`(){
        val model = clientEntity(12).toModel()

        assertEquals( 12, model.clietn_id)
        assertEquals("Alex", model.name)
        assertEquals("89876948625", model.phone)
        assertEquals("Moscow, engelsa, 43b", model.address)
        assertEquals(54.325422, model.latitude, 0.00000001)
        assertEquals(34.543342, model.longitude, 0.00000001)
        assertEquals(3, model.period_month)
        assertEquals("debt is 2000", model.comment)
    }

    @Test
    fun `Client to Entity check fields`(){
        val entity = clientEntity(52).toModel().toEntity()

        assertEquals( 52, entity.clientId)
        assertEquals("Alex", entity.name)
        assertEquals("89876948625", entity.phone)
        assertEquals("Moscow, engelsa, 43b", entity.address)
        assertEquals(54.325422, entity.latitude, 0.00000001)
        assertEquals(34.543342, entity.longitude, 0.00000001)
        assertEquals(3, entity.periodMonth)
        assertEquals("debt is 2000", entity.comment)
    }

    @Test
    fun `ClientEntity id null transform to0`(){
        assertEquals(0, clientEntity().toModel().clietn_id)
    }

    @Test
    fun `Client id 0 transform to null`(){
        val entity = clientEntity(0).toModel().toEntity()

        assertNull(entity.clientId)
    }

    @Test
    fun `Client save id after transform`(){
        assertEquals(7, clientEntity(7).toModel().toEntity().clientId)
    }

    @Test
    fun `Client round trip return original entity`(){
        val original = clientEntity(5)

        assertEquals(original, original.toModel().toEntity())
    }

    @Test
    fun `VisitClientEntity combine Visit and Client`(){
        val visitEntity = visitEntity()
        val clientEntity = clientEntity()

        val result = VisitWithClientEntity(visitEntity, clientEntity).toModel()

        assertEquals(visitEntity.toModel(), result.visit)
        assertEquals(clientEntity.toModel(), result.client)
    }
}