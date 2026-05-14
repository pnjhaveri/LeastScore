import React, { useEffect, useState } from 'react';
import { MoveInfo } from '../types';
import gameApi from '../services/api';
import './MoveHistory.css';

interface MoveHistoryProps {
  roomCode: string;
}

export function MoveHistory({ roomCode }: MoveHistoryProps) {
  const [moves, setMoves] = useState<MoveInfo[]>([]);

  useEffect(() => {
    const fetchMoves = async () => {
      try {
        const history = await gameApi.getMoves(roomCode);
        setMoves(history);
      } catch (err) {
        console.error('Failed to fetch moves:', err);
      }
    };

    fetchMoves();
    const interval = setInterval(fetchMoves, 5000);
    return () => clearInterval(interval);
  }, [roomCode]);

  if (moves.length === 0) {
    return null;
  }

  const recentMoves = moves.slice(-10).reverse();

  return (
    <div className="move-history">
      <h4>Recent Moves</h4>
      <div className="moves-list">
        {recentMoves.map((move, index) => (
          <div key={index} className="move-item">
            <span className="move-player">{move.username}:</span>
            <span className="move-type">{formatMoveType(move.moveType)}</span>
            {move.discarded && (
              <span className="move-discarded">Discarded: {move.discarded}</span>
            )}
            <span className="move-arrow">→</span>
            {move.picked && (
              <span className="move-picked">
                {move.source === 'OPEN' ? move.picked : 'Deck'}
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

function formatMoveType(type: string): string {
  switch (type) {
    case 'DRAW_FROM_DECK': return 'Drew';
    case 'TAKE_OPEN_CARD': return 'Took';
    case 'DISCARD_COMBO': return 'Combo';
    case 'DECLARE': return 'Declared';
    case 'START': return 'Started';
    default: return type;
  }
}