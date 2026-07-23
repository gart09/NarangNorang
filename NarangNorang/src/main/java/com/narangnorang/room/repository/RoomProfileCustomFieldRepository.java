package com.narangnorang.room.repository;

import com.narangnorang.room.entity.OptionType;
import com.narangnorang.room.entity.RoomProfileCustomField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomProfileCustomFieldRepository
        extends JpaRepository<RoomProfileCustomField, Long> {

    List<RoomProfileCustomField> findAllByRoomIdOrderByIdAsc(Long roomId);
    List<RoomProfileCustomField> findAllByRoomIdAndRequiredTrue(Long roomId);


    boolean existsByIdAndOptionTypeIn(Long id, List<OptionType> optionTypes);

    @Query("SELECT o.optionValue " +
            "FROM RoomProfileCustomFieldOption o " +
            "JOIN o.customField r " +
            "WHERE r.id = :fieldId " +
            "AND r.optionType IN ('SINGLE_SELECT', 'MULTI_SELECT')")
    List<String> findSelectTypeOptionsByFieldId(@Param("fieldId") Long fieldId);

}