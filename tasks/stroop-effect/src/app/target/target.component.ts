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
  @Input() activeIdx!: number;
  @Input() activeLED!: number;

  constructor(private _sanitizer: DomSanitizer) { }

  isActive(i: number, j: number): boolean {
    console.log(`row: ${this.activeIdx % 3}, col: ${Math.floor(this.activeIdx / 3)}`);
    return Math.floor(this.activeIdx/3) == j && this.activeIdx % 3 == i
  }

  getTransform(i: number, j: number): SafeStyle {
    return this._sanitizer.bypassSecurityTrustStyle(`rotateY(${(i-1)*15}deg) rotateX(${-(j-2)*15}deg) skew(${-(j-2)}deg, ${(j-2)*2}deg) translate(${i == 1 ? (j-2)*4 : 0}px, ${Math.abs(j-2)*20}px)`);
  }
}
