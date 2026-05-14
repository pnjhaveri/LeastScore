import { GameState } from '../types';

class GameSocket {
  private stompClient: any = null;
  private listeners: Map<string, Set<(data: any) => void>> = new Map();
  private subscriptions: any[] = [];

  connect() {
    if (this.stompClient?.connected) return;

    const socket = new WebSocket('ws://localhost:8080/ws');

    const Stomp = (window as any).Stomp;
    if (!Stomp) {
      console.error('Stomp.js not loaded');
      return;
    }

    this.stompClient = Stomp.over(socket);
    this.stompClient.connect({}, () => {
      console.log('WebSocket connected');
    }, (error: string) => {
      console.error('WebSocket error:', error);
    });
  }

  subscribeToRoom(roomCode: string) {
    if (!this.stompClient?.connected) {
      console.warn('WebSocket not connected');
      return;
    }

    const sub = this.stompClient.subscribe(`/topic/room.${roomCode}.state`, (message: any) => {
      const state = JSON.parse(message.body);
      this.emit('game_state', state);
    });
    this.subscriptions.push(sub);

    const roomSub = this.stompClient.subscribe(`/topic/room.${roomCode}.room`, (message: any) => {
      const roomInfo = JSON.parse(message.body);
      this.emit('room_state', roomInfo);
    });
    this.subscriptions.push(roomSub);

    const playerJoinedSub = this.stompClient.subscribe(`/topic/room.${roomCode}.joined`, (message: any) => {
      this.emit('player_joined', JSON.parse(message.body));
    });
    this.subscriptions.push(playerJoinedSub);

    const playerLeftSub = this.stompClient.subscribe(`/topic/room.${roomCode}.left`, (message: any) => {
      this.emit('player_left', JSON.parse(message.body));
    });
    this.subscriptions.push(playerLeftSub);
  }

  disconnect() {
    this.subscriptions.forEach((sub) => sub?.unsubscribe());
    this.subscriptions = [];
    this.stompClient?.disconnect();
    this.stompClient = null;
  }

  on(event: string, callback: (data: any) => void) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    this.listeners.get(event)!.add(callback);
  }

  off(event: string, callback: (data: any) => void) {
    this.listeners.get(event)?.delete(callback);
  }

  private emit(event: string, data: any) {
    this.listeners.get(event)?.forEach((callback) => callback(data));
  }
}

export const gameSocket = new GameSocket();
export default gameSocket;
