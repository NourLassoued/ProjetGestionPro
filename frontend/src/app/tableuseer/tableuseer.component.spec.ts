import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableuseerComponent } from './tableuseer.component';

describe('TableuseerComponent', () => {
  let component: TableuseerComponent;
  let fixture: ComponentFixture<TableuseerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TableuseerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TableuseerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
