import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImiComponent } from './imi.component';

describe('ImiComponent', () => {
  let component: ImiComponent;
  let fixture: ComponentFixture<ImiComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ImiComponent]
    });
    fixture = TestBed.createComponent(ImiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
