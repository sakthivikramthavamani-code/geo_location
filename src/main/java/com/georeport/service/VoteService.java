package com.georeport.service;

import com.georeport.entity.Issue;
import com.georeport.entity.User;
import com.georeport.entity.Vote;
import com.georeport.exception.ResourceNotFoundException;
import com.georeport.repository.IssueRepository;
import com.georeport.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing issue votes/endorsements.
 */
@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private IssueRepository issueRepository;

    /**
     * Toggle vote on an issue - if voted, unvote; if not voted, vote
     * 
     * @return Map containing hasVoted status and voteCount
     */
    @Transactional
    public Map<String, Object> toggleVote(Long issueId, User user) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        boolean hasVoted;
        if (voteRepository.existsByUserIdAndIssueId(user.getId(), issueId)) {
            // Remove vote
            voteRepository.deleteByUserIdAndIssueId(user.getId(), issueId);
            hasVoted = false;
        } else {
            // Add vote
            Vote vote = Vote.builder()
                    .user(user)
                    .issue(issue)
                    .build();
            voteRepository.save(vote);
            hasVoted = true;
        }

        long voteCount = voteRepository.countByIssueId(issueId);

        Map<String, Object> result = new HashMap<>();
        result.put("hasVoted", hasVoted);
        result.put("voteCount", voteCount);
        result.put("issueId", issueId);
        return result;
    }

    /**
     * Get vote status for a user and issue
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getVoteStatus(Long issueId, User user) {
        boolean hasVoted = voteRepository.existsByUserIdAndIssueId(user.getId(), issueId);
        long voteCount = voteRepository.countByIssueId(issueId);

        Map<String, Object> result = new HashMap<>();
        result.put("hasVoted", hasVoted);
        result.put("voteCount", voteCount);
        result.put("issueId", issueId);
        return result;
    }

    /**
     * Get vote count for an issue (public)
     */
    @Transactional(readOnly = true)
    public long getVoteCount(Long issueId) {
        return voteRepository.countByIssueId(issueId);
    }
}
