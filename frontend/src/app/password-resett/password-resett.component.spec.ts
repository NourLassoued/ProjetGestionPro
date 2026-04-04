import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordResettComponent } from './password-resett.component';

describe('PasswordResettComponent', () => {
  let component: PasswordResettComponent;
  let fixture: ComponentFixture<PasswordResettComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PasswordResettComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordResettComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
