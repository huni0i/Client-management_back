package com.counseling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.counseling.entity.Room;
import com.counseling.entity.RoomMember;
import com.counseling.entity.User;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    Optional<RoomMember> findByRoomAndUser(Room room, User user);
    boolean existsByRoomAndUser(Room room, User user);
    List<RoomMember> findByRoom(Room room);
    List<RoomMember> findByUser(User user);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RoomMember rm WHERE rm.room = :room")
    void deleteByRoom(@Param("room") Room room);
}

