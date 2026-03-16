package sp26.group3.computer.sba301_computershop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group3.computer.sba301_computershop.entity.Blog;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Integer> {
    List<Blog> findByUser_UserId(int userId);
    Page<Blog> findAll(Pageable pageable);
    Page<Blog> findByUser_UserId(int userId, Pageable pageable);
    boolean existsByTitle(String title);
}