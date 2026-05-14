import { GameState, RoomInfo, TurnRequest } from '../types';

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
  createRoom: () => api<RoomInfo>('/room', { method: 'POST' }),

  joinRoom: (roomCode: string) =>
    api<RoomInfo>(`/room/${roomCode}/join`, { method: 'POST' }),

  getRoom: (roomCode: string) =>
    api<RoomInfo>(`/room/${roomCode}`),

  startGame: (roomCode: string) =>
    api<GameState>(`/room/${roomCode}/start`, { method: 'POST' }),

  getGameState: (roomCode: string) =>
    api<GameState>(`/rooms/${roomCode}/state`),

  takeTurn: (roomCode: string, request: TurnRequest) =>
    api<GameState>(`/rooms/${roomCode}/turn`, {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  declare: (roomCode: string) =>
    api<GameState>(`/rooms/${roomCode}/declare`, { method: 'POST' }),

  startNextRound: (roomCode: string) =>
    api<GameState>(`/room/${roomCode}/next-round`, { method: 'POST' }),

  getSession: () =>
    api<{ userId: number; username: string }>('/session'),

  setUsername: (username: string) =>
    api<{ userId: number; username: string }>('/session/username', {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),
};

export default gameApi;
