import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BackAdminComponent } from './back-admin.component';

describe('BackAdminComponent', () => {
  let component: BackAdminComponent;
  let fixture: ComponentFixture<BackAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BackAdminComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BackAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
