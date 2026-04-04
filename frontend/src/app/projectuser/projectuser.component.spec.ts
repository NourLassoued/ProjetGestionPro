import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectuserComponent } from './projectuser.component';

describe('ProjectuserComponent', () => {
  let component: ProjectuserComponent;
  let fixture: ComponentFixture<ProjectuserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectuserComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjectuserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
