import React, { useState } from 'react';
import { Card } from './Card';
import { PlayerHand } from './PlayerHand';
import { GameState, TurnRequest, TurnAction } from '../types';
import './GameTable.css';

interface GameTableProps {
  gameState: GameState;
  userId: number;
  onTakeTurn: (request: TurnRequest) => Promise<void>;
  onDeclare: () => Promise<void>;
  loading: boolean;
}

export function GameTable({
  gameState,
  userId,
  onTakeTurn,
  onDeclare,
  loading,
}: GameTableProps) {
  const [selectedIndices, setSelectedIndices] = useState<number[]>([]);
  const [selectedSource, setSelectedSource] = useState<'deck' | 'open' | null>(null);

  const currentPlayerIndex = gameState.currentTurnIndex;
  const currentPlayer = gameState.players[currentPlayerIndex];
  const isMyTurn = currentPlayer?.userId === userId;
  const myPlayer = gameState.players.find((p) => p.userId === userId);

  const canSelectCards = isMyTurn && !gameState.ended;

  const handleCardClick = (index: number) => {
    if (!canSelectCards) return;
    setSelectedIndices((prev) => {
      if (prev.includes(index)) return prev.filter((i) => i !== index);
      return [...prev, index];
    });
  };

  const handleSourceClick = (source: 'deck' | 'open') => {
    if (!isMyTurn || gameState.ended) return;
    setSelectedSource((prev) => (prev === source ? null : source));
  };

  const handleMove = async () => {
    if (!isMyTurn || !selectedSource || selectedIndices.length === 0 || !myPlayer) return;
    try {
      const action: TurnAction =
        selectedSource === 'open' ? 'TAKE_OPEN_CARD' : 'DRAW_FROM_DECK';
      await onTakeTurn({ action, discardIndices: selectedIndices });
      setSelectedIndices([]);
      setSelectedSource(null);
    } catch (err) {
      console.error('Move failed:', err);
    }
  };

  const handleDeclare = async () => {
    if (!isMyTurn || gameState.ended) return;
    if (selectedIndices.length > 0 || selectedSource !== null) return;
    await onDeclare();
  };

  const canMove = isMyTurn && !gameState.ended && selectedSource !== null && selectedIndices.length > 0 && !loading;
  const canDeclare = isMyTurn && !gameState.ended && selectedSource === null && selectedIndices.length === 0 && myPlayer && myPlayer.hand.length === 5 && myPlayer.total <= 10 && !loading;
  const moveHint = !selectedSource ? 'Select deck or open card' : 'Select cards to discard';

  return (
    <div className="game-table">
      <div className="table-area">
        <div
          className={`deck-area ${selectedSource === 'deck' ? 'source-selected' : ''}`}
          onClick={() => handleSourceClick('deck')}
        >
          <div className="deck">
            <div className="card-back">Deck</div>
            <span className="deck-count">{gameState.deckSize} left</span>
          </div>
          {gameState.openCard && (
            <div
              className={`open-card ${selectedSource === 'open' ? 'source-selected' : ''}`}
              onClick={(e) => { e.stopPropagation(); handleSourceClick('open'); }}
            >
              <Card card={gameState.openCard} />
            </div>
          )}
        </div>

        <div className="players-area">
          {gameState.players.map((player, index) => (
            <PlayerHand
              key={player.userId}
              player={player}
              isCurrentPlayer={index === currentPlayerIndex}
              isMyHand={player.userId === userId}
              selectedIndices={player.userId === userId ? selectedIndices : []}
              onSelectCard={handleCardClick}
            />
          ))}
        </div>
      </div>

      <div className="action-area">
        {isMyTurn && !gameState.ended && (
          <>
            <div className="action-hint">{moveHint}</div>
            <div className="action-buttons">
              <button
                className="btn btn-move"
                onClick={handleMove}
                disabled={!canMove}
              >
                Move
              </button>
              <button
                className="btn btn-declare"
                onClick={handleDeclare}
                disabled={!canDeclare}
              >
                Declare
              </button>
            </div>
          </>
        )}

        {!isMyTurn && !gameState.ended && (
          <div className="waiting-turn">
            Waiting for {currentPlayer?.username}'s turn...
          </div>
        )}

        {gameState.ended && (
          <div className="game-ended">
            <h3>Round Over</h3>
            {gameState.declaredByUserId && (
              <p>
                {gameState.players.find((p) => p.userId === gameState.declaredByUserId)
                  ?.username} declared!
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
