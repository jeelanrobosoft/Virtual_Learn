package com.robosoft.VirtualLearn.AdminPanel.service;

import com.robosoft.VirtualLearn.AdminPanel.dao.ModuleTestDataAccess;
import com.robosoft.VirtualLearn.AdminPanel.dto.ModuleTestRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultAnswerRequest;
import com.robosoft.VirtualLearn.AdminPanel.dto.ResultHeaderRequest;
import com.robosoft.VirtualLearn.AdminPanel.entity.ModuleTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuleTestService {

    @Autowired
    ModuleTestDataAccess dataAccess;

    public ModuleTest moduleTestQuestions(ModuleTestRequest request) {
        return dataAccess.moduleTestQuestions(request);
    }

    public float userAnswers(UserAnswers userAnswers) {
        return dataAccess.userAnswers(userAnswers);
    }

    public ResultHeaderRequest getResultHeader(ModuleTestRequest testRequest) {
        return dataAccess.getResultHeader(testRequest);
    }

    public List<ResultAnswerRequest> getResultAnswers(ModuleTest request) {
        return dataAccess.getResultAnswers(request);
    }
}
