package com.robosoft.VirtualLearn.AdminPanel.service;


import com.robosoft.VirtualLearn.AdminPanel.dao.FinalTestDataAccess;
import com.robosoft.VirtualLearn.AdminPanel.entity.FinalTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.request.FinalTestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinalTestService {

    @Autowired
    FinalTestDataAccess testDataAccess;
    public FinalTest finalTestService(FinalTestRequest request) {
        return testDataAccess.getFinalTestS(request);
    }

    public Float getFinalTestResult(FinalTestRequest request) {
        return testDataAccess.getFinalTestResult(request);
    }

    public float userAnswers(UserAnswers userAnswers) {
        return testDataAccess.userAnswers(userAnswers);
    }
}
