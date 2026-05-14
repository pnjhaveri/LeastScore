import { useState, useEffect, useCallback } from 'react';
import { GameState, TurnRequest } from '../types';
import gameApi from '../services/api';
import gameSocket from '../services/socket';

export function useGame(roomCode: string, userId: number | null) {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshState = useCallback(async () => {
    try {
      const state = await gameApi.getGameState(roomCode);
      setGameState(state);
    } catch (err) {
      console.error('Failed to refresh game state:', err);
    }
  }, [roomCode]);

  useEffect(() => {
    if (!roomCode) return;

    refreshState();

    gameSocket.connect();
    gameSocket.subscribeToRoom(roomCode);

    gameSocket.on('game_state', (state: GameState) => {
      setGameState(state);
    });

    return () => {
      gameSocket.off('game_state');
      gameSocket.disconnect();
    };
  }, [roomCode, refreshState]);

  const takeTurn = useCallback(
    async (request: TurnRequest) => {
      setLoading(true);
      setError(null);
      try {
        const newState = await gameApi.takeTurn(roomCode, request);
        setGameState(newState);
        return newState;
      } catch (err: any) {
        setError(err.message);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [roomCode]
  );

  const declare = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const newState = await gameApi.declare(roomCode);
      setGameState(newState);
      return newState;
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [roomCode]);

  const startNextRound = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const newState = await gameApi.startNextRound(roomCode);
      setGameState(newState);
      return newState;
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [roomCode]);

  return {
    gameState,
    loading,
    error,
    refreshState,
    takeTurn,
    declare,
    startNextRound,
  };
}
