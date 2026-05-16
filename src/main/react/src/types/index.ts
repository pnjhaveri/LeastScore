export interface Card {
  suit: 'SPADES' | 'HEARTS' | 'DIAMONDS' | 'CLUBS';
  rank: number;
}

export interface PlayerState {
  userId: number;
  username: string;
  hand: Card[];
  total: number;
  cumulativeScore: number;
  eliminated: boolean;
  handSize?: number;
}

export interface MoveEntry {
  username: string;
  moveType: string;
  summary: string;
}

export interface GameState {
  roomCode: string;
  gameId: number;
  players: PlayerState[];
  eliminatedPlayers: PlayerState[];
  currentTurnIndex: number;
  deckSize: number;
  openCard: Card | null;
  ended: boolean;
  declaredByUserId: number | null;
  roundNumber: number;
  turnsInRound: number;
  moves: MoveEntry[];
}

export interface RoomInfo {
  roomCode: string;
  status: 'LOBBY' | 'IN_GAME' | 'ROUND_ENDED' | 'GAME_OVER';
  players: RoomPlayer[];
}

export interface RoomPlayer {
  userId: number;
  username: string;
  seatIndex: number;
}

export type TurnAction = 'TAKE_OPEN_CARD' | 'DRAW_FROM_DECK' | 'DISCARD_COMBO';

export interface TurnRequest {
  action: TurnAction;
  discardIndices: number[];
}
