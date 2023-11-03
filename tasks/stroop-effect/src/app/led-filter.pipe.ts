import { Pipe, PipeTransform } from '@angular/core';
import { LED } from './target/target.component';

@Pipe({
  name: 'ledFilter'
})
export class LedFilterPipe implements PipeTransform {

  transform(value: LED, filter: number): undefined {
    // led
  }

}
