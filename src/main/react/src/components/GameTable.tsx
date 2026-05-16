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
  const [phase, setPhase] = useState<'draw' | 'discard'>('draw');
  const [drawSource, setDrawSource] = useState<'deck' | 'open' | null>(null);

  const currentPlayerIndex = gameState.currentTurnIndex;
  const currentPlayer = gameState.players[currentPlayerIndex];
  const isMyTurn = currentPlayer?.userId === userId;
  const myPlayer = gameState.players.find((p) => p.userId === userId);

  const handleCardClick = (index: number) => {
    if (!isMyTurn || phase !== 'discard') return;

    setSelectedIndices((prev) => {
      if (prev.includes(index)) {
        return prev.filter((i) => i !== index);
      }
      return [...prev, index];
    });
  };

  const handleDraw = async (source: 'deck' | 'open') => {
    if (!isMyTurn || phase !== 'draw') return;

    setDrawSource(source);
    setPhase('discard');
  };

  const handleDiscard = async () => {
    if (!isMyTurn || phase !== 'discard' || !myPlayer) return;

    try {
      const action: TurnAction =
        drawSource === 'open' ? 'TAKE_OPEN_CARD' : 'DRAW_FROM_DECK';

      if (selectedIndices.length === 1) {
        await onTakeTurn({
          action,
          discardIndices: selectedIndices,
        });
      } else if (selectedIndices.length >= 2) {
        await onTakeTurn({
          action: 'DISCARD_COMBO',
          discardIndices: selectedIndices,
        });
      }

      setSelectedIndices([]);
      setPhase('draw');
      setDrawSource(null);
    } catch (err) {
      console.error('Turn failed:', err);
    }
  };

  const handleDeclare = async () => {
    if (!isMyTurn) return;
    await onDeclare();
  };

  const canDeclare =
    isMyTurn &&
    myPlayer &&
    myPlayer.hand.length === 5 &&
    !gameState.ended;

  return (
    <div className="game-table">
      <div className="table-area">
        <div className="deck-area">
          <div className="deck" onClick={() => handleDraw('deck')}>
            <div className="card-back">Deck</div>
            <span className="deck-count">{gameState.deckSize} cards</span>
          </div>
          {gameState.openCard && (
            <div className="open-card" onClick={() => handleDraw('open')}>
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
            {phase === 'draw' && (
              <div className="action-buttons">
                <button
                  className="btn btn-primary"
                  onClick={() => handleDraw('deck')}
                  disabled={loading}
                >
                  Draw from Deck
                </button>
                {gameState.openCard && (
                  <button
                    className="btn btn-secondary"
                    onClick={() => handleDraw('open')}
                    disabled={loading}
                  >
                    Take Open Card
                  </button>
                )}
              </div>
            )}

            {phase === 'discard' && (
              <div className="action-buttons">
                <button
                  className="btn btn-primary"
                  onClick={handleDiscard}
                  disabled={loading || selectedIndices.length === 0}
                >
                  Discard {selectedIndices.length} card
                  {selectedIndices.length !== 1 ? 's' : ''}
                </button>
              </div>
            )}

            {canDeclare && (
              <button
                className="btn btn-declare"
                onClick={handleDeclare}
                disabled={loading}
              >
                Declare!
              </button>
            )}
          </>
        )}

        {gameState.ended && (
          <div className="game-ended">
            <h3>Game Ended!</h3>
            {gameState.declaredByUserId && (
              <p>
                {gameState.players.find((p) => p.userId === gameState.declaredByUserId)
                  ?.username}{' '}
                declared!
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
