import { Component, OnInit } from '@angular/core';
import * as Tone from 'tone';
// import { WebSocket } from 'ws';

const synth = new Tone.Synth().toDestination();
// const now = Tone.now();

interface StroopBody {
  colour: string;
  word: string;
}

interface CountdownBody {
  count: number;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent implements OnInit {
  title: String = 'stroop-effect';
  display = {
    colour: "White",
    word: "Pending"
  };
  stroopWS!: WebSocket;
  countdownWS!: WebSocket;

  ngOnInit(): void {
    this.stroopWS = new WebSocket(`ws://${window.location.host}/stroop`)
    this.countdownWS = new WebSocket(`ws://${window.location.host}/count`)
    this.stroopWS.onopen = (event) => {
      console.log('Connected to server', event);
    };
    
    this.stroopWS.onmessage = (event: MessageEvent) => {
      let msg = <StroopBody>JSON.parse(event.data);
      console.log(`Received message from server: ${msg}`);
      this.display.colour = msg.colour;
      this.display.word = msg.word;
      if (this.display.word.length > 0) {
        synth.triggerAttackRelease("C4", 0.5)
      }
      console.log(this.display);
    };
    
    this.stroopWS.onclose = (event) => {
      console.log('Disconnected from server');
    };

    this.countdownWS.onopen = (event) => {
      console.log('Connected to server', event);
    };
    
    this.countdownWS.onmessage = (event: MessageEvent) => {
      let msg = <CountdownBody>JSON.parse(event.data);
      console.log(`Received message from server: ${msg}`);
      if (msg.count > 0) {
        this.display.colour = "white";
        this.display.word = msg.count.toString();
        console.log(this.display);
      } else if (msg.count == 0 && this.display.word == "1") {
        this.display.word = "";
      }
    };
    
    this.countdownWS.onclose = (event) => {
      console.log('Disconnected from server');
    };

    this.stroopWS.onerror = (event) => {
      console.log('Error with server connection', event);
    };
  }
}