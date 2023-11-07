import { Component, Input } from '@angular/core';
import { LED } from '../target/target.component';

@Component({
  selector: 'app-led-array',
  templateUrl: './led-array.component.html',
  styleUrls: ['./led-array.component.sass']
})
export class LedArrayComponent {
    // leds: Array<LED> = [
    //   { active: false },
    //   { active: false },
    //   { active: false },
    //   { active: false },
    //   { active: false },
    //   { active: false },
    //   { active: false },
    //   { active: false },
    //   { active: false }
    // ]
    @Input() activeLED!: number;
    @Input() clusterActive!: boolean;
}
