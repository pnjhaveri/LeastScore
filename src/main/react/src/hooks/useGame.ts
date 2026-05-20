import { useState, useEffect, useCallback, useRef } from 'react';
import { Card, GameState, TurnRequest } from '../types';
import gameApi from '../services/api';
import gameSocket from '../services/socket';

export function useGame(roomCode: string, userId: number | null) {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const myHandRef = useRef<Card[]>([]);

  const fetchAndMergeHand = useCallback(async (state: GameState) => {
    if (!userId) return state;
    try {
      const { cards } = await gameApi.getHand(roomCode);
      myHandRef.current = cards;
      const myPlayer = state.players.find(p => p.userId === userId);
      if (myPlayer) {
        myPlayer.hand = cards;
      }
    } catch (e) {
      console.error('Failed to fetch hand:', e);
    }
    return state;
  }, [roomCode, userId]);

  const refreshState = useCallback(async () => {
    try {
      const state = await gameApi.getGameState(roomCode);
      await fetchAndMergeHand(state);
      setGameState(state);
    } catch (err) {
      console.error('Failed to refresh game state:', err);
    }
  }, [roomCode, fetchAndMergeHand]);

  useEffect(() => {
    if (!roomCode) return;

    refreshState();

    gameSocket.connect();
    gameSocket.subscribeToRoom(roomCode);

    gameSocket.on('game_state', async (state: GameState) => {
      await fetchAndMergeHand(state);
      setGameState(state);
    });

    const pollInterval = setInterval(() => {
      refreshState();
    }, 5000);

    return () => {
      clearInterval(pollInterval);
      gameSocket.off('game_state');
      gameSocket.disconnect();
    };
  }, [roomCode, refreshState, fetchAndMergeHand]);

  const takeTurn = useCallback(
    async (request: TurnRequest) => {
      setLoading(true);
      setError(null);
      try {
        const newState = await gameApi.takeTurn(roomCode, request);
        await fetchAndMergeHand(newState);
        setGameState(newState);
        return newState;
      } catch (err: any) {
        setError(err.message);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [roomCode, fetchAndMergeHand]
  );

  const declare = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const newState = await gameApi.declare(roomCode);
      await fetchAndMergeHand(newState);
      setGameState(newState);
      return newState;
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [roomCode, fetchAndMergeHand]);

  const startNextRound = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const newState = await gameApi.startNextRound(roomCode);
      await fetchAndMergeHand(newState);
      setGameState(newState);
      return newState;
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [roomCode, fetchAndMergeHand]);

  const startNewGame = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const newState = await gameApi.startNewGame(roomCode);
      await fetchAndMergeHand(newState);
      setGameState(newState);
      return newState;
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [roomCode, fetchAndMergeHand]);

  return {
    gameState,
    loading,
    error,
    refreshState,
    takeTurn,
    declare,
    startNextRound,
    startNewGame,
  };
}
