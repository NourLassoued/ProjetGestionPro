import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CarteuserComponent } from './carteuser.component';

describe('CarteuserComponent', () => {
  let component: CarteuserComponent;
  let fixture: ComponentFixture<CarteuserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CarteuserComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CarteuserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
