package cn.zh.community.schedule;

import cn.zh.community.cache.HotTagCache;
import cn.zh.community.mapper.QuestionMapper;
import cn.zh.community.model.Question;
import cn.zh.community.model.QuestionExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
/**
 * @Author: zhanghuan
 * @date: 2020/2/1 10:31
 * @description:
 */
@Component
@Slf4j
public class HotTagTasks {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private HotTagCache hotTagCache;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 3)
    public void hotTagSchedule() {
        int offset = 0;
        int limit = 20;
        log.info("hotTagSchedule start {}", new Date());
        List<Question> list = new ArrayList<>();

        Map<String, Integer> priorities = new HashMap<>();
        while (offset == 0 || list.size() == limit) {
            list = questionMapper.selectByExampleWithRowbounds(new QuestionExample(), new RowBounds(offset, limit));
            for (Question question : list) {
                String[] tags = StringUtils.split(question.getTag(), ",");
                for (String tag : tags) {
                    Integer priority = priorities.get(tag);
                    if (priority != null) {
                        priorities.put(tag, priority + 5 + question.getCommentCount());
                    } else {
                        priorities.put(tag, 5 + question.getCommentCount());
                    }
                }
            }
            offset += limit;
        }
        hotTagCache.updateTags(priorities);
        log.info("hotTagSchedule stop {}", new Date());
    }
}
