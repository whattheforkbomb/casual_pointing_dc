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
}
