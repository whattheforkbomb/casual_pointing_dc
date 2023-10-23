import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NasaTLXComponent } from './nasa-tlx.component';

describe('NasaTLXComponent', () => {
  let component: NasaTLXComponent;
  let fixture: ComponentFixture<NasaTLXComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NasaTLXComponent]
    });
    fixture = TestBed.createComponent(NasaTLXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
