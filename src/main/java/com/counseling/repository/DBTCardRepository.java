package com.counseling.repository;

import com.counseling.entity.DBTCard;
import com.counseling.entity.Room;
import com.counseling.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DBTCardRepository extends JpaRepository<DBTCard, String> {
    Optional<DBTCard> findByRoomAndClientAndDate(Room room, User client, LocalDate date);
    List<DBTCard> findByRoomAndClient(Room room, User client);
    List<DBTCard> findByRoom(Room room);
    
    @Query("SELECT d FROM DBTCard d WHERE d.room = :room AND d.date = :date")
    List<DBTCard> findByRoomAndDate(@Param("room") Room room, @Param("date") LocalDate date);
    
    long countByClient(User client);
    long countByRoom(Room room);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DBTCard d WHERE d.room = :room")
    void deleteByRoom(@Param("room") Room room);
}

