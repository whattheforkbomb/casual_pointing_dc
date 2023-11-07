import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LedArrayComponent } from './led-array.component';

describe('LedArrayComponent', () => {
  let component: LedArrayComponent;
  let fixture: ComponentFixture<LedArrayComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LedArrayComponent]
    });
    fixture = TestBed.createComponent(LedArrayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
