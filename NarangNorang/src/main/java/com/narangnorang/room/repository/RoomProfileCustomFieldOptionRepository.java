package com.narangnorang.room.repository;

import com.narangnorang.room.entity.RoomProfileCustomFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomProfileCustomFieldOptionRepository extends JpaRepository<RoomProfileCustomFieldOption, Long> {

    List<RoomProfileCustomFieldOption> findAllByCustomFieldIdOrderByDisplayOrderAsc(Long fieldId);
}
