package ru.practicum.shareit.request.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestorOrderByCreatedDesc(User user);

    Page<ItemRequest> findAll(Pageable pageable);
}
