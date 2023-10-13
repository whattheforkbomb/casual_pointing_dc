import { Component, OnInit } from '@angular/core';
// import { WebSocket } from 'ws';

interface StroopBody {
  colour: string;
  word: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent implements OnInit {
  title: String = 'stroop-effect';
  stroop = {
    colour: "red",
    word: "GREEN"
  };
  stroopWS!: WebSocket;

  ngOnInit(): void {
    this.stroopWS = new WebSocket('ws://localhost:8080/stroop')    
    this.stroopWS.onopen = (event) => {
      console.log('Connected to server', event);
      this.stroop.word = "Ready";
      this.stroop.colour = "green";
    };
    
    this.stroopWS.onmessage = (event: MessageEvent) => {
      let msg = <StroopBody>JSON.parse(event.data);
      console.log(`Received message from server: ${msg}`);
      this.stroop.colour = msg.colour;
      this.stroop.word = msg.word;
      console.log(this.stroop);
    };
    
    this.stroopWS.onclose = (event) => {
      console.log('Disconnected from server');
    };

    this.stroopWS.onerror = (event) => {
      console.log('Error with server connection', event);
    };
  }
}