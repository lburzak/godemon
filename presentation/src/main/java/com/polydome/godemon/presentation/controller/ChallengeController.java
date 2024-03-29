package com.polydome.godemon.presentation.controller;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.exception.ActionForbiddenException;
import com.polydome.godemon.domain.exception.AuthenticationException;
import com.polydome.godemon.domain.exception.NoSuchChallengeException;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeProposition;
import com.polydome.godemon.domain.model.ChallengeStatus;
import com.polydome.godemon.domain.usecase.*;
import com.polydome.godemon.presentation.contract.ChallengeContract;

import java.util.EnumSet;
import java.util.List;

public class ChallengeController implements ChallengeContract.Presenter {
    private final JoinChallengeUseCase joinChallengeUseCase;
    private final AcceptChallengeUseCase acceptChallengeUseCase;
    private final GetChallengeStatusUseCase getChallengeStatusUseCase;
    private final IntroduceUseCase introduceUseCase;
    private final StartChallengeUseCase startChallengeUseCase;
    private final GetAvailableChallengesUseCase getAvailableChallengesUseCase;
    private final GetAllChallengesUseCase getAllChallengesUseCase;

    public ChallengeController(JoinChallengeUseCase joinChallengeUseCase, AcceptChallengeUseCase acceptChallengeUseCase, GetChallengeStatusUseCase getChallengeStatusUseCase, IntroduceUseCase introduceUseCase, StartChallengeUseCase startChallengeUseCase, GetAvailableChallengesUseCase getAvailableChallengesUseCase, GetAllChallengesUseCase getAllChallengesUseCase) {
        this.joinChallengeUseCase = joinChallengeUseCase;
        this.acceptChallengeUseCase = acceptChallengeUseCase;
        this.getChallengeStatusUseCase = getChallengeStatusUseCase;
        this.introduceUseCase = introduceUseCase;
        this.startChallengeUseCase = startChallengeUseCase;
        this.getAvailableChallengesUseCase = getAvailableChallengesUseCase;
        this.getAllChallengesUseCase = getAllChallengesUseCase;
    }

    @Override
    public void onJoinChallenge(ChallengeContract.View view, int challengeId, long challengerId) {
        ChallengeProposition proposition;

        try {
            proposition = joinChallengeUseCase.withChallengeId(challengerId, challengeId);
        } catch (ActionForbiddenException e) {
            view.showNotification(ChallengeContract.Notification.CHALLENGER_ALREADY_PARTICIPATES);
            return;
        } catch (AuthenticationException e) {
            view.showNotification(ChallengeContract.Notification.CHALLENGER_NOT_REGISTERED);
            return;
        }

        view.showProposition(challengeId, proposition.getGods());
    }

    @Override
    public void onShowChallengesList(ChallengeContract.View view, long challengerId) {
        List<ChallengeBrief> challenges = getAvailableChallengesUseCase.withChallengerId(challengerId);
        view.showChallengesList(challenges);
    }

    @Override
    public void onCreateChallenge(ChallengeContract.View challengeView, long challengerId) {
        challengeView.showModeChoice(EnumSet.allOf(GameMode.class));
    }

    @Override
    public void onModeChoice(ChallengeContract.View challengeView, long challengerId, GameMode mode) {
        int challengeId;

        try {
            challengeId = startChallengeUseCase.execute(challengerId, mode);
        } catch (AuthenticationException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGER_NOT_REGISTERED);
            return;
        }

        ChallengeStatus status = getChallengeStatusUseCase.execute(challengerId, challengeId, false);

        challengeView.showNotification(ChallengeContract.Notification.CHALLENGE_CREATED);
        challengeView.showInitialChallengeStatus(status, challengeId, false).subscribe();
    }

    @Override
    public void onRegister(ChallengeContract.View view, long challengerId, String hiRezName) {
        IntroduceUseCase.Result result = introduceUseCase.execute(challengerId, hiRezName);

        if (result.getError() == null) {
            view.showRegistrationSuccess(result.getNewName());
        } else {
            switch (result.getError()) {
                case CHALLENGER_ALREADY_REGISTERED: view.showNotification(ChallengeContract.Notification.REGISTRATION_ALREADY_REGISTERED);
                case PLAYER_NOT_EXISTS: view.showNotification(ChallengeContract.Notification.REGISTRATION_INVALID_HIREZNAME);
            };
        }
    }

    @Override
    public void onShowChallengeStatus(ChallengeContract.View challengeView, int challengeId, long challengerId) {
        try {
            ChallengeStatus status;

            try {
                status = getChallengeStatusUseCase.execute(challengerId, challengeId, false);
            } catch (NoSuchChallengeException e) {
                challengeView.showNotification(ChallengeContract.Notification.CHALLENGE_NOT_EXISTS);
                return;
            }

            challengeView.showInitialChallengeStatus(status, challengeId, true).subscribe(() -> {
                ChallengeStatus updatedStatus = getChallengeStatusUseCase.execute(challengerId, challengeId, true);
                challengeView.showUpdatedChallengeStatus(updatedStatus, challengeId);
            });
        } catch (AuthenticationException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGER_NOT_REGISTERED);
        } catch (NoSuchChallengeException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGE_NOT_EXISTS);
        } catch (ActionForbiddenException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGER_NOT_PARTICIPATES);
        }
    }

    @Override
    public void onGodChoice(ChallengeContract.View view, long challengerId, int challengeId, int choice) {
        acceptChallengeUseCase.execute(challengerId, challengeId, choice);
        view.showStartingGod(choice);
    }

    @Override
    public void onLobbyRequest(ChallengeContract.View challengeView, long challengerId) {
        List<ChallengeBrief> challenges = List.of();

        try {
            challenges = getAllChallengesUseCase.execute(challengerId);
        } catch (AuthenticationException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGER_NOT_REGISTERED);
        }

        challengeView.showLobby(challenges);
    }

    @Override
    public void onUpdateChallenge(ChallengeContract.View challengeView, long challengerId, int challengeId) {
        challengeView.showUpdating();

        try {
            ChallengeStatus status = getChallengeStatusUseCase.execute(challengerId, challengeId, true);
            challengeView.showUpdatedChallengeStatus(status, challengeId);
        } catch (AuthenticationException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGER_NOT_REGISTERED);
        } catch (NoSuchChallengeException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGE_NOT_EXISTS);
        } catch (ActionForbiddenException e) {
            challengeView.showNotification(ChallengeContract.Notification.CHALLENGER_NOT_PARTICIPATES);
        }
    }
}
