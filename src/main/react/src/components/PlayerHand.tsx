import React from 'react';
import { Card } from './Card';
import { Card as CardType, PlayerState } from '../types';
import './PlayerHand.css';

interface PlayerHandProps {
  player: PlayerState;
  isCurrentPlayer: boolean;
  isMyHand: boolean;
  selectedIndices: number[];
  onSelectCard: (index: number, ctrlKey?: boolean) => void;
}

function CardBack({ small }: { small?: boolean }) {
  return (
    <div className={`card card-back ${small ? 'small' : ''}`}>
      <div className="card-back-inner">?</div>
    </div>
  );
}

export function PlayerHand({
  player,
  isCurrentPlayer,
  isMyHand,
  selectedIndices,
  onSelectCard,
}: PlayerHandProps) {
  const displayHand = isMyHand ? player.hand : [];
  const cardCount = isMyHand ? player.hand.length : (player.handSize || 0);

  return (
    <div className={`player-hand ${isCurrentPlayer ? 'current-turn' : ''}`}>
      <div className="player-info">
        <span className="player-name">
          {player.username}
          {isCurrentPlayer && <span className="turn-indicator">▶</span>}
        </span>
        {isMyHand && (
          <>
            <span className="player-score">Score: {player.total}</span>
            {player.cumulativeScore > 0 && (
              <span className="cumulative-score">Total: {player.cumulativeScore}</span>
            )}
          </>
        )}
        {isMyHand && player.eliminated && <span className="eliminated">ELIMINATED</span>}
        {!isMyHand && <span className="card-count">{cardCount} cards</span>}
      </div>
      <div className="hand-cards">
        {isMyHand
          ? displayHand.map((card, index) => (
              <Card
                key={`${card.suit}-${card.rank}`}
                card={card}
                selected={selectedIndices.includes(index)}
                onClick={(e) => onSelectCard(index, e.ctrlKey || e.metaKey)}
              />
            ))
          : Array.from({ length: cardCount }, (_, i) => (
              <CardBack key={i} small />
            ))}
      </div>
    </div>
  );
}
