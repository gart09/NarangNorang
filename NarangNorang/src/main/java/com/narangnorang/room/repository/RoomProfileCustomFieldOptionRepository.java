package com.narangnorang.room.repository;

import com.narangnorang.room.entity.RoomProfileCustomFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomProfileCustomFieldOptionRepository extends JpaRepository<RoomProfileCustomFieldOption, Long> {

    List<RoomProfileCustomFieldOption> findAllByCustomField_IdOrderByDisplayOrderAsc(Long fieldId);
}
