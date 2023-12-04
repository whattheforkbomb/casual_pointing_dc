import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';

interface ParticipantInfo {
  PID: string;
  condition: string[];
}

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.sass']
})
export class HomePageComponent {

  PID: string = "";
  condition: string[] = [];
  httpClient!: HttpClient;

  constructor(private http: HttpClient) { 
    this.httpClient = http;
  }

  // Need the http client
  // send call to UI on button click

  generateParticipantDetails(pIdxStr: string) {
    let pIdx = parseInt(pIdxStr);
    this.http.post(`http://${window.location.hostname}:8080/study/pid/${pIdx}`, {}, {responseType: 'text'}).subscribe((resp: string) => {
      let parsedResp = <ParticipantInfo>JSON.parse(resp);
      this.PID = parsedResp.PID;
      this.condition = parsedResp.condition;
      console.log("We have new pid.", this.PID, this.condition);
    })
  }

  fetchParticipantDetails() {
    this.http.get(`http://${window.location.hostname}:8080/study/pid`, {responseType: 'text'}).subscribe((resp: string) => {
      let parsedResp = <ParticipantInfo>JSON.parse(resp);
      this.PID = parsedResp.PID;
      this.condition = parsedResp.condition;
      console.log("We fetch a pid.", this.PID, this.condition);
    })
  }

}
