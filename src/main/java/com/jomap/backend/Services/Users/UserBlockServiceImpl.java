package com.jomap.backend.Services.Users;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Users.BlockedUserResponse;
import com.jomap.backend.Entities.Friendship.Friendship;
import com.jomap.backend.Entities.Friendship.FriendshipRepository;
import com.jomap.backend.Entities.Friendship.FriendshipStatus;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserBlock;
import com.jomap.backend.Entities.Users.UserBlockRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Entities.Users.Profile.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements UserBlockService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public ApiResponse<String> blockUser(String blockerEmail, Long blockedUserId) {
        User blocker = userRepository.findByEmail(blockerEmail).orElse(null);
        if (blocker == null) {
            return ApiResponse.error("User not found");
        }

        User blocked = userRepository.findById(blockedUserId).orElse(null);
        if (blocked == null) {
            return ApiResponse.error("Target user not found");
        }

        if (blocker.getId().equals(blockedUserId)) {
            return ApiResponse.error("Cannot block yourself");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            return ApiResponse.error("User is already blocked");
        }

        // Remove any existing friendship between the two users
        List<Friendship> friendships = friendshipRepository
                .findByRequesterAndReceiverOrRequesterAndReceiver(blocker, blocked, blocked, blocker);
        for (Friendship f : friendships) {
            friendshipRepository.delete(f);
        }

        UserBlock userBlock = new UserBlock();
        userBlock.setBlocker(blocker);
        userBlock.setBlocked(blocked);
        userBlockRepository.save(userBlock);

        return ApiResponse.success("تم حظر المستخدم بنجاح", "User blocked");
    }

    @Override
    @Transactional
    public ApiResponse<String> unblockUser(String blockerEmail, Long blockedUserId) {
        User blocker = userRepository.findByEmail(blockerEmail).orElse(null);
        if (blocker == null) {
            return ApiResponse.error("User not found");
        }

        User blocked = userRepository.findById(blockedUserId).orElse(null);
        if (blocked == null) {
            return ApiResponse.error("Target user not found");
        }

        if (!userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            return ApiResponse.error("User is not blocked");
        }

        userBlockRepository.deleteByBlockerAndBlocked(blocker, blocked);

        return ApiResponse.success("تم إلغاء حظر المستخدم بنجاح", "User unblocked");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<BlockedUserResponse>> getBlockedUsers(String blockerEmail) {
        User blocker = userRepository.findByEmail(blockerEmail).orElse(null);
        if (blocker == null) {
            return ApiResponse.error("User not found");
        }

        List<UserBlock> blocks = userBlockRepository.findByBlocker(blocker);

        List<BlockedUserResponse> responseList = blocks.stream()
                .map(block -> {
                    User blockedUser = block.getBlocked();
                    String firstName = "";
                    String lastName = "";
                    String profileImageUrl = blockedUser.getProfileImageUrl();

                    var profileOpt = userProfileRepository.findByUserId(blockedUser.getId());
                    if (profileOpt.isPresent()) {
                        var profile = profileOpt.get();
                        firstName = profile.getFirstName() != null ? profile.getFirstName() : "";
                        lastName = profile.getLastName() != null ? profile.getLastName() : "";
                        if (profile.getProfileImageUrl() != null) {
                            profileImageUrl = profile.getProfileImageUrl();
                        }
                    }

                    return new BlockedUserResponse(
                            blockedUser.getId(),
                            blockedUser.getUsername(),
                            profileImageUrl,
                            firstName,
                            lastName
                    );
                })
                .collect(Collectors.toList());

        return ApiResponse.success("تم تحميل قائمة الحظر بنجاح", responseList);
    }
}
