package com.icompound.phone_auth.data.mapper

import com.icompound.core.data.mapper.Mapper
import com.icompound.phone_auth.data.model.dto.ManagerContactResponseDto
import com.icompound.phone_auth.domain.model.ManagerContact

/** No local database for this feature, so the Entity type is Unit. */
class ManagerContactMapper : Mapper<ManagerContactResponseDto, ManagerContact, Unit>() {

    override fun dtoToDomain(model: ManagerContactResponseDto): ManagerContact =
        ManagerContact(
            name = model.data?.name.orEmpty().trim(),
            phone = model.data?.phone.orEmpty().trim(),
            email = model.data?.email.orEmpty().trim(),
        ).also { model.mapBaseFields(it) }      // copies message / code
}


