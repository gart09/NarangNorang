package com.narangnorang.room.repository;

import com.narangnorang.room.entity.RoomProfileCustomField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomProfileCustomFieldRepository
        extends JpaRepository<RoomProfileCustomField, Long> {

    List<RoomProfileCustomField> findAllByRoom_IdOrderByIdAsc(Long roomId);
}
