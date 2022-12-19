package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.dao.FinalTestDataAccess;
import com.robosoft.VirtualLearn.AdminPanel.dao.ModuleTestDataAccess;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultAnswerRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultHeaderRequest;
import com.robosoft.VirtualLearn.AdminPanel.entity.ModuleTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.response.SubmitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ModuleTestService {

    @Autowired
    ModuleTestDataAccess dataAccess;

    @Autowired
    FinalTestDataAccess finalTestDataAccess;

    public ModuleTest moduleTestQuestions(Integer testId) {
        return dataAccess.moduleTestQuestions(testId);
    }

    public SubmitResponse userAnswers(UserAnswers userAnswers) throws SQLException {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer courseScore = dataAccess.checkForCourseScore(userAnswers.getTestId());
        if(courseScore != null){
            Integer courseId = dataAccess.getCourseId(userAnswers.getTestId());
            float coursePercentage = finalTestDataAccess.calculateOverallScore(courseId,userName);
            finalTestDataAccess.updateCourseScore(coursePercentage,courseId,userName);
        }
        return dataAccess.userAnswers(userAnswers);
    }

    public ResultHeaderRequest getResultHeader(Integer testId) {
        return dataAccess.getResultHeader(testId);
    }

    public List<ResultAnswerRequest> getResultAnswers(Integer testId) {
        return dataAccess.getResultAnswers(testId);
    }

    public String checkForCompletedStatus(Integer testId) {
        return dataAccess.checkForCompletedStatus(testId);
    }
}
