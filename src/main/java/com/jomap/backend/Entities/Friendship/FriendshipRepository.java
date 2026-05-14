package com.jomap.backend.Entities.Friendship;

import com.jomap.backend.Entities.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByRequesterAndReceiver(User requester, User receiver);

    boolean existsByRequesterAndReceiver(User requester, User receiver);

    Optional<Friendship> findByRequesterAndReceiverOrRequesterAndReceiver(
            User requester1,
            User receiver1,
            User requester2,
            User receiver2
    );

    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);

    List<Friendship> findByRequesterAndStatusOrReceiverAndStatus(
            User requester,
            FriendshipStatus status1,
            User receiver,
            FriendshipStatus status2
    );
}