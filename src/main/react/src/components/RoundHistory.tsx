import React from 'react';
import './RoundHistory.css';

interface PlayerScore {
  userId: number;
  username: string;
  cumulativeScore: number;
  handScore: number;
  eliminated: boolean;
}

interface RoundScoreEntry {
  roundNumber: number;
  scores: PlayerScore[];
}

interface RoundHistoryProps {
  rounds: RoundScoreEntry[];
  currentScores: { userId: number; cumulativeScore: number; username: string }[];
}

export function RoundHistory({ rounds, currentScores }: RoundHistoryProps) {
  if (rounds.length === 0 && currentScores.length === 0) {
    return null;
  }

  const players = currentScores.length > 0
    ? currentScores
    : rounds.length > 0
      ? rounds[0].scores.map(p => ({ userId: p.userId, username: p.username, cumulativeScore: 0 }))
      : [];

  return (
    <div className="round-history">
      <h4>Score History</h4>
      <div className="round-table">
        <div className="round-header">
          <span className="round-col">Round</span>
          {players.map(p => (
            <span key={p.userId} className={`player-col ${p.cumulativeScore >= 100 ? 'eliminated' : ''}`}>
              {p.username}
            </span>
          ))}
        </div>

        {rounds.map((entry, idx) => (
          <div key={idx} className="round-row">
            <span className="round-col">{entry.roundNumber}</span>
            {players.map(player => {
              const score = entry.scores.find(s => s.userId === player.userId);
              return (
                <span
                  key={player.userId}
                  className={`player-col ${score?.eliminated ? 'eliminated' : ''}`}
                >
                  {score?.cumulativeScore ?? 0}
                </span>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
}