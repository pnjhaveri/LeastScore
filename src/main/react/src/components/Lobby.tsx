import React from 'react';
import { RoomInfo } from '../types';
import './Lobby.css';

interface LobbyProps {
  room: RoomInfo | null;
  userId: number;
  onStartGame: () => void;
  loading: boolean;
}

export function Lobby({ room, userId, onStartGame, loading }: LobbyProps) {
  if (!room) return null;

  const isHost = room.players.length > 0 && room.players[0].userId === userId;
  const canStart = isHost && room.players.length >= 2 && room.status === 'LOBBY';

  return (
    <div className="lobby">
      <h2>Room: {room.roomCode}</h2>
      
      <div className="players-list">
        <h3>Players ({room.players.length}/6)</h3>
        {room.players.map((player, index) => (
          <div key={player.userId} className="player-item">
            <span className="player-seat">Seat {index + 1}</span>
            <span className="player-name">
              {player.username}
              {index === 0 && <span className="host-badge">HOST</span>}
            </span>
          </div>
        ))}
        {room.players.length < 6 && (
          <div className="waiting-for-players">
            Waiting for players...
          </div>
        )}
      </div>

      {canStart && (
        <button
          className="btn btn-start"
          onClick={onStartGame}
          disabled={loading}
        >
          {loading ? 'Starting...' : 'Start Game'}
        </button>
      )}

      <div className="share-link">
        <p>Share this link to invite players:</p>
        <code>{window.location.origin}/room/{room.roomCode}</code>
      </div>

      <div className="rules-info">
        <h4>Rules:</h4>
        <ul>
          <li>2-6 players, 5 cards each</li>
          <li>Discard pairs, two pairs, sequences of 3-5, flushes of 5</li>
          <li>3-of-a-kind NOT allowed</li>
          <li>After discard, draw from deck or discard pile</li>
          <li>Declare when you have the lowest score</li>
          <li>Scoring: A=1, 2-10=face value, J/Q/K=10</li>
        </ul>
      </div>
    </div>
  );
}
