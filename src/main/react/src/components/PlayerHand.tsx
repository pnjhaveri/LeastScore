import React from 'react';
import { Card } from './Card';
import { Card as CardType, PlayerState } from '../types';
import './PlayerHand.css';

interface PlayerHandProps {
  player: PlayerState;
  isCurrentPlayer: boolean;
  isMyHand: boolean;
  selectedIndices: number[];
  onSelectCard: (index: number) => void;
}

export function PlayerHand({
  player,
  isCurrentPlayer,
  isMyHand,
  selectedIndices,
  onSelectCard,
}: PlayerHandProps) {
  return (
    <div className={`player-hand ${isCurrentPlayer ? 'current-turn' : ''}`}>
      <div className="player-info">
        <span className="player-name">
          {player.username}
          {isCurrentPlayer && <span className="turn-indicator">▶</span>}
        </span>
        <span className="player-score">Score: {player.total}</span>
        {player.cumulativeScore > 0 && (
          <span className="cumulative-score">Total: {player.cumulativeScore}</span>
        )}
        {player.eliminated && <span className="eliminated">ELIMINATED</span>}
      </div>
      <div className="hand-cards">
        {player.hand.map((card, index) => (
          <Card
            key={`${card.suit}-${card.rank}-${index}`}
            card={card}
            selected={selectedIndices.includes(index)}
            onClick={isMyHand ? () => onSelectCard(index) : undefined}
          />
        ))}
      </div>
    </div>
  );
}
