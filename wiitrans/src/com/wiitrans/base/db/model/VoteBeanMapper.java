package com.wiitrans.base.db.model;

import java.math.BigDecimal;
import java.util.Map;

public interface VoteBeanMapper {
    public int addVote(VoteBean voteBean);
    public int getUnKnowJudgeVoteCount(int strategy_id);
    public Map<String, BigDecimal> getJudgeVoteCount(int strategy_id);
    public void deleteVote(int strategy_id);
}
