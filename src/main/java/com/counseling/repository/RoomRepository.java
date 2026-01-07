package com.counseling.repository;

import com.counseling.entity.Room;
import com.counseling.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    Optional<Room> findByInviteCode(String inviteCode);
    List<Room> findByCreatedBy(User createdBy);
    
    @Query("SELECT r FROM Room r JOIN RoomMember rm ON r.roomId = rm.room.roomId WHERE rm.user.userId = :userId")
    List<Room> findRoomsByUserId(@Param("userId") String userId);
}

