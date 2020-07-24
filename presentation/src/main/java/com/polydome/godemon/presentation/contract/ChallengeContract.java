package com.polydome.godemon.presentation.contract;

import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeStatus;

import java.util.List;
import java.util.Set;

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
        void showInitialChallengeStatus(ChallengeStatus challengeStatus, int challengeId, boolean isUpdating);
        void showUpdatedChallengeStatus(ChallengeStatus challengeStatus, int challengeId);
        void showRegistrationSuccess(String registeredName);
        void showChallengesList(List<ChallengeBrief> challenges);
        void showModeChoice(Set<GameMode> modes);
        void showLobby(List<ChallengeBrief> challenges);
        void showUpdating();
    }

    interface Presenter {
        void onJoinChallenge(View challengeView, int challengeId, long challengerId);
        void onCreateChallenge(View challengeView, long challengerId);
        void onShowChallengeStatus(View challengeView, int challengeId, long challengerId);
        void onGodChoice(ChallengeContract.View view, long challengerId, int challengeId, int choice);
        void onRegister(ChallengeContract.View view, long challengerId, String hiRezName);
        void onShowChallengesList(ChallengeContract.View view, long challengerId);
        void onModeChoice(View challengeView, long challengerId, GameMode mode);
        void onLobbyRequest(View challengeView, long challengerId);
        void onUpdateChallenge(View challengeView, long challengerId, int challengeId);
    }
}
