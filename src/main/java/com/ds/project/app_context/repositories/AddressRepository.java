package com.ds.project.app_context.repositories;

import com.ds.project.app_context.models.Address;
import com.ds.project.app_context.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByUser(User user);
    Optional<Address> findByIdAndUser(String id, User user);
    Optional<Address> findByUserAndIsDefaultTrue(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    void clearDefaultAddress(String userId);
}
