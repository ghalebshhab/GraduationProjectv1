package com.start.demo.Entities.Stories;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Storyrepo extends JpaRepository<Story,Long> {
}
