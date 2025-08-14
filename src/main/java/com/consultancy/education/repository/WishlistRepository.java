package com.consultancy.education.repository;

import com.consultancy.education.model.Wishlist;
import com.consultancy.education.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByStudent(Student student);
    Optional<Wishlist> findByStudentId(Long studentId);
}
