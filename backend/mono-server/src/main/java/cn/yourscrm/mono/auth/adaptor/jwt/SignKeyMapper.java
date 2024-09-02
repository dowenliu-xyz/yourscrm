package cn.yourscrm.mono.auth.adaptor.jwt;

import cn.yourscrm.mono.auth.domain.jwt.SignKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SignKeyMapper {
    SignKeyMapper INSTANCE = Mappers.getMapper(SignKeyMapper.class);

    SignKey toDomain(SignKeyEntity entity);

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastModifiedAt", expression = "java(java.time.LocalDateTime.now())")
    SignKeyEntity toEntity(SignKey domain);
}
