import { Component, OnInit } from '@angular/core';
import * as Tone from 'tone';
// import { WebSocket } from 'ws';

const synth = new Tone.Synth().toDestination();
const now = Tone.now();

interface StroopBody {
  colour: string;
  word: string;
}

interface MetaBody {
  count: number;
  progress: number;
  target: number;
  subTarget: number;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent implements OnInit {
  title: String = 'stroop-effect';
  display = {
    colour: "black",
    word: "Pending",
    progress: 0
  };
  stroopWS!: WebSocket;
  metaWS!: WebSocket;
  activeIdx : number = -1;
  activeLED : number = -1;

  ngOnInit(): void {
    this.stroopWS = new WebSocket(`ws://${window.location.hostname}:8080/stroop`)
    this.metaWS = new WebSocket(`ws://${window.location.hostname}:8080/meta`)
    this.stroopWS.onopen = (event) => {
      console.log('Connected to server', event);
      this.display.colour = "gray";
      this.display.word = "";
      synth.triggerAttackRelease("C4", "8n", now);
    };
    
    this.stroopWS.onmessage = (event: MessageEvent) => {
      let msg = <StroopBody>JSON.parse(event.data);
      console.log(`Stroop Received message from server:`, msg);
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

    this.metaWS.onopen = (event) => {
      console.log('Connected to server', event);
    };
    
    this.metaWS.onmessage = (event: MessageEvent) => {
      let msg = <MetaBody>JSON.parse(event.data);
      console.log(`META Received message from server:`, msg);
      let isCountdown = msg.count > -1;
      if (isCountdown) {
        if (msg.count > 0) {
          this.display.colour = "black";
          this.display.word = msg.count.toString();
          this.display.progress = 0;
          console.log(this.display);
        } else if (msg.count == 0 && this.display.word == "1") {
          this.display.word = "";
        }
      } else {
        if (msg.progress > -1) {
          this.display.progress = msg.progress;
        }
        this.activeIdx = msg.target;
        this.activeLED = msg.subTarget;
      }
    };
    
    this.metaWS.onclose = (event) => {
      console.log('Disconnected from server');
    };

    this.stroopWS.onerror = (event) => {
      console.log('Error with server connection', event);
    };
  }
}