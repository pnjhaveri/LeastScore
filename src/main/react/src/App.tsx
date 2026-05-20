import React, { useState, useEffect } from 'react';
import { useGame } from './hooks/useGame';
import { GameTable } from './components/GameTable';
import { Lobby } from './components/Lobby';
import { RoomInfo } from './types';
import gameApi from './services/api';
import gameSocket from './services/socket';
import './App.css';

function App() {
  const [roomCode, setRoomCode] = useState<string | null>(null);
  const [roomInfo, setRoomInfo] = useState<RoomInfo | null>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [username, setUsername] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [joiningRoom, setJoiningRoom] = useState<string | null>(null);
  const [needsUsername, setNeedsUsername] = useState(false);
  const [initializing, setInitializing] = useState(true);

  const { gameState, takeTurn, declare, startNextRound, startNewGame, refreshState, error: gameError } = useGame(
    roomCode || '',
    userId
  );

  useEffect(() => {
    if (roomCode && userId) {
      gameSocket.connect();
      gameSocket.subscribeToRoom(roomCode);
      gameSocket.on('room_state', (data: any) => {
        if (data.players) {
          setRoomInfo({
            roomCode: data.roomCode,
            status: data.status as RoomInfo['status'],
            players: data.players.map((p: any) => ({
              userId: p.userId,
              username: p.username,
              seatIndex: p.seatIndex,
            })),
          });
        }
      });
      return () => {
        gameSocket.off('room_state');
        gameSocket.disconnect();
      };
    }
  }, [roomCode, userId]);

  useEffect(() => {
    const init = async () => {
      try {
        const session = await gameApi.getSession();
        if (session.userId) {
          setUserId(session.userId);
          setUsername(session.username || `Player${session.userId}`);
        } else {
          setNeedsUsername(true);
        }
      } catch (err) {
        console.error('Failed to get session:', err);
        setNeedsUsername(true);
      }
    };
    init();
  }, []);

  useEffect(() => {
    const path = window.location.pathname;
    const match = path.match(/\/room\/([A-Z0-9]+)/i);
    if (match) {
      setJoiningRoom(match[1].toUpperCase());
    }
  }, []);

  useEffect(() => {
    if (joiningRoom && userId) {
      handleJoinRoom(joiningRoom);
      setJoiningRoom(null);
    }
  }, [joiningRoom, userId]);

  useEffect(() => {
    if (needsUsername || (joiningRoom === null && userId !== null)) {
      const t = setTimeout(() => setInitializing(false), 100);
      return () => clearTimeout(t);
    }
  }, [needsUsername, userId, joiningRoom]);

  const handleCreateRoom = async () => {
    setLoading(true);
    setError(null);
    try {
      const room = await gameApi.createRoom();
      setRoomCode(room.roomCode);
      setRoomInfo(room);
      window.history.pushState({}, '', `/room/${room.roomCode}`);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinRoom = async (code: string) => {
    if (!userId) return;
    setLoading(true);
    setError(null);
    try {
      await gameApi.joinRoom(code);
      const room = await gameApi.getRoom(code);
      setRoomCode(room.roomCode);
      setRoomInfo(room);
      window.history.pushState({}, '', `/room/${room.roomCode}`);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStartGame = async () => {
    if (!roomCode) return;
    setLoading(true);
    setError(null);
    try {
      await gameApi.startGame(roomCode);
      await refreshState();
      setRoomInfo((prev) => (prev ? { ...prev, status: 'IN_GAME' } : null));
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStartNextRound = async () => {
    if (!roomCode) return;
    setLoading(true);
    setError(null);
    try {
      await startNextRound();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStartNewGame = async () => {
    if (!roomCode) return;
    setLoading(true);
    setError(null);
    try {
      await startNewGame();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (initializing) {
    return (
      <div className="app loading">
        <p>Loading...</p>
      </div>
    );
  }

  if (needsUsername) {
    return <UsernameSetup onComplete={(id, name) => {
      setUserId(id);
      setUsername(name);
      setNeedsUsername(false);
    }} />;
  }

  if (!userId) {
    return (
      <div className="app loading">
        <p>Loading...</p>
      </div>
    );
  }

  if (!roomCode) {
    return (
      <div className="app">
        <div className="welcome-screen">
          <h1>Least Score</h1>
          <p>A card game where the lowest score wins!</p>

          <div className="menu-buttons">
            <button
              className="btn btn-primary"
              onClick={handleCreateRoom}
              disabled={loading}
            >
              Create Room
            </button>

            <div className="join-form">
              <input
                type="text"
                placeholder="Enter room code"
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleJoinRoom(e.currentTarget.value.toUpperCase());
                  }
                }}
              />
              <button
                className="btn btn-secondary"
                onClick={() => {
                  const input = document.querySelector('.join-form input') as HTMLInputElement;
                  if (input?.value) {
                    handleJoinRoom(input.value.toUpperCase());
                  }
                }}
                disabled={loading}
              >
                Join
              </button>
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="rules-summary">
            <h3>How to Play</h3>
            <ul>
              <li>2-6 players, 5 cards each</li>
              <li>Discard pairs, two pairs, sequences (3-5), or flushes (5)</li>
              <li>3-of-a-kind is NOT allowed</li>
              <li>Draw from deck or discard pile, then discard one card</li>
              <li>Declare when you think you have the lowest score</li>
              <li>Scoring: A=1, 2-10=face, J/Q/K=10</li>
            </ul>
          </div>
        </div>
      </div>
    );
  }

  const gameStatus = gameState?.status;
  const isScorePage = gameStatus === 'ROUND_ENDED' || gameStatus === 'GAME_OVER' || (gameState && gameState.ended);
  const isLobby = gameStatus === 'LOBBY' || (!gameState && roomInfo?.status === 'LOBBY');
  const isGame = gameState && gameStatus === 'IN_GAME' && !gameState.ended;

  if (isScorePage) {
    const allPlayers = [...gameState!.players, ...gameState!.eliminatedPlayers].sort(
      (a, b) => a.cumulativeScore - b.cumulativeScore
    );
    const winner = allPlayers[0];
    const isGameOver = gameStatus === 'GAME_OVER';

    return (
      <div className="app">
        <div className="round-ended">
          <h2>{isGameOver ? 'Game Over!' : 'Round Ended!'}</h2>
          <div className="scores">
            <h3>Scores</h3>
            {allPlayers.map((player, i) => (
              <div key={player.userId} className={`score-row ${i === 0 ? 'winner' : ''} ${player.eliminated ? 'eliminated' : ''}`}>
                <span className="score-rank">#{i + 1}</span>
                <span className="score-name">{player.username}</span>
                <span className="score-hand">Hand: {player.total}</span>
                <span className="score-total">Total: {player.cumulativeScore}</span>
              </div>
            ))}
          </div>
          {!isGameOver && (
            <button
              className="btn btn-primary"
              onClick={handleStartNextRound}
              disabled={loading}
            >
              {loading ? 'Starting...' : 'Start Next Round'}
            </button>
          )}
          {isGameOver && (
            <div className="game-over">
              <h3>Final Scores</h3>
              <div className="winner-banner">
                <span className="winner-name">{winner.username}</span>
                <span className="winner-score">{winner.cumulativeScore} pts</span>
              </div>
              <div className="final-standings">
                {allPlayers.map((p, i) => (
                  <div key={p.userId} className={`final-row ${i === 0 ? 'winner' : ''} ${p.eliminated ? 'eliminated' : ''}`}>
                    <span className="final-rank">#{i + 1}</span>
                    <span className="final-name">{p.username}</span>
                    <span className="final-score">{p.cumulativeScore}</span>
                    {p.eliminated && <span className="final-badge">ELIMINATED</span>}
                  </div>
                ))}
              </div>
              <div className="game-over-buttons">
                <button
                  className="btn btn-primary"
                  onClick={handleStartNewGame}
                  disabled={loading}
                >
                  {loading ? 'Starting...' : 'Start New Game'}
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => { setRoomCode(null); window.history.pushState({}, '', '/'); }}
                >
                  Leave
                </button>
              </div>
              {error && <div className="error-message">{error}</div>}
            </div>
          )}
        </div>
      </div>
    );
  }

  if (isLobby) {
    return (
      <div className="app">
        <Lobby
          room={roomInfo}
          userId={userId}
          onStartGame={handleStartGame}
          loading={loading}
        />
        {(error || gameError) && <div className="error-message">{error || gameError}</div>}
      </div>
    );
  }

  if (isGame) {
    return (
      <div className="app">
        <div className="game-header">
          <span>Room: {roomCode}</span>
          <span>Round: {gameState!.roundNumber}</span>
        </div>
        <GameTable
          gameState={gameState!}
          userId={userId}
          onTakeTurn={takeTurn}
          onDeclare={declare}
          loading={loading}
        />
        {(error || gameError) && <div className="error-message">{error || gameError}</div>}
      </div>
    );
  }

  return (
    <div className="app loading">
      <p>Loading game...</p>
      {gameError && <div className="error-message">{gameError}</div>}
    </div>
  );
}

function UsernameSetup({ onComplete }: { onComplete: (userId: number, username: string) => void }) {
  const [username, setUsername] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const res = await gameApi.setUsername(username.trim());
      onComplete(res.userId, res.username);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <div className="welcome-screen">
        <h1>Least Score</h1>
        <p>Enter your name to play!</p>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Enter your name"
            disabled={loading}
          />
          {error && <p className="error">{error}</p>}
          <button type="submit" className="btn btn-primary" disabled={loading || !username.trim()}>
            {loading ? 'Starting...' : 'Play'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default App;
