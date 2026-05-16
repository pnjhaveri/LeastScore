import React from 'react';
import { Card as CardType } from '../types';
import './Card.css';

interface CardProps {
  card: CardType;
  selected?: boolean;
  onClick?: (e: React.MouseEvent) => void;
  small?: boolean;
}

const suitSymbols: Record<string, string> = {
  SPADES: '♠',
  HEARTS: '♥',
  DIAMONDS: '♦',
  CLUBS: '♣',
};

const suitColors: Record<string, string> = {
  SPADES: 'black',
  HEARTS: 'red',
  DIAMONDS: 'red',
  CLUBS: 'black',
};

const rankSymbols: Record<number, string> = {
  1: 'A',
  11: 'J',
  12: 'Q',
  13: 'K',
};

export function Card({ card, selected, onClick, small }: CardProps) {
  const rank = card.rank;
  const suit = card.suit;
  const displayRank = rankSymbols[rank] || rank.toString();
  const symbol = suitSymbols[suit];
  const color = suitColors[suit];

  return (
    <div
      className={`card ${selected ? 'selected' : ''} ${small ? 'small' : ''}`}
      onClick={onClick}
      style={{ color }}
    >
      <div className="card-corner top-left">
        <span>{displayRank}</span>
        <span>{symbol}</span>
      </div>
      <div className="card-center">{symbol}</div>
      <div className="card-corner bottom-right">
        <span>{displayRank}</span>
        <span>{symbol}</span>
      </div>
    </div>
  );
}
