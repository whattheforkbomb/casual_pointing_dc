import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BorgRPEComponent } from './borg-rpe.component';

describe('BorgRPEComponent', () => {
  let component: BorgRPEComponent;
  let fixture: ComponentFixture<BorgRPEComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [BorgRPEComponent]
    });
    fixture = TestBed.createComponent(BorgRPEComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
