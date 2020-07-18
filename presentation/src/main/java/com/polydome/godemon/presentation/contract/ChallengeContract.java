package com.polydome.godemon.presentation.contract;

import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeStatus;

import java.util.List;

public interface ChallengeContract {
    enum Notification {
        CHALLENGER_ALREADY_PARTICIPATES,
        CHALLENGER_NOT_PARTICIPATES,
        CHALLENGER_NOT_REGISTERED,
        REGISTRATION_ALREADY_REGISTERED,
        REGISTRATION_INVALID_HIREZNAME,
        CHALLENGE_CREATED,
        CHALLENGE_NOT_EXISTS
    }

    interface View {
        void showProposition(int challengeId, int[] godsIds);
        void showNotification(Notification notification);
        void showStartingGod(int godId);
        void showChallengeStatus(ChallengeStatus challengeStatus);
        void showRegistrationSuccess(String registeredName);
        void showChallengesList(List<ChallengeBrief> challenges);
    }

    interface Presenter {
        void onJoinChallenge(View challengeView, int challengeId, long challengerId);
        void onCreateChallenge(View challengeView, long challengerId);
        void onShowChallengeStatus(View challengeView, int challengeId, long challengerId);
        void onGodChoice(ChallengeContract.View view, long challengerId, int challengeId, int choice);
        void onRegister(ChallengeContract.View view, long challengerId, String hiRezName);
        void onShowChallengesList(ChallengeContract.View view, long challengerId);
    }
}
