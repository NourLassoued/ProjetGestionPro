import { TestBed } from '@angular/core/testing';

import { TableauxService } from './tableaux.service';

describe('TableauxService', () => {
  let service: TableauxService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TableauxService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
