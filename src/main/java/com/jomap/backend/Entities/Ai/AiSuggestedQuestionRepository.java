package com.jomap.backend.Entities.Ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiSuggestedQuestionRepository extends JpaRepository<AiSuggestedQuestion, Long> {

    @Query("SELECT q.question FROM AiSuggestedQuestion q WHERE q.active = true")
    List<String> findAllActiveQuestions();
}
