import { Component, Input } from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';

export interface LED {
  active: Boolean;
}

@Component({
  selector: 'app-target',
  templateUrl: './target.component.html',
  styleUrls: ['./target.component.sass']
})
export class TargetComponent {
  leds: Array<LED> = [
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false },
    { active: false }
  ]
  @Input() activeIdx!: number;

  constructor(private _sanitizer: DomSanitizer) { }

  getTransform(i: number, j: number): SafeStyle {
    return this._sanitizer.bypassSecurityTrustStyle(`rotateY(${(i-1)*15}deg) rotateX(${-(j-2)*15}deg) translate(${i == 1 ? (j-2)*4 : 0}px, ${Math.abs(j-2)*20}px)`);
  }
}
