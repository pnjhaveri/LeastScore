import { Card, GameState, RoomInfo, TurnRequest } from '../types';

const API_BASE = '/api';

async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    credentials: 'include',
    ...options,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(error.error || `HTTP ${response.status}`);
  }

  return response.json();
}

export const gameApi = {
  createRoom: () => api<RoomInfo>('/rooms', { method: 'POST' }),

  joinRoom: (roomCode: string) =>
    api<RoomInfo>(`/rooms/${roomCode}/join`, { method: 'POST' }),

  getRoom: (roomCode: string) =>
    api<RoomInfo>(`/rooms/${roomCode}`),

  startGame: (roomCode: string) =>
    api<GameState>(`/rooms/${roomCode}/start`, { method: 'POST' }),

  getGameState: (roomCode: string) =>
    api<GameState>(`/rooms/${roomCode}/state`),

  takeTurn: (roomCode: string, request: TurnRequest) =>
    api<GameState>(`/rooms/${roomCode}/turn`, {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  declare: (roomCode: string) =>
    api<GameState>(`/rooms/${roomCode}/declare`, { method: 'POST' }),

  startNewGame: (roomCode: string) =>
    api<GameState>(`/rooms/${roomCode}/new-game`, { method: 'POST' }),

  startNextRound: (roomCode: string) =>
    api<GameState>(`/rooms/${roomCode}/next-round`, { method: 'POST' }),

  getHand: (roomCode: string) =>
    api<{ cards: Card[] }>(`/rooms/${roomCode}/hand`),

  getSession: () =>
    api<{ userId: number; username: string }>('/session'),

  setUsername: (username: string) =>
    api<{ userId: number; username: string }>('/session/username', {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),
};

export default gameApi;
