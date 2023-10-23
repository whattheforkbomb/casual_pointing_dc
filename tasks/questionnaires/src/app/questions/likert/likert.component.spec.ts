import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LikertComponent } from './likert.component';

describe('LikertComponent', () => {
  let component: LikertComponent;
  let fixture: ComponentFixture<LikertComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LikertComponent]
    });
    fixture = TestBed.createComponent(LikertComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
